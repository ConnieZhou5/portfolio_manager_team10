package com.portfolio.backend.service;

import com.portfolio.backend.dto.BuyRequest;
import com.portfolio.backend.dto.PortfolioItemResponse;
import com.portfolio.backend.dto.TradeHistoryRequest;
import com.portfolio.backend.dto.TradeHistoryResponse;
import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.model.TradeHistory;
import com.portfolio.backend.repository.PortfolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BuyService {

    @Autowired
    private CashService cashService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private TradeHistoryService tradeHistoryService;

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    /**
     * Execute a buy transaction
     * 
     * @param request The buy request
     * @return Map containing transaction result
     * @throws IllegalArgumentException if insufficient funds or invalid request
     */
    @Transactional
    public Map<String, Object> executeBuyTransaction(BuyRequest request) {
        // Validate request
        validateBuyRequest(request);

        // Calculate total cost
        BigDecimal totalCost = request.getTotalCost();

        // Check if user has sufficient cash
        BigDecimal currentCash = cashService.getCashBalance();
        if (currentCash.compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("Insufficient funds. Required: $" + totalCost + ", Available: $" + currentCash);
        }

        // Deduct cash
        boolean cashDeducted = cashService.subtractCash(totalCost);
        if (!cashDeducted) {
            throw new IllegalArgumentException("Insufficient funds. Required: $" + totalCost + ", Available: $" + currentCash);
        }

        // Add to portfolio (check if ticker already exists)
        PortfolioItem portfolioItem = addToPortfolio(request);

        // Record in trade history
        TradeHistoryResponse tradeRecord = recordTradeHistory(request);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Buy transaction completed successfully");
        response.put("totalCost", totalCost);
        response.put("remainingCash", cashService.getCashBalance());
        response.put("portfolioItem", convertToPortfolioResponse(portfolioItem));
        response.put("tradeRecord", tradeRecord);

        return response;
    }

    /**
     * Validate buy request
     * 
     * @param request The buy request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateBuyRequest(BuyRequest request) {
        if (request.getTicker() == null || request.getTicker().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (request.getTradeDate() == null) {
            throw new IllegalArgumentException("Trade date cannot be null");
        }
    }

    /**
     * Add or update portfolio item
     * 
     * @param request The buy request
     * @return The portfolio item
     */
    private PortfolioItem addToPortfolio(BuyRequest request) {
        // Check if ticker already exists in portfolio
        List<PortfolioItem> existingItems = portfolioItemRepository.findByTicker(request.getTicker());
        PortfolioItem existingItem = existingItems.isEmpty() ? null : existingItems.get(0);

        if (existingItem != null) {
            // Update existing position (average cost basis)
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            BigDecimal newTotalCost = existingItem.getTotalValue().add(request.getTotalCost());
            BigDecimal newAveragePrice = newTotalCost.divide(BigDecimal.valueOf(newQuantity), 2, java.math.RoundingMode.HALF_UP);

            existingItem.setQuantity(newQuantity);
            existingItem.setBuyPrice(newAveragePrice);
            existingItem.setBuyDate(request.getTradeDate()); // Update to most recent buy date

            return portfolioItemRepository.save(existingItem);
        } else {
            // Create new portfolio item
            PortfolioItem newItem = new PortfolioItem(
                    request.getTicker(),
                    request.getQuantity(),
                    request.getPrice(),
                    request.getTradeDate()
            );

            return portfolioItemRepository.save(newItem);
        }
    }

    /**
     * Record trade in trade history
     * 
     * @param request The buy request
     * @return The trade history response
     */
    private TradeHistoryResponse recordTradeHistory(BuyRequest request) {
        TradeHistoryRequest tradeRequest = new TradeHistoryRequest(
                request.getTradeDate(),
                request.getTicker(),
                request.getQuantity(),
                request.getPrice(),
                "BUY"
        );

        return tradeHistoryService.addTrade(tradeRequest);
    }

    /**
     * Convert PortfolioItem to PortfolioItemResponse
     * 
     * @param portfolioItem The portfolio item
     * @return The response DTO
     */
    private PortfolioItemResponse convertToPortfolioResponse(PortfolioItem portfolioItem) {
        return new PortfolioItemResponse(
                portfolioItem.getId(),
                portfolioItem.getTicker(),
                portfolioItem.getQuantity(),
                portfolioItem.getBuyPrice(),
                portfolioItem.getBuyDate(),
                portfolioItem.getTotalValue()
        );
    }
} 