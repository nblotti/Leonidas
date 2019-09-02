package ch.nblotti.leonidas.security.entry.security;

import ch.nblotti.leonidas.security.account.Account;
import ch.nblotti.leonidas.security.account.AccountService;
import ch.nblotti.leonidas.security.asset.*;
import ch.nblotti.leonidas.security.entry.EntryReceiver;
import ch.nblotti.leonidas.security.order.Order;
import ch.nblotti.leonidas.security.quote.Quote;
import ch.nblotti.leonidas.security.quote.asset.QuoteService;
import ch.nblotti.leonidas.security.quote.fx.FXQuoteService;
import ch.nblotti.leonidas.security.technical.Message;
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

  @JmsListener(destination = "orderbox", containerFactory = "factory")
  public void orderListener(Message message) {
    switch (message.getMessageType()) {
      case MARKET_ORDER:
        this.receiveNewOrder(message);
        break;
      default:
        LOGGER.log(Level.FINE, String.format("Type unknown for entry with id %s", message.getEntity_id()));

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
    securityEntry.setcIOrdID(order.getcIOrdID());
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
    return securityEntryService.save(entry);
  }


}
