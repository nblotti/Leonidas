package ch.nblotti.leonidas.security.position.cash;

import ch.nblotti.leonidas.security.account.Account;
import ch.nblotti.leonidas.security.account.AccountService;
import ch.nblotti.leonidas.security.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.security.entry.cash.CashEntry;
import ch.nblotti.leonidas.security.entry.cash.CashEntryService;
import ch.nblotti.leonidas.security.position.Position;
import ch.nblotti.leonidas.security.position.PositionRepository;
import ch.nblotti.leonidas.security.quote.asset.QuoteService;
import ch.nblotti.leonidas.security.quote.fx.FXQuoteService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Component
@Transactional
public class CashPositionService {

  private static Logger LOGGER = Logger.getLogger("CashPositionService");

  @Autowired
  private PositionRepository repository;

  @Autowired
  CashEntryService cashEntryService;


  @Autowired
  DateTimeFormatter dateTimeFormatter;

  @Autowired
  AccountService accountService;

  @Autowired
  JmsTemplate jmsOrderTemplate;

  @Autowired
  QuoteService quoteService;

  @Autowired
  FXQuoteService fxQuoteService;


  public Iterable<Position> saveAll(List<Position> positions) {
    return repository.saveAll(positions);
  }

 /* @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void deleteAll(int account, String currency) {
    repository.deleteByPosTypeAndAccountIdAndCurrency(Position.POS_TYPE.CASH, account, currency);
  }
  */


  //TODO NBL : test me
  public Position updatePosition(CashEntry entry) {

    LOGGER.info("Started update process");


    repository.deleteByPosTypeAndAccountIdAndCurrency(Position.POS_TYPE.CASH, entry.getAccount(), entry.getCurrency());

    if (LocalDate.now().compareTo(entry.getValueDate()) >= 0) {

      LOGGER.info("Suppression des positions");
    }

    //3. On obtient la liste des mouvements
    Iterable<CashEntry> cashEntries = cashEntryService.findAllByAccountAndCurrencyOrderByValueDateAsc(entry.getAccount(), entry.getCurrency());

    //4. On les aggrège par jour
    Iterable<AggregatedCashEntry> aggegatedCashEntries = aggregateCashEntries(cashEntries);


    Account currentAccount = accountService.findAccountById(entry.getAccount());


    //5. On duplique les quantités entre les deux dates
    updatePositions(currentAccount, aggegatedCashEntries);


    return null;

  }

  private void updatePositions(Account currentAccount, Iterable<AggregatedCashEntry> cashEntries) {

    Iterable<Position> positions = null;


    List<AggregatedCashEntry> entries = Lists.newArrayList(cashEntries);


    for (int i = 0; i < entries.size(); i++) {

      int nextEntryIndex = i + 1;

      AggregatedCashEntry currentEntry = entries.get(i);
      AggregatedCashEntry nextEntry = entries.size() > i + 1 ? entries.get(nextEntryIndex) : null;
      positions = positionFromEntry(currentAccount, positions, currentEntry, nextEntry);
    }

  }


  private Iterable<AggregatedCashEntry> aggregateCashEntries(Iterable<CashEntry> cashEntries) {


    Map<LocalDate, AggregatedCashEntry> cashEntryByDAte = Maps.newHashMap();

    for (CashEntry currentCashEntry : cashEntries) {
      if (cashEntryByDAte.containsKey(currentCashEntry.getValueDate())) {

        AggregatedCashEntry existingEntry = cashEntryByDAte.get(currentCashEntry.getValueDate());

        //dans les cas ou les deux mouvments sont dans le même sens on les cumule
        if (existingEntry.getDebitCreditCode().equals(currentCashEntry.getDebitCreditCode())) {
          existingEntry.setNetAmount(existingEntry.getNetAmount() + currentCashEntry.getNetAmount());
          existingEntry.setGrossAmount(existingEntry.getGrossAmount() + currentCashEntry.getGrossAmount());
        } else {
          //dans les cas ou les deux mouvments sont dans un sens différent

          //si le mouvement fait changer de sens le total on inversse
          if (existingEntry.getNetAmount() - currentCashEntry.getNetAmount() < 0 && existingEntry.getDebitCreditCode().equals(DEBIT_CREDIT.CRDT)) {
            existingEntry.setDebitCreditCode(DEBIT_CREDIT.DBIT);
          } else {
            existingEntry.setDebitCreditCode(DEBIT_CREDIT.CRDT);
          }
          //on soustraitss les deux montants
          existingEntry.setNetAmount(existingEntry.getNetAmount() - currentCashEntry.getNetAmount());
          existingEntry.setGrossAmount(existingEntry.getGrossAmount() - currentCashEntry.getGrossAmount());
        }


      } else {
        cashEntryByDAte.put(currentCashEntry.getValueDate(), new AggregatedCashEntry(currentCashEntry));
      }
    }

    //on ordonne par date valeur

    List<AggregatedCashEntry> sortedCashEntry = Lists.newArrayList(cashEntryByDAte.values());

    Collections.sort(sortedCashEntry, new Comparator<AggregatedCashEntry>() {
      @Override
      public int compare(AggregatedCashEntry cashEntry1, AggregatedCashEntry cashEntry2) {

        return cashEntry1.getValueDate().compareTo(cashEntry2.getValueDate());
      }
    });

    return sortedCashEntry;


  }


