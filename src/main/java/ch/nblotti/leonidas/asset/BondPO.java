package ch.nblotti.leonidas.asset;


import com.fasterxml.jackson.annotation.JsonProperty;

public class BondPO {


  private String date;
  private String price;
  private String yield;
  private String volume;

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
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
}
