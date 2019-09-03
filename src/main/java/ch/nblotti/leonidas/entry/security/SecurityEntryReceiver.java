package ch.nblotti.leonidas.entry.security;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.entry.EntryReceiver;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.asset.QuoteService;
import ch.nblotti.leonidas.quote.fx.FXQuoteService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.logging.Level;
import java.util.logging.Logger;


@Component
public class SecurityEntryReceiver extends EntryReceiver<SecurityEntryPO> {


  private static final Logger LOGGER = Logger.getLogger("SecurityEntryReceiver");

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
  public void orderListener(MessageVO messageVO) {
    switch (messageVO.getMessageType()) {
      case MARKET_ORDER:
        this.receiveNewOrder(messageVO);
        break;
      case CASH_ENTRY:
      case CASH_POSITION:
      case SECURITY_ENTRY:
      case SECURITY_POSITION:
        break;
      default:
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine(String.format("Type unknown for entry with id %s", messageVO.getOrderID()));
        }
        break;
    }
  }

  //TODO NBL : test me
  protected SecurityEntryPO fromOrder(OrderPO orderPO) {


    SecurityEntryPO securityEntry = new SecurityEntryPO();
    AccountPO accountPO = accountService.findAccountById(orderPO.getAccountId());
    AssetPO assetPO = assetService.getSymbol(orderPO.getExchange(), orderPO.getSymbol());
    QuoteDTO quoteDTO = quoteService.getQuoteForDate(orderPO.getExchange(), orderPO.getSymbol(), orderPO.getTransactTime().plusDays(assetService.getValueDateForExchange(assetPO.getExchange())));


    securityEntry.setAccount(orderPO.getAccountId());
    securityEntry.setOrderID(orderPO.getId());
    securityEntry.setEntryDate(orderPO.getTransactTime());
    securityEntry.setDebitCreditCode(orderPO.getSide());
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

  @Override
  protected SecurityEntryPO save(SecurityEntryPO entry) {

    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine(String.format("Created new entry with id %s", entry.getId()));
    }
    SecurityEntryPO saved = securityEntryService.save(entry);

    marketProcessService.setSecurityhEntryRunningForProcess(entry.getOrderID(), entry.getAccount());


    return saved;


  }


}
