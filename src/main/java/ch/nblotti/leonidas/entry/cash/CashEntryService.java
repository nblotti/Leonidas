package ch.nblotti.leonidas.entry.cash;

import ch.nblotti.leonidas.technical.Message;
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


  public Iterable<CashEntry> findAll() {

    return this.repository.findAll();

  }

  //TODO NBL : test me
  public CashEntry save(CashEntry cashEntry) {

    CashEntry createdCashEntry = this.repository.save(cashEntry);

    jmsOrderTemplate.convertAndSend("cashentrybox", new Message(cashEntry.getOrderID(), cashEntry.getAccount(), Message.MESSAGE_TYPE.CASH_ENTRY, Message.ENTITY_ACTION.CREATE));


    return createdCashEntry;

  }


  public Iterable<CashEntry> findAllByAccountAndCurrencyOrderByValueDateAsc(int account, String currency) {

    return this.repository.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency);
  }


  public CashEntry findByAccountAndOrderID(int account, long orderID) {

    return this.repository.findByAccountAndOrderID(account, orderID);
  }


  public Optional<CashEntry> findById(String toString) {
    return this.repository.findById(Long.valueOf(toString));
  }


}



