package com.portfolio.backend.service;

import com.portfolio.backend.dto.SellRequest;
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
public class SellService {

    @Autowired
    private CashService cashService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private TradeHistoryService tradeHistoryService;

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    /**
     * Execute a sell transaction
     * 
     * @param request The sell request
     * @return Map containing transaction result
     * @throws IllegalArgumentException if insufficient shares or invalid request
     */
    @Transactional
    public Map<String, Object> executeSellTransaction(SellRequest request) {
        // Validate request
        validateSellRequest(request);

        // Check if user has sufficient shares
        List<PortfolioItem> holdings = portfolioItemRepository.findByTicker(request.getTicker());
        if (holdings.isEmpty()) {
            throw new IllegalArgumentException("No holdings found for ticker: " + request.getTicker());
        }

        PortfolioItem holding = holdings.get(0);
        if (holding.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient shares. Available: " + holding.getQuantity() + ", Requested: " + request.getQuantity());
        }

        // Calculate total proceeds
        BigDecimal totalProceeds = request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // Add cash from sale
        cashService.addCash(totalProceeds);

        // Update portfolio (reduce quantity or remove if all sold)
        PortfolioItem updatedHolding = updatePortfolio(request, holding);

        // Record in trade history
        TradeHistoryResponse tradeRecord = recordTradeHistory(request);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sell transaction completed successfully");
        response.put("totalProceeds", totalProceeds);
        response.put("remainingCash", cashService.getCashBalance());
        response.put("portfolioItem", convertToPortfolioResponse(updatedHolding));
        response.put("tradeRecord", tradeRecord);

        return response;
    }

    /**
     * Validate sell request
     * 
     * @param request The sell request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSellRequest(SellRequest request) {
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
     * Update portfolio after sell
     * 
     * @param request The sell request
     * @param holding The current holding
     * @return The updated portfolio item
     */
    private PortfolioItem updatePortfolio(SellRequest request, PortfolioItem holding) {
        int newQuantity = holding.getQuantity() - request.getQuantity();
        
        if (newQuantity == 0) {
            // Remove the holding if all shares are sold
            portfolioItemRepository.delete(holding);
            return null;
        } else {
            // Update quantity
            holding.setQuantity(newQuantity);
            return portfolioItemRepository.save(holding);
        }
    }

    /**
     * Record trade in trade history
     * 
     * @param request The sell request
     * @return The trade history response
     */
    private TradeHistoryResponse recordTradeHistory(SellRequest request) {
        TradeHistoryRequest tradeRequest = new TradeHistoryRequest(
                request.getTradeDate(),
                request.getTicker(),
                request.getQuantity(),
                request.getPrice(),
                "SELL"
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
        if (portfolioItem == null) {
            return null;
        }
        
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