package ch.nblotti.leonidas.entry.security;

import org.springframework.data.repository.CrudRepository;

public interface SecurityEntryRepository extends CrudRepository<SecurityEntryPO, Long> {


  Iterable<SecurityEntryPO> findAllByAccountAndSecurityIDOrderByValueDateAsc(int account, String securityID);

  SecurityEntryPO findByAccountAndOrderID(int account, long orderID);
}
