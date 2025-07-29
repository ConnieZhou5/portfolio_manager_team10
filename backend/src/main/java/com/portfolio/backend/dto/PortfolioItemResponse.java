package com.portfolio.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for portfolio item responses
 */
public class PortfolioItemResponse {
    
    private Long id;
    private String ticker;
    private Integer quantity;
    private BigDecimal buyPrice;
    private LocalDate buyDate;
    private BigDecimal totalValue;
    
    // Constructors
    public PortfolioItemResponse() {}
    
    public PortfolioItemResponse(Long id, String ticker, Integer quantity, 
                                BigDecimal buyPrice, LocalDate buyDate, BigDecimal totalValue) {
        this.id = id;
        this.ticker = ticker;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.buyDate = buyDate;
        this.totalValue = totalValue;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
    
    @Override
    public String toString() {
        return "PortfolioItemResponse{" +
                "id=" + id +
                ", ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                ", buyPrice=" + buyPrice +
                ", buyDate=" + buyDate +
                ", totalValue=" + totalValue +
                '}';
    }
} 