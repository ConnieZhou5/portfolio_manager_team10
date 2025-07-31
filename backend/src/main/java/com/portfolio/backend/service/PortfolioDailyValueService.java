package com.portfolio.backend.service;

import com.portfolio.backend.model.PortfolioDailyValue;
import com.portfolio.backend.repository.PortfolioDailyValueRepository;
import com.portfolio.backend.repository.PortfolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        return saveDailySnapshot(LocalDate.now());
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
            LocalDate today = LocalDate.now();
            
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
            LocalDate cutoffDate = LocalDate.now().minusDays(30);
            int deletedCount = portfolioDailyValueRepository.deleteSnapshotsOlderThan(cutoffDate);
            System.out.println("🧹 Cleaned up " + deletedCount + " snapshots older than " + cutoffDate);
        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Create dummy daily data for the last 30 days
     * 
     * @return Number of daily records created
     */
    public int createDummyDailyData() {
        LocalDate now = LocalDate.now();
        BigDecimal startingTotalValue = new BigDecimal("52000.00");
        BigDecimal startingInvestmentsValue = new BigDecimal("45000.00");
        BigDecimal startingCashValue = new BigDecimal("7000.00");
        
        BigDecimal currentTotalValue = startingTotalValue;
        BigDecimal currentInvestmentsValue = startingInvestmentsValue;
        BigDecimal currentCashValue = startingCashValue;
        
        int createdCount = 0;
        
        // Create data for the last 30 days
        for (int i = 29; i >= 0; i--) {
            LocalDate targetDate = now.minusDays(i);
            
            // Skip if we already have data for this date
            if (portfolioDailyValueRepository.existsBySnapshotDate(targetDate)) {
                continue;
            }
            
            // Generate realistic daily changes
            BigDecimal dailyTotalChange;
            BigDecimal dailyInvestmentsChange;
            BigDecimal dailyCashChange;
            
            if (i == 29) {
                // First day (30 days ago) - no previous data
                dailyTotalChange = BigDecimal.ZERO;
                dailyInvestmentsChange = BigDecimal.ZERO;
                dailyCashChange = BigDecimal.ZERO;
            } else {
                // Generate realistic daily gains/losses
                // Random daily change between -3% and +4% for investments
                double randomPercent = (Math.random() * 7) - 3; // -3 to +4
                BigDecimal dailyChangePercent = new BigDecimal(String.valueOf(randomPercent));
                dailyInvestmentsChange = currentInvestmentsValue.multiply(dailyChangePercent)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                
                // Cash changes are smaller and more stable
                double cashRandomPercent = (Math.random() * 2) - 1; // -1 to +1
                BigDecimal cashChangePercent = new BigDecimal(String.valueOf(cashRandomPercent));
                dailyCashChange = currentCashValue.multiply(cashChangePercent)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                
                dailyTotalChange = dailyInvestmentsChange.add(dailyCashChange);
            }
            
            // Calculate new values
            BigDecimal newTotalValue = currentTotalValue.add(dailyTotalChange);
            BigDecimal newInvestmentsValue = currentInvestmentsValue.add(dailyInvestmentsChange);
            BigDecimal newCashValue = currentCashValue.add(dailyCashChange);
            
            // Create the daily value
            PortfolioDailyValue dailyValue = new PortfolioDailyValue(
                    targetDate, newTotalValue, newInvestmentsValue, newCashValue
            );
            
            portfolioDailyValueRepository.save(dailyValue);
            createdCount++;
            
            // Update current values for next iteration
            currentTotalValue = newTotalValue;
            currentInvestmentsValue = newInvestmentsValue;
            currentCashValue = newCashValue;
        }
        
        return createdCount;
    }
} 