package com.portfolio.backend.service;

import com.portfolio.backend.model.PortfolioMonthlySummary;
import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.model.TradeHistory;
import com.portfolio.backend.repository.PortfolioItemRepository;
import com.portfolio.backend.repository.PortfolioMonthlySummaryRepository;
import com.portfolio.backend.repository.TradeHistoryRepository;
import com.portfolio.backend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioMonthlySummaryService {

    @Autowired
    private PortfolioMonthlySummaryRepository portfolioMonthlySummaryRepository;
    
    @Autowired
    private PortfolioService portfolioService;
    
    @Autowired
    private CashService cashService;

    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    @Autowired
    private StockDataService stockDataService;

    /**
     * Get all monthly summaries
     * 
     * @return List of all monthly summaries
     */
    public List<PortfolioMonthlySummary> getAllMonthlySummaries() {
        return portfolioMonthlySummaryRepository.findAll();
    }

    /**
     * Get monthly summary by year and month
     * 
     * @param year The year
     * @param month The month (1-12)
     * @return Optional containing the monthly summary if found
     */
    public Optional<PortfolioMonthlySummary> getMonthlySummary(Integer year, Integer month) {
        return portfolioMonthlySummaryRepository.findByYearAndMonth(year, month);
    }

    /**
     * Get all monthly summaries for a specific year
     * 
     * @param year The year
     * @return List of monthly summaries for the year
     */
    public List<PortfolioMonthlySummary> getMonthlySummariesByYear(Integer year) {
        return portfolioMonthlySummaryRepository.findByYearOrderByMonth(year);
    }

    /**
     * Get monthly summaries for the last 12 months
     * 
     * @return List of monthly summaries for the last 12 months
     */
    public List<PortfolioMonthlySummary> getLast12Months() {
        LocalDate now = DateUtil.getCurrentDateInEST();
        return portfolioMonthlySummaryRepository.findLast12Months(now.getYear(), now.getMonthValue());
    }

    /**
     * Get monthly summaries for the last year
     * 
     * @return List of monthly summaries for the last year
     */
    public List<PortfolioMonthlySummary> getLastYear() {
        LocalDate now = DateUtil.getCurrentDateInEST();
        int startYear = now.getYear() - 1;
        return portfolioMonthlySummaryRepository.findLastYear(startYear);
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
    public PortfolioMonthlySummary createOrUpdateMonthlySummary(Integer year, Integer month,
                                                               BigDecimal totalValue, BigDecimal previousMonthValue) {
        
        // Calculate monthly gain
        BigDecimal monthlyGain = totalValue.subtract(previousMonthValue);
        
        // Calculate monthly gain percentage
        BigDecimal monthlyGainPercentage = BigDecimal.ZERO;
        if (previousMonthValue.compareTo(BigDecimal.ZERO) > 0) {
            monthlyGainPercentage = monthlyGain.divide(previousMonthValue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        
        // Calculate realized/unrealized for this month
        YearMonth targetMonth = YearMonth.of(year, month);
        BigDecimal realizedForMonth = calculateMonthlyRealized(targetMonth);
        // If the month is before the current month, use month-end unrealized; otherwise use today's unrealized
        LocalDate today = DateUtil.getCurrentDateInEST();
        LocalDate asOfDate = targetMonth.isBefore(YearMonth.from(today)) ? targetMonth.atEndOfMonth() : today;
        BigDecimal unrealizedAsOf = calculateUnrealizedAsOf(asOfDate);

        // Check if summary already exists
        Optional<PortfolioMonthlySummary> existingSummary = portfolioMonthlySummaryRepository.findByYearAndMonth(year, month);
        
        PortfolioMonthlySummary summary;
        if (existingSummary.isPresent()) {
            // Update existing summary
            summary = existingSummary.get();
            summary.setTotalValue(totalValue);
            summary.setMonthlyGain(monthlyGain);
            summary.setMonthlyGainPercentage(monthlyGainPercentage);
            summary.setRealizedGain(realizedForMonth);
            summary.setUnrealizedGain(unrealizedAsOf);
        } else {
            // Create new summary
            summary = new PortfolioMonthlySummary(year, month, totalValue, monthlyGain, monthlyGainPercentage,
                    realizedForMonth, unrealizedAsOf);
        }
        
        return portfolioMonthlySummaryRepository.save(summary);
    }

    /**
     * Create monthly summary for current month
     * 
     * @return The created monthly summary
     */
    public PortfolioMonthlySummary createCurrentMonthSummary() {
        LocalDate now = DateUtil.getCurrentDateInEST();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        // Get current total portfolio value (investments + cash)
        BigDecimal currentTotalValue = portfolioService.getTotalPortfolioValue().add(cashService.getCashBalance());
        
        // Get previous month's value or use current value if no previous data
        BigDecimal previousMonthValue = getPreviousMonthValue(year, month);
        
        return createOrUpdateMonthlySummary(year, month, currentTotalValue, previousMonthValue);
    }

    /**
     * Get the total portfolio value for the previous month
     * 
     * @param year Current year
     * @param month Current month
     * @return Previous month's total value or current value if no previous data
     */
    private BigDecimal getPreviousMonthValue(Integer year, Integer month) {
        // Calculate previous month
        int previousYear = year;
        int previousMonth = month - 1;
        
        if (previousMonth == 0) {
            previousMonth = 12;
            previousYear = year - 1;
        }
        
        // Try to get previous month's summary
        Optional<PortfolioMonthlySummary> previousSummary = portfolioMonthlySummaryRepository.findByYearAndMonth(previousYear, previousMonth);
        
        if (previousSummary.isPresent()) {
            return previousSummary.get().getTotalValue();
        } else {
            // If no previous data, use current value (will result in 0 gain)
            return portfolioService.getTotalPortfolioValue().add(cashService.getCashBalance());
        }
    }

    /**
     * Delete monthly summary by year and month
     * 
     * @param year The year
     * @param month The month (1-12)
     * @return true if deleted, false if not found
     */
    public boolean deleteMonthlySummary(Integer year, Integer month) {
        Optional<PortfolioMonthlySummary> summary = portfolioMonthlySummaryRepository.findByYearAndMonth(year, month);
        if (summary.isPresent()) {
            portfolioMonthlySummaryRepository.delete(summary.get());
            return true;
        }
        return false;
    }

    /**
     * Clean up old monthly summaries (keep only last year)
     * 
     * @return Number of summaries deleted
     */
    public int cleanupOldSummaries() {
        LocalDate now = DateUtil.getCurrentDateInEST();
        int cutoffYear = now.getYear() - 1;
        
        List<PortfolioMonthlySummary> allSummaries = portfolioMonthlySummaryRepository.findAll();
        int deletedCount = 0;
        
        for (PortfolioMonthlySummary summary : allSummaries) {
            if (summary.getYear() < cutoffYear) {
                portfolioMonthlySummaryRepository.delete(summary);
                deletedCount++;
            }
        }
        
        return deletedCount;
    }

    /**
     * Delete all monthly summaries
     * 
     * @return Number of summaries deleted
     */
    public int deleteAllMonthlySummaries() {
        List<PortfolioMonthlySummary> allSummaries = portfolioMonthlySummaryRepository.findAll();
        int count = allSummaries.size();
        portfolioMonthlySummaryRepository.deleteAll();
        return count;
    }

    /**
     * Delete all daily records (records with month > 12)
     * 
     * @return Number of daily records deleted
     */
    public int deleteAllDailyRecords() {
        List<PortfolioMonthlySummary> allRecords = portfolioMonthlySummaryRepository.findAll();
        int deletedCount = 0;
        
        for (PortfolioMonthlySummary record : allRecords) {
            if (record.getMonth() > 12) {
                portfolioMonthlySummaryRepository.delete(record);
                deletedCount++;
            }
        }
        
        return deletedCount;
    }

    // ================= Helper calculations and scheduler =================

    private BigDecimal calculateMonthlyRealized(YearMonth yearMonth) {
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<TradeHistory> monthTrades = tradeHistoryRepository
                .findByTradeDateBetweenOrderByTradeDateDesc(monthStart, monthEnd);

        BigDecimal realizedGains = monthTrades.stream()
                .filter(trade -> trade.getTradeType() == TradeHistory.TradeType.SELL)
                .map(this::calculateRealizedGainForTrade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return realizedGains.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRealizedGainForTrade(TradeHistory sellTrade) {
        List<TradeHistory> buyTrades = tradeHistoryRepository
                .findByTickerAndTradeTypeAndTradeDateBeforeOrderByTradeDateAsc(
                        sellTrade.getTicker(), TradeHistory.TradeType.BUY, sellTrade.getTradeDate());

        if (buyTrades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalBuyValue = buyTrades.stream()
                .map(TradeHistory::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalBuyQuantity = buyTrades.stream()
                .mapToInt(TradeHistory::getQuantity)
                .sum();

        if (totalBuyQuantity == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal averageBuyPrice = totalBuyValue.divide(BigDecimal.valueOf(totalBuyQuantity), RoundingMode.HALF_UP);

        BigDecimal sellValue = sellTrade.getTotalValue();
        BigDecimal buyValue = averageBuyPrice.multiply(BigDecimal.valueOf(sellTrade.getQuantity()));
        return sellValue.subtract(buyValue);
    }

    private BigDecimal calculateUnrealizedAsOf(LocalDate asOfDate) {
        List<PortfolioItem> holdings = portfolioItemRepository.findAll();
        BigDecimal totalUnrealized = BigDecimal.ZERO;

        for (PortfolioItem holding : holdings) {
            if (holding.getBuyDate().isAfter(asOfDate)) {
                continue;
            }
            BigDecimal currentPrice = getCurrentPrice(holding.getTicker());
            BigDecimal buyPrice = holding.getBuyPrice();
            BigDecimal unrealized = currentPrice.subtract(buyPrice)
                    .multiply(BigDecimal.valueOf(holding.getQuantity()));
            totalUnrealized = totalUnrealized.add(unrealized);
        }

        return totalUnrealized.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getCurrentPrice(String ticker) {
        try {
            List<java.util.Map<String, Object>> stockData = stockDataService.getStockData(java.util.List.of(ticker));
            if (!stockData.isEmpty() && stockData.get(0).get("price") != null) {
                Object priceObj = stockData.get(0).get("price");
                if (priceObj instanceof Number) {
                    return BigDecimal.valueOf(((Number) priceObj).doubleValue());
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching current price for " + ticker + ": " + e.getMessage());
        }
        List<PortfolioItem> holdings = portfolioItemRepository.findByTicker(ticker);
        if (!holdings.isEmpty()) {
            return holdings.get(0).getBuyPrice();
        }
        return BigDecimal.ZERO;
    }

    // Run daily at 23:55 EST; if today is last day of month, write the monthly summary
    @Scheduled(cron = "0 55 23 * * *")
    public void scheduledMonthEndSummary() {
        LocalDate today = DateUtil.getCurrentDateInEST();
        YearMonth ym = YearMonth.from(today);
        if (!today.equals(ym.atEndOfMonth())) {
            return;
        }

        int year = ym.getYear();
        int month = ym.getMonthValue();
        if (portfolioMonthlySummaryRepository.existsByYearAndMonth(year, month)) {
            return;
        }

        BigDecimal totalValue = portfolioService.getTotalPortfolioValue().add(cashService.getCashBalance());
        BigDecimal previousMonthValue = getPreviousMonthValue(year, month);
        createOrUpdateMonthlySummary(year, month, totalValue, previousMonthValue);
    }
} 