package ch.nblotti.leonidas.process.order;

import ch.nblotti.leonidas.entry.cash.CashEntryPO;
import ch.nblotti.leonidas.entry.cash.CashEntryService;
import ch.nblotti.leonidas.entry.security.SecurityEntryPO;
import ch.nblotti.leonidas.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.order.OrderPO;
import ch.nblotti.leonidas.order.OrderService;
import ch.nblotti.leonidas.position.cash.CashPositionService;
import ch.nblotti.leonidas.position.security.SecurityPositionService;
import ch.nblotti.leonidas.process.MarketProcessService;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.CompositeStateMachineListener;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@EnableStateMachine
@WithStateMachine
@Component
public class MarketProcessStrategy extends CompositeStateMachineListener<ORDER_STATES, ORDER_EVENTS> {


  private final Logger logger = Logger.getLogger("MarketProcessor");


  @Autowired
  SecurityEntryService securityEntryService;

  @Autowired
  SecurityPositionService securityPositionService;

  @Autowired
  CashEntryService cashEntryService;

  @Autowired
  MarketProcessService marketProcessService;

  @Autowired
  CashPositionService cashPositionService;

  @Autowired
  OrderService orderService;


  private final StateMachine<ORDER_STATES, ORDER_EVENTS> stateMachine;

  public MarketProcessStrategy() throws Exception {

    stateMachine = buildMachine1();
    stateMachine.addStateListener(this);
    stateMachine.start();

  }


  public void addStateListener(StateMachineListener<ORDER_STATES, ORDER_EVENTS> listener) {
    getStateMachine().addStateListener(listener);
  }

  @JmsListener(destination = "orderbox", containerFactory = "factory")
  public void orderListener(MessageVO messageVO) {


    Optional<OrderPO> order = orderService.findById(String.valueOf(messageVO.getOrderID()));

    if (!order.isPresent()) {
      throw new IllegalStateException(String.format("No order for id %s, returning", messageVO.getOrderID()));
    }
    OrderPO orderPO = order.get();

    if (orderPO.getType() == ORDER_TYPE.MARKET_ORDER) {
      this.getStateMachine().sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
      CashEntryPO marketCashEntryTO = cashEntryService.fromMarketOrder(orderPO);
      cashEntryService.save(marketCashEntryTO);
      SecurityEntryPO marketSecurityEntryTO = securityEntryService.fromSecurityEntryOrder(orderPO);
      securityEntryService.save(marketSecurityEntryTO);
    }
    if (orderPO.getType() == ORDER_TYPE.CASH_ENTRY) {
      this.getStateMachine().sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
      CashEntryPO cashEntryTO = cashEntryService.fromCashEntryOrder(orderPO);
      cashEntryService.save(cashEntryTO);
    }
    if (orderPO.getType() == ORDER_TYPE.SECURITY_ENTRY) {
      this.getStateMachine().sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
      SecurityEntryPO securityEntryTO = securityEntryService.fromSecurityEntryOrder(orderPO);
      securityEntryService.save(securityEntryTO);
    }

  }

