package ch.nblotti.leonidas.position;

import ch.nblotti.leonidas.entry.ACHAT_VENTE_TITRE;
import ch.nblotti.leonidas.entry.EntryPO;

import java.time.LocalDate;

public abstract class AggregatedEntryVO {


  private int account;

  private ACHAT_VENTE_TITRE achatVenteTitre;


  private LocalDate entryDate;

  private LocalDate valueDate;

  private Float fxchangeRate;
  private int status;


  public AggregatedEntryVO(EntryPO currentEntryTO) {

    this.achatVenteTitre = currentEntryTO.getAchatVenteCode();
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

  public ACHAT_VENTE_TITRE getAchatVenteTitre() {
    return achatVenteTitre;
  }

  public void setAchatVenteTitre(ACHAT_VENTE_TITRE achatVenteTitre) {
    this.achatVenteTitre = achatVenteTitre;
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
