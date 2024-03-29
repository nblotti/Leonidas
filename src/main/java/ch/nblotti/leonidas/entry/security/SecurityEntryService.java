package ch.nblotti.leonidas.entry.security;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
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
public class SecurityEntryService {


  static final Logger logger = Logger.getLogger("SecurityEntryService");

  @Autowired
  private SecurityEntryRepository repository;

  @Autowired
  AccountService accountService;

  @Autowired
  AssetService assetService;

  @Autowired
  QuoteService quoteService;


  @Autowired
  FXQuoteService fxQuoteService;


  public Iterable<SecurityEntryPO> findAll() {

    return this.repository.findAll();

  }


  public Optional<SecurityEntryPO> findById(String toString) {
    return this.repository.findById(Long.valueOf(toString));
  }

  public Iterable<SecurityEntryPO> findAllByAccountAndSecurityIDOrderByValueDateAsc(int account, String securityID) {

    return this.repository.findAllByAccountAndSecurityIDOrderByValueDateAsc(account, securityID);
  }

  public SecurityEntryPO findByAccountAndOrderID(int account, long orderID) {

    return this.repository.findByAccountAndOrderID(account, orderID);
  }

  public SecurityEntryPO fromSecurityEntryOrder(OrderPO orderPO) {


    SecurityEntryPO securityEntry = new SecurityEntryPO();
    AccountPO accountPO = accountService.findAccountById(orderPO.getAccountId());
    AssetPO assetPO = assetService.getSymbol(orderPO.getExchange(), orderPO.getSymbol());
    QuoteDTO quoteDTO = quoteService.getQuoteForDate(orderPO.getExchange(), orderPO.getSymbol(), orderPO.getTransactTime().plusDays(assetService.getValueDateForExchange(assetPO.getExchange())));


    securityEntry.setAccount(orderPO.getAccountId());
    securityEntry.setOrderID(orderPO.getId());
    securityEntry.setEntryDate(orderPO.getTransactTime());
    securityEntry.setAchatVenteCode(orderPO.getSide());
    securityEntry.setStatus(orderPO.getStatus());
    securityEntry.setValueDate(orderPO.getTransactTime().plusDays(assetService.getValueDateForExchange(assetPO.getExchange())));
    securityEntry.setGrossAmount(orderPO.getOrderQtyData() * Float.valueOf(quoteDTO.getAdjustedClose()));
    securityEntry.setNetAmount(orderPO.getOrderQtyData() * Float.valueOf(quoteDTO.getAdjustedClose()));
    securityEntry.setFxExchangeRate(Float.valueOf(fxQuoteService.getFXQuoteForDate(assetPO.getCurrency(), accountPO.getPerformanceCurrency(), securityEntry.getValueDate()).getAdjustedClose()));
    securityEntry.setAccountReportingCurrency(accountPO.getPerformanceCurrency());
    securityEntry.setQuantity(orderPO.getOrderQtyData());
    securityEntry.setSecurityID(assetPO.getCode());
    securityEntry.setExchange(orderPO.getExchange());
    securityEntry.setCurrency(assetPO.getCurrency());
    securityEntry.setEntryValueReportingCurrency(securityEntry.getFxExchangeRate() * securityEntry.getNetAmount());

    return securityEntry;
  }

  @MarketProcess(entity = SecurityEntryPO.class)
  public SecurityEntryPO save(SecurityEntryPO securityEntryPO) {

    if (getLogger().isLoggable(Level.FINE)) {
      getLogger().fine(String.format("Created new entry with id %s", securityEntryPO.getId()));
    }
    return repository.save(securityEntryPO);


  }

  protected Logger getLogger() {
    return logger;
  }


}



