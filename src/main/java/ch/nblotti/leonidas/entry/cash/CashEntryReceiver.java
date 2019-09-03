package ch.nblotti.leonidas.entry.cash;

import ch.nblotti.leonidas.account.Account;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.Asset;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.quote.Quote;
import ch.nblotti.leonidas.quote.asset.QuoteService;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.EntryReceiver;
import ch.nblotti.leonidas.order.Order;
import ch.nblotti.leonidas.quote.fx.FXQuoteService;
import ch.nblotti.leonidas.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CashEntryReceiver extends EntryReceiver<CashEntry> {

  private  static final Logger LOGGER = Logger.getLogger("CashEntryReceiver");

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

  @Autowired
  MarketProcessService marketProcessService;


  @JmsListener(destination = "orderbox", containerFactory = "factory")
  public void orderListener(Message message) {

    switch (message.getMessageType()) {
      case MARKET_ORDER:

        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Create cash entry from market order with id %s", message.getOrderID()));
        }
        this.receiveNewOrder(message);
        break;
      case CASH_ENTRY:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Create cash entry from cash order with id %s", message.getOrderID()));
        }
        this.receiveNewOrder(message);
        break;
      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Action unknown for order with id %s", message.getOrderID()));
        }

        break;
    }


  }

  //TODO NBL : test me
  protected CashEntry fromOrder(Order order) {

    CashEntry cashEntry = new CashEntry();
    Quote fxQquote;

    Account currentAccount = acountService.findAccountById(order.getAccountId());

    cashEntry.setAccount(currentAccount.getId());
    cashEntry.setOrderID(order.getId());
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
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Type unknown for order with id %s", order.getId()));
        }
        break;
    }
    cashEntry.setAccountReportingCurrency(currentAccount.getPerformanceCurrency());
    cashEntry.setEntryValueReportingCurrency(cashEntry.getFxExchangeRate() * cashEntry.getNetAmount());


    return cashEntry;
  }

  @Override
  protected CashEntry save(CashEntry entry) {

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(String.format("Created new entry with id %s", entry.getId()));
    }
    CashEntry saved = cashEntryService.save(entry);

    marketProcessService.setCashEntryRunningForProcess(entry.getOrderID(), entry.getAccount());


    return saved;
  }


}
