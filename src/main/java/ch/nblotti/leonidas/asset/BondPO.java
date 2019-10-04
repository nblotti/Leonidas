package ch.nblotti.leonidas.asset;


import com.fasterxml.jackson.annotation.JsonProperty;

public class BondPO {


  @JsonProperty("isin")
  private String date;
  private String price;
  private String yield;
  private String volume;

}
