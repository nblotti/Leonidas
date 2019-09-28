package ch.nblotti.leonidas.process;

import org.junit.Assert;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class MarketProcessConfigTest {

  private static final Logger logger = Logger.getLogger("MarketProcessConfigTest");

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
  StateMachine<MARKET_ORDER_STATES, MARKET_ORDER_EVENTS> stateMachine;


  @Test
  public void testlogAll() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);

    stateMachine.addStateListener(stateMachineListenerAdapter);
    stateMachine.start();
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.EVENT3);
  }


  @Test
  public void testStartMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.start();
    verify(mockedStateMachineListenerAdapter, times(1)).stateMachineStarted(any());
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.EVENT3);
  }

  @Test
  public void testOrderReceivedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(MARKET_ORDER_STATES.ORDER_CREATING, stateCaptor1.getValue().getId());
  }

  @Test
  public void testEntryCreatingMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(3)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(MARKET_ORDER_STATES.ORDER_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(MARKET_ORDER_STATES.CREATING_CASH_ENTRY, stateCaptor1.getAllValues().get(1).getId());
    Assert.assertEquals(MARKET_ORDER_STATES.CREATING_SECURITY_ENTRY, stateCaptor1.getAllValues().get(2).getId());
  }

  @Test
  public void testCashEntryCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(MARKET_ORDER_STATES.CASH_ENTRY_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(MARKET_ORDER_STATES.CREATING_CASH_POSITIONS, stateCaptor1.getAllValues().get(1).getId());
  }

  @Test
  public void testCashPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(MARKET_ORDER_STATES.CASH_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
  }

  @Test
  public void testSecurityEntryCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(MARKET_ORDER_STATES.SECURITY_ENTRY_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(MARKET_ORDER_STATES.CREATING_SECURITY_POSITIONS, stateCaptor1.getAllValues().get(1).getId());
  }

  @Test
  public void testSecurityPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(MARKET_ORDER_STATES.SECURITY_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
  }

  @Test
  public void testAllPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(3)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(MARKET_ORDER_STATES.SECURITY_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(MARKET_ORDER_STATES.CASH_POSITIONS_CREATED, stateCaptor1.getAllValues().get(1).getId());
    Assert.assertEquals(MARKET_ORDER_STATES.LAST, stateCaptor1.getAllValues().get(2).getId());
  }

  @Test
  public void testReadyMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    stateMachine.start();

    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_RECEIVED);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    stateMachine.addStateListener(mockedStateMachineListenerAdapter);
    stateMachine.sendEvent(MARKET_ORDER_EVENTS.EVENT3);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(MARKET_ORDER_STATES.READY, stateCaptor1.getAllValues().get(0).getId());
  }


}
