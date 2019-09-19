package ch.nblotti.leonidas.accountrelation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity(name = "ACCOUNT_RELATION")
public class AccountRelationPO {

  @Id
  @GeneratedValue
  private Integer id;


  @NotNull(message = "First account is mandatory")
  @Column(name = "FIRST_ACCOUNT_ID")
  private int firstAccountId;

  @NotNull(message = "Second account is mandatory")
  @Column(name = "SECOND_ACCOUNT_ID")
  private int secondAccountId;

  @Column(name = "CREATION_DATE")
  private LocalDate creationDate;


  @Column(name = "RELATION_TYPE")
  private RELATION_TYPE relationType;


  @Column(name = "RELATION_STATUS")
  private RELATION_STATUS relationStatus;

  public AccountRelationPO() {

  }

  public AccountRelationPO(int firstAccountId, int secondAccountId, LocalDate creationDate) {
    this.firstAccountId = firstAccountId;
    this.secondAccountId = secondAccountId;
    this.creationDate = creationDate;
    this.relationStatus = RELATION_STATUS.OPEN;
    this.relationType = RELATION_TYPE.BENCHMARK;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public int getFirstAccountId() {
    return firstAccountId;
  }

  public void setFirstAccountId(int firstAccountId) {
    this.firstAccountId = firstAccountId;
  }

  public int getSecondAccountId() {
    return secondAccountId;
  }

  public void setSecondAccountId(int secondAccountId) {
    this.secondAccountId = secondAccountId;
  }

  public LocalDate getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDate creationDate) {
    this.creationDate = creationDate;
  }

  public RELATION_TYPE getRelationType() {
    return relationType;
  }

  public void setRelationType(RELATION_TYPE relationType) {
    this.relationType = relationType;
  }

  public RELATION_STATUS getRelationStatus() {
    return relationStatus;
  }

  public void setRelationStatus(RELATION_STATUS relationStatus) {
    this.relationStatus = relationStatus;
  }

  public enum RELATION_TYPE {
    BENCHMARK, PORTFOLIO_MODEL;
  }

  public enum RELATION_STATUS {
    OPEN, CLOSED;
  }


}
