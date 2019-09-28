package ch.nblotti.leonidas.process.marketorder;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.logging.Logger;

@Configuration
@EnableStateMachine
@WithStateMachine
public class MarketProcessConfig extends EnumStateMachineConfigurerAdapter<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> {

  private static final Logger logger = Logger.getLogger("MarketProcessConfig");

  @Override
  public void configure(StateMachineStateConfigurer<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> states)
    throws Exception {
    states
      .withStates()
      .initial(MARKET_ORDER_STATES.READY)
      .state(MARKET_ORDER_STATES.ORDER_CREATING)
      .fork(MARKET_ORDER_STATES.ORDER_CREATED)
      .join(MARKET_ORDER_STATES.JOIN)
      .state(MARKET_ORDER_STATES.LAST)
      .end(MARKET_ORDER_STATES.READY)
      .and()
      .withStates()
      .parent(MARKET_ORDER_STATES.ORDER_CREATED)
      .initial(MARKET_ORDER_STATES.CREATING_CASH_ENTRY)
      .state(MARKET_ORDER_STATES.CASH_ENTRY_CREATED)
      .state(MARKET_ORDER_STATES.CREATING_CASH_POSITIONS)
      .end(MARKET_ORDER_STATES.CASH_POSITIONS_CREATED)
      .and()
      .withStates()
      .parent(MARKET_ORDER_STATES.ORDER_CREATED)
      .initial(MARKET_ORDER_STATES.CREATING_SECURITY_ENTRY)
      .state(MARKET_ORDER_STATES.SECURITY_ENTRY_CREATED)
      .state(MARKET_ORDER_STATES.CREATING_SECURITY_POSITIONS)
      .end(MARKET_ORDER_STATES.SECURITY_POSITIONS_CREATED);


  }


  @Override
  public void configure(StateMachineTransitionConfigurer<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> transitions)
    throws Exception {
    transitions
      .withExternal()
      .source(MARKET_ORDER_STATES.READY).target(MARKET_ORDER_STATES.ORDER_CREATING).event(MARKET_ORDER_EVENTS.ORDER_RECEIVED)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.ORDER_CREATING).target(MARKET_ORDER_STATES.ORDER_CREATED).event(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL)
      .and()
      .withFork()
      .source(MARKET_ORDER_STATES.ORDER_CREATED)
      .target(MARKET_ORDER_STATES.CREATING_CASH_ENTRY)
      .target(MARKET_ORDER_STATES.CREATING_SECURITY_ENTRY)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.CREATING_CASH_ENTRY).target(MARKET_ORDER_STATES.CASH_ENTRY_CREATED).event(MARKET_ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.CASH_ENTRY_CREATED).target(MARKET_ORDER_STATES.CREATING_CASH_POSITIONS)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.CREATING_CASH_POSITIONS).target(MARKET_ORDER_STATES.CASH_POSITIONS_CREATED).event(MARKET_ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.CREATING_SECURITY_ENTRY).target(MARKET_ORDER_STATES.SECURITY_ENTRY_CREATED).event(MARKET_ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.SECURITY_ENTRY_CREATED).target(MARKET_ORDER_STATES.CREATING_SECURITY_POSITIONS)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.CREATING_SECURITY_POSITIONS).target(MARKET_ORDER_STATES.SECURITY_POSITIONS_CREATED).event(MARKET_ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL)
      .and()
      .withJoin()
      .source(MARKET_ORDER_STATES.CASH_POSITIONS_CREATED)
      .source(MARKET_ORDER_STATES.SECURITY_POSITIONS_CREATED)
      .target(MARKET_ORDER_STATES.JOIN)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.JOIN).target(MARKET_ORDER_STATES.LAST)
      .and()
      .withExternal()
      .source(MARKET_ORDER_STATES.LAST).target(MARKET_ORDER_STATES.READY).event(MARKET_ORDER_EVENTS.EVENT3);

  }

  @OnTransition(target = "READY")
  public void toReady() {
    logger.severe("READY");
  }

  @OnTransition(target = "ORDER_CREATING")
  public void toOrderCreating() {
    logger.severe("ORDER_CREATING");
  }

  @OnTransition(target = "ORDER_CREATED")
  public void toOrderCreated() {
    logger.severe("ORDER_CREATED");
  }

  /***********************CASH***************************************/

  @OnTransition(target = "CREATING_CASH_ENTRY")
  public void toCreatingCashEntry() {
    logger.severe("CREATING_CASH_ENTRY");
  }

  @OnTransition(target = "CASH_ENTRY_CREATED")
  public void toCashEntryrCreated() {
    logger.severe("CASH_ENTRY_CREATED");
  }

  @OnTransition(target = "CREATING_CASH_POSITIONS")
  public void toCreatingCashPosition() {
    logger.severe("CREATING_CASH_POSITIONS");
  }

  @OnTransition(target = "CASH_POSITIONS_CREATED")
  public void toCashPositionCreated() {
    logger.severe("CASH_POSITIONS_CREATED");
  }

  /***********************SECURITY***************************************/

  @OnTransition(target = "CREATING_SECURITY_ENTRY")
  public void toCreatingSecurityEntry() {
    logger.severe("CREATING_SECURITY_ENTRY");
  }

  @OnTransition(target = "SECURITY_ENTRY_CREATED")
  public void toSecurityEntryrCreated() {
    logger.severe("SECURITY_ENTRY_CREATED");
  }

  @OnTransition(target = "CREATING_SECURITY_POSITIONS")
  public void toCreatingSecurityPosition() {
    logger.severe("CREATING_SECURITY_POSITIONS");
  }

  @OnTransition(target = "SECURITY_POSITIONS_CREATED")
  public void toSecurityPositionCreated() {
    logger.severe("SECURITY_POSITIONS_CREATED");
  }

  /***********************SEND OF PROCESS***************************************/

  @OnTransition(target = "JOIN")
  public void toJoin() {
    logger.severe("JOIN");
  }

  @OnTransition(target = "LAST")
  public void toLast() {
    logger.severe("LAST");
  }

}

