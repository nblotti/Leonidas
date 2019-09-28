package ch.nblotti.leonidas.process.order;

import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.process.order.MarketProcessConfig;
import ch.nblotti.leonidas.process.order.ORDER_EVENTS;
import ch.nblotti.leonidas.process.order.ORDER_STATES;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class CashEntryProcessConfigTest {

  private static final Logger logger = Logger.getLogger("MarketProcessConfigTest");

  @TestConfiguration
  static class MarketProcessConfigTestContextConfiguration {


    @Bean
    public StateMachine<ORDER_STATES, ORDER_EVENTS> stateMachine() throws Exception {
      StateMachineBuilder.Builder<ORDER_STATES, ORDER_EVENTS> builder = StateMachineBuilder.builder();

      MarketProcessConfig marketProcessConfig = new MarketProcessConfig();
      marketProcessConfig.configure(builder.configureStates());
      marketProcessConfig.configure(builder.configureTransitions());


      return builder.build();

    }

    @Bean
    public StateMachineListener stateMachineListenerAdapter() {

      return new StateMachineListenerAdapter() {

        @Override
        public void stateChanged(State from, State to) {
          logger.severe(String.format("%s %s", from == null ? "" : from.getId(), to.getId()));
        }

      };
    }

  }


  @Autowired
  StateMachineListener stateMachineListenerAdapter;

  @Autowired
  StateMachine<ORDER_STATES, ORDER_EVENTS> stateMachine;


  @Test
  public void testStartMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.start();
    verify(mockedStateMachineListenerAdapter, times(1)).stateMachineStarted(any());

    stateMachine.sendEvent(ORDER_EVENTS.EVENT3);
  }

  @Test
  public void testCashOrderOrderReceivedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.addStateListener(mockedStateMachineListenerAdapter);

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.CASH_ENTRY)
      .build();
    stateMachine.sendEvent(message);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.CE_CREATING_CASH_ENTRY, stateCaptor1.getValue().getId());
  }


  @Test
  public void testCashOrderEntryCreatingMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.CASH_ENTRY)
      .build();
    stateMachine.sendEvent(message);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.CE_CASH_ENTRY_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.CE_CREATING_CASH_POSITIONS, stateCaptor1.getAllValues().get(1).getId());
  }


  @Test
  public void testCashOrderCashPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.CASH_ENTRY)
      .build();
    stateMachine.sendEvent(message);
    stateMachine.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.CE_CASH_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.READY, stateCaptor1.getAllValues().get(1).getId());
  }


}
