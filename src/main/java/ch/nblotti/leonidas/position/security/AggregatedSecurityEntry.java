package ch.nblotti.leonidas.position.security;

import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.Entry;
import ch.nblotti.leonidas.entry.security.SecurityEntry;
import ch.nblotti.leonidas.position.AggregatedEntry;

import java.time.LocalDate;

public class AggregatedSecurityEntry extends AggregatedEntry {


  private Float quantity;

  private String securityID;

  private String currency;

  private float netPosValue;
  private float grossPosValue;
  private String exchange;


  public AggregatedSecurityEntry(SecurityEntry currentSecurityEntry) {
    super(currentSecurityEntry);

    setAccount(currentSecurityEntry.getAccount());
    this.securityID = currentSecurityEntry.getSecurityID();
    this.quantity = currentSecurityEntry.getQuantity();
    this.netPosValue = currentSecurityEntry.getNetAmount();
    this.grossPosValue = currentSecurityEntry.getGrossAmount();
    this.exchange = currentSecurityEntry.getExchange();
    this.currency = currentSecurityEntry.getCurrency();

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

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public float getNetPosValue() {
    return netPosValue;
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
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }
}
