package ch.nblotti.leonidas.order;


import ch.nblotti.leonidas.entry.DEBIT_CREDIT;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity(name = "ORDERS")
public class Order {

  @Id
  @GeneratedValue
  private Long id;

  @NotBlank(message = "cIOrdID is mandatory")
  @Column(name = "CI_ORD_ID")
  private String cIOrdID;


  @Column(name = "ORDER_TYPE")
  private ORDER_TYPE type;


  @NotNull(message = "accountId is mandatory")
  @Column(name = "ACCOUNT_ID")
  private int accountId;


  @Column(name = "SYMBOL")
  private String symbol;


  @NotNull(message = "side is mandatory")
  @Column(name = "SIDE")
  private DEBIT_CREDIT side;

  @NotNull(message = "Transaction time is mandatory")
  @Column(name = "TRANSACT_TIME")
  private LocalDate transactTime;

  @Column(name = "AMOUNT")
  private float amount;


  @Column(name = "ORDER_QTY_DATA")
  private float orderQtyData;


  @Column(name = "STATUS")
  private int status;

  @Column(name = "EXCHANGE")
  private String exchange;


  @Column(name = "CASH_CURRENCY")
  private String cashCurrency;

  public Order() {

  }

  public Order(String cIOrdID, int accountId, String symbol, DEBIT_CREDIT side, LocalDate transactTime, float orderQtyData, int status) {
    this.cIOrdID = cIOrdID;
    this.accountId = accountId;
    this.symbol = symbol;
    this.side = side;
    this.transactTime = transactTime;
    this.orderQtyData = orderQtyData;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getcIOrdID() {
    return cIOrdID;
  }

  public void setcIOrdID(String cIOrdID) {
    this.cIOrdID = cIOrdID;
  }

  public int getAccountId() {
    return accountId;
  }

  public void setAccountId(int accountId) {
    this.accountId = accountId;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public DEBIT_CREDIT getSide() {
    return side;
  }

  public void setSide(DEBIT_CREDIT side) {
    this.side = side;
  }

  public LocalDate getTransactTime() {
    return transactTime;
  }

  public void setTransactTime(LocalDate transactTime) {
    this.transactTime = transactTime;
  }

  public float getOrderQtyData() {
    return orderQtyData;
  }

  public void setOrderQtyData(float orderQtyData) {
    this.orderQtyData = orderQtyData;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getExchange() {
    return this.exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public ORDER_TYPE getType() {
    return type;
  }

  public void setType(ORDER_TYPE type) {
    this.type = type;
  }

  public float getAmount() {
    return amount;
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public String getCashCurrency() {
    return this.cashCurrency;
  }

  public void setCashCurrency(String cashCurrency) {
    this.cashCurrency = cashCurrency;
  }
}
