package ch.nblotti.leonidas.security.account;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity(name = "ACCOUNT")
public class Account {

  @Id
  @GeneratedValue
  private Integer id;


  @NotNull(message = "Opening date is mandatory")
  @Column(name = "OPENING_DATE")
  private LocalDate entryDate;

  @NotNull(message = "Currency is mandatory")
  @Column(name = "PERF_CURRENCY")
  private String performanceCurrency;



  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public LocalDate getEntryDate() {
    return entryDate;
  }

  public void setEntryDate(LocalDate entryDate) {
    this.entryDate = entryDate;
  }

  public String getPerformanceCurrency() {
    return performanceCurrency;
  }

  public void setPerformanceCurrency(String performanceCurrency) {
    this.performanceCurrency = performanceCurrency;
  }
}
