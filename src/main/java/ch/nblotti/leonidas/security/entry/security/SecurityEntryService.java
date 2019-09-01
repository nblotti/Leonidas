package ch.nblotti.leonidas.security.entry.security;

import ch.nblotti.leonidas.security.technical.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.logging.Logger;

@Component
public class SecurityEntryService {

  private static Logger LOGGER = Logger.getLogger("SecurityEntryService");

  @Autowired
  private SecurityEntryRepository repository;

  @Autowired
  private JmsTemplate jmsOrderTemplate;


  public Iterable<SecurityEntry> findAll() {

    return this.repository.findAll();

  }

  //TODO NBL : test me
  public SecurityEntry save(SecurityEntry cashEntry) {

    SecurityEntry createdSecurityEntry = this.repository.save(cashEntry);

    jmsOrderTemplate.convertAndSend("securityentrybox", new Message(String.valueOf(cashEntry.getId()), Message.MESSAGE_TYPE.CASH_ENTRY, Message.ENTITY_ACTION.CREATE));


    return createdSecurityEntry;

  }


  public Optional<SecurityEntry> findById(String toString) {
    return this.repository.findById(Long.valueOf(toString));
  }

  public Iterable<SecurityEntry> findAllByAccountAndSecurityIDOrderByValueDateAsc(int account, String securityID) {

    return this.repository.findAllByAccountAndSecurityIDOrderByValueDateAsc(account, securityID);
  }
}



