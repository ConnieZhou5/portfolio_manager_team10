package com.portfolio.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "trade_history")
public class TradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "ticker", nullable = false, length = 12)
    private String ticker;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false)
    private TradeType tradeType;

    // Constructors
    public TradeHistory() {}

    public TradeHistory(LocalDate tradeDate, String ticker, Integer quantity, BigDecimal price, TradeType tradeType) {
        this.tradeDate = tradeDate;
        this.ticker = ticker;
        this.quantity = quantity;
        this.price = price;
        this.tradeType = tradeType;
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

    public TradeType getTradeType() {
        return tradeType;
    }

    public void setTradeType(TradeType tradeType) {
        this.tradeType = tradeType;
    }

    // Calculate total value of the trade
    public BigDecimal getTotalValue() {
        return price.multiply(new BigDecimal(quantity));
    }

    @Override
    public String toString() {
        return "TradeHistory{" +
                "id=" + id +
                ", tradeDate=" + tradeDate +
                ", ticker='" + ticker + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", tradeType=" + tradeType +
                '}';
    }

    // Enum for trade type
    public enum TradeType {
        BUY, SELL
    }
}
