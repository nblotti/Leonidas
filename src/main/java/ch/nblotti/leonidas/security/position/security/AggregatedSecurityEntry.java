package ch.nblotti.leonidas.security.position.security;

import ch.nblotti.leonidas.security.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.security.entry.security.SecurityEntry;

import java.time.LocalDate;

public class AggregatedSecurityEntry {

  private String cIOrdID;

  private int account;

  private DEBIT_CREDIT debitCreditCode;


  private LocalDate entryDate;

  private LocalDate valueDate;

  private Float quantity;

  private String securityID;

  private String currency;

  private int status;
  private float netPosValue;
  private float grossPosValue;
  private String exchange;

  private Float fxchangeRate;


  public AggregatedSecurityEntry(SecurityEntry currentSecurityEntry) {
    this.account = currentSecurityEntry.getAccount();
    this.debitCreditCode = currentSecurityEntry.getDebitCreditCode();
    this.entryDate = currentSecurityEntry.getEntryDate();
    this.valueDate = currentSecurityEntry.getValueDate();
    this.securityID = currentSecurityEntry.getSecurityID();
    this.quantity = currentSecurityEntry.getQuantity();
    this.netPosValue = currentSecurityEntry.getNetAmount();
    this.grossPosValue = currentSecurityEntry.getGrossAmount();
    this.exchange = currentSecurityEntry.getExchange();
    this.currency = currentSecurityEntry.getCurrency();
    this.fxchangeRate = currentSecurityEntry.getFxExchangeRate();
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


  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public Float getQuantity() {
    return quantity;
  }

  public void setQuantity(Float quantity) {
    this.quantity = quantity;
  }

  public String getSecurityID() {
    return securityID;
  }

  public void setSecurityID(String securityID) {
    this.securityID = securityID;
  }

  public Float getNetPosValue() {
    return this.netPosValue;
  }

  public void setNetPosValue(float netPosValue) {
    this.netPosValue = netPosValue;
  }


  public float getGrossPosValue() {
    return grossPosValue;
  }

  public void setGrossPosValue(float grossPosValue) {
    this.grossPosValue = grossPosValue;
  }

  public String getExchange() {
    return this.exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
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
