package ch.nblotti.leonidas.order;

import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;

public interface OrderRepository extends CrudRepository<Order, Long> {


  public Iterable<Order> findByAccountId(String accountId);


  Iterable<Order> findByAccountIdAndTransactTimeAfter(Integer accountId, LocalDate transactTime);
}
