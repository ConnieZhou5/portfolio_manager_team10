package com.portfolio.backend.repository;

import com.portfolio.backend.model.CashAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashAccountRepository extends JpaRepository<CashAccount, Long> {
    
    /**
     * Find the first (and should be only) cash account
     * Since we only have one cash account per user
     * 
     * @return Optional containing the cash account
     */
    Optional<CashAccount> findFirstByOrderByIdAsc();
    
    /**
     * Check if any cash account exists
     * 
     * @return true if a cash account exists
     */
    boolean existsBy();
} 