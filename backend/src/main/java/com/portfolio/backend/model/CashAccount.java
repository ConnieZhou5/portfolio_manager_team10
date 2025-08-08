package com.portfolio.backend.model;

import com.portfolio.backend.util.DateUtil;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents the user's cash account balance.
 * Used for tracking available cash for investments and deposits/withdrawals.
 */
@Entity
@Table(name = "cash_account")
public class CashAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    // Constructor
    public CashAccount() {
        this.balance = BigDecimal.ZERO;
        this.lastUpdated = DateUtil.getCurrentDateTimeInNYC();
    }
    
    public CashAccount(BigDecimal balance) {
        this.balance = balance;
        this.lastUpdated = DateUtil.getCurrentDateTimeInNYC();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        this.lastUpdated = DateUtil.getCurrentDateTimeInNYC();
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    /**
     * Add cash to the account
     * 
     * @param amount Amount to add
     */
    public void addCash(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.lastUpdated = DateUtil.getCurrentDateTimeInNYC();
    }
    
    /**
     * Subtract cash from the account
     * 
     * @param amount Amount to subtract
     * @return true if successful, false if insufficient funds
     */
    public boolean subtractCash(BigDecimal amount) {
        if (this.balance.compareTo(amount) >= 0) {
            this.balance = this.balance.subtract(amount);
            this.lastUpdated = DateUtil.getCurrentDateTimeInNYC();
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "CashAccount{" +
                "id=" + id +
                ", balance=" + balance +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
} 