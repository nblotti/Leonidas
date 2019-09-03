package ch.nblotti.leonidas.entry.cash;

import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CashEntryService {


  @Autowired
  private CashEntryRepository repository;

  @Autowired
  private JmsTemplate jmsOrderTemplate;


  public Iterable<CashEntryPO> findAll() {

    return this.repository.findAll();

  }

  //TODO NBL : test me
  public CashEntryPO save(CashEntryPO cashEntryTO) {

    CashEntryPO createdCashEntryTO = this.repository.save(cashEntryTO);

    jmsOrderTemplate.convertAndSend("cashentrybox", new MessageVO(cashEntryTO.getOrderID(), cashEntryTO.getAccount(), MessageVO.MESSAGE_TYPE.CASH_ENTRY, MessageVO.ENTITY_ACTION.CREATE));


    return createdCashEntryTO;

  }


  public Iterable<CashEntryPO> findAllByAccountAndCurrencyOrderByValueDateAsc(int account, String currency) {

    return this.repository.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency);
  }


  public CashEntryPO findByAccountAndOrderID(int account, long orderID) {

    return this.repository.findByAccountAndOrderID(account, orderID);
  }


  public Optional<CashEntryPO> findById(String toString) {
    return this.repository.findById(Long.valueOf(toString));
  }


}



