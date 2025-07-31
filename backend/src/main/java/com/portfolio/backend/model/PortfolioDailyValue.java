package com.portfolio.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a daily snapshot of portfolio values.
 * Used for tracking historical performance and calculating day-over-day gains.
 */
@Entity
@Table(name = "portfolio_daily_values")
public class PortfolioDailyValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;
    
    @Column(name = "total_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalValue;
    
    @Column(name = "investments_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal investmentsValue;
    
    @Column(name = "cash_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal cashValue;
    
    // Constructor
    public PortfolioDailyValue() {
    }
    
    public PortfolioDailyValue(LocalDate snapshotDate, BigDecimal totalValue, 
                             BigDecimal investmentsValue, BigDecimal cashValue) {
        this.snapshotDate = snapshotDate;
        this.totalValue = totalValue;
        this.investmentsValue = investmentsValue;
        this.cashValue = cashValue;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }
    
    public void setSnapshotDate(LocalDate snapshotDate) {
        this.snapshotDate = snapshotDate;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
    
    public BigDecimal getInvestmentsValue() {
        return investmentsValue;
    }
    
    public void setInvestmentsValue(BigDecimal investmentsValue) {
        this.investmentsValue = investmentsValue;
    }
    
    public BigDecimal getCashValue() {
        return cashValue;
    }
    
    public void setCashValue(BigDecimal cashValue) {
        this.cashValue = cashValue;
    }
    
    @Override
    public String toString() {
        return "PortfolioDailyValue{" +
                "id=" + id +
                ", snapshotDate=" + snapshotDate +
                ", totalValue=" + totalValue +
                ", investmentsValue=" + investmentsValue +
                ", cashValue=" + cashValue +
                '}';
    }
} 