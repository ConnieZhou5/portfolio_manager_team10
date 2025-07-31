package com.portfolio.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Represents a monthly summary of the user's financial portfolio.
 * This entity is mapped to a database table using JPA annotations.
 *
 * Fields:
 *   id - Unique identifier
 *   year - The year of the summary
 *   month - The month of the summary (1-12)
 *   totalValue - The total portfolio value at the end of the month
 *   monthlyGain - The absolute gain/loss for the month
 *   monthlyGainPercentage - The percentage gain/loss for the month
 */
@Entity
@Table(name = "portfolio_monthly_summaries")
public class PortfolioMonthlySummary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer year;
    
    @Column(nullable = false)
    private Integer month;
    
    @Column(name = "total_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalValue;
    
    @Column(name = "monthly_gain", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyGain;
    
    @Column(name = "monthly_gain_percentage", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyGainPercentage;
    
    // Constructor
    public PortfolioMonthlySummary() {
    }

    public PortfolioMonthlySummary(Integer year, Integer month, BigDecimal totalValue, 
                                 BigDecimal monthlyGain, BigDecimal monthlyGainPercentage) {
        this.year = year;
        this.month = month;
        this.totalValue = totalValue;
        this.monthlyGain = monthlyGain;
        this.monthlyGainPercentage = monthlyGainPercentage;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    public BigDecimal getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }
    
    public BigDecimal getMonthlyGain() {
        return monthlyGain;
    }
    
    public void setMonthlyGain(BigDecimal monthlyGain) {
        this.monthlyGain = monthlyGain;
    }
    
    public BigDecimal getMonthlyGainPercentage() {
        return monthlyGainPercentage;
    }
    
    public void setMonthlyGainPercentage(BigDecimal monthlyGainPercentage) {
        this.monthlyGainPercentage = monthlyGainPercentage;
    }
    
    @Override
    public String toString() {
        return "PortfolioMonthlySummary{" +
                "id=" + id +
                ", year=" + year +
                ", month=" + month +
                ", totalValue=" + totalValue +
                ", monthlyGain=" + monthlyGain +
                ", monthlyGainPercentage=" + monthlyGainPercentage +
                '}';
    }
} 