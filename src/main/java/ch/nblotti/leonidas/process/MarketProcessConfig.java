package ch.nblotti.leonidas.process;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
@WithStateMachine
public class MarketProcessConfig extends EnumStateMachineConfigurerAdapter<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> {


  @Override
  public void configure(StateMachineStateConfigurer<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> states)
    throws Exception {
    states
      .withStates()
      .initial(MARKET_ORDER_STATES.READY)
      .fork(MARKET_ORDER_STATES.ORDER_CREATED)
      .join(MARKET_ORDER_STATES.JOIN)
      .state(MARKET_ORDER_STATES.LAST)
      .end(MARKET_ORDER_STATES.FINAL)
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
      .source(MARKET_ORDER_STATES.READY).target(MARKET_ORDER_STATES.ORDER_CREATED).event(MARKET_ORDER_EVENTS.ORDER_RECEIVED)
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
      .source(MARKET_ORDER_STATES.LAST).target(MARKET_ORDER_STATES.FINAL).event(MARKET_ORDER_EVENTS.EVENT3);

  }
}

