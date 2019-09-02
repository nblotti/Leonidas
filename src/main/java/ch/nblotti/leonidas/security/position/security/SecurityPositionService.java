package ch.nblotti.leonidas.security.position.security;

import ch.nblotti.leonidas.security.account.Account;
import ch.nblotti.leonidas.security.account.AccountService;
import ch.nblotti.leonidas.security.quote.asset.QuoteService;
import ch.nblotti.leonidas.security.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.security.entry.security.SecurityEntry;
import ch.nblotti.leonidas.security.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.security.position.Position;
import ch.nblotti.leonidas.security.position.PositionRepository;
import ch.nblotti.leonidas.security.quote.fx.FXQuoteService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Transactional
public class SecurityPositionService {

  private static Logger LOGGER = Logger.getLogger("SecurityPositionService");

  @Autowired
  private PositionRepository repository;

  @Autowired
  SecurityEntryService securityEntryService;
  @Autowired
  DateTimeFormatter dateTimeFormatter;


  @Autowired
  AccountService accountService;


  @Autowired
  FXQuoteService fxQuoteService;

  @Autowired
  QuoteService quoteService;


  @Autowired
  JmsTemplate jmsOrderTemplate;

  public Iterable<Position> saveAll(List<Position> positions) {
    return repository.saveAll(positions);
  }

  /*@Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteAll(int account, String securityID, String currency) {
    repository.deleteByPosTypeAndAccountIdAndSecurityIDAndCurrency(Position.POS_TYPE.SECURITY, account, securityID, currency);
  }
*/
  //TODO NBL : test me
  public Position updatePosition(SecurityEntry entry) {

    LOGGER.log(Level.FINE,"Started update process");
    Account currentAccount = accountService.findAccountById(entry.getAccount());


    if (LocalDate.now().compareTo(entry.getValueDate()) >= 0) {
      //2. oui -Suppression position futures
      //deleteAll(entry.getAccount(), entry.getSecurityID(), entry.getCurrency());
      repository.deleteByPosTypeAndAccountIdAndSecurityIDAndCurrency(Position.POS_TYPE.SECURITY, entry.getAccount(), entry.getSecurityID(), entry.getCurrency());


      LOGGER.log(Level.FINE,"Suppression des positions");
    }

    //3. On obtient la liste des mouvements
    Iterable<SecurityEntry> securityEntries = securityEntryService.findAllByAccountAndSecurityIDOrderByValueDateAsc(entry.getAccount(), entry.getSecurityID());

    //4. On les aggrège par jour
    Iterable<AggregatedSecurityEntry> aggregatedSecurityEntries = aggregateSecuritiesEntriesByDay(securityEntries);

    //5. On duplique les quantités entre les deux dates
    updatePositions(currentAccount, aggregatedSecurityEntries);


    return null;

  }


  private Iterable<AggregatedSecurityEntry> aggregateSecuritiesEntriesByDay(Iterable<SecurityEntry> securityEntries) {


    Map<LocalDate, AggregatedSecurityEntry> entryByDate = Maps.newHashMap();


    for (Iterator<SecurityEntry> securityEntriesIterator = securityEntries.iterator(); securityEntriesIterator.hasNext(); ) {

      SecurityEntry currentEntry = securityEntriesIterator.next();

      if (entryByDate.containsKey(currentEntry.getValueDate())) {

        AggregatedSecurityEntry existingAggregatedEntry = entryByDate.get(currentEntry.getValueDate());


        if (existingAggregatedEntry.getDebitCreditCode().equals(DEBIT_CREDIT.ZERO)) {
          //dans les cas ou le cumul actuel est null
          updateEntryAtZero(currentEntry, existingAggregatedEntry);

        } else if (existingAggregatedEntry.getDebitCreditCode().equals(currentEntry.getDebitCreditCode())) {
          //dans les cas ou les deux mouvments sont dans le même sens on les cumule
          updateEntryWithSameSign(currentEntry, existingAggregatedEntry);
        } else {
          //dans les cas ou les deux mouvments sont dans un sens différent
          if (existingAggregatedEntry.getQuantity() - currentEntry.getQuantity() == 0) {
            securityEntriesIterator.remove();
            entryByDate.remove(currentEntry.getValueDate());
          } else {
            updateEntryWithDifferentSign(currentEntry, existingAggregatedEntry);
          }

        }


      } else {
        entryByDate.put(currentEntry.getValueDate(), new AggregatedSecurityEntry(currentEntry));
      }
    }

    //on ordonne par date valeur

    List<AggregatedSecurityEntry> aggregatedSecurityEntries = Lists.newArrayList(entryByDate.values());

    Collections.sort(aggregatedSecurityEntries, new Comparator<AggregatedSecurityEntry>() {
      @Override
      public int compare(AggregatedSecurityEntry entry1, AggregatedSecurityEntry entry2) {

        return entry1.getValueDate().compareTo(entry2.getValueDate());
      }
    });


    return aggregatedSecurityEntries;


  }

