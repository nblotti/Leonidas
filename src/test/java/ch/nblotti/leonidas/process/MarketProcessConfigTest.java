package ch.nblotti.leonidas.process;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.logging.Logger;

@RunWith(SpringRunner.class)

@TestPropertySource(locations = "classpath:applicationtest.properties")
public class MarketProcessConfigTest {

  Logger logger = Logger.getLogger("MarketProcessConfigTest");

  @TestConfiguration
  static class MarketProcessConfigTestContextConfiguration {


    @Bean
    public StateMachine<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> stateMachine() throws Exception {
      StateMachineBuilder.Builder<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> builder = StateMachineBuilder.builder();

      MarketProcessConfig marketProcessConfig = new MarketProcessConfig();
      marketProcessConfig.configure(builder.configureStates());
      marketProcessConfig.configure(builder.configureTransitions());


      return builder.build();

    }
  }

  @Autowired
  StateMachine<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> stateMachine;


  @Test
  public void testInitMachine() {

    StateMachineListener stateMachineListenerAdapter = new StateMachineListenerAdapter() {
      @Override
      public void transitionEnded(Transition var1) {
        logger.info(String.format("%s %s", var1.getSource() == null ? "init" : var1.getSource().getId(), var1.getTarget().getId()));
      }


    };
    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);

    stateMachine.addStateListener(stateMachineListenerAdapter);
    stateMachine.start();
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.EVENT3);
  }


}
