package ch.nblotti.leonidas.order;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface OrderRepository extends CrudRepository<OrderPO, Long> {


  public Iterable<OrderPO> findByAccountId(String accountId);


  Iterable<OrderPO> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime);
}
