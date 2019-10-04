package ch.nblotti.leonidas.quote;


import com.fasterxml.jackson.annotation.JsonProperty;

public class BondDTO {


  private String isin;
  @JsonProperty("date")
  private String date;
  @JsonProperty("price")
  private String price;
  @JsonProperty("yield")
  private String yield;
  @JsonProperty("volume")
  private String volume;

  public BondDTO() {

  }

  public BondDTO(String isin, String date, String price, String yield, String volume) {
    this.date = date;
    this.isin = isin;
    this.price = price;
    this.yield = yield;
    this.volume = volume;
  }

  public String getIsin() {
    return isin;
  }

  public void setIsin(String isin) {
    this.isin = isin;
  }

  public String getPrice() {
    return price;
  }

  public void setPrice(String price) {
    this.price = price;
  }

  public String getYield() {
    return yield;
  }

  public void setYield(String yield) {
    this.yield = yield;
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }
}
