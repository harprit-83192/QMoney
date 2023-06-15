
package com.crio.warmup.stock.dto;

import java.util.Comparator;

public class TotalReturnsDto {

  private String symbol;
  private Double closingPrice;

  public TotalReturnsDto(String symbol, Double closingPrice) {
    this.symbol = symbol;
    this.closingPrice = closingPrice;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public Double getClosingPrice() {
    return closingPrice;
  }

  public void setClosingPrice(Double closingPrice) {
    this.closingPrice = closingPrice;
  }

  public static final Comparator<TotalReturnsDto> closingComparator = new Comparator<TotalReturnsDto>() {
    public int compare(TotalReturnsDto x, TotalReturnsDto y){
      return (int)(x.getClosingPrice() - y.getClosingPrice());
    }
  };
}
