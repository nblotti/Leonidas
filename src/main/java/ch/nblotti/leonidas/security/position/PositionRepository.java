package ch.nblotti.leonidas.security.position;

import org.springframework.data.repository.CrudRepository;

public interface PositionRepository extends CrudRepository<Position, Long> {

  void deleteByPosTypeAndAccountIdAndCurrency(Position.POS_TYPE cash, int account,String currency);
  void deleteByPosTypeAndAccountIdAndSecurityIDAndCurrency(Position.POS_TYPE cash, int account, String securityID,String currency);
}
