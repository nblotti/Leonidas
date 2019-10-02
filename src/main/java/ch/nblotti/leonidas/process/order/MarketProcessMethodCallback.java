package ch.nblotti.leonidas.process.order;

import ch.nblotti.leonidas.entry.cash.CashEntryPO;
import ch.nblotti.leonidas.entry.security.SecurityEntryPO;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.logging.Logger;

@Aspect
@Component
public class MarketProcessMethodCallback {


  public static final String ORDERBOX = "orderbox";
  public static final String SECURITYENTRYBOX = "securityentrybox";
  public static final String CASHENTRYBOX = "cashentrybox";

  @Autowired
  private JmsTemplate jmsTemplate;


  @Autowired
  MarketProcessService marketProcessService;

  @Around("@annotation(ch.nblotti.leonidas.process.order.MarketProcess)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    Method method = signature.getMethod();

    MarketProcess myAnnotation = method.getAnnotation(MarketProcess.class);

    Object toreturn = joinPoint.proceed();

    if (myAnnotation.entity().isAssignableFrom(OrderPO.class)) {

      OrderPO createdOrderPO = (OrderPO) toreturn;
      postMessage(createdOrderPO);
      marketProcessService.startMarketProcessService(createdOrderPO, createdOrderPO.getAccountId());

    } else if (myAnnotation.entity().isAssignableFrom(CashEntryPO.class)) {

      CashEntryPO cashEntryPO = (CashEntryPO) toreturn;
      marketProcessService.setCashEntryRunningForProcess(cashEntryPO.getOrderID(), cashEntryPO.getAccount());

      jmsTemplate.convertAndSend(CASHENTRYBOX, new MessageVO(cashEntryPO.getOrderID(), cashEntryPO.getAccount(), MessageVO.MESSAGE_TYPE.CASH_ENTRY, MessageVO.ENTITY_ACTION.CREATE));

    } else if (myAnnotation.entity().isAssignableFrom(SecurityEntryPO.class)) {

      SecurityEntryPO securityEntryPO = (SecurityEntryPO) toreturn;


      marketProcessService.setSecurityhEntryRunningForProcess(securityEntryPO.getOrderID(), securityEntryPO.getAccount());
      jmsTemplate.convertAndSend(SECURITYENTRYBOX, new MessageVO(securityEntryPO.getOrderID(), securityEntryPO.getAccount(), MessageVO.MESSAGE_TYPE.CASH_ENTRY, MessageVO.ENTITY_ACTION.CREATE));
    }
    return toreturn;

  }

  MessageVO postMessage(OrderPO createdOrderPO) {


    MessageVO messageVO;
    switch (createdOrderPO.getType()) {

      case CASH_ENTRY:
        messageVO = new MessageVO(createdOrderPO.getId(), createdOrderPO.getAccountId(), MessageVO.MESSAGE_TYPE.CASH_ENTRY, MessageVO.ENTITY_ACTION.CREATE);
        jmsTemplate.convertAndSend(ORDERBOX, messageVO);
        break;


      case MARKET_ORDER:
        messageVO = new MessageVO(createdOrderPO.getId(), createdOrderPO.getAccountId(), MessageVO.MESSAGE_TYPE.MARKET_ORDER, MessageVO.ENTITY_ACTION.CREATE);
        jmsTemplate.convertAndSend(ORDERBOX, messageVO);
        break;


      case SECURITY_ENTRY:
        messageVO = new MessageVO(createdOrderPO.getId(), createdOrderPO.getAccountId(), MessageVO.MESSAGE_TYPE.SECURITY_ENTRY, MessageVO.ENTITY_ACTION.CREATE);
        jmsTemplate.convertAndSend(ORDERBOX, messageVO);
        break;
      default:
        throw new IllegalArgumentException("Order type should be one of the known value");

    }
    return messageVO;
  }

}
