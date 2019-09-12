package ch.nblotti.leonidas.position.cash;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.cash.CashEntryPO;
import ch.nblotti.leonidas.entry.cash.CashEntryService;
import ch.nblotti.leonidas.position.PositionPO;
import ch.nblotti.leonidas.position.PositionRepository;
import ch.nblotti.leonidas.quote.QuoteService;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.technical.MessageVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
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


  public Iterable<PositionPO> saveAll(List<PositionPO> positionPOS) {
    return repository.saveAll(positionPOS);
  }


  public PositionPO updatePosition(CashEntryPO entry) {

    LOGGER.log(Level.FINE, "Started update process");


    repository.deleteByPosTypeAndAccountIdAndCurrency(PositionPO.POS_TYPE.CASH, entry.getAccount(), entry.getCurrency());

    if (LocalDate.now().compareTo(entry.getValueDate()) >= 0) {

      LOGGER.log(Level.FINE, "Suppression des positions");
    }

    //3. On obtient la liste des mouvements
    Iterable<CashEntryPO> cashEntries = cashEntryService.findAllByAccountAndCurrencyOrderByValueDateAsc(entry.getAccount(), entry.getCurrency());

    //4. On les aggrège par jour
    Iterable<AggregatedCashEntryVO> aggegatedCashEntries = aggregateCashEntries(cashEntries);


    AccountPO currentAccountPO = accountService.findAccountById(entry.getAccount());


    //5. On duplique les quantités entre les deux dates
    updatePositions(currentAccountPO, aggegatedCashEntries);


    jmsOrderTemplate.convertAndSend("cashpositionbox", new MessageVO(entry.getOrderID(), entry.getAccount(), MessageVO.MESSAGE_TYPE.CASH_POSITION, MessageVO.ENTITY_ACTION.CREATE));


    return null;

  }

  private void updatePositions(AccountPO currentAccountPO, Iterable<AggregatedCashEntryVO> cashEntries) {

    Iterable<PositionPO> positions = null;


    List<AggregatedCashEntryVO> entries = Lists.newArrayList(cashEntries);


    for (int i = 0; i < entries.size(); i++) {

      int nextEntryIndex = i + 1;

      AggregatedCashEntryVO currentEntry = entries.get(i);
      AggregatedCashEntryVO nextEntry = entries.size() > i + 1 ? entries.get(nextEntryIndex) : null;
      positions = positionFromEntry(currentAccountPO, positions, currentEntry, nextEntry);
    }

  }


  Iterable<AggregatedCashEntryVO> aggregateCashEntries(Iterable<CashEntryPO> cashEntries) {


    Map<LocalDate, AggregatedCashEntryVO> cashEntryByDAte = Maps.newHashMap();

    for (CashEntryPO currentCashEntryTO : cashEntries) {
      if (cashEntryByDAte.containsKey(currentCashEntryTO.getValueDate())) {

        AggregatedCashEntryVO existingEntry = cashEntryByDAte.get(currentCashEntryTO.getValueDate());

        //dans les cas ou les deux mouvments sont dans le même sens on les cumule
        if (existingEntry.getDebitCreditCode().equals(currentCashEntryTO.getDebitCreditCode())) {
          existingEntry.setNetAmount(existingEntry.getNetAmount() + currentCashEntryTO.getNetAmount());
          existingEntry.setGrossAmount(existingEntry.getGrossAmount() + currentCashEntryTO.getGrossAmount());
        } else {
          //dans les cas ou les deux mouvments sont dans un sens différent

          //si le mouvement fait changer de sens le total on inversse
          if (existingEntry.getNetAmount() - currentCashEntryTO.getNetAmount() < 0 && existingEntry.getDebitCreditCode().equals(DEBIT_CREDIT.CRDT)) {
            existingEntry.setDebitCreditCode(DEBIT_CREDIT.DBIT);
          } else {
            existingEntry.setDebitCreditCode(DEBIT_CREDIT.CRDT);
          }
          //on soustraitss les deux montants
          existingEntry.setNetAmount(existingEntry.getNetAmount() - currentCashEntryTO.getNetAmount());
          existingEntry.setGrossAmount(existingEntry.getGrossAmount() - currentCashEntryTO.getGrossAmount());
        }


      } else {
        cashEntryByDAte.put(currentCashEntryTO.getValueDate(), new AggregatedCashEntryVO(currentCashEntryTO));
      }
    }

    //on ordonne par date valeur

    List<AggregatedCashEntryVO> sortedCashEntry = Lists.newArrayList(cashEntryByDAte.values());


    sortedCashEntry.sort((AggregatedCashEntryVO cashEntry1, AggregatedCashEntryVO cashEntry2) -> cashEntry1.getValueDate().compareTo(cashEntry2.getValueDate()));

    return sortedCashEntry;


  }


  Iterable<PositionPO> positionFromEntry(AccountPO currentAccountPO, Iterable<PositionPO> positions, AggregatedCashEntryVO currentEntry, AggregatedCashEntryVO nextEntry) {


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


      //la position de la veille
      PositionPO lastDayPositionPO = Lists.newArrayList(positions).get(Lists.newArrayList(positions).size() - 1);

      //on additionne la quantité à la quantité de la valeur existante.
      if (currentEntry.getDebitCreditCode() == DEBIT_CREDIT.DBIT) {
        //il s'agit d'un achat, il faut adapter le CMA

        amount = lastDayPositionPO.getPosValue() + currentEntry.getNetAmount();
        tma = (lastDayPositionPO.getTMA() * lastDayPositionPO.getPosValue() + (currentEntry.getFxchangeRate() * currentEntry.getNetAmount())) / lastDayPositionPO.getPosValue() + currentEntry.getNetAmount();

      } else {
        //il s'agit d'une vente, il faut adapter le réalisé
        amount = lastDayPositionPO.getPosValue() - currentEntry.getNetAmount();
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

    return createPositions(currentAccountPO, amount, tma, currentEntry, endDate);

  }

  private Iterable<PositionPO> createPositions(AccountPO currentAccountPO, Float netAmount, Float tma, AggregatedCashEntryVO currentEntry, LocalDate endDate) {

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(String.format("Création de position de %s à %s pour un montant de %s", currentEntry.getValueDate().format(dateTimeFormatter), endDate.format(dateTimeFormatter), netAmount));
    }
    long loop = ChronoUnit.DAYS.between(currentEntry.getValueDate(), endDate);

    List<PositionPO> positionPOS = Lists.newArrayList();
    for (int i = 0; i <= loop; i++) {

      PositionPO currentPositionPO = new PositionPO();
      currentPositionPO.setQuantity(1f);
      currentPositionPO.setPosDate(currentEntry.getValueDate().plusDays(i));
      currentPositionPO.setAccountId(currentEntry.getAccount());
      currentPositionPO.setPosType(PositionPO.POS_TYPE.CASH);
      currentPositionPO.setPosValue(netAmount);
      currentPositionPO.setCMA(netAmount);
      currentPositionPO.setCurrency(currentEntry.getCurrency());
      currentPositionPO.setAccountPerformanceCurrency(currentAccountPO.getPerformanceCurrency());
      currentPositionPO.setTMA(tma);

      currentPositionPO.setPosValueReportingCurrency(currentEntry.getNetAmount() * Float.valueOf(fxQuoteService.getFXQuoteForDate(currentPositionPO.getCurrency(), currentAccountPO.getPerformanceCurrency(), currentPositionPO.getPosDate()).getAdjustedClose()));

      positionPOS.add(currentPositionPO);
    }

    return repository.saveAll(positionPOS);


  }


}



