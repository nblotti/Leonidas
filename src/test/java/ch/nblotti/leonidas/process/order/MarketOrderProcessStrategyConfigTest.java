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
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:applicationtest.properties")
public class MarketOrderProcessStrategyConfigTest {

  private static final Logger localLogger = Logger.getLogger("MarketProcessConfigTest");

  @MockBean
  Logger logger;

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

  @MockBean
  OrderService orderService;

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
          localLogger.severe(String.format("%s %s", from == null ? "" : from.getId(), to.getId()));
        }

      };
    }

  }


  @Autowired
  StateMachineListener stateMachineListenerAdapter;

  @Autowired
  MarketProcessStrategy marketProcessStrategy;


  @Test(expected = IllegalStateException.class)
  public void orderListenerOrderNotFound() {

    MessageVO messageVO = mock(MessageVO.class);
    Optional<OrderPO> optional = mock(Optional.class);
    when(messageVO.getOrderID()).thenReturn(0l);
    when(orderService.findById(String.valueOf(messageVO.getOrderID()))).thenReturn(optional);
    when(optional.isPresent()).thenReturn(false);

    marketProcessStrategy.orderListener(messageVO);

  }

  @Test
  public void orderListenerMarketOrder() {

    MessageVO messageVO = mock(MessageVO.class);
    Optional<OrderPO> optional = mock(Optional.class);
    when(messageVO.getOrderID()).thenReturn(0l);
    OrderPO orderPO = mock(OrderPO.class);
    CashEntryPO marketCashEntryTO = mock(CashEntryPO.class);
    SecurityEntryPO marketSecurityEntryTO = mock(SecurityEntryPO.class);

    when(cashEntryService.fromMarketOrder(orderPO)).thenReturn(marketCashEntryTO);
    when(securityEntryService.fromSecurityEntryOrder(orderPO)).thenReturn(marketSecurityEntryTO);


    when(orderService.findById(String.valueOf(messageVO.getOrderID()))).thenReturn(optional);
    when(optional.isPresent()).thenReturn(true);
    when(optional.get()).thenReturn(orderPO);
    when(orderPO.getType()).thenReturn(ORDER_TYPE.MARKET_ORDER);

    marketProcessStrategy.orderListener(messageVO);
    verify(cashEntryService, times(1)).save(marketCashEntryTO);

    verify(securityEntryService, times(1)).save(marketSecurityEntryTO);


  }

  @Test
  public void orderListenerOrderNotValid() {


    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(false);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(message);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.INVALID_EVENT, stateCaptor1.getAllValues().get(0).getId());

  }


  @Test
  public void testInvalidOrder() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(false);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(message);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.INVALID_EVENT, stateCaptor1.getAllValues().get(0).getId());


  }


  @Test
  public void cashOrderListenerCashOrder() {

    MessageVO messageVO = mock(MessageVO.class);
    Optional<OrderPO> optional = mock(Optional.class);
    when(messageVO.getOrderID()).thenReturn(0l);
    OrderPO orderPO = mock(OrderPO.class);
    CashEntryPO marketCashEntryTO = mock(CashEntryPO.class);
    SecurityEntryPO marketSecurityEntryTO = mock(SecurityEntryPO.class);

    when(cashEntryService.fromCashEntryOrder(orderPO)).thenReturn(marketCashEntryTO);
    when(securityEntryService.fromSecurityEntryOrder(orderPO)).thenReturn(marketSecurityEntryTO);


    when(orderService.findById(String.valueOf(messageVO.getOrderID()))).thenReturn(optional);
    when(optional.isPresent()).thenReturn(true);
    when(optional.get()).thenReturn(orderPO);
    when(orderPO.getType()).thenReturn(ORDER_TYPE.CASH_ENTRY);

    marketProcessStrategy.orderListener(messageVO);
    verify(cashEntryService, times(1)).save(marketCashEntryTO);

    verify(cashEntryService, times(0)).fromMarketOrder(orderPO);
    verify(securityEntryService, times(0)).save(marketSecurityEntryTO);
  }


  @Test
  public void orderListenerSecurityOrder() {

    MarketProcessStrategy marketProcessStrategySpy = spy(marketProcessStrategy);
    MessageVO messageVO = mock(MessageVO.class);
    Optional<OrderPO> optional = mock(Optional.class);
    when(messageVO.getOrderID()).thenReturn(0l);
    OrderPO orderPO = mock(OrderPO.class);
    CashEntryPO marketCashEntryTO = mock(CashEntryPO.class);
    SecurityEntryPO marketSecurityEntryTO = mock(SecurityEntryPO.class);

    when(cashEntryService.fromCashEntryOrder(orderPO)).thenReturn(marketCashEntryTO);
    when(securityEntryService.fromSecurityEntryOrder(orderPO)).thenReturn(marketSecurityEntryTO);


    when(orderService.findById(String.valueOf(messageVO.getOrderID()))).thenReturn(optional);
    when(optional.isPresent()).thenReturn(true);
    when(optional.get()).thenReturn(orderPO);
    when(orderPO.getType()).thenReturn(ORDER_TYPE.SECURITY_ENTRY);

    doReturn(logger).when(marketProcessStrategySpy).getLogger();
    doNothing().when(marketProcessStrategySpy).log(any());
    when(logger.isLoggable(Level.FINE)).thenReturn(true);
    marketProcessStrategySpy.orderListener(messageVO);
    verify(cashEntryService, times(0)).save(marketCashEntryTO);

    verify(cashEntryService, times(0)).fromMarketOrder(orderPO);
    verify(securityEntryService, times(1)).save(marketSecurityEntryTO);
  }

  @Test
  public void orderListenerCashEntry() {

    StateMachine stateMachine = mock(StateMachine.class);
    MessageVO messageVO = mock(MessageVO.class);
    MarketProcessStrategy marketProcessStrategySpy = spy(marketProcessStrategy);
    doReturn(logger).when(marketProcessStrategySpy).getLogger();
    doReturn(stateMachine).when(marketProcessStrategySpy).getStateMachine();
    when(logger.isLoggable(Level.FINE)).thenReturn(true);
    when(messageVO.getOrderID()).thenReturn(1l);
    when(messageVO.getAccountID()).thenReturn(1);

    SecurityEntryPO securityEntryPO = mock(SecurityEntryPO.class);
    when(securityEntryService.findByAccountAndOrderID(anyInt(), anyLong())).thenReturn(securityEntryPO);

    marketProcessStrategySpy.receiveNewSecurityEntry(messageVO);
    verify(securityPositionService, times(1)).updatePosition(securityEntryPO);
    verify(marketProcessService, times(1)).setSecurityPositionRunningForProcess(messageVO.getOrderID(), messageVO.getAccountID());
    verify(stateMachine, times(1)).sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);


  }

  @Test
  public void testMarketOrderOrderReceivedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    marketProcessStrategy.start();


    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(true);

    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.sendEvent(message);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_ORDER_CREATING, stateCaptor1.getValue().getId());
  }

  @Test
  public void testMarketOrderEntryCreatingMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(true);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.sendEvent(message);
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(3)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_ORDER_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.MO_CREATING_CASH_ENTRY, stateCaptor1.getAllValues().get(1).getId());
    Assert.assertEquals(ORDER_STATES.MO_CREATING_SECURITY_ENTRY, stateCaptor1.getAllValues().get(2).getId());
  }


  @Test
  public void testMarketOrderCashEntryCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(true);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.sendEvent(message);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_CASH_ENTRY_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.MO_CREATING_CASH_POSITIONS, stateCaptor1.getAllValues().get(1).getId());
  }

  @Test
  public void testMarketOrderCashPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(true);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.sendEvent(message);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_CASH_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
  }

  @Test
  public void testMarketOrderSecurityEntryCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);

    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(true);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.sendEvent(message);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(2)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_SECURITY_ENTRY_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.MO_CREATING_SECURITY_POSITIONS, stateCaptor1.getAllValues().get(1).getId());
  }

  @Test
  public void testMarketOrderSecurityPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(true);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.sendEvent(message);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(1)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_SECURITY_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
  }

  @Test
  public void testMarketOrderAllPositionCreatedMachine() {

    ArgumentCaptor<State> stateCaptor1 = ArgumentCaptor.forClass(State.class);
    ArgumentCaptor<State> stateCaptor2 = ArgumentCaptor.forClass(State.class);
    StateMachineListener mockedStateMachineListenerAdapter = mock(StateMachineListener.class);
    OrderPO orderPO = mock(OrderPO.class);
    when(orderService.isOrderValid(orderPO)).thenReturn(true);

    marketProcessStrategy.start();

    Message<ORDER_EVENTS> message = MessageBuilder
      .withPayload(ORDER_EVENTS.EVENT_RECEIVED)
      .setHeader("type", ORDER_TYPE.MARKET_ORDER)
      .setHeader("order", orderPO)
      .build();
    marketProcessStrategy.sendEvent(message);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.ORDER_CREATION_SUCCESSFULL);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.SECURITY_ENTRY_CREATION_SUCCESSFULL);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);
    marketProcessStrategy.addStateListener(mockedStateMachineListenerAdapter);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);
    marketProcessStrategy.sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
    verify(mockedStateMachineListenerAdapter, times(3)).stateEntered(stateCaptor1.capture());
    Assert.assertEquals(ORDER_STATES.MO_SECURITY_POSITIONS_CREATED, stateCaptor1.getAllValues().get(0).getId());
    Assert.assertEquals(ORDER_STATES.MO_CASH_POSITIONS_CREATED, stateCaptor1.getAllValues().get(1).getId());
    Assert.assertEquals(ORDER_STATES.READY, stateCaptor1.getAllValues().get(2).getId());
  }


  @Test
  public void testReceiveNewCashEntry() {


    MarketProcessStrategy marketProcessStrategySpy = spy(marketProcessStrategy);
    MessageVO messageVO = mock(MessageVO.class);
    when(messageVO.getOrderID()).thenReturn(0l);
    when(messageVO.getAccountID()).thenReturn(1);
    CashEntryPO cashEntryTO = mock(CashEntryPO.class);

    StateMachine stateMachine = mock(StateMachine.class);


    when(cashEntryService.findByAccountAndOrderID(messageVO.getAccountID(), messageVO.getOrderID())).thenReturn(cashEntryTO);


    doReturn(logger).when(marketProcessStrategySpy).getLogger();
    doNothing().when(marketProcessStrategySpy).log(any());
    doReturn(stateMachine).when(marketProcessStrategySpy).getStateMachine();


    marketProcessStrategySpy.receiveNewCashEntry(messageVO);

    verify(cashPositionService, times(1)).updatePositions(cashEntryTO);

    verify(marketProcessService, times(1)).setCashPositionRunningForProcess(messageVO.getOrderID(), messageVO.getAccountID());

    verify(stateMachine, times(1)).sendEvent(ORDER_EVENTS.CASH_ENTRY_CREATION_SUCCESSFULL);

  }

  @Test
  public void testReceiveNewCashPosition() {
    MarketProcessStrategy marketProcessStrategySpy = spy(marketProcessStrategy);
    MessageVO messageVO = mock(MessageVO.class);
    StateMachine stateMachine = mock(StateMachine.class);


    when(messageVO.getOrderID()).thenReturn(0l);
    when(messageVO.getAccountID()).thenReturn(1);
    doReturn(stateMachine).when(marketProcessStrategySpy).getStateMachine();

    marketProcessStrategySpy.receiveNewCashPosition(messageVO);

    verify(marketProcessService, times(1)).setCashFinishedForProcess(messageVO.getOrderID(), messageVO.getAccountID());
    verify(stateMachine, times(1)).sendEvent(ORDER_EVENTS.CASH_POSITION_CREATION_SUCCESSFULL);
  }

  @Test
  public void testReceiveNewSecurityPosition() {

    MarketProcessStrategy marketProcessStrategySpy = spy(marketProcessStrategy);
    MessageVO messageVO = mock(MessageVO.class);
    StateMachine stateMachine = mock(StateMachine.class);


    when(messageVO.getOrderID()).thenReturn(0l);
    when(messageVO.getAccountID()).thenReturn(1);
    doReturn(stateMachine).when(marketProcessStrategySpy).getStateMachine();

    marketProcessStrategySpy.receiveNewSecurityPosition(messageVO);

    verify(marketProcessService, times(1)).setSecurityFinishedForProcess(messageVO.getOrderID(), messageVO.getAccountID());
    verify(stateMachine, times(1)).sendEvent(ORDER_EVENTS.SECURITY_POSITION_CREATION_SUCCESSFULL);


  }


}
