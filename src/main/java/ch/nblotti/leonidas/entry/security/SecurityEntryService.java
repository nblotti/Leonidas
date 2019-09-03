package ch.nblotti.leonidas.entry.security;

import ch.nblotti.leonidas.technical.MessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityEntryService {


  @Autowired
  private SecurityEntryRepository repository;

  @Autowired
  private JmsTemplate jmsOrderTemplate;


  public Iterable<SecurityEntryPO> findAll() {

    return this.repository.findAll();

  }

  //TODO NBL : test me
  public SecurityEntryPO save(SecurityEntryPO cashEntry) {

    SecurityEntryPO createdSecurityEntry = this.repository.save(cashEntry);


    jmsOrderTemplate.convertAndSend("securityentrybox", new MessageVO(cashEntry.getOrderID(), cashEntry.getAccount(), MessageVO.MESSAGE_TYPE.CASH_ENTRY, MessageVO.ENTITY_ACTION.CREATE));


    return createdSecurityEntry;

  }


  public Optional<SecurityEntryPO> findById(String toString) {
    return this.repository.findById(Long.valueOf(toString));
  }

  public Iterable<SecurityEntryPO> findAllByAccountAndSecurityIDOrderByValueDateAsc(int account, String securityID) {

    return this.repository.findAllByAccountAndSecurityIDOrderByValueDateAsc(account, securityID);
  }

  public SecurityEntryPO findByAccountAndOrderID(int account, long orderID) {

    return this.repository.findByAccountAndOrderID(account, orderID);
  }

}



