package ch.nblotti.leonidas.position;

import ch.nblotti.leonidas.entry.ACHAT_VENTE;
import ch.nblotti.leonidas.entry.EntryPO;

import java.time.LocalDate;

public abstract class AggregatedEntryVO {


  private int account;

  private ACHAT_VENTE debitCreditCode;


  private LocalDate entryDate;

  private LocalDate valueDate;

  private Float fxchangeRate;
  private int status;


  public AggregatedEntryVO(EntryPO currentEntryTO) {

    this.debitCreditCode = currentEntryTO.getAchatVenteCode();
    this.entryDate = currentEntryTO.getEntryDate();
    this.valueDate = currentEntryTO.getValueDate();
    this.fxchangeRate = currentEntryTO.getFxExchangeRate();
    this.status = currentEntryTO.getStatus();

  }

  public int getAccount() {
    return account;
  }

  public void setAccount(int account) {
    this.account = account;
  }

  public ACHAT_VENTE getDebitCreditCode() {
    return debitCreditCode;
  }

  public void setDebitCreditCode(ACHAT_VENTE debitCreditCode) {
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
