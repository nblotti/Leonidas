package ch.nblotti.leonidas.entry.cash;


import ch.nblotti.leonidas.entry.DEBIT_CREDIT;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity(name = "CASH_ENTRY")
public class CashEntry {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull(message = "Order ID is mandatory")
  @Column(name = "ORDER_ID")
  private long orderID;

  @NotNull(message = "Account is mandatory")
  @Column(name = "CASH_ACCOUNT")
  private int account;

  @NotNull(message = "Debit/Credit code is mandatory")
  @Column(name = "DEBIT_CREDIT_CODE")
  private DEBIT_CREDIT debitCreditCode;


  @NotNull(message = "Entry date is mandatory")
  @Column(name = "ENTRY_DATE")
  private LocalDate entryDate;

  @NotNull(message = "Transaction time is mandatory")
  @Column(name = "VALUE_DATE")
  private LocalDate valueDate;

  @NotNull(message = "Net Amount is mandatory")
  @Column(name = "NET_AMOUNT")
  private Float netAmount;

  @NotNull(message = "Brut Amount is mandatory")
  @Column(name = "GROSS_AMOUNT")
  private Float grossAmount;

  @NotNull(message = "Currency is mandatory")
  @Column(name = "CURRENCY")
  private String currency;

  @NotNull(message = "Reporting currency value is mandatory")
  @Column(name = "ENTRY_VALUE_REPORTING_CURRENCY")
  private Float entryValueReportingCurrency;

  @NotNull(message = "Reporting currency is mandatory")
  @Column(name = "ACCOUNT_REPORTING_CURRENCY")
  private String accountReportingCurrency;


  @NotNull(message = "Fx exchange rate is mandatory")
  @Column(name = "FX_EXCHANGE_RATE")
  private Float fxExchangeRate;

  @Column(name = "STATUS")
  private int status;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public long getOrderID() {
    return orderID;
  }

  public void setOrderID(long orderID) {
    this.orderID = orderID;
  }

  public int getAccount() {
    return account;
  }

  public void setAccount(int account) {
    this.account = account;
  }

  public DEBIT_CREDIT getDebitCreditCode() {
    return debitCreditCode;
  }

  public void setDebitCreditCode(DEBIT_CREDIT debitCreditCode) {
    this.debitCreditCode = debitCreditCode;
  }

  public LocalDate getEntryDate() {
    return entryDate;
  }

  public void setEntryDate(LocalDate entryDate) {
    this.entryDate = entryDate;
  }

  public LocalDate getValueDate() {
    return valueDate;
  }

  public void setValueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
  }

  public Float getNetAmount() {
    return netAmount;
  }

  public void setNetAmount(Float netAmount) {
    this.netAmount = netAmount;
  }

  public Float getGrossAmount() {
    return grossAmount;
  }

  public void setGrossAmount(Float grossAmount) {
    this.grossAmount = grossAmount;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Float getFxExchangeRate() {
    return fxExchangeRate;
  }

  public void setFxExchangeRate(Float fxExchangeRate) {
    this.fxExchangeRate = fxExchangeRate;
  }


  public Float getEntryValueReportingCurrency() {
    return entryValueReportingCurrency;
  }

  public void setEntryValueReportingCurrency(Float entryValueReportingCurrency) {
    this.entryValueReportingCurrency = entryValueReportingCurrency;
  }

  public String getAccountReportingCurrency() {
    return accountReportingCurrency;
  }

  public void setAccountReportingCurrency(String accountReportingCurrency) {
    this.accountReportingCurrency = accountReportingCurrency;
  }
}
