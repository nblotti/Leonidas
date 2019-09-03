package ch.nblotti.leonidas.process;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface MarketProcessRepository extends CrudRepository<MarketProcessPO, Long> {


  MarketProcessPO readByOrderIDAndAccountID(long orderID, Integer accountID);

  @Query(value = "SELECT count(*) FROM RUNNING_ORDER_PROCESS WHERE ACCOUNT_ID = :accountID" ,  nativeQuery = true)
  public int findRunningProcessForAccount(@Param("accountID") int accountID);
}
