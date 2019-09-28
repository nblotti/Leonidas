package ch.nblotti.leonidas.process.order;

import ch.nblotti.leonidas.order.ORDER_TYPE;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.logging.Logger;

@Configuration
@EnableStateMachine
@WithStateMachine
public class MarketProcessConfig extends EnumStateMachineConfigurerAdapter<ORDER_STATES, ORDER_EVENTS> {

  private static final Logger logger = Logger.getLogger("MarketProcessConfig");

  @Override
  public void configure(StateMachineStateConfigurer<ORDER_STATES, ORDER_EVENTS> states)
    throws Exception {
    states
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


  }


  @Override
  public void configure(StateMachineTransitionConfigurer<ORDER_STATES, ORDER_EVENTS> transitions)
    throws Exception {
    transitions
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
      .source(ORDER_STATES.MO_JOIN).target(ORDER_STATES.LAST)
      .and()
      .withExternal()
      .source(ORDER_STATES.LAST).target(ORDER_STATES.READY).event(ORDER_EVENTS.EVENT3);

  }

  @Bean
  public Guard<ORDER_STATES, ORDER_EVENTS> isSecurityEntry() {

    return new Guard<ORDER_STATES, ORDER_EVENTS>() {

      @Override
      public boolean evaluate(StateContext<ORDER_STATES, ORDER_EVENTS> context) {
        ORDER_TYPE order_type = (ORDER_TYPE) context.getMessageHeader("type");
        return ORDER_TYPE.SECURITY_ENTRY.equals(order_type);

      }
    };
  }


  private Guard<ORDER_STATES, ORDER_EVENTS> isCashEntry() {
    return new Guard<ORDER_STATES, ORDER_EVENTS>() {

      @Override
      public boolean evaluate(StateContext<ORDER_STATES, ORDER_EVENTS> context) {
        ORDER_TYPE order_type = (ORDER_TYPE) context.getMessageHeader("type");

        return ORDER_TYPE.CASH_ENTRY.equals(order_type);
      }
    };
  }

  private Guard<ORDER_STATES, ORDER_EVENTS> isMarketOrder() {
    return new Guard<ORDER_STATES, ORDER_EVENTS>() {

      @Override
      public boolean evaluate(StateContext<ORDER_STATES, ORDER_EVENTS> context) {
        ORDER_TYPE order_type = (ORDER_TYPE) context.getMessageHeader("type");

        return ORDER_TYPE.MARKET_ORDER.equals(order_type);
      }
    };
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

  @OnTransition(target = "CE_CREATING_CASH_ENTRY")
  public void toCreatingCashEntry() {
    logger.severe("CE_CREATING_CASH_ENTRY");
  }

  @OnTransition(target = "CE_CASH_ENTRY_CREATED")
  public void toCashEntryrCreated() {
    logger.severe("CE_CASH_ENTRY_CREATED");
  }

  @OnTransition(target = "CE_CREATING_CASH_POSITIONS")
  public void toCreatingCashPosition() {
    logger.severe("CE_CREATING_CASH_POSITIONS");
  }

  @OnTransition(target = "CE_CASH_POSITIONS_CREATED")
  public void toCashPositionCreated() {
    logger.severe("CE_CASH_POSITIONS_CREATED");
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

