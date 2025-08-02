package com.portfolio.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for sell requests
 */
public class SellRequest {
    
    private String ticker;
    private Integer quantity;
    private BigDecimal price;
    private LocalDate tradeDate;
    
    // Constructors
    public SellRequest() {}
    
    public SellRequest(String ticker, Integer quantity, BigDecimal price, LocalDate tradeDate) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.tradeDate = tradeDate;
    }
    
    // Getters and Setters
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
    
    public LocalDate getTradeDate() {
        return tradeDate;
    }
    
    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }
    
    @Override
    public String toString() {
        return "SellRequest{" +
                "ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", tradeDate=" + tradeDate +
                '}';
    }
} 