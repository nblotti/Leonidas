package ch.nblotti.leonidas.entry.security;


import ch.nblotti.leonidas.entry.EntryPO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;


@Entity(name = "SECURITY_ENTRY")
public class SecurityEntryPO extends EntryPO {


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
