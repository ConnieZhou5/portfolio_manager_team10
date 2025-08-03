package com.portfolio.backend.repository;

import com.portfolio.backend.model.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

    /**
     * Find all trades by ticker symbol
     */
    List<TradeHistory> findByTickerOrderByTradeDateDesc(String ticker);

    /**
     * Find all trades by trade type (BUY or SELL)
     */
    List<TradeHistory> findByTradeTypeOrderByTradeDateDesc(TradeHistory.TradeType tradeType);

    /**
     * Find all trades for a specific ticker and trade type
     */
    List<TradeHistory> findByTickerAndTradeTypeOrderByTradeDateDesc(String ticker, TradeHistory.TradeType tradeType);

    /**
     * Find all trades between two dates
     */
    List<TradeHistory> findByTradeDateBetweenOrderByTradeDateDesc(LocalDate startDate, LocalDate endDate);

    /**
     * Find all trades for a specific ticker and trade type before a given date
     */
    List<TradeHistory> findByTickerAndTradeTypeAndTradeDateBeforeOrderByTradeDateAsc(String ticker, TradeHistory.TradeType tradeType, LocalDate date);

    /**
     * Check if there are any trades for a specific ticker
     */
    boolean existsByTicker(String ticker);

    /**
     * Count total number of trades for a specific ticker
     */
    long countByTicker(String ticker);
} 