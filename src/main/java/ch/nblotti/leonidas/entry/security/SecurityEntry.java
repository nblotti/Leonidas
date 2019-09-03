package ch.nblotti.leonidas.entry.security;


import ch.nblotti.leonidas.entry.DEBIT_CREDIT;
import ch.nblotti.leonidas.entry.Entry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


@Entity(name = "SECURITY_ENTRY")
public class SecurityEntry extends Entry {


  @NotNull(message = "Account is mandatory")
  @Column(name = "SECURITY_ACCOUNT")
  private int account;

  @NotNull(message = "Quantity is mandatory")
  @Column(name = "QUANTITY")
  private Float quantity;

  @Column(name = "EXCHANGE")
  private String exchange;

  @NotNull(message = "Quantity is mandatory")
  @Column(name = "SECURITY_ID")
  private String securityID;

  public int getAccount() {
    return account;
  }

  public void setAccount(int account) {
    this.account = account;
  }

  public Float getQuantity() {
    return quantity;
  }

  public void setQuantity(Float quantity) {
    this.quantity = quantity;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getSecurityID() {
    return securityID;
  }

  public void setSecurityID(String securityID) {
    this.securityID = securityID;
  }
}
