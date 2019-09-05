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
  SecurityEntryService securityEntryService;


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

  protected SecurityEntryPO fromOrder(OrderPO orderPO) {

    return securityEntryService.fromOrder(orderPO);
  }

  @Override
  protected SecurityEntryPO save(SecurityEntryPO securityEntryPO) {

    return securityEntryService.save(securityEntryPO);

  }


}
