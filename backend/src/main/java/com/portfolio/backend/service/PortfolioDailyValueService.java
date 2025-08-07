package com.portfolio.backend.service;

import com.portfolio.backend.model.PortfolioDailyValue;
import com.portfolio.backend.repository.PortfolioDailyValueRepository;
import com.portfolio.backend.repository.PortfolioItemRepository;
import com.portfolio.backend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class PortfolioDailyValueService {

    @Autowired
    private PortfolioDailyValueRepository portfolioDailyValueRepository;
    
    @Autowired
    private PortfolioItemRepository portfolioItemRepository;
    
    @Autowired
    private CashService cashService;

    /**
     * Save a daily portfolio snapshot
     * 
     * @param date The date for the snapshot
     * @return The saved daily value
     */
    public PortfolioDailyValue saveDailySnapshot(LocalDate date) {
        BigDecimal cash = cashService.getCashBalance();
        BigDecimal totalPortfolioValue = getTotalPortfolioValue();
        BigDecimal totalAssets = totalPortfolioValue.add(cash);
        
        PortfolioDailyValue dailyValue = new PortfolioDailyValue(
            date, totalAssets, totalPortfolioValue, cash
        );
        
        return portfolioDailyValueRepository.save(dailyValue);
    }
    
    /**
     * Get total portfolio value directly from repository
     * 
     * @return Total value of all portfolio items
     */
    private BigDecimal getTotalPortfolioValue() {
        return portfolioItemRepository.getTotalPortfolioValue()
                .orElse(BigDecimal.ZERO);
    }
    
    /**
     * Save today's portfolio snapshot
     * 
     * @return The saved daily value
     */
    public PortfolioDailyValue saveTodaySnapshot() {
        return saveDailySnapshot(DateUtil.getCurrentDateInEST());
    }
    
    /**
     * Get the most recent portfolio value before a given date
     * 
     * @param date The date to find the previous snapshot for
     * @return Optional containing the most recent daily value before the given date
     */
    public Optional<PortfolioDailyValue> getMostRecentBeforeDate(LocalDate date) {
        return portfolioDailyValueRepository.findTopBySnapshotDateBeforeOrderBySnapshotDateDesc(date);
    }
    
    /**
     * Check if a snapshot exists for a specific date
     * 
     * @param date The date to check
     * @return true if a snapshot exists for the given date
     */
    public boolean existsByDate(LocalDate date) {
        return portfolioDailyValueRepository.existsBySnapshotDate(date);
    }

    /**
     * Scheduled job to save daily portfolio snapshot
     * Runs at 4:00 PM EST (market close) on weekdays only
     * Cron format: "0 0 16 * * MON-FRI" = minute hour day month day-of-week
     */
    @Scheduled(cron = "0 0 16 * * MON-FRI")
    public void scheduledDailySnapshot() {
        try {
            LocalDate today = DateUtil.getCurrentDateInEST();
            
            // Only save if we don't already have a snapshot for today
            if (!portfolioDailyValueRepository.existsBySnapshotDate(today)) {
                PortfolioDailyValue savedSnapshot = saveTodaySnapshot();
                System.out.println("✅ Daily portfolio snapshot saved for " + today + ": $" + savedSnapshot.getTotalValue());
            } else {
                System.out.println("ℹ️ Daily snapshot already exists for " + today);
            }
        } catch (Exception e) {
            System.err.println("❌ Error saving daily snapshot: " + e.getMessage());
        }
    }
    
    /**
     * Scheduled job to clean up old snapshots
     * Runs every Sunday at 2:00 AM to delete snapshots older than 30 days
     * Cron format: "0 0 2 * * SUN" = minute hour day month day-of-week
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void scheduledCleanup() {
        try {
            LocalDate cutoffDate = DateUtil.getCurrentDateInEST().minusDays(30);
            int deletedCount = portfolioDailyValueRepository.deleteSnapshotsOlderThan(cutoffDate);
            System.out.println("🧹 Cleaned up " + deletedCount + " snapshots older than " + cutoffDate);
        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
        }
    }
} 