package ch.nblotti.leonidas.entry.security;

import ch.nblotti.leonidas.account.Account;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.Asset;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.entry.EntryReceiver;
import ch.nblotti.leonidas.order.Order;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.quote.Quote;
import ch.nblotti.leonidas.quote.asset.QuoteService;
import ch.nblotti.leonidas.quote.fx.FXQuoteService;
import ch.nblotti.leonidas.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class SecurityEntryReceiver extends EntryReceiver<SecurityEntry> {


  private final static Logger LOGGER = Logger.getLogger("SecurityEntryReceiver");

  @Autowired
  AccountService accountService;

  @Autowired
  AssetService assetService;

  @Autowired
  QuoteService quoteService;

  @Autowired
  FXQuoteService fxQuoteService;

  @Autowired
  SecurityEntryService securityEntryService;

  @Autowired
  MarketProcessService marketProcessService;

  @JmsListener(destination = "orderbox", containerFactory = "factory")
  public void orderListener(Message message) {
    switch (message.getMessageType()) {
      case MARKET_ORDER:
        this.receiveNewOrder(message);
        break;
      default:
        LOGGER.log(Level.FINE, String.format("Type unknown for entry with id %s", message.getOrderID()));

        break;
    }
  }

  //TODO NBL : test me
  protected SecurityEntry fromOrder(Order order) {


    SecurityEntry securityEntry = new SecurityEntry();
    Account account = accountService.findAccountById(order.getAccountId());
    Asset asset = assetService.getSymbol(order.getExchange(), order.getSymbol());
    Quote quote = quoteService.getQuoteForDate(order.getExchange(), order.getSymbol(), order.getTransactTime().plusDays(assetService.getValueDateForExchange(asset.getExchange())));


    securityEntry.setAccount(order.getAccountId());
    securityEntry.setOrderID(order.getId());
    securityEntry.setEntryDate(order.getTransactTime());
    securityEntry.setDebitCreditCode(order.getSide());
    securityEntry.setStatus(order.getStatus());
    securityEntry.setValueDate(order.getTransactTime().plusDays(assetService.getValueDateForExchange(asset.getExchange())));
    securityEntry.setGrossAmount(order.getOrderQtyData() * Float.valueOf(quote.getAdjustedClose()));
    securityEntry.setNetAmount(order.getOrderQtyData() * Float.valueOf(quote.getAdjustedClose()));
    securityEntry.setFxExchangeRate(Float.valueOf(fxQuoteService.getFXQuoteForDate(asset.getCurrency(), account.getPerformanceCurrency(), securityEntry.getValueDate()).getAdjustedClose()));
    securityEntry.setAccountReportingCurrency(account.getPerformanceCurrency());
    securityEntry.setQuantity(order.getOrderQtyData());
    securityEntry.setSecurityID(asset.getCode());
    securityEntry.setExchange(order.getExchange());
    securityEntry.setCurrency(asset.getCurrency());
    securityEntry.setEntryValueReportingCurrency(securityEntry.getFxExchangeRate() * securityEntry.getNetAmount());

    return securityEntry;
  }

  @Override
  protected SecurityEntry save(SecurityEntry entry) {

    LOGGER.log(Level.FINE, String.format("Created new entry with id %s", entry.getId()));
    SecurityEntry saved = securityEntryService.save(entry);

    marketProcessService.setSecurityhEntryRunningForProcess(entry.getOrderID(),entry.getAccount());


    return saved;


  }


}
