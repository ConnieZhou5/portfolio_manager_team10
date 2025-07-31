package com.portfolio.backend.model;

import jakarta.persistence.*; // JPA aka java specification api 
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents an item in the user's financial portfolio.
 * This entity is mapped to a database table using JPA annotations.
 *
 * Fields:
 *   id - Unique identifier
 *   ticker - The symbol representing the asset (e.g., "AAPL" for Apple Inc.)
 *   quantity - The number of units owned
 *   buyPrice - The price per unit at which the asset was purchased
 *   buyDate - The date the asset was purchased
 */
@Entity // This tells JPA that this is a JPA entity
public class PortfolioItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // using long to avoid overflow
    
    @Column(nullable = false, length = 10)
    private String ticker;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "buy_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal buyPrice; // not using double for precision
    
    @Column(name = "buy_date", nullable = false)
    private LocalDate buyDate;    
    
    // Constructor
    public PortfolioItem() {
    }

    public PortfolioItem(String ticker, Integer quantity, BigDecimal buyPrice, LocalDate buyDate) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.buyDate = buyDate;
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
 
    
    // Helper method to calculate total value
    public BigDecimal getTotalValue() {
        if (buyPrice != null && quantity != null) {
            return buyPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
    
    @Override
    public String toString() {
        return "PortfolioItem{" +
                "id=" + id +
                ", ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                ", buyPrice=" + buyPrice +
                ", buyDate=" + buyDate +
                ", totalValue=" + getTotalValue() +
                '}';
    }
}
