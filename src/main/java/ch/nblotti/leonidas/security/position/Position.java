package ch.nblotti.leonidas.security.position;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity(name = "POSITIONS")
public class Position {


  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "POS_TYPE")
  private POS_TYPE posType;

  @Column(name = "UNIQUE_ID")
  private String uniqueID;


  @NotNull(message = "Date is mandatory")
  @Column(name = "POS_DATE")
  private LocalDate posDate;


  @NotNull(message = "Account is mandatory")
  @Column(name = "ACCOUNT_ID")
  private int accountId;


  @Column(name = "SECURITY_ID")
  private String securityID;


  @Column(name = "QUANTITY")
  private Float quantity;


  @Column(name = "EXCHANGE")
  private String exchange;

  //TODO NBL fix me
  //@NotNull(message = "Valorisation is mandatory")
  @Column(name = "POS_VALUE")
  private Float posValue;

  @NotNull(message = "Currency is mandatory")
  @Column(name = "CURRENCY")
  private String currency;


  @Column(name = "CMA")
  private Float CMA;

  @Column(name = "TMA")
  private Float tma;

  @Column(name = "UNREALIZED")
  private Float unrealized;


  @Column(name = "REALIZED")
  private Float realized;


  @NotNull(message = "Reporting currency value is mandatory")
  @Column(name = "POS_VALUE_REPORTING_CURRENCY")
  private Float posValueReportingCurrency;

  @NotNull(message = "Account Reporting Currency is mandatory")
  @Column(name = "ACCOUNT_REPORTING_CURRENCY")
  private String accountPerformanceCurrency;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }


  public String getUniqueID() {
    return uniqueID;
  }

  public void setUniqueID(String uniqueID) {
    this.uniqueID = uniqueID;
  }

  public LocalDate getPosDate() {
    return posDate;
  }

  public void setPosDate(LocalDate posDate) {
    this.posDate = posDate;
  }

  public int getAccountId() {
    return accountId;
  }

  public void setAccountId(int accountId) {
    this.accountId = accountId;
  }

  public String getSecurityID() {
    return securityID;
  }

  public void setSecurityID(String securityID) {
    this.securityID = securityID;
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

  public Float getPosValue() {
    return posValue;
  }

  public void setPosValue(Float posValue) {
    this.posValue = posValue;
  }

  public Float getCMA() {
    return CMA;
  }

  public void setCMA(Float CMA) {
    this.CMA = CMA;
  }

  public POS_TYPE getPosType() {
    return posType;
  }

  public void setPosType(POS_TYPE posType) {
    this.posType = posType;
  }

  public Float getUnrealized() {
    return unrealized;
  }

  public void setUnrealized(Float unrealized) {
    this.unrealized = unrealized;
  }

  public Float getRealized() {
    return realized;
  }

  public void setRealized(Float realized) {
    this.realized = realized;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getAccountPerformanceCurrency() {
    return accountPerformanceCurrency;
  }

  public void setAccountPerformanceCurrency(String accountPerformanceCurrency) {
    this.accountPerformanceCurrency = accountPerformanceCurrency;
  }

  public Float getPosValueReportingCurrency() {
    return posValueReportingCurrency;
  }

  public void setPosValueReportingCurrency(Float posValueReportingCurrency) {
    this.posValueReportingCurrency = posValueReportingCurrency;
  }

  public Float getTMA() {
    return this.tma;
  }

  public void setTMA(float tma) {
    this.tma = tma;
  }

  public static enum POS_TYPE {

    CASH(0), SECURITY(1);

    private final int type;

    POS_TYPE(int type) {
      this.type = type;
    }

    public int getType() {
      return type;
    }

    public static POS_TYPE fromType(int type) {
      for (POS_TYPE current : POS_TYPE.values()) {
        if (current.getType() == type)
          return current;
      }
      throw new IllegalStateException();
    }
  }

}