  private Iterable<Position> positionFromEntry(Account currentAccount, Iterable<Position> positions, AggregatedCashEntry currentEntry, AggregatedCashEntry nextEntry) {


    Float amount;
    Float tma = 0F;

    //l'entrée aggrégée est à zéro, on ne crée pas de position pour ce jour
    if (currentEntry.getDebitCreditCode() == DEBIT_CREDIT.ZERO) {
      return Lists.newArrayList();
    }

    LocalDate endDate = currentEntry.getValueDate().isAfter(LocalDate.now()) ? currentEntry.getValueDate().minusDays(1) : LocalDate.now();

    if (nextEntry != null) {
      endDate = nextEntry.getValueDate().minusDays(1);
    }


    if (positions != null && !Lists.newArrayList(positions).isEmpty()) {

      Float currencyNetPosValue = currentEntry.getNetAmount() * currentEntry.getFxchangeRate();

      //la position de la veille
      Position lastDayPosition = Lists.newArrayList(positions).get(Lists.newArrayList(positions).size() - 1);

      //accountReportingRealized = lastDayPosition.getAccountReportingRealized();
      //on additionne la quantité à la quantité de la valeur existante.
      if (currentEntry.getDebitCreditCode() == DEBIT_CREDIT.DBIT) {
        //il s'agit d'un achat, il faut adapter le CMA

        amount = lastDayPosition.getPosValue() + currentEntry.getNetAmount();
        tma = (lastDayPosition.getTMA() * lastDayPosition.getPosValue() + (currentEntry.getFxchangeRate() * currentEntry.getNetAmount())) / lastDayPosition.getPosValue() + currentEntry.getNetAmount();

      } else {
        //il s'agit d'une vente, il faut adapter le réalisé
        amount = lastDayPosition.getPosValue() - currentEntry.getNetAmount();
      }

    } else {
      if (currentEntry.getDebitCreditCode() == DEBIT_CREDIT.DBIT) {
        amount = -currentEntry.getNetAmount();
        tma = currentEntry.getFxchangeRate();

      } else {
        amount = currentEntry.getNetAmount();
        tma = currentEntry.getFxchangeRate();
      }

    }

    return createPositions(currentAccount, amount, tma, currentEntry, endDate);

  }

  private Iterable<Position> createPositions(Account currentAccount, Float netAmount, Float tma, AggregatedCashEntry currentEntry, LocalDate endDate) {

    LOGGER.info(String.format("Création de position de %s à %s pour un montant de %s", currentEntry.getValueDate().format(dateTimeFormatter), endDate.format(dateTimeFormatter), netAmount));

    long loop = ChronoUnit.DAYS.between(currentEntry.getValueDate(), endDate);

    List<Position> positions = Lists.newArrayList();
    for (int i = 0; i <= loop; i++) {

      Position currentPosition = new Position();
      currentPosition.setQuantity(1f);
      currentPosition.setPosDate(currentEntry.getValueDate().plusDays(i));
      currentPosition.setAccountId(currentEntry.getAccount());
      currentPosition.setPosType(Position.POS_TYPE.CASH);
      currentPosition.setPosValue(netAmount);
      currentPosition.setCMA(netAmount);
      currentPosition.setCurrency(currentEntry.getCurrency());
      currentPosition.setAccountPerformanceCurrency(currentAccount.getPerformanceCurrency());
      currentPosition.setTMA(tma);

      currentPosition.setPosValueReportingCurrency(currentEntry.getNetAmount() * Float.valueOf(fxQuoteService.getFXQuoteForDate(currentPosition.getCurrency(), currentAccount.getPerformanceCurrency(), currentPosition.getPosDate()).getAdjustedClose()));

      positions.add(currentPosition);
    }

    return repository.saveAll(positions);


  }


}



