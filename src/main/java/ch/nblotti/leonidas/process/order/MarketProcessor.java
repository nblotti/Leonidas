package ch.nblotti.leonidas.process.order;

import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
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
import org.springframework.web.context.WebApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@EnableStateMachine
@WithStateMachine
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MarketProcessor extends CompositeStateMachineListener<ORDER_STATES, ORDER_EVENTS> {
  private static final Logger logger = Logger.getLogger("MarketProcessor");


  private final StateMachine<ORDER_STATES, ORDER_EVENTS> stateMachine;

  public MarketProcessor() {

    stateMachine = buildMachine1();
    stateMachine.addStateListener(this);
    stateMachine.start();

  }


  public void addStateListener(StateMachineListener<ORDER_STATES, ORDER_EVENTS> listener) {
    stateMachine.addStateListener(listener);
  }

  @JmsListener(destination = "orderbox", containerFactory = "factory")
  public void orderListener(MessageVO messageVO) {

    if (MessageVO.MESSAGE_TYPE.MARKET_ORDER == messageVO.getMessageType()) {
      this.stateMachine.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    }
  }

  @JmsListener(destination = "cashentrybox", containerFactory = "factory")
  public void receiveNewCashEntry(MessageVO messageVO) {
    this.stateMachine.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);

  }

  @JmsListener(destination = "securityentrybox", containerFactory = "factory")
  public void receiveNewSecurityEntry(MessageVO messageVO) {
    this.stateMachine.sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
  }

  @JmsListener(destination = "cashpositionbox", containerFactory = "factory")
  public void receiveNewCashPosition(MessageVO messageVO) {
    this.stateMachine.sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
  }

  @JmsListener(destination = "securitypositionbox", containerFactory = "factory")
  public void receiveNewSecurityPosition(MessageVO messageVO) {
    this.stateMachine.sendEvent(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);

  }


  public boolean sendEvent(Message<ORDER_EVENTS> event) {
    return stateMachine.sendEvent(event);
  }

  public boolean sendEvent(ORDER_EVENTS event) {
    return stateMachine.sendEvent(event);
  }

  public void start() {
    stateMachine.start();
  }

  public StateMachine<ORDER_STATES, ORDER_EVENTS> buildMachine1() {
    StateMachineBuilder.Builder<ORDER_STATES, ORDER_EVENTS> builder = StateMachineBuilder.builder();
    try {
      builder.configureStates()
        .withStates()
        .initial(ORDER_STATES.READY)
        .choice(ORDER_STATES.TREATING_EVENT)
        .state(ORDER_STATES.ORDER_CREATING)
        .fork(ORDER_STATES.ORDER_CREATED)
        .join(ORDER_STATES.MO_JOIN)
        .state(ORDER_STATES.LAST)
        .end(ORDER_STATES.READY)
        .and()
        .withStates()
        .parent(ORDER_STATES.ORDER_CREATED)
        .initial(ORDER_STATES.MO_CREATING_CASH_ENTRY)
        .state(ORDER_STATES.MO_CASH_ENTRY_CREATED)
        .state(ORDER_STATES.MO_CREATING_CASH_POSITIONS)
        .end(ORDER_STATES.MO_CASH_POSITIONS_CREATED)
        .and()
        .withStates()
        .parent(ORDER_STATES.ORDER_CREATED)
        .initial(ORDER_STATES.MO_CREATING_SECURITY_ENTRY)
        .state(ORDER_STATES.MO_SECURITY_ENTRY_CREATED)
        .state(ORDER_STATES.MO_CREATING_SECURITY_POSITIONS)
        .end(ORDER_STATES.MO_SECURITY_POSITIONS_CREATED)
        .and()
        .withStates()
        .state(ORDER_STATES.CE_CREATING_CASH_ENTRY)
        .state(ORDER_STATES.CE_CASH_ENTRY_CREATED)
        .state(ORDER_STATES.CE_CREATING_CASH_POSITIONS)
        .state(ORDER_STATES.CE_CASH_POSITIONS_CREATED)
        .and()
        .withStates()
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
        .first(ORDER_STATES.ORDER_CREATING, isMarketOrder())
        .then(ORDER_STATES.CE_CREATING_CASH_ENTRY, isCashEntry())
        .then(ORDER_STATES.SE_CREATING_SECURITY_ENTRY, isSecurityEntry())
        .last(ORDER_STATES.READY)
/* Flux d'un apport cash*/
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
        .source(ORDER_STATES.ORDER_CREATING).target(ORDER_STATES.ORDER_CREATED).event(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL)
        .and()
        .withFork()
        .source(ORDER_STATES.ORDER_CREATED)
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
    } catch (Exception e) {

      logger.severe(e.getMessage());

    }
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

    if (logger.isLoggable(Level.FINE)) {
      logger.fine(String.format("%s %s", from == null ? "" : from.getId(), to.getId()));
    }
  }

}