  @JmsListener(destination = "cashentrybox", containerFactory = "factory")
  public void receiveNewCashEntry(MessageVO messageVO) {
    CashEntryPO cashEntryTO = cashEntryService.findByAccountAndOrderID(messageVO.getAccountID(), messageVO.getOrderID());
    log(String.format("Start creation of cash positions for entry with id %s", messageVO.getOrderID()));

    long startTime = System.nanoTime();
    cashPositionService.updatePositions(cashEntryTO);
    long endTime = System.nanoTime();
    long elapsedTime = TimeUnit.SECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
    log(String.format("End creation of cash positions for entry from order with id %s, it took me %d seconds", messageVO.getOrderID(), elapsedTime));

    marketProcessService.setCashPositionRunningForProcess(messageVO.getOrderID(), messageVO.getAccountID());
    this.getStateMachine().sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);

  }

  @JmsListener(destination = "securityentrybox", containerFactory = "factory")
  public void receiveNewSecurityEntry(MessageVO messageVO) {
    SecurityEntryPO securityEntry = securityEntryService.findByAccountAndOrderID(messageVO.getAccountID(), messageVO.getOrderID());
    log(String.format("Start creation of security positions for entry with id %s", messageVO.getAccountID()));

    long startTime = System.nanoTime();
    securityPositionService.updatePosition(securityEntry);
    long endTime = System.nanoTime();
    long elapsedTime = TimeUnit.SECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
    log(String.format("End creation of security positions for entry from order with id %s, it took me %d seconds", messageVO.getAccountID(), elapsedTime));

    marketProcessService.setSecurityPositionRunningForProcess(messageVO.getOrderID(), messageVO.getAccountID());


    this.getStateMachine().sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
  }

  @JmsListener(destination = "cashpositionbox", containerFactory = "factory")
  public void receiveNewCashPosition(MessageVO messageVO) {
    marketProcessService.setCashFinishedForProcess(messageVO.getOrderID(), messageVO.getAccountID());
    this.getStateMachine().sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
  }

  @JmsListener(destination = "securitypositionbox", containerFactory = "factory")
  public void receiveNewSecurityPosition(MessageVO messageVO) {
    marketProcessService.setSecurityFinishedForProcess(messageVO.getOrderID(), messageVO.getAccountID());
    this.getStateMachine().sendEvent(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);

  }


  public boolean sendEvent(Message<ORDER_EVENTS> event) {
    return getStateMachine().sendEvent(event);
  }

  public boolean sendEvent(ORDER_EVENTS event) {
    return getStateMachine().sendEvent(event);
  }

  public void start() {
    getStateMachine().start();
  }

  public StateMachine<ORDER_STATES, ORDER_EVENTS> buildMachine1() throws Exception {
    StateMachineBuilder.Builder<ORDER_STATES, ORDER_EVENTS> builder = StateMachineBuilder.builder();
    builder.configureStates()
      .withStates()
      .initial(ORDER_STATES.READY)
      .choice(ORDER_STATES.TREATING_EVENT)
      .state(ORDER_STATES.MO_ORDER_CREATING)
      .fork(ORDER_STATES.MO_ORDER_CREATED)
      .join(ORDER_STATES.MO_JOIN)
      .state(ORDER_STATES.LAST)
      .end(ORDER_STATES.READY)
      .and()
      .withStates()
      .parent(ORDER_STATES.MO_ORDER_CREATED)
      .initial(ORDER_STATES.MO_CREATING_CASH_ENTRY)
      .state(ORDER_STATES.MO_CASH_ENTRY_CREATED)
      .state(ORDER_STATES.MO_CREATING_CASH_POSITIONS)
      .end(ORDER_STATES.MO_CASH_POSITIONS_CREATED)
      .and()
      .withStates()
      .parent(ORDER_STATES.MO_ORDER_CREATED)
      .initial(ORDER_STATES.MO_CREATING_SECURITY_ENTRY)
      .state(ORDER_STATES.MO_SECURITY_ENTRY_CREATED)
      .state(ORDER_STATES.MO_CREATING_SECURITY_POSITIONS)
      .end(ORDER_STATES.MO_SECURITY_POSITIONS_CREATED)
      .and()
      .withStates()
      .state(ORDER_STATES.CE_ORDER_CREATING)
      .state(ORDER_STATES.CE_ORDER_CREATED)
      .state(ORDER_STATES.CE_CREATING_CASH_ENTRY)
      .state(ORDER_STATES.CE_CASH_ENTRY_CREATED)
      .state(ORDER_STATES.CE_CREATING_CASH_POSITIONS)
      .state(ORDER_STATES.CE_CASH_POSITIONS_CREATED)
      .and()
      .withStates()
      .state(ORDER_STATES.SE_ORDER_CREATING)
      .state(ORDER_STATES.SE_ORDER_CREATED)
      .state(ORDER_STATES.SE_CREATING_SECURITY_ENTRY)
      .state(ORDER_STATES.SE_SECURITY_ENTRY_CREATED)
      .state(ORDER_STATES.SE_CREATING_SECURITY_POSITIONS)
      .state(ORDER_STATES.SE_SECURITY_POSITIONS_CREATED);


    builder.configureTransitions()
      .withExternal()
      .source(ORDER_STATES.READY).target(ORDER_STATES.TREATING_EVENT).event(ORDER_EVENTS.EVENT_RECEIVED)
      .and()
      .withChoice()
      .source(ORDER_STATES.TREATING_EVENT)
      .first(ORDER_STATES.MO_ORDER_CREATING, isMarketOrder())
      .then(ORDER_STATES.CE_ORDER_CREATING, isCashEntry())
      .then(ORDER_STATES.SE_ORDER_CREATING, isSecurityEntry())
      .last(ORDER_STATES.READY)
/* Flux d'un apport cash*/
      .and()
      .withExternal()
      .source(ORDER_STATES.CE_ORDER_CREATING).target(ORDER_STATES.CE_ORDER_CREATED).event(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.CE_ORDER_CREATED).target(ORDER_STATES.CE_CREATING_CASH_ENTRY)
      .and()
      .withExternal()
      .source(ORDER_STATES.CE_CREATING_CASH_ENTRY).target(ORDER_STATES.CE_CASH_ENTRY_CREATED).event(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.CE_CASH_ENTRY_CREATED).target(ORDER_STATES.CE_CREATING_CASH_POSITIONS)
      .and()
      .withExternal()
      .source(ORDER_STATES.CE_CREATING_CASH_POSITIONS).target(ORDER_STATES.CE_CASH_POSITIONS_CREATED).event(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.CE_CASH_POSITIONS_CREATED).target(ORDER_STATES.READY)
/* Flux d'un apport de titre*/
      .and()
      .withExternal()
      .source(ORDER_STATES.SE_ORDER_CREATING).target(ORDER_STATES.SE_ORDER_CREATED).event(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.SE_ORDER_CREATED).target(ORDER_STATES.SE_CREATING_SECURITY_ENTRY)
      .and()
      .withExternal()
      .source(ORDER_STATES.SE_CREATING_SECURITY_ENTRY).target(ORDER_STATES.SE_SECURITY_ENTRY_CREATED).event(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.SE_SECURITY_ENTRY_CREATED).target(ORDER_STATES.SE_CREATING_SECURITY_POSITIONS)
      .and()
      .withExternal()
      .source(ORDER_STATES.SE_CREATING_SECURITY_POSITIONS).target(ORDER_STATES.SE_SECURITY_POSITIONS_CREATED).event(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.SE_SECURITY_POSITIONS_CREATED).target(ORDER_STATES.READY)
/* Flux d'un ordre de marché*/
      .and()
      .withExternal()
      .source(ORDER_STATES.MO_ORDER_CREATING).target(ORDER_STATES.MO_ORDER_CREATED).event(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL)
      .and()
      .withFork()
      .source(ORDER_STATES.MO_ORDER_CREATED)
      .target(ORDER_STATES.MO_CREATING_CASH_ENTRY)
      .target(ORDER_STATES.MO_CREATING_SECURITY_ENTRY)
      .and()
      .withExternal()
      .source(ORDER_STATES.MO_CREATING_CASH_ENTRY).target(ORDER_STATES.MO_CASH_ENTRY_CREATED).event(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.MO_CASH_ENTRY_CREATED).target(ORDER_STATES.MO_CREATING_CASH_POSITIONS)
      .and()
      .withExternal()
      .source(ORDER_STATES.MO_CREATING_CASH_POSITIONS).target(ORDER_STATES.MO_CASH_POSITIONS_CREATED).event(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.MO_CREATING_SECURITY_ENTRY).target(ORDER_STATES.MO_SECURITY_ENTRY_CREATED).event(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(ORDER_STATES.MO_SECURITY_ENTRY_CREATED).target(ORDER_STATES.MO_CREATING_SECURITY_POSITIONS)
      .and()
      .withExternal()
      .source(ORDER_STATES.MO_CREATING_SECURITY_POSITIONS).target(ORDER_STATES.MO_SECURITY_POSITIONS_CREATED).event(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL)
      .and()
      .withJoin()
      .source(ORDER_STATES.MO_CASH_POSITIONS_CREATED)
      .source(ORDER_STATES.MO_SECURITY_POSITIONS_CREATED)
      .target(ORDER_STATES.MO_JOIN)
      .and()
      .withExternal()
      .source(ORDER_STATES.MO_JOIN).target(ORDER_STATES.READY);
    return builder.build();
  }


  private Guard<ORDER_STATES, ORDER_EVENTS> isSecurityEntry() {


    return context -> ORDER_TYPE.SECURITY_ENTRY.equals((ORDER_TYPE) context.getMessageHeader("type"));

  }


  private Guard<ORDER_STATES, ORDER_EVENTS> isCashEntry() {

    return context -> ORDER_TYPE.CASH_ENTRY.equals((ORDER_TYPE) context.getMessageHeader("type"));

  }

  private Guard<ORDER_STATES, ORDER_EVENTS> isMarketOrder() {

    return context -> ORDER_TYPE.MARKET_ORDER.equals((ORDER_TYPE) context.getMessageHeader("type"));

  }

  @Override
  public void stateChanged(State from, State to) {

    log(String.format("%s %s", from == null ? "" : from.getId(), to.getId()));

  }

  Logger getLogger() {
    return logger;
  }

  public StateMachine<ORDER_STATES, ORDER_EVENTS> getStateMachine() {
    return stateMachine;
  }

  void log(String logValue) {
    if (getLogger().isLoggable(Level.FINE)) {
      logger.fine(logValue);
    }
  }


}
