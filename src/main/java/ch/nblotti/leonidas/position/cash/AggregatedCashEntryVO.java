package ch.nblotti.leonidas.position.cash;

import ch.nblotti.leonidas.entry.cash.CashEntryPO;
import ch.nblotti.leonidas.position.AggregatedEntryVO;

public class AggregatedCashEntryVO extends AggregatedEntryVO {


  private Float netAmount;

  private Float grossAmount;

  private String currency;


  public AggregatedCashEntryVO(CashEntryPO currentCashEntryTO) {
    super(currentCashEntryTO);
    setAccount(currentCashEntryTO.getAccount());
    this.netAmount = currentCashEntryTO.getNetAmount();
    this.grossAmount = currentCashEntryTO.getGrossAmount();
    this.currency = currentCashEntryTO.getCurrency();

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
