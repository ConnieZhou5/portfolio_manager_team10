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
     * Save a daily portfolio snapshot (idempotent - won't overwrite existing snapshots)
     * 
     * @param date The date for the snapshot
     * @return The saved daily value (existing or newly created)
     */
    public PortfolioDailyValue saveDailySnapshot(LocalDate date) {
        return portfolioDailyValueRepository.findBySnapshotDate(date)
            .orElseGet(() -> {
                try {
                    // Calculate current portfolio values
                    BigDecimal cashValue = cashService.getCashBalance();
                    BigDecimal investmentsValue = getTotalPortfolioValue();
                    BigDecimal totalValue = cashValue.add(investmentsValue);
                    
                    // Create new snapshot
                    PortfolioDailyValue pdv = new PortfolioDailyValue();
                    pdv.setSnapshotDate(date); // never overwrite a past date
                    pdv.setCashValue(cashValue);
                    pdv.setInvestmentsValue(investmentsValue);
                    pdv.setTotalValue(totalValue);
                    
                    return portfolioDailyValueRepository.save(pdv);
                } catch (Exception e) {
                    // Log error and rethrow to maintain transaction integrity
                    System.err.println("❌ Error creating daily snapshot for " + date + ": " + e.getMessage());
                    throw new RuntimeException("Failed to create daily snapshot for " + date, e);
                }
            });
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
        return saveDailySnapshot(DateUtil.getCurrentDateInNYC());
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
     * Runs at 4:00 PM EDT (market close) on weekdays only
     * Cron format: "0 0 16 * * MON-FRI" = minute hour day month day-of-week
     */
    @Scheduled(cron = "0 0 16 * * MON-FRI", zone = "America/New_York")
    public void scheduledDailySnapshot() {
        try {
            LocalDate today = DateUtil.getCurrentDateInNYC();
            
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
            LocalDate cutoffDate = DateUtil.getCurrentDateInNYC().minusDays(30);
            int deletedCount = portfolioDailyValueRepository.deleteSnapshotsOlderThan(cutoffDate);
            System.out.println("🧹 Cleaned up " + deletedCount + " snapshots older than " + cutoffDate);
        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
        }
    }
} 