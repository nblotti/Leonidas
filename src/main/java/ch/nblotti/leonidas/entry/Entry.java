package ch.nblotti.leonidas.entry;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@MappedSuperclass
public class Entry {
  @Id
  @GeneratedValue
  protected Long id;
  @NotNull(message = "Order ID is mandatory")
  @Column(name = "ORDER_ID")
  protected long orderID;
  @NotNull(message = "Debit/Credit code is mandatory")
  @Column(name = "DEBIT_CREDIT_CODE")
  protected DEBIT_CREDIT debitCreditCode;
  @NotNull(message = "Entry date is mandatory")
  @Column(name = "ENTRY_DATE")
  protected LocalDate entryDate;
  @NotNull(message = "Transaction time is mandatory")
  @Column(name = "VALUE_DATE")
  protected LocalDate valueDate;
  @NotNull(message = "Currency is mandatory")
  @Column(name = "CURRENCY")
  protected String currency;
  @NotNull(message = "Reporting currency value is mandatory")
  @Column(name = "ENTRY_VALUE_REPORTING_CURRENCY")
  protected Float entryValueReportingCurrency;

  @NotNull(message = "Net Amount is mandatory")
  @Column(name = "NET_AMOUNT")
  protected Float netAmount;

  @NotNull(message = "Brut Amount is mandatory")
  @Column(name = "GROSS_AMOUNT")
  protected Float grossAmount;


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

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Float getEntryValueReportingCurrency() {
    return entryValueReportingCurrency;
  }

  public void setEntryValueReportingCurrency(Float entryValueReportingCurrency) {
    this.entryValueReportingCurrency = entryValueReportingCurrency;
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

  public String getAccountReportingCurrency() {
    return accountReportingCurrency;
  }

  public void setAccountReportingCurrency(String accountReportingCurrency) {
    this.accountReportingCurrency = accountReportingCurrency;
  }

  public Float getFxExchangeRate() {
    return fxExchangeRate;
  }

  public void setFxExchangeRate(Float fxExchangeRate) {
    this.fxExchangeRate = fxExchangeRate;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
