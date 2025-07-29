package com.portfolio.backend.repository;

import com.portfolio.backend.model.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    
    // Find all portfolio items by ticker symbol
    List<PortfolioItem> findByTicker(String ticker);
    
    // Get total portfolio value
    @Query("SELECT SUM(p.buyPrice * p.quantity) FROM PortfolioItem p")
    Optional<BigDecimal> getTotalPortfolioValue();
    
    // Get total value by ticker
    @Query("SELECT SUM(p.buyPrice * p.quantity) FROM PortfolioItem p WHERE p.ticker = :ticker")
    Optional<BigDecimal> getTotalValueByTicker(@Param("ticker") String ticker);
}
