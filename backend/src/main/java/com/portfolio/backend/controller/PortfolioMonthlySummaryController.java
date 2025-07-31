package com.portfolio.backend.controller;

import com.portfolio.backend.model.PortfolioMonthlySummary;
import com.portfolio.backend.service.PortfolioMonthlySummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/monthly-summaries")
@CrossOrigin(origins = "*")
public class PortfolioMonthlySummaryController {

    @Autowired
    private PortfolioMonthlySummaryService portfolioMonthlySummaryService;

    /**
     * Get all monthly summaries
     * 
     * @return List of all monthly summaries
     */
    @GetMapping
    public ResponseEntity<List<PortfolioMonthlySummary>> getAllMonthlySummaries() {
        List<PortfolioMonthlySummary> summaries = portfolioMonthlySummaryService.getAllMonthlySummaries();
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get monthly summary by year and month
     * 
     * @param year The year
     * @param month The month (1-12)
     * @return Monthly summary if found
     */
    @GetMapping("/{year}/{month}")
    public ResponseEntity<PortfolioMonthlySummary> getMonthlySummary(@PathVariable Integer year, @PathVariable Integer month) {
        Optional<PortfolioMonthlySummary> summary = portfolioMonthlySummaryService.getMonthlySummary(year, month);
        return summary.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all monthly summaries for a specific year
     * 
     * @param year The year
     * @return List of monthly summaries for the year
     */
    @GetMapping("/year/{year}")
    public ResponseEntity<List<PortfolioMonthlySummary>> getMonthlySummariesByYear(@PathVariable Integer year) {
        List<PortfolioMonthlySummary> summaries = portfolioMonthlySummaryService.getMonthlySummariesByYear(year);
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get monthly summaries for the last 12 months
     * 
     * @return List of monthly summaries for the last 12 months
     */
    @GetMapping("/last-12-months")
    public ResponseEntity<List<PortfolioMonthlySummary>> getLast12Months() {
        List<PortfolioMonthlySummary> summaries = portfolioMonthlySummaryService.getLast12Months();
        return ResponseEntity.ok(summaries);
    }

    /**
     * Get monthly summaries for the last year
     * 
     * @return List of monthly summaries for the last year
     */
    @GetMapping("/last-year")
    public ResponseEntity<List<PortfolioMonthlySummary>> getLastYear() {
        List<PortfolioMonthlySummary> summaries = portfolioMonthlySummaryService.getLastYear();
        return ResponseEntity.ok(summaries);
    }

    /**
     * Create monthly summary for current month
     * 
     * @return The created monthly summary
     */
    @PostMapping("/current-month")
    public ResponseEntity<PortfolioMonthlySummary> createCurrentMonthSummary() {
        PortfolioMonthlySummary summary = portfolioMonthlySummaryService.createCurrentMonthSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Create or update a monthly summary
     * 
     * @param year The year
     * @param month The month (1-12)
     * @param totalValue The total portfolio value at the end of the month
     * @param previousMonthValue The total portfolio value at the end of the previous month
     * @return The saved monthly summary
     */
    @PostMapping("/{year}/{month}")
    public ResponseEntity<PortfolioMonthlySummary> createOrUpdateMonthlySummary(
            @PathVariable Integer year,
            @PathVariable Integer month,
            @RequestParam("totalValue") String totalValue,
            @RequestParam("previousMonthValue") String previousMonthValue) {
        
        try {
            java.math.BigDecimal totalValueBD = new java.math.BigDecimal(totalValue);
            java.math.BigDecimal previousMonthValueBD = new java.math.BigDecimal(previousMonthValue);
            
            PortfolioMonthlySummary summary = portfolioMonthlySummaryService.createOrUpdateMonthlySummary(
                    year, month, totalValueBD, previousMonthValueBD);
            return ResponseEntity.ok(summary);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete monthly summary by year and month
     * 
     * @param year The year
     * @param month The month (1-12)
     * @return Success response
     */
    @DeleteMapping("/{year}/{month}")
    public ResponseEntity<Void> deleteMonthlySummary(@PathVariable Integer year, @PathVariable Integer month) {
        boolean deleted = portfolioMonthlySummaryService.deleteMonthlySummary(year, month);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Clean up old monthly summaries (keep only last year)
     * 
     * @return Number of summaries deleted
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupOldSummaries() {
        int deletedCount = portfolioMonthlySummaryService.cleanupOldSummaries();
        return ResponseEntity.ok("Deleted " + deletedCount + " old monthly summaries");
    }

    /**
     * Delete all monthly summaries
     * 
     * @return Number of summaries deleted
     */
    @DeleteMapping("/delete-all-monthly")
    public ResponseEntity<String> deleteAllMonthlySummaries() {
        int deletedCount = portfolioMonthlySummaryService.deleteAllMonthlySummaries();
        return ResponseEntity.ok("Deleted " + deletedCount + " monthly summaries");
    }

    /**
     * Delete all daily records (records with month > 12)
     * 
     * @return Number of daily records deleted
     */
    @DeleteMapping("/delete-all-daily")
    public ResponseEntity<String> deleteAllDailyRecords() {
        int deletedCount = portfolioMonthlySummaryService.deleteAllDailyRecords();
        return ResponseEntity.ok("Deleted " + deletedCount + " daily records");
    }

    /**
     * Create dummy historical data for the last year
     * 
     * @return Number of monthly summaries created
     */
    @PostMapping("/create-dummy-year")
    public ResponseEntity<String> createDummyYearData() {
        int createdCount = portfolioMonthlySummaryService.createDummyHistoricalData();
        return ResponseEntity.ok("Created " + createdCount + " monthly summaries for the last year");
    }

    /**
     * Create comprehensive dummy data (monthly only)
     * 
     * @return Map with counts of created records
     */
    @PostMapping("/create-dummy-comprehensive")
    public ResponseEntity<java.util.Map<String, Integer>> createComprehensiveDummyData() {
        java.util.Map<String, Integer> results = portfolioMonthlySummaryService.createComprehensiveDummyData();
        return ResponseEntity.ok(results);
    }

    /**
     * Create dummy data from June 2024 to current month
     * 
     * @return Number of monthly summaries created
     */
    @PostMapping("/create-june-2024-to-current")
    public ResponseEntity<String> createJune2024ToCurrent() {
        int createdCount = portfolioMonthlySummaryService.createDummyHistoricalData();
        return ResponseEntity.ok("Created " + createdCount + " monthly summaries from June 2024 to current month");
    }
} 