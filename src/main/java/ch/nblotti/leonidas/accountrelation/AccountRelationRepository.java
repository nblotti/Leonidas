package ch.nblotti.leonidas.accountrelation;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRelationRepository extends CrudRepository<AccountRelationPO, Long> {


  public Iterable<AccountRelationPO> findAllByFirstAccountIdAndSecondAccountIdAndRelationTypeAndRelationStatus(int firstAccountId, int secondAccountId, AccountRelationPO.RELATION_TYPE relationType, AccountRelationPO.RELATION_STATUS relationStatus);
}
