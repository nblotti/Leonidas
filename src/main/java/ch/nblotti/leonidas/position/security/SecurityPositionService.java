package ch.nblotti.leonidas.position.security;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.ACHAT_VENTE_TITRE;
import ch.nblotti.leonidas.entry.security.SecurityEntryPO;
import ch.nblotti.leonidas.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.position.PositionPO;
import ch.nblotti.leonidas.position.PositionRepository;
import ch.nblotti.leonidas.process.order.MarketProcess;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.quote.QuoteService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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

  static Logger logger = Logger.getLogger("SecurityPositionService");

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


  public Iterable<PositionPO> saveAll(List<PositionPO> positionPOS) {
    return repository.saveAll(positionPOS);
  }

  @MarketProcess(entity = PositionPO.class, postype = 1)
  public SecurityEntryPO updatePosition(SecurityEntryPO entry) {


    getLogger().severe("Creating security positions");
    getLogger().log(Level.FINE, "Started update process");
    AccountPO currentAccountPO = accountService.findAccountById(entry.getAccount());


    if (LocalDate.now().compareTo(entry.getValueDate()) >= 0) {
      //2. oui -Suppression position futures
      repository.deleteByPosTypeAndAccountIdAndSecurityIDAndCurrency(PositionPO.POS_TYPE.SECURITY, entry.getAccount(), entry.getSecurityID(), entry.getCurrency());


      getLogger().log(Level.FINE, "Suppression des positions");
    }

    //3. On obtient la liste des mouvements
    Iterable<SecurityEntryPO> securityEntries = securityEntryService.findAllByAccountAndSecurityIDOrderByValueDateAsc(entry.getAccount(), entry.getSecurityID());

    //4. On les aggrège par jour
    Iterable<AggregatedSecurityEntryVO> aggregatedSecurityEntries = aggregateSecuritiesEntriesByDay(securityEntries);

    //5. On duplique les quantités entre les deux dates
    updatePositions(currentAccountPO, aggregatedSecurityEntries);
    getLogger().severe("end security positions");
    return entry;
  }


  Iterable<AggregatedSecurityEntryVO> aggregateSecuritiesEntriesByDay(Iterable<SecurityEntryPO> securityEntries) {


    Map<LocalDate, AggregatedSecurityEntryVO> entryByDate = Maps.newHashMap();


    for (Iterator<SecurityEntryPO> securityEntriesIterator = securityEntries.iterator(); securityEntriesIterator.hasNext(); ) {

      SecurityEntryPO currentEntry = securityEntriesIterator.next();

      if (entryByDate.containsKey(currentEntry.getValueDate())) {

        AggregatedSecurityEntryVO existingAggregatedEntry = entryByDate.get(currentEntry.getValueDate());


        if (existingAggregatedEntry.getAchatVenteTitre().equals(ACHAT_VENTE_TITRE.ZERO)) {
          //dans les cas ou le cumul actuel est null
          updateEntryAtZero(currentEntry, existingAggregatedEntry);

        } else if (existingAggregatedEntry.getAchatVenteTitre().equals(currentEntry.getAchatVenteCode())) {
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
        entryByDate.put(currentEntry.getValueDate(), new AggregatedSecurityEntryVO(currentEntry));
      }
    }

    //on ordonne par date valeur

    List<AggregatedSecurityEntryVO> aggregatedSecurityEntries = Lists.newArrayList(entryByDate.values());


    aggregatedSecurityEntries.sort((AggregatedSecurityEntryVO entry1, AggregatedSecurityEntryVO entry2) -> entry1.getValueDate().compareTo(entry2.getValueDate()));


    return aggregatedSecurityEntries;


  }

  void updateEntryWithDifferentSign(SecurityEntryPO currentEntry, AggregatedSecurityEntryVO existingEntry) {

    //On adapte le signe de l'entrée en fonction des cas
    if (existingEntry.getQuantity() - currentEntry.getQuantity() < 0) {
      existingEntry.setAchatVenteTitre(ACHAT_VENTE_TITRE.ACHAT);
    } else {
      existingEntry.setAchatVenteTitre(ACHAT_VENTE_TITRE.VENTE);
    }
    //les signes sont opposés, on soustrait donc les quantités
    existingEntry.setQuantity(existingEntry.getQuantity() - currentEntry.getQuantity());
    existingEntry.setNetPosValue(existingEntry.getNetPosValue() - currentEntry.getNetAmount());
    existingEntry.setGrossPosValue(existingEntry.getGrossPosValue() - currentEntry.getGrossAmount());

  }

  void updateEntryWithSameSign(SecurityEntryPO currentEntry, AggregatedSecurityEntryVO existingEntry) {
    //les signes sont identiques, on cumule donc les quantités
    existingEntry.setQuantity(existingEntry.getQuantity() + currentEntry.getQuantity());
    existingEntry.setNetPosValue(existingEntry.getNetPosValue() + currentEntry.getNetAmount());
    existingEntry.setGrossPosValue(existingEntry.getGrossPosValue() + currentEntry.getGrossAmount());
  }

  void updateEntryAtZero(SecurityEntryPO currentEntry, AggregatedSecurityEntryVO aggregatedSecurityEntryVO) {

    if (currentEntry.getAchatVenteCode().equals(ACHAT_VENTE_TITRE.VENTE)) {
      aggregatedSecurityEntryVO.setQuantity(currentEntry.getQuantity());
      aggregatedSecurityEntryVO.setNetPosValue(currentEntry.getNetAmount());
      aggregatedSecurityEntryVO.setGrossPosValue(currentEntry.getGrossAmount());
    } else {
      aggregatedSecurityEntryVO.setQuantity(-currentEntry.getQuantity());
      aggregatedSecurityEntryVO.setNetPosValue(-currentEntry.getNetAmount());
      aggregatedSecurityEntryVO.setGrossPosValue(-currentEntry.getGrossAmount());
    }
  }

  public interface UUIDHolder {
    String getNewRandomUUID();

    String getCurrentRandomUUID();

  }

  void updatePositions(AccountPO currentAccountPO, Iterable<AggregatedSecurityEntryVO> securityEntries) {

    Iterable<PositionPO> positions = Lists.newArrayList();

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


    List<AggregatedSecurityEntryVO> entries = Lists.newArrayList(securityEntries);


    for (int i = 0; i < entries.size(); i++) {

      int nextEntryIndex = i + 1;

      AggregatedSecurityEntryVO currentEntry = entries.get(i);
      AggregatedSecurityEntryVO nextEntry = entries.size() > i + 1 ? entries.get(nextEntryIndex) : null;
      positions = positionFromEntry(currentAccountPO, positions, currentEntry, nextEntry, uUIDHolder);
    }

  }


  Iterable<PositionPO> positionFromEntry(AccountPO currentAccountPO, Iterable<PositionPO> positions, AggregatedSecurityEntryVO currentEntry, AggregatedSecurityEntryVO nextEntry, UUIDHolder uuidHolder) {


    Float realized = 0F;
    Float quantity;
    Float cma;
    Float tma;


    //l'entrée aggrégée est à zéro, on ne crée pas de position pour ce jour
    if (currentEntry.getAchatVenteTitre() == ACHAT_VENTE_TITRE.ZERO) {
      return Lists.newArrayList();
    }


    LocalDate endDate = currentEntry.getValueDate().isAfter(LocalDate.now()) ? currentEntry.getValueDate().minusDays(1) : LocalDate.now();


    if (nextEntry != null) {
      endDate = nextEntry.getValueDate().minusDays(1);
    }

    ArrayList<PositionPO> positionList;
    positionList = getPositionList(positions);


    //Il existe des entrées dans la journée et il existe des positions le jour précédent
    if (!positionList.isEmpty()) {
      //la position de la veille
      PositionPO lastDayPositionPO = positionList.get(positionList.size() - 1);
      realized = lastDayPositionPO.getRealized();
      //on additionne la quantité à la quantité de la valeur existante.
      if (currentEntry.getAchatVenteTitre() == ACHAT_VENTE_TITRE.ACHAT) {
        //il s'agit d'un achat, il faut adapter le CMA
        quantity = currentEntry.getQuantity() + lastDayPositionPO.getQuantity();
        cma = ((lastDayPositionPO.getCma() * lastDayPositionPO.getQuantity()) + (currentEntry.getNetPosValue())) / (lastDayPositionPO.getQuantity() + currentEntry.getQuantity());
        tma = (lastDayPositionPO.getTMA() * lastDayPositionPO.getQuantity() + (currentEntry.getFxchangeRate() * currentEntry.getQuantity())) / lastDayPositionPO.getQuantity() + currentEntry.getQuantity();


      } else {
        //il s'agit d'une vente, il faut adapter le réalisé
        quantity = -currentEntry.getQuantity() + lastDayPositionPO.getQuantity();
        realized += currentEntry.getNetPosValue() - (lastDayPositionPO.getCma() * currentEntry.getQuantity());
        cma = quantity == 0 ? 0 : lastDayPositionPO.getCma();
        tma = quantity == 0 ? 0 : lastDayPositionPO.getTMA();


      }

    } else {
      quantity = currentEntry.getQuantity();
      cma = currentEntry.getNetPosValue() / currentEntry.getQuantity();
      tma = currentEntry.getFxchangeRate();

    }


    return createSecurityPositions(currentAccountPO, quantity, cma, tma, realized, currentEntry, endDate, uuidHolder);

  }

  private ArrayList<PositionPO> getPositionList(Iterable<PositionPO> positions) {
    ArrayList<PositionPO> positionPOS;
    if (positions == null)
      positionPOS = Lists.newArrayList();
    else
      positionPOS = Lists.newArrayList(positions);
    return positionPOS;
  }

  Iterable<PositionPO> createSecurityPositions(AccountPO currentAccountPO, Float quantity, Float cma, Float tma, Float realized, AggregatedSecurityEntryVO firstEntry, LocalDate endDate, UUIDHolder uuidHolder) {

    if (getLogger().isLoggable(Level.FINE)) {
      getLogger().fine(String.format("Création de position de %s à %s avec une quantité de %s", firstEntry.getValueDate().format(dateTimeFormatter), endDate.format(dateTimeFormatter), quantity));
    }


    long loop = ChronoUnit.DAYS.between(firstEntry.getValueDate(), endDate);

    //dans le cas ou la quantité est à zéro, on ne crée qu'une position pour visualiser la plus value réalisée lors de la vente
    if (quantity == 0) {
      loop = 0;
    }


    List<PositionPO> positionPOS = Lists.newArrayList();
    for (int i = 0; i <= loop; i++) {


      PositionPO positionPO = new PositionPO();
      positionPO.setUniqueID(uuidHolder.getCurrentRandomUUID());
      positionPO.setPosDate(firstEntry.getValueDate().plusDays(i));
      positionPO.setAccountId(firstEntry.getAccount());
      positionPO.setPosType(PositionPO.POS_TYPE.SECURITY);
      positionPO.setQuantity(quantity);
      positionPO.setCma(cma);
      positionPO.setTMA(tma);
      positionPO.setSecurityID(firstEntry.getSecurityID());
      positionPO.setRealized(realized);
      positionPO.setAccountPerformanceCurrency(currentAccountPO.getPerformanceCurrency());
      positionPO.setPosValue(Float.valueOf(quoteService.getQuoteForDate(firstEntry.getExchange(), firstEntry.getSecurityID(), positionPO.getPosDate()).getAdjustedClose()) * quantity);
      positionPO.setUnrealized(positionPO.getPosValue() - (positionPO.getCma() * quantity));
      positionPO.setCurrency(firstEntry.getCurrency());
      positionPO.setPosValueReportingCurrency(positionPO.getPosValue() * Float.valueOf(fxQuoteService.getFXQuoteForDate(positionPO.getCurrency(), currentAccountPO.getPerformanceCurrency(), positionPO.getPosDate()).getAdjustedClose()));


      positionPOS.add(positionPO);
      if (loop == 0)
        uuidHolder.getNewRandomUUID();
    }

    return repository.saveAll(positionPOS);


  }

  Logger getLogger() {
    return logger;
  }


}



