package ch.nblotti.leonidas.process.order;

import ch.nblotti.leonidas.entry.cash.CashEntryService;
import ch.nblotti.leonidas.entry.security.SecurityEntryService;
import ch.nblotti.leonidas.order.ORDER_TYPE;
import ch.nblotti.leonidas.order.OrderService;
import ch.nblotti.leonidas.position.cash.CashPositionService;
import ch.nblotti.leonidas.position.security.SecurityPositionService;
import ch.nblotti.leonidas.process.MarketProcessService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
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
public class CashEntryProcessStrategyConfigTest {

  private static final Logger logger = Logger.getLogger("MarketProcessConfigTest");


  @MockBean
  OrderService orderService;

  @MockBean
  SecurityEntryService securityEntryService;

  @MockBean
  SecurityPositionService securityPositionService;

  @MockBean
  CashEntryService cashEntryService;

  @MockBean
  MarketProcessService marketProcessService;

  @MockBean
  CashPositionService cashPositionService;

  @TestConfiguration
  static class MarketProcessConfigTestContextConfiguration {


    @Bean
    public MarketProcessStrategy stateMachine() throws Exception {

      return new MarketProcessStrategy();

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
  MarketProcessStrategy marketProcessStrategy;


  @Test
  public void testCashOrderOrderReceivedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessStrategy.start();

    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.CASH_ENTRY)
      .build();
    marketProcessStrategy.sendEvent(message);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.CE_CREATING_CASH_ENTRY, stateCaptor1.getValue().getId());
  }


  @Test
  public void testCashOrderEntryCreatingMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.CASH_ENTRY)
      .build();
    marketProcessStrategy.sendEvent(message);
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.CE_CASH_ENTRY_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.CE_CREATING_CASH_POSITIONS, stateCaptor1.getAllValues().get(1).getId());
  }


  @Test
  public void testCashOrderCashPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.CASH_ENTRY)
      .build();
    marketProcessStrategy.sendEvent(message);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.CE_CASH_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.READY, stateCaptor1.getAllValues().get(1).getId());
  }


}
