package com.portfolio.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeHistoryRequest {
    private LocalDate tradeDate;
    private String ticker;
    private Integer quantity;
    private BigDecimal price;
    private String tradeType; // "BUY" or "SELL"

    // Default constructor
    public TradeHistoryRequest() {}

    // Constructor with all fields
    public TradeHistoryRequest(LocalDate tradeDate, String ticker, Integer quantity, BigDecimal price, String tradeType) {
        this.tradeDate = tradeDate;
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.tradeType = tradeType;
    }

    // Getters and Setters
    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    @Override
    public String toString() {
        return "TradeHistoryRequest{" +
                "tradeDate=" + tradeDate +
                ", ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", tradeType='" + tradeType + '\'' +
                '}';
    }
} 