  private boolean updateEntryWithDifferentSign(SecurityEntry currentEntry, AggregatedSecurityEntry existingEntry) {

    //On adapte le signe de l'entrée en fonction des cas
    if (existingEntry.getQuantity() - currentEntry.getQuantity() < 0) {
      existingEntry.setDebitCreditCode(DEBIT_CREDIT.CRDT);
    } else {
      existingEntry.setDebitCreditCode(DEBIT_CREDIT.DBIT);
    }
    //les signes sont opposés, on soustrait donc les quantités
    existingEntry.setQuantity(existingEntry.getQuantity() - currentEntry.getQuantity());
    existingEntry.setNetPosValue(existingEntry.getNetPosValue() - currentEntry.getNetAmount());
    existingEntry.setGrossPosValue(existingEntry.getGrossPosValue() - currentEntry.getGrossAmount());
//la somme est non null on retourne false;
    return false;
  }

  private void updateEntryWithSameSign(SecurityEntry currentEntry, AggregatedSecurityEntry existingEntry) {
    //les signes sont opposés, on cumule donc les quantités
    existingEntry.setQuantity(existingEntry.getQuantity() + currentEntry.getQuantity());
    existingEntry.setNetPosValue(existingEntry.getNetPosValue() + currentEntry.getNetAmount());
    existingEntry.setGrossPosValue(existingEntry.getGrossPosValue() + currentEntry.getGrossAmount());
  }

  private void updateEntryAtZero(SecurityEntry currentEntry, AggregatedSecurityEntry aggregatedSecurityEntry) {

    if (currentEntry.getDebitCreditCode().equals(DEBIT_CREDIT.DBIT)) {
      aggregatedSecurityEntry.setQuantity(currentEntry.getQuantity());
      aggregatedSecurityEntry.setNetPosValue(currentEntry.getNetAmount());
      aggregatedSecurityEntry.setGrossPosValue(currentEntry.getGrossAmount());
    } else {
      aggregatedSecurityEntry.setQuantity(-currentEntry.getQuantity());
      aggregatedSecurityEntry.setNetPosValue(-currentEntry.getNetAmount());
      aggregatedSecurityEntry.setGrossPosValue(-currentEntry.getGrossAmount());
    }
  }

  public interface UUIDHolder {
    String getNewRandomUUID();

    String getCurrentRandomUUID();

  }

  private void updatePositions(Account currentAccount, Iterable<AggregatedSecurityEntry> securityEntries) {

    Iterable<Position> positions = null;

    UUIDHolder uUIDHolder = new UUIDHolder() {
      private String uniqueID;


      @Override
      public String getNewRandomUUID() {
        this.uniqueID = UUID.randomUUID().toString();
        return this.uniqueID;
      }

      @Override
      public String getCurrentRandomUUID() {
        return this.uniqueID == null ? getNewRandomUUID() : this.uniqueID;
      }
    };


    List<AggregatedSecurityEntry> entries = Lists.newArrayList(securityEntries);


    for (int i = 0; i < entries.size(); i++) {

      int nextEntryIndex = i + 1;

      AggregatedSecurityEntry currentEntry = entries.get(i);
      AggregatedSecurityEntry nextEntry = entries.size() > i + 1 ? entries.get(nextEntryIndex) : null;
      positions = positionFromEntry(currentAccount, positions, currentEntry, nextEntry, uUIDHolder);
    }

  }


