package ch.nblotti.leonidas.position;

import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.Entry;
import ch.nblotti.leonidas.entry.cash.CashEntry;

import java.time.LocalDate;

public abstract class AggregatedEntry {


  private int account;

  private DEBIT_CREDIT debitCreditCode;


  private LocalDate entryDate;

  private LocalDate valueDate;

  private Float fxchangeRate;
  private int status;


  public AggregatedEntry(Entry currentEntry) {

    this.debitCreditCode = currentEntry.getDebitCreditCode();
    this.entryDate = currentEntry.getEntryDate();
    this.valueDate = currentEntry.getValueDate();
    this.fxchangeRate = currentEntry.getFxExchangeRate();
    this.status = currentEntry.getStatus();

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

  public Float getFxchangeRate() {
    return fxchangeRate;
  }

  public void setFxchangeRate(Float fxchangeRate) {
    this.fxchangeRate = fxchangeRate;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
}
