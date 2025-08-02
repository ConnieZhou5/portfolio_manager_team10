package com.portfolio.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BuyRequest {
    private String ticker;
    private Integer quantity;
    private BigDecimal price;
    private LocalDate tradeDate;

    // Default constructor
    public BuyRequest() {}

    // Constructor with all fields
    public BuyRequest(String ticker, Integer quantity, BigDecimal price, LocalDate tradeDate) {
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

    // Calculate total cost
    public BigDecimal getTotalCost() {
        if (price != null && quantity != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "BuyRequest{" +
                "ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", tradeDate=" + tradeDate +
                ", totalCost=" + getTotalCost() +
                '}';
    }
} 