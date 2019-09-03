package ch.nblotti.leonidas.position.cash;

import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.cash.CashEntry;
import ch.nblotti.leonidas.position.AggregatedEntry;

import java.time.LocalDate;

public class AggregatedCashEntry extends AggregatedEntry {


  private Float netAmount;

  private Float grossAmount;

  private String currency;


  public AggregatedCashEntry(CashEntry currentCashEntry) {
    super(currentCashEntry);
    setAccount(currentCashEntry.getAccount());
    this.netAmount = currentCashEntry.getNetAmount();
    this.grossAmount = currentCashEntry.getGrossAmount();
    this.currency = currentCashEntry.getCurrency();

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

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }
}
