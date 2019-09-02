package ch.nblotti.leonidas.security.entry.cash;

import ch.nblotti.leonidas.security.account.Account;
import ch.nblotti.leonidas.security.account.AccountService;
import ch.nblotti.leonidas.security.asset.Asset;
import ch.nblotti.leonidas.security.asset.AssetService;
import ch.nblotti.leonidas.security.quote.Quote;
import ch.nblotti.leonidas.security.quote.asset.QuoteService;
import ch.nblotti.leonidas.security.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.security.entry.EntryReceiver;
import ch.nblotti.leonidas.security.order.Order;
import ch.nblotti.leonidas.security.quote.fx.FXQuoteService;
import ch.nblotti.leonidas.security.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CashEntryReceiver extends EntryReceiver<CashEntry> {

  private final static Logger LOGGER = Logger.getLogger("CashEntryReceiver");

  @Autowired
  CashEntryService cashEntryService;

  @Autowired
  QuoteService quoteService;

  @Autowired
  FXQuoteService fxQuoteService;


  @Autowired
  AssetService assetService;

  @Autowired
  AccountService acountService;


  @JmsListener(destination = "orderbox", containerFactory = "factory")
  public void orderListener(Message message) {

    switch (message.getMessageType()) {
      case MARKET_ORDER:
        LOGGER.log(Level.FINE, String.format("Create cash entry from market order with id %s", message.getEntity_id()));
        break;
      case CASH_ENTRY:
        LOGGER.log(Level.FINE, String.format("Create cash entry from cash entry with id %s", message.getEntity_id()));
        this.receiveNewOrder(message);
        break;
      default:
        LOGGER.log(Level.FINE, String.format("Action unknown for order with id %s", message.getEntity_id()));

        break;
    }


  }

  //TODO NBL : test me
  protected CashEntry fromOrder(Order order) {

    CashEntry cashEntry = new CashEntry();
    Quote fxQquote;

    Account currentAccount = acountService.findAccountById(order.getAccountId());
    cashEntry.setAccount(currentAccount.getId());
    cashEntry.setcIOrdID(order.getcIOrdID());
    cashEntry.setEntryDate(order.getTransactTime());

    cashEntry.setStatus(order.getStatus());

    switch (order.getType()) {
      case MARKET_ORDER:

        Asset asset = assetService.getSymbol(order.getExchange(), order.getSymbol());
        fxQquote = fxQuoteService.getFXQuoteForDate(asset.getCurrency(), currentAccount.getPerformanceCurrency(), order.getTransactTime().plusDays(assetService.getValueDateForExchange(asset.getExchange())));
        Quote quote = quoteService.getQuoteForDate(order.getExchange(), order.getSymbol(), order.getTransactTime().plusDays(assetService.getValueDateForExchange(asset.getExchange())));
        cashEntry.setValueDate(order.getTransactTime().plusDays(assetService.getValueDateForExchange(asset.getExchange())));
        cashEntry.setGrossAmount(order.getOrderQtyData() * Float.valueOf(quote.getAdjustedClose()));
        cashEntry.setDebitCreditCode(order.getSide().equals(DEBIT_CREDIT.CRDT) ? DEBIT_CREDIT.DBIT : DEBIT_CREDIT.CRDT);
        cashEntry.setNetAmount(cashEntry.getGrossAmount());
        cashEntry.setCurrency(asset.getCurrency());
        cashEntry.setFxExchangeRate(Float.valueOf(fxQquote.getAdjustedClose()));
        break;
      case CASH_ENTRY:

        fxQquote = fxQuoteService.getFXQuoteForDate(order.getCashCurrency(), currentAccount.getPerformanceCurrency(), order.getTransactTime().plusDays(3));

        cashEntry.setDebitCreditCode(order.getSide());
        cashEntry.setValueDate(order.getTransactTime().plusDays(3));
        cashEntry.setGrossAmount(order.getAmount());
        cashEntry.setNetAmount(cashEntry.getGrossAmount());
        cashEntry.setCurrency(order.getCashCurrency());
        cashEntry.setFxExchangeRate(Float.valueOf(fxQquote.getAdjustedClose()));
        break;
      default:
        LOGGER.log(Level.FINE, String.format("Type unknown for order with id %s", order.getId()));

        break;
    }
    cashEntry.setAccountReportingCurrency(currentAccount.getPerformanceCurrency());
    cashEntry.setEntryValueReportingCurrency(cashEntry.getFxExchangeRate() * cashEntry.getNetAmount());


    return cashEntry;
  }

  @Override
  protected CashEntry save(CashEntry entry) {

    LOGGER.log(Level.FINE,String.format("Created new entry with id %s", entry.getId()));
    return cashEntryService.save(entry);
  }


}
