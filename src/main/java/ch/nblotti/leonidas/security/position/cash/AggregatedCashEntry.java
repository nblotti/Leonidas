package ch.nblotti.leonidas.security.position.cash;

import ch.nblotti.leonidas.security.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.security.entry.cash.CashEntry;

import java.time.LocalDate;

public class AggregatedCashEntry {

  private String cIOrdID;

  private int account;

  private DEBIT_CREDIT debitCreditCode;


  private LocalDate entryDate;

  private LocalDate valueDate;

  private Float netAmount;

  private Float grossAmount;

  private String currency;

  private Float fxchangeRate;

  private int status;


  public AggregatedCashEntry(CashEntry currentCashEntry) {
    this.account = currentCashEntry.getAccount();
    this.debitCreditCode = currentCashEntry.getDebitCreditCode();
    this.entryDate = currentCashEntry.getEntryDate();
    this.valueDate = currentCashEntry.getValueDate();
    this.netAmount = currentCashEntry.getNetAmount();
    this.grossAmount = currentCashEntry.getGrossAmount();
    this.currency = currentCashEntry.getCurrency();
    this.fxchangeRate = currentCashEntry.getFxExchangeRate();
  }

  public String getcIOrdID() {
    return cIOrdID;
  }

  public void setcIOrdID(String cIOrdID) {
    this.cIOrdID = cIOrdID;
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

  public Float getFxchangeRate() {
    return fxchangeRate;
  }

  public void setFxchangeRate(Float fxchangeRate) {
    this.fxchangeRate = fxchangeRate;
  }
}
