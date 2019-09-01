package ch.nblotti.leonidas.security.entry.security;

import org.springframework.data.repository.CrudRepository;

public interface SecurityEntryRepository extends CrudRepository<SecurityEntry, Long> {

  
  Iterable<SecurityEntry> findAllByAccountAndSecurityIDOrderByValueDateAsc(int account,String securityID);
}
