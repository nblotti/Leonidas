package ch.nblotti.leonidas.position.cash;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.entry.ACHAT_VENTE_TITRE;
import ch.nblotti.leonidas.entry.cash.CashEntryPO;
import ch.nblotti.leonidas.entry.cash.CashEntryService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Transactional
public class CashPositionService {

  private static Logger logger = Logger.getLogger("CashPositionService");


  @Autowired
  private PositionRepository repository;

  @Autowired
  CashEntryService cashEntryService;


  @Autowired
  DateTimeFormatter dateTimeFormatter;

  @Autowired
  AccountService accountService;

  @Autowired
  QuoteService quoteService;

  @Autowired
  FXQuoteService fxQuoteService;


  public Iterable<PositionPO> saveAll(List<PositionPO> positionPOS) {
    return repository.saveAll(positionPOS);
  }


  @MarketProcess(entity = PositionPO.class, postype = 0)
  public CashEntryPO updatePositions(CashEntryPO entry) {

    getLogger().severe("Creating Cash positions");
    repository.deleteByPosTypeAndAccountIdAndCurrency(PositionPO.POS_TYPE.CASH, entry.getAccount(), entry.getCurrency());


    //3. On obtient la liste des mouvements
    Iterable<CashEntryPO> cashEntries = cashEntryService.findAllByAccountAndCurrencyOrderByValueDateAsc(entry.getAccount(), entry.getCurrency());

    //4. On les aggrège par jour
    Iterable<AggregatedCashEntryVO> aggegatedCashEntries = aggregateCashEntries(cashEntries);


    AccountPO currentAccountPO = accountService.findAccountById(entry.getAccount());


    //5. On duplique les quantités entre les deux dates
    updatePositions(currentAccountPO, aggegatedCashEntries);

    getLogger().severe("end cash positions");

    return entry;


  }

  void updatePositions(AccountPO currentAccountPO, Iterable<AggregatedCashEntryVO> cashEntries) {

    Iterable<PositionPO> positions = Lists.newArrayList();


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
        if (existingEntry.getAchatVenteTitre().equals(currentCashEntryTO.getAchatVenteCode())) {
          existingEntry.setNetAmount(existingEntry.getNetAmount() + currentCashEntryTO.getNetAmount());
          existingEntry.setGrossAmount(existingEntry.getGrossAmount() + currentCashEntryTO.getGrossAmount());
        } else {
          //dans les cas ou les deux mouvments sont dans un sens différent

          //si le mouvement fait changer de sens le total on inversse
          if (existingEntry.getNetAmount() - currentCashEntryTO.getNetAmount() < 0 && existingEntry.getAchatVenteTitre().equals(ACHAT_VENTE_TITRE.ACHAT)) {
            existingEntry.setAchatVenteTitre(ACHAT_VENTE_TITRE.VENTE);
          } else {
            existingEntry.setAchatVenteTitre(ACHAT_VENTE_TITRE.ACHAT);
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
    Float tma;

    //l'entrée aggrégée est à zéro, on ne crée pas de position pour ce jour
    if (currentEntry.getAchatVenteTitre() == ACHAT_VENTE_TITRE.ZERO) {
      return Lists.newArrayList();
    }

    LocalDate endDate = currentEntry.getValueDate().isAfter(LocalDate.now()) ? currentEntry.getValueDate().minusDays(1) : LocalDate.now();

    if (nextEntry != null) {
      endDate = nextEntry.getValueDate().minusDays(1);
    }

    ArrayList<PositionPO> positionPOS;

    positionPOS = Lists.newArrayList(positions);

    if (!positionPOS.isEmpty()) {


      //la position de la veille
      PositionPO lastDayPositionPO = positionPOS.get(positionPOS.size() - 1);

      //on additionne la quantité à la quantité de la valeur existante.
      if (currentEntry.getAchatVenteTitre() == ACHAT_VENTE_TITRE.VENTE) {
        //il s'agit d'une vente de titre et donc d'un achat de cash : il faut adapter le CMA

        amount = lastDayPositionPO.getPosValue() + currentEntry.getNetAmount();
        tma = (lastDayPositionPO.getTMA() * lastDayPositionPO.getPosValue() + (currentEntry.getFxchangeRate() * currentEntry.getNetAmount())) / lastDayPositionPO.getPosValue() + currentEntry.getNetAmount();

      } else {
        //il s'agit d'un achat de cash il faut adapter le réalisé
        amount = lastDayPositionPO.getPosValue() - currentEntry.getNetAmount();
        tma = lastDayPositionPO.getTMA();
      }

    } else {
      if (currentEntry.getAchatVenteTitre() == ACHAT_VENTE_TITRE.VENTE) {
        amount = -currentEntry.getNetAmount();
        tma = currentEntry.getFxchangeRate();

      } else {
        amount = currentEntry.getNetAmount();
        tma = currentEntry.getFxchangeRate();
      }

    }

    return createPositions(currentAccountPO, amount, tma, currentEntry, endDate);

  }

  Iterable<PositionPO> createPositions(AccountPO currentAccountPO, Float netAmount, Float tma, AggregatedCashEntryVO currentEntry, LocalDate endDate) {

    if (getLogger().isLoggable(Level.FINE)) {
      getLogger().fine(String.format("Création de position de %s à %s pour un montant de %s", currentEntry.getValueDate().format(dateTimeFormatter), endDate.format(dateTimeFormatter), netAmount));
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
      currentPositionPO.setCma(netAmount);
      currentPositionPO.setCurrency(currentEntry.getCurrency());
      currentPositionPO.setAccountPerformanceCurrency(currentAccountPO.getPerformanceCurrency());
      currentPositionPO.setTMA(tma);

      Float exchangeRate = Float.valueOf(fxQuoteService.getFXQuoteForDate(currentPositionPO.getCurrency(), currentAccountPO.getPerformanceCurrency(), currentPositionPO.getPosDate()).getAdjustedClose());

      currentPositionPO.setPosValueReportingCurrency(netAmount * exchangeRate);

      positionPOS.add(currentPositionPO);
    }

    return repository.saveAll(positionPOS);


  }

  Logger getLogger() {
    return logger;

  }


}



