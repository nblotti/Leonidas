package ch.nblotti.leonidas.entry.cash;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.entry.ACHAT_VENTE_TITRE;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.process.order.MarketProcess;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CashEntryService {

  private static final Logger logger = Logger.getLogger("CashEntryService");


  @Autowired
  private CashEntryRepository repository;

  @Autowired
  private QuoteService quoteService;

  @Autowired
  private FXQuoteService fxQuoteService;


  @Autowired
  private AssetService assetService;

  @Autowired
  private AccountService acountService;


  public Iterable<CashEntryPO> findAll() {

    return this.repository.findAll();

  }


  public Iterable<CashEntryPO> findAllByAccountAndCurrencyOrderByValueDateAsc(int account, String currency) {

    return this.repository.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency);
  }


  public CashEntryPO findByAccountAndOrderID(int account, long orderID) {

    return this.repository.findByAccountAndOrderID(account, orderID);
  }


  public Optional<CashEntryPO> findById(String toString) {
    return this.repository.findById(Long.valueOf(toString));
  }

  public CashEntryPO fromMarketOrder(OrderPO orderPO) {

    QuoteDTO fxQquote;
    QuoteDTO quoteDTO;

    CashEntryPO cashEntryTO = new CashEntryPO();
    AccountPO currentAccountPO = acountService.findAccountById(orderPO.getAccountId());

    cashEntryTO.setAccount(currentAccountPO.getId());
    cashEntryTO.setOrderID(orderPO.getId());
    cashEntryTO.setEntryDate(orderPO.getTransactTime());
    cashEntryTO.setStatus(orderPO.getStatus());
    cashEntryTO.setAccount(currentAccountPO.getId());

    AssetPO assetPO = assetService.getSymbol(orderPO.getExchange(), orderPO.getSymbol());
    int valueDateForExchange = assetService.getValueDateForExchange(assetPO.getExchange());
    cashEntryTO.setAccount(currentAccountPO.getId());
    fxQquote = fxQuoteService.getFXQuoteForDate(assetPO.getCurrency(), currentAccountPO.getPerformanceCurrency(), orderPO.getTransactTime().plusDays(valueDateForExchange));
    quoteDTO = quoteService.getQuoteForDate(orderPO.getExchange(), orderPO.getSymbol(), orderPO.getTransactTime().plusDays(valueDateForExchange));

    cashEntryTO.setValueDate(orderPO.getTransactTime().plusDays(valueDateForExchange));
    cashEntryTO.setGrossAmount(orderPO.getOrderQtyData() * Float.valueOf(quoteDTO.getAdjustedClose()));
    cashEntryTO.setAchatVenteCode(orderPO.getSide().equals(ACHAT_VENTE_TITRE.ACHAT) ? ACHAT_VENTE_TITRE.VENTE : ACHAT_VENTE_TITRE.ACHAT);

    cashEntryTO.setNetAmount(cashEntryTO.getGrossAmount());
    cashEntryTO.setCurrency(assetPO.getCurrency());
    cashEntryTO.setFxExchangeRate(Float.valueOf(fxQquote.getAdjustedClose()));
    cashEntryTO.setAccountReportingCurrency(currentAccountPO.getPerformanceCurrency());
    cashEntryTO.setEntryValueReportingCurrency(cashEntryTO.getFxExchangeRate() * cashEntryTO.getNetAmount());


    return cashEntryTO;
  }

  public CashEntryPO fromCashEntryOrder(OrderPO orderPO) {
    CashEntryPO cashEntryTO = new CashEntryPO();
    QuoteDTO fxQquote;

    AccountPO currentAccountPO = acountService.findAccountById(orderPO.getAccountId());

    cashEntryTO.setAccount(currentAccountPO.getId());
    cashEntryTO.setOrderID(orderPO.getId());
    cashEntryTO.setEntryDate(orderPO.getTransactTime());

    cashEntryTO.setStatus(orderPO.getStatus());
    fxQquote = fxQuoteService.getFXQuoteForDate(orderPO.getCashCurrency(), currentAccountPO.getPerformanceCurrency(), orderPO.getTransactTime().plusDays(3));
    cashEntryTO.setAchatVenteCode(orderPO.getSide());
    cashEntryTO.setValueDate(orderPO.getTransactTime().plusDays(3));
    cashEntryTO.setGrossAmount(orderPO.getAmount());
    cashEntryTO.setNetAmount(cashEntryTO.getGrossAmount());
    cashEntryTO.setCurrency(orderPO.getCashCurrency());
    cashEntryTO.setFxExchangeRate(Float.valueOf(fxQquote.getAdjustedClose()));
    cashEntryTO.setAccountReportingCurrency(currentAccountPO.getPerformanceCurrency());
    cashEntryTO.setEntryValueReportingCurrency(cashEntryTO.getFxExchangeRate() * cashEntryTO.getNetAmount());


    return cashEntryTO;

  }


  @MarketProcess(entity = CashEntryPO.class)
  public CashEntryPO save(CashEntryPO entry) {

    if (getLogger().isLoggable(Level.FINE)) {
      getLogger().fine(String.format("Created new entry with id %s", entry.getId()));
    }
    return repository.save(entry);
  }

  protected Logger getLogger() {
    return logger;
  }


}



