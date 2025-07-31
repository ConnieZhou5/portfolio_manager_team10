package com.portfolio.backend.service;

import com.portfolio.backend.model.CashAccount;
import com.portfolio.backend.repository.CashAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CashService {

    @Autowired
    private CashAccountRepository cashAccountRepository;

    /**
     * Get the current cash balance
     * 
     * @return Current cash balance
     */
    public BigDecimal getCashBalance() {
        Optional<CashAccount> cashAccount = cashAccountRepository.findFirstByOrderByIdAsc();
        return cashAccount.map(CashAccount::getBalance).orElse(BigDecimal.ZERO);
    }
    
    /**
     * Initialize cash account if it doesn't exist
     * 
     * @param initialBalance Initial cash balance
     * @return The cash account
     */
    public CashAccount initializeCashAccount(BigDecimal initialBalance) {
        Optional<CashAccount> existingAccount = cashAccountRepository.findFirstByOrderByIdAsc();
        
        if (existingAccount.isPresent()) {
            // Update existing account with new initial balance
            CashAccount account = existingAccount.get();
            account.setBalance(initialBalance);
            return cashAccountRepository.save(account);
        } else {
            // Create new account
            CashAccount cashAccount = new CashAccount(initialBalance);
            return cashAccountRepository.save(cashAccount);
        }
    }
    
    /**
     * Add cash to the account
     * 
     * @param amount Amount to add
     * @return Updated cash account
     */
    public CashAccount addCash(BigDecimal amount) {
        CashAccount cashAccount = getOrCreateCashAccount();
        cashAccount.addCash(amount);
        return cashAccountRepository.save(cashAccount);
    }
    
    /**
     * Subtract cash from the account (for buying investments)
     * 
     * @param amount Amount to subtract
     * @return true if successful, false if insufficient funds
     */
    public boolean subtractCash(BigDecimal amount) {
        CashAccount cashAccount = getOrCreateCashAccount();
        if (cashAccount.subtractCash(amount)) {
            cashAccountRepository.save(cashAccount);
            return true;
        }
        return false;
    }
    
    /**
     * Get or create cash account
     * 
     * @return The cash account
     */
    private CashAccount getOrCreateCashAccount() {
        return cashAccountRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> initializeCashAccount(BigDecimal.ZERO));
    }
} 