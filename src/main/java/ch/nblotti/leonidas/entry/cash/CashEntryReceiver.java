package ch.nblotti.leonidas.entry.cash;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.asset.QuoteService;
import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.EntryReceiver;
import ch.nblotti.leonidas.quote.fx.FXQuoteService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CashEntryReceiver extends EntryReceiver<CashEntryPO> {

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
  public void orderListener(MessageVO messageVO) {

    switch (messageVO.getMessageType()) {
      case MARKET_ORDER:

        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Create cash entry from market order with id %s", messageVO.getOrderID()));
        }
        this.receiveNewOrder(messageVO);
        break;
      case CASH_ENTRY:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Create cash entry from cash order with id %s", messageVO.getOrderID()));
        }
        this.receiveNewOrder(messageVO);
        break;
      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Action unknown for order with id %s", messageVO.getOrderID()));
        }

        break;
    }


  }

  //TODO NBL : test me
  protected CashEntryPO fromOrder(OrderPO orderPO) {

    CashEntryPO cashEntryTO = new CashEntryPO();
    QuoteDTO fxQquote;

    AccountPO currentAccountPO = acountService.findAccountById(orderPO.getAccountId());

    cashEntryTO.setAccount(currentAccountPO.getId());
    cashEntryTO.setOrderID(orderPO.getId());
    cashEntryTO.setEntryDate(orderPO.getTransactTime());

    cashEntryTO.setStatus(orderPO.getStatus());

    switch (orderPO.getType()) {
      case MARKET_ORDER:

        AssetPO assetPO = assetService.getSymbol(orderPO.getExchange(), orderPO.getSymbol());
        fxQquote = fxQuoteService.getFXQuoteForDate(assetPO.getCurrency(), currentAccountPO.getPerformanceCurrency(), orderPO.getTransactTime().plusDays(assetService.getValueDateForExchange(assetPO.getExchange())));
        QuoteDTO quoteDTO = quoteService.getQuoteForDate(orderPO.getExchange(), orderPO.getSymbol(), orderPO.getTransactTime().plusDays(assetService.getValueDateForExchange(assetPO.getExchange())));
        cashEntryTO.setValueDate(orderPO.getTransactTime().plusDays(assetService.getValueDateForExchange(assetPO.getExchange())));
        cashEntryTO.setGrossAmount(orderPO.getOrderQtyData() * Float.valueOf(quoteDTO.getAdjustedClose()));
        cashEntryTO.setDebitCreditCode(orderPO.getSide().equals(DEBIT_CREDIT.CRDT) ? DEBIT_CREDIT.DBIT : DEBIT_CREDIT.CRDT);
        cashEntryTO.setNetAmount(cashEntryTO.getGrossAmount());
        cashEntryTO.setCurrency(assetPO.getCurrency());
        cashEntryTO.setFxExchangeRate(Float.valueOf(fxQquote.getAdjustedClose()));
        break;
      case CASH_ENTRY:

        fxQquote = fxQuoteService.getFXQuoteForDate(orderPO.getCashCurrency(), currentAccountPO.getPerformanceCurrency(), orderPO.getTransactTime().plusDays(3));

        cashEntryTO.setDebitCreditCode(orderPO.getSide());
        cashEntryTO.setValueDate(orderPO.getTransactTime().plusDays(3));
        cashEntryTO.setGrossAmount(orderPO.getAmount());
        cashEntryTO.setNetAmount(cashEntryTO.getGrossAmount());
        cashEntryTO.setCurrency(orderPO.getCashCurrency());
        cashEntryTO.setFxExchangeRate(Float.valueOf(fxQquote.getAdjustedClose()));
        break;
      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Type unknown for order with id %s", orderPO.getId()));
        }
        break;
    }
    cashEntryTO.setAccountReportingCurrency(currentAccountPO.getPerformanceCurrency());
    cashEntryTO.setEntryValueReportingCurrency(cashEntryTO.getFxExchangeRate() * cashEntryTO.getNetAmount());


    return cashEntryTO;
  }

  @Override
  protected CashEntryPO save(CashEntryPO entry) {

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(String.format("Created new entry with id %s", entry.getId()));
    }
    CashEntryPO saved = cashEntryService.save(entry);

    marketProcessService.setCashEntryRunningForProcess(entry.getOrderID(), entry.getAccount());


    return saved;
  }


}
