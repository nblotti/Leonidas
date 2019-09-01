package ch.nblotti.leonidas.security.asset;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Asset {


  @JsonProperty("Code")
  private String code;
  @JsonProperty("Name")
  private String name;
  @JsonProperty("Country")
  private String country;
  @JsonProperty("Exchange")
  private String exchange;
  @JsonProperty("Currency")
  private String currency;
  @JsonProperty("Type")
  private String type;

  public Asset() {
  }

  public Asset(String code, String name, String country, String exchange, String currency, String type) {
    this.code = code;
    this.name = name;
    this.country = country;
    this.exchange = exchange;
    this.currency = currency;
    this.type = type;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
