package ch.nblotti.leonidas.entry.cash;


import ch.nblotti.leonidas.entry.EntryPO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity(name = "CASH_ENTRY")
public class CashEntryPO extends EntryPO {

  @NotNull(message = "Account is mandatory")
  @Column(name = "CASH_ACCOUNT")
  private int account;

  public int getAccount() {
    return account;
  }

  public void setAccount(int account) {
    this.account = account;
  }
}
