package ch.nblotti.leonidas.process;

import ch.nblotti.leonidas.order.ORDER_TYPE;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity(name = "MARKET_PROCESS")
public class MarketProcessPO {

  @Id
  @GeneratedValue
  private Integer id;

  @NotNull(message = "Account ID is mandatory")
  @Column(name = "ACCOUNT_ID")
  private int accountID;

  @Column(name = "CASH_ENTRY")
  private LocalDate cashEntry;

  @Column(name = "SECURITY_ENTRY")
  private LocalDate securityEntry;

  @Column(name = "CASH_POSITION")
  private LocalDate cashPosition;

  @Column(name = "SECURITY_POSITION")
  private LocalDate securityPosition;

  @Column(name = "CASH_PERFORMANCE")
  private LocalDate cashPerformance;

  @Column(name = "SECURITY_PERFORMANCE")
  private LocalDate securityPerformance;

  @Column(name = "ORDER_TYPE")
  private ORDER_TYPE type;

  @Column(name = "ORDER_ID")
  private Long orderID;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public int getAccountID() {
    return accountID;
  }

  public void setAccountID(int accountID) {
    this.accountID = accountID;
  }


  public LocalDate getCashEntry() {
    return cashEntry;
  }

  public void setCashEntry(LocalDate cashEntry) {
    this.cashEntry = cashEntry;
  }

  public LocalDate getSecurityEntry() {
    return securityEntry;
  }

  public void setSecurityEntry(LocalDate securityEntry) {
    this.securityEntry = securityEntry;
  }

  public LocalDate getCashPosition() {
    return cashPosition;
  }

  public void setCashPosition(LocalDate cashPosition) {
    this.cashPosition = cashPosition;
  }

  public LocalDate getSecurityPosition() {
    return securityPosition;
  }

  public void setSecurityPosition(LocalDate securityPosition) {
    this.securityPosition = securityPosition;
  }

  public LocalDate getSecurityPerformance() {
    return securityPerformance;
  }

  public void setSecurityPerformance(LocalDate securityPerformance) {
    this.securityPerformance = securityPerformance;
  }

  public LocalDate getCashPerformance() {
    return cashPerformance;
  }

  public void setCashPerformance(LocalDate cashPerformance) {
    this.cashPerformance = cashPerformance;
  }

  public void setOrderType(ORDER_TYPE type) {
    this.type = type;
  }

  public ORDER_TYPE getOrderType() {
    return this.type;
  }

  public void setOrderID(Long orderID) {
    this.orderID = orderID;
  }

  public long getOrderID() {
    return this.orderID;
  }
}
