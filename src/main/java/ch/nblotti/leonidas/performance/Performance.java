package ch.nblotti.leonidas.performance;

import java.time.LocalDate;

public class Performance {

  private int type;
  private LocalDate posDate;
  private double perf;
  private int accountId;

  public Performance(int accountId, LocalDate posDate, int type, double perf) {
    this.accountId = accountId;
    this.type = type;
    this.posDate = posDate;
    this.perf = perf;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public LocalDate getPosDate() {
    return posDate;
  }

  public void setPosDate(LocalDate posDate) {
    this.posDate = posDate;
  }

  public double getPerf() {
    return perf;
  }

  public void setPerf(double perf) {
    this.perf = perf;
  }

  public int getAccountId() {
    return accountId;
  }

  public void setAccountId(int accountId) {
    this.accountId = accountId;
  }
}
