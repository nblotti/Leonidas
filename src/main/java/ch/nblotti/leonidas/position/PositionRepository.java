package ch.nblotti.leonidas.position;

import org.springframework.data.repository.CrudRepository;

public interface PositionRepository extends CrudRepository<PositionPO, Long> {

  void deleteByPosTypeAndAccountIdAndCurrency(PositionPO.POS_TYPE cash, int account, String currency);
  void deleteByPosTypeAndAccountIdAndSecurityIDAndCurrency(PositionPO.POS_TYPE cash, int account, String securityID, String currency);
}
