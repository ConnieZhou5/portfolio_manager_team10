package com.portfolio.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeHistoryResponse {
    private Long id;
    private LocalDate tradeDate;
    private String ticker;
    private Integer quantity;
    private BigDecimal price;
    private String tradeType;
    private BigDecimal totalValue;

    // Default constructor
    public TradeHistoryResponse() {}

    // Constructor with all fields
    public TradeHistoryResponse(Long id, LocalDate tradeDate, String ticker, Integer quantity, 
                               BigDecimal price, String tradeType, BigDecimal totalValue) {
        this.id = id;
        this.tradeDate = tradeDate;
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.tradeType = tradeType;
        this.totalValue = totalValue;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    @Override
    public String toString() {
        return "TradeHistoryResponse{" +
                "id=" + id +
                ", tradeDate=" + tradeDate +
                ", ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", tradeType='" + tradeType + '\'' +
                ", totalValue=" + totalValue +
                '}';
    }
} 