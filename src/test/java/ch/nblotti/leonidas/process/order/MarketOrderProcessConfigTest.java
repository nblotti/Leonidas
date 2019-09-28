package ch.nblotti.leonidas.process.order;

import ch.nblotti.leonidas.order.ORDER_TYPE;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
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
public class MarketOrderProcessConfigTest {

  private static final Logger logger = Logger.getLogger("MarketProcessConfigTest");

  @TestConfiguration
  static class MarketProcessConfigTestContextConfiguration {


    @Bean
    public MarketProcessor stateMachine() throws Exception {

      return new MarketProcessor();

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
  MarketProcessor marketProcessor;



  @Test
  public void testMarketOrderOrderReceivedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessor.start();

    marketProcessor.addStateListener(mockedStateMachineListenerAdapter);

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .build();
    marketProcessor.sendEvent(message);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.ORDER_CREATING, stateCaptor1.getValue().getId());
  }

  @Test
  public void testMarketOrderEntryCreatingMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessor.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .build();
    marketProcessor.sendEvent(message);
    marketProcessor.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessor.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(3)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.ORDER_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.MO_CREATING_CASH_ENTRY, stateCaptor1.getAllValues().get(1).getId());
    Assert.assertEquals(ORDER_STATES.MO_CREATING_SECURITY_ENTRY, stateCaptor1.getAllValues().get(2).getId());
  }


  @Test
  public void testMarketOrderCashEntryCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessor.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .build();
    marketProcessor.sendEvent(message);
    marketProcessor.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessor.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessor.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_CASH_ENTRY_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.MO_CREATING_CASH_POSITIONS, stateCaptor1.getAllValues().get(1).getId());
  }

  @Test
  public void testMarketOrderCashPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessor.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .build();
    marketProcessor.sendEvent(message);
    marketProcessor.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessor.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    marketProcessor.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessor.sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_CASH_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
  }

  @Test
  public void testMarketOrderSecurityEntryCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessor.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .build();
    marketProcessor.sendEvent(message);
    marketProcessor.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessor.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessor.sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_SECURITY_ENTRY_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.MO_CREATING_SECURITY_POSITIONS, stateCaptor1.getAllValues().get(1).getId());
  }

  @Test
  public void testMarketOrderSecurityPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessor.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .build();
    marketProcessor.sendEvent(message);
    marketProcessor.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessor.sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    marketProcessor.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessor.sendEvent(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_SECURITY_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
  }

  @Test
  public void testMarketOrderAllPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessor.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .build();
    marketProcessor.sendEvent(message);
    marketProcessor.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessor.sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    marketProcessor.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    marketProcessor.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessor.sendEvent(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);
    marketProcessor.sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(3)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_SECURITY_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.MO_CASH_POSITIONS_CREATED, stateCaptor1.getAllValues().get(1).getId());
    Assert.assertEquals(ORDER_STATES.READY, stateCaptor1.getAllValues().get(2).getId());
  }



}