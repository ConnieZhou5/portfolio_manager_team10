package com.portfolio.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for portfolio item requests
 */
public class PortfolioItemRequest {
    
    private String ticker;
    private Integer quantity;
    private BigDecimal buyPrice;
    private LocalDate buyDate;
    
    // Constructors
    public PortfolioItemRequest() {}
    
    public PortfolioItemRequest(String ticker, Integer quantity, BigDecimal buyPrice, LocalDate buyDate) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.buyDate = buyDate;
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
    
    public BigDecimal getBuyPrice() {
        return buyPrice;
    }
    
    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }
    
    public LocalDate getBuyDate() {
        return buyDate;
    }
    
    public void setBuyDate(LocalDate buyDate) {
        this.buyDate = buyDate;
    }
    
    @Override
    public String toString() {
        return "PortfolioItemRequest{" +
                "ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                ", buyPrice=" + buyPrice +
                ", buyDate=" + buyDate +
                '}';
    }
} 