  private Iterable<Position> positionFromEntry(Account currentAccount, Iterable<Position> positions, AggregatedSecurityEntry currentEntry, AggregatedSecurityEntry nextEntry, UUIDHolder uuidHolder) {


    Float realized = 0F;
    Float accountReportingRealized = 0F;
    Float quantity = 0F;
    Float cma = 0F;
    Float tma = 0F;
    //Float accountReportingCurrencyCMA = 0F;


    //l'entrée aggrégée est à zéro, on ne crée pas de position pour ce jour
    if (currentEntry.getDebitCreditCode() == DEBIT_CREDIT.ZERO) {
      return Lists.newArrayList();
    }


    LocalDate endDate = currentEntry.getValueDate().isAfter(LocalDate.now()) ? currentEntry.getValueDate().minusDays(1) : LocalDate.now();


    Float currencyNetPosValue = currentEntry.getNetPosValue() * currentEntry.getFxchangeRate();

    if (nextEntry != null) {
      endDate = nextEntry.getValueDate().minusDays(1);
    }


    //Il existe des entrées dans la journée et il existe des positions le jour précédent
    if (positions != null && !Lists.newArrayList(positions).isEmpty()) {
      //la position de la veille
      Position lastDayPosition = Lists.newArrayList(positions).get(Lists.newArrayList(positions).size() - 1);
      realized = lastDayPosition.getRealized();
      //on additionne la quantité à la quantité de la valeur existante.
      if (currentEntry.getDebitCreditCode() == DEBIT_CREDIT.CRDT) {
        //il s'agit d'un achat, il faut adapter le CMA
        quantity = currentEntry.getQuantity() + lastDayPosition.getQuantity();
        cma = ((lastDayPosition.getCMA() * lastDayPosition.getQuantity()) + (currentEntry.getNetPosValue())) / (lastDayPosition.getQuantity() + currentEntry.getQuantity());
        tma = (lastDayPosition.getTMA() * lastDayPosition.getQuantity() + (currentEntry.getFxchangeRate() * currentEntry.getQuantity())) / lastDayPosition.getQuantity() + currentEntry.getQuantity();


      } else {
        //il s'agit d'une vente, il faut adapter le réalisé
        quantity = -currentEntry.getQuantity() + lastDayPosition.getQuantity();
        realized += currentEntry.getNetPosValue() - (lastDayPosition.getCMA() * currentEntry.getQuantity());
        cma = quantity == 0 ? 0 : lastDayPosition.getCMA();
        tma = quantity == 0 ? 0 : lastDayPosition.getTMA();


      }

    } else {
      quantity = currentEntry.getQuantity();
      cma = currentEntry.getNetPosValue() / currentEntry.getQuantity();
      tma = currentEntry.getFxchangeRate();

    }


    return createSecurityPositions(currentAccount, quantity, cma, tma, /*accountReportingCurrencyCMA, */realized /*accountReportingRealized*/, currentEntry, endDate, uuidHolder);

  }

  private Iterable<Position> createSecurityPositions(Account currentAccount, Float quantity, Float cma, Float tma/*accountReportingCurrencyCMA*/, Float realized, /*Float accountReportingRealized,*/ AggregatedSecurityEntry firstEntry, LocalDate endDate, UUIDHolder uuidHolder) {

    LOGGER.log(Level.FINE,String.format("Création de position de %s à %s avec une quantité de %s", firstEntry.getValueDate().format(dateTimeFormatter), endDate.format(dateTimeFormatter), quantity));


    //on valorise

    //dans le cas ou la quantité est à zéro, on ne crée qu'une position pour visualiser la plus value réalisée lors de la vente
    long loop = ChronoUnit.DAYS.between(firstEntry.getValueDate(), endDate);

    if (quantity == 0) {
      loop = 0;
    }


    List<Position> positions = Lists.newArrayList();
    for (int i = 0; i <= loop; i++) {


      Position position = new Position();
      position.setUniqueID(uuidHolder.getCurrentRandomUUID());
      position.setPosDate(firstEntry.getValueDate().plusDays(i));
      position.setAccountId(firstEntry.getAccount());
      position.setPosType(Position.POS_TYPE.SECURITY);
      position.setQuantity(quantity);
      position.setCMA(cma);
      position.setTMA(tma);
      position.setSecurityID(firstEntry.getSecurityID());
      position.setRealized(realized);
      position.setAccountPerformanceCurrency(currentAccount.getPerformanceCurrency());
      position.setPosValue(Float.valueOf(quoteService.getQuoteForDate(firstEntry.getExchange(), firstEntry.getSecurityID(), position.getPosDate()).getAdjustedClose()) * quantity);
      position.setUnrealized(position.getPosValue() - (position.getCMA() * quantity));
      position.setCurrency(firstEntry.getCurrency());
      position.setPosValueReportingCurrency(position.getPosValue() * Float.valueOf(fxQuoteService.getFXQuoteForDate(position.getCurrency(), currentAccount.getPerformanceCurrency(), position.getPosDate()).getAdjustedClose()));


      positions.add(position);
      if (loop == 0)
        uuidHolder.getNewRandomUUID();
    }

    return repository.saveAll(positions);


  }


}



