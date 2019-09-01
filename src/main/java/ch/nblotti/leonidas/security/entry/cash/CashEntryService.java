package ch.nblotti.leonidas.security.entry.cash;

import ch.nblotti.leonidas.security.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.logging.Logger;

@Component
public class CashEntryService {

  private static Logger LOGGER = Logger.getLogger("CashEntryService");

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

    jmsOrderTemplate.convertAndSend("cashentrybox", new Message(String.valueOf(cashEntry.getId()), Message.MESSAGE_TYPE.CASH_ENTRY, Message.ENTITY_ACTION.CREATE));


    return createdCashEntry;

  }


  public Iterable<CashEntry> findAllByAccountAndCurrencyOrderByValueDateAsc(int account, String currency) {

    return this.repository.findAllByAccountAndCurrencyOrderByValueDateAsc(account, currency);
  }

  public Optional<CashEntry> findById(String toString) {
    return this.repository.findById(Long.valueOf(toString));
  }


}



