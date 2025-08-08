package com.portfolio.backend.service;

import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.model.PortfolioMonthlySummary;
import com.portfolio.backend.model.TradeHistory;
import com.portfolio.backend.repository.PortfolioItemRepository;
import com.portfolio.backend.repository.PortfolioMonthlySummaryRepository;
import com.portfolio.backend.repository.TradeHistoryRepository;
import com.portfolio.backend.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class PnLService {

    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private PortfolioMonthlySummaryRepository portfolioMonthlySummaryRepository;

    /**
     * Calculate monthly P&L data for the last 7 months
     * 
     * @return Map containing monthly P&L data
     */
    public Map<String, Object> getMonthlyPnLData() {
        LocalDate today = DateUtil.getCurrentDateInNYC();
        LocalDate startDate = today.minusMonths(6).withDayOfMonth(1); // 7 months ago, start of month
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        // Generate data for each month from startDate to current month
        YearMonth current = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(today);
        
        while (!current.isAfter(end)) {
            Map<String, Object> monthData = calculateMonthlyPnL(current);
            monthlyData.add(monthData);
            current = current.plusMonths(1);
        }
        
        // Get current month's unrealized gains from the chart data
        double currentMonthUnrealized = 0.0;
        if (!monthlyData.isEmpty()) {
            Map<String, Object> currentMonthData = monthlyData.get(monthlyData.size() - 1);
            currentMonthUnrealized = (Double) currentMonthData.get("unrealized");
        }
        
        result.put("monthlyData", monthlyData);
        result.put("totalRealized", calculateTotalRealizedGains());
        result.put("totalUnrealized", currentMonthUnrealized);
        result.put("totalPnL", calculateTotalPnL());
        
        return result;
    }

    /**
     * Calculate P&L for a specific month
     * 
     * @param yearMonth The year and month to calculate for
     * @return Map containing month's P&L data
     */
    private Map<String, Object> calculateMonthlyPnL(YearMonth yearMonth) {
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        YearMonth currentMonth = YearMonth.from(DateUtil.getCurrentDateInNYC());

        BigDecimal realizedGains;
        BigDecimal unrealizedGains;

        if (yearMonth.isBefore(currentMonth)) {
            // Past months: use stored monthly summary
            Optional<PortfolioMonthlySummary> summaryOpt = portfolioMonthlySummaryRepository
                    .findByYearAndMonth(yearMonth.getYear(), yearMonth.getMonthValue());
            if (summaryOpt.isPresent()) {
                PortfolioMonthlySummary summary = summaryOpt.get();
                realizedGains = summary.getRealizedGain() != null ? summary.getRealizedGain() : BigDecimal.ZERO;
                unrealizedGains = summary.getUnrealizedGain() != null ? summary.getUnrealizedGain() : BigDecimal.ZERO;
            } else {
                // Fallback to on-the-fly computation if summary missing
                List<TradeHistory> monthTrades = tradeHistoryRepository
                        .findByTradeDateBetweenOrderByTradeDateDesc(monthStart, monthEnd);
                realizedGains = monthTrades.stream()
                        .filter(trade -> trade.getTradeType() == TradeHistory.TradeType.SELL)
                        .map(this::calculateRealizedGainForTrade)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                // Use as-of month end (note: uses current prices due to lack of historical price store)
                unrealizedGains = calculateUnrealizedGainsAsOf(monthEnd);
            }
        } else {
            // Current month: compute live
            List<TradeHistory> monthTrades = tradeHistoryRepository
                    .findByTradeDateBetweenOrderByTradeDateDesc(monthStart, DateUtil.getCurrentDateInNYC());
            realizedGains = monthTrades.stream()
                    .filter(trade -> trade.getTradeType() == TradeHistory.TradeType.SELL)
                    .map(this::calculateRealizedGainForTrade)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            unrealizedGains = calculateUnrealizedGainsAsOf(DateUtil.getCurrentDateInNYC());
        }

        Map<String, Object> monthData = new HashMap<>();
        monthData.put("month", yearMonth.getMonth().toString().substring(0, 3).toUpperCase());
        monthData.put("realized", realizedGains.doubleValue());
        monthData.put("unrealized", unrealizedGains.doubleValue());

        return monthData;
    }

    /**
     * Calculate realized gain for a specific SELL trade
     * 
     * @param sellTrade The SELL trade
     * @return Realized gain amount
     */
    private BigDecimal calculateRealizedGainForTrade(TradeHistory sellTrade) {
        // Find corresponding BUY trades for this ticker before the sell date
        List<TradeHistory> buyTrades = tradeHistoryRepository.findByTickerAndTradeTypeAndTradeDateBeforeOrderByTradeDateAsc(
                sellTrade.getTicker(), 
                TradeHistory.TradeType.BUY, 
                sellTrade.getTradeDate()
        );
        
        if (buyTrades.isEmpty()) {
            return BigDecimal.ZERO; // No buy trades found
        }
        
        // Calculate average buy price
        BigDecimal totalBuyValue = buyTrades.stream()
                .map(TradeHistory::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int totalBuyQuantity = buyTrades.stream()
                .mapToInt(TradeHistory::getQuantity)
                .sum();
        
        if (totalBuyQuantity == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal averageBuyPrice = totalBuyValue.divide(BigDecimal.valueOf(totalBuyQuantity), java.math.RoundingMode.HALF_UP);
        
        // Calculate realized gain
        BigDecimal sellValue = sellTrade.getTotalValue();
        BigDecimal buyValue = averageBuyPrice.multiply(BigDecimal.valueOf(sellTrade.getQuantity()));
        BigDecimal realizedGain = sellValue.subtract(buyValue);
        
        return realizedGain;
    }

    /**
     * Calculate unrealized gains as of a specific date
     * 
     * @param asOfDate The date to calculate unrealized gains as of
     * @return Unrealized gains amount
     */
    private BigDecimal calculateUnrealizedGainsAsOf(LocalDate asOfDate) {
        // Get all portfolio items that existed as of the given date
        List<PortfolioItem> holdings = portfolioItemRepository.findAll();
        
        BigDecimal totalUnrealized = BigDecimal.ZERO;
        
        for (PortfolioItem holding : holdings) {
            // Only include holdings that were bought before or on the asOfDate
            if (holding.getBuyDate().isBefore(asOfDate) || holding.getBuyDate().isEqual(asOfDate)) {
                // Get current market price
                BigDecimal currentPrice = getCurrentPrice(holding.getTicker());
                BigDecimal buyPrice = holding.getBuyPrice();
                BigDecimal unrealizedGain = currentPrice.subtract(buyPrice)
                        .multiply(BigDecimal.valueOf(holding.getQuantity()));
                totalUnrealized = totalUnrealized.add(unrealizedGain);
            }
        }
        
        return totalUnrealized;
    }

    /**
     * Get current price for a ticker using stock data service
     * 
     * @param ticker The stock ticker
     * @return Current price
     */
    private BigDecimal getCurrentPrice(String ticker) {
        try {
            // Get current stock data from the stock data service
            List<Map<String, Object>> stockData = stockDataService.getStockData(List.of(ticker));
            
            if (!stockData.isEmpty() && stockData.get(0).get("price") != null) {
                Object priceObj = stockData.get(0).get("price");
                if (priceObj instanceof Number) {
                    return BigDecimal.valueOf(((Number) priceObj).doubleValue());
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the entire calculation
            System.err.println("Error fetching current price for " + ticker + ": " + e.getMessage());
        }
        
        // Fallback to buy price if stock data service fails
        List<PortfolioItem> holdings = portfolioItemRepository.findByTicker(ticker);
        if (!holdings.isEmpty()) {
            return holdings.get(0).getBuyPrice();
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calculate total realized gains (sum of all historical realized gains from monthly summaries)
     * 
     * @return Total realized gains
     */
    private BigDecimal calculateTotalRealizedGains() {
        LocalDate today = DateUtil.getCurrentDateInNYC();
        int currentYear = today.getYear();
        
        // Get all monthly summaries for the current year
        List<PortfolioMonthlySummary> monthlySummaries = portfolioMonthlySummaryRepository.findByYearOrderByMonth(currentYear);
        
        BigDecimal totalRealized = BigDecimal.ZERO;
        
        // Sum realized gains from all monthly summaries
        for (PortfolioMonthlySummary summary : monthlySummaries) {
            if (summary.getRealizedGain() != null) {
                totalRealized = totalRealized.add(summary.getRealizedGain());
            }
        }
        
        // Add current month's realized gains from trade history (since it might not be in summary yet)
        YearMonth currentMonth = YearMonth.from(today);
        LocalDate monthStart = currentMonth.atDay(1);
        List<TradeHistory> currentMonthTrades = tradeHistoryRepository
                .findByTradeDateBetweenOrderByTradeDateDesc(monthStart, today);
        
        BigDecimal currentMonthRealized = currentMonthTrades.stream()
                .filter(trade -> trade.getTradeType() == TradeHistory.TradeType.SELL)
                .map(this::calculateRealizedGainForTrade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        totalRealized = totalRealized.add(currentMonthRealized);
        
        return totalRealized.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculate total unrealized gains
     * 
     * @return Total unrealized gains
     */
    private BigDecimal calculateTotalUnrealizedGains() {
        return calculateUnrealizedGainsAsOf(DateUtil.getCurrentDateInNYC()).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Calculate total P&L
     * 
     * @return Total P&L
     */
    private BigDecimal calculateTotalPnL() {
        return calculateTotalRealizedGains().add(calculateTotalUnrealizedGains()).setScale(2, java.math.RoundingMode.HALF_UP);
    }
} 