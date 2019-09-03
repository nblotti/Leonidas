package ch.nblotti.leonidas.entry.cash;


import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity(name = "CASH_ENTRY")
public class CashEntry extends Entry {

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
