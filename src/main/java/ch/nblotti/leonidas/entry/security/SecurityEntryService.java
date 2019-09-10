package ch.nblotti.leonidas.entry.security;

import ch.nblotti.leonidas.account.AccountPO;
import ch.nblotti.leonidas.account.AccountService;
import ch.nblotti.leonidas.asset.AssetPO;
import ch.nblotti.leonidas.asset.AssetService;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.quote.QuoteDTO;
import ch.nblotti.leonidas.quote.QuoteService;
import ch.nblotti.leonidas.quote.FXQuoteService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class SecurityEntryService {


  static final Logger LOGGER = Logger.getLogger("SecurityEntryService");
  static final String SECURITYENTRYBOX = "securityentrybox";

  @Autowired
  private SecurityEntryRepository repository;

  @Autowired
  private JmsTemplate jmsOrderTemplate;

  @Autowired
  AccountService accountService;

  @Autowired
  AssetService assetService;

  @Autowired
  QuoteService quoteService;


  @Autowired
  FXQuoteService fxQuoteService;

  @Autowired
  MarketProcessService marketProcessService;


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

  public SecurityEntryPO fromOrder(OrderPO orderPO) {


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


  public SecurityEntryPO save(SecurityEntryPO securityEntryPO) {

    if (getLogger().isLoggable(Level.FINE)) {
      getLogger().fine(String.format("Created new entry with id %s", securityEntryPO.getId()));
    }
    SecurityEntryPO saved = this.repository.save(securityEntryPO);
    marketProcessService.setSecurityhEntryRunningForProcess(securityEntryPO.getOrderID(), securityEntryPO.getAccount());
    jmsOrderTemplate.convertAndSend(SECURITYENTRYBOX, new MessageVO(securityEntryPO.getOrderID(), securityEntryPO.getAccount(), MessageVO.MESSAGE_TYPE.CASH_ENTRY, MessageVO.ENTITY_ACTION.CREATE));

    return saved;

  }

  protected Logger getLogger() {
    return LOGGER;
  }


}



