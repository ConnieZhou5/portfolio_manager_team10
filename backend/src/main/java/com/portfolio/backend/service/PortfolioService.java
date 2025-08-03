package com.portfolio.backend.service;

import com.portfolio.backend.dto.PortfolioItemRequest;
import com.portfolio.backend.dto.PortfolioItemResponse;
import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.model.PortfolioDailyValue;
import com.portfolio.backend.repository.PortfolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;
    
    @Autowired
    private CashService cashService;
    
    @Autowired
    private PortfolioDailyValueService portfolioDailyValueService;

    /**
     * Get all portfolio items
     * 
     * @return List of all portfolio item responses
     */
    public List<PortfolioItemResponse> getAllPortfolioItems() {
        return portfolioItemRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a portfolio item by ID
     * 
     * @param id The ID of the portfolio item
     * @return Optional containing the portfolio item response if found
     */
    public Optional<PortfolioItemResponse> getPortfolioItemById(Long id) {
        return portfolioItemRepository.findById(id)
                .map(this::convertToResponse);
    }

    /**
     * Add a new portfolio item with validation
     * 
     * @param request The portfolio item request
     * @return The saved portfolio item response
     * @throws IllegalArgumentException if validation fails
     */
    public PortfolioItemResponse addPortfolioItem(PortfolioItemRequest request) {
        validatePortfolioItemRequest(request);
        PortfolioItem portfolioItem = convertToEntity(request);
        PortfolioItem savedItem = portfolioItemRepository.save(portfolioItem);
        return convertToResponse(savedItem);
    }

    /**
     * Update an existing portfolio item
     * 
     * @param id The ID of the portfolio item to update
     * @param portfolioItem The updated portfolio item data
     * @return The updated portfolio item
     * @throws IllegalArgumentException if item not found
     */
    public PortfolioItemResponse updatePortfolioItem(Long id, PortfolioItemRequest request) {
        PortfolioItem existingItem = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio item not found with id: " + id));

        // Update fields if provided
        if (request.getTicker() != null) {
            existingItem.setTicker(request.getTicker());
        }
        if (request.getQuantity() != null) {
            existingItem.setQuantity(request.getQuantity());
        }
        if (request.getBuyPrice() != null) {
            existingItem.setBuyPrice(request.getBuyPrice());
        }
        if (request.getBuyDate() != null) {
            existingItem.setBuyDate(request.getBuyDate());
        }

        PortfolioItem updatedItem = portfolioItemRepository.save(existingItem);
        return convertToResponse(updatedItem);
    }

    /**
     * Delete a portfolio item
     * 
     * @param id The ID of the portfolio item to delete
     * @return true if deleted, false if not found
     */
    public boolean deletePortfolioItem(Long id) {
        if (portfolioItemRepository.existsById(id)) {
            portfolioItemRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Validate portfolio item request data
     * 
     * @param request The portfolio item request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePortfolioItemRequest(PortfolioItemRequest request) {
        if (request.getTicker() == null || request.getTicker().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker is required and cannot be empty");
        }
        
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        if (request.getBuyPrice() == null || request.getBuyPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Buy price must be greater than 0");
        }
    }

    /**
     * Get portfolio items by ticker
     * 
     * @param ticker The ticker symbol
     * @return List of portfolio item responses with the given ticker
     */
    public List<PortfolioItemResponse> getPortfolioItemsByTicker(String ticker) {
        return portfolioItemRepository.findByTicker(ticker)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get total portfolio value
     * 
     * @return Total value of all portfolio items
     */
    public BigDecimal getTotalPortfolioValue() {
        return portfolioItemRepository.getTotalPortfolioValue()
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get total value by ticker
     * 
     * @param ticker The ticker symbol
     * @return Total value of portfolio items with the given ticker
     */
    public BigDecimal getTotalValueByTicker(String ticker) {
        return portfolioItemRepository.getTotalValueByTicker(ticker)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get portfolio statistics including total assets, investments, day's gain, and cash
     * 
     * @return Map containing portfolio statistics
     */
    public Map<String, Object> getPortfolioStats() {
        Map<String, Object> stats = new HashMap<>();
        
        BigDecimal cash = cashService.getCashBalance();
        BigDecimal totalPortfolioValue = getTotalPortfolioValue(); // investments
        BigDecimal totalAssets = totalPortfolioValue.add(cash);
                
        // Calculate day's gain
        LocalDate today = LocalDate.now();
        BigDecimal daysGain = BigDecimal.ZERO;
        String daysGainPercentage = "0.00%";
        
        // Get previous day's portfolio value
        try {
            Optional<PortfolioDailyValue> previousDayValue = portfolioDailyValueService.getMostRecentBeforeDate(today);
            
            if (previousDayValue.isPresent()) {
                BigDecimal previousTotalValue = previousDayValue.get().getTotalValue();
                
                // Calculate day's gain
                daysGain = totalAssets.subtract(previousTotalValue);
                
                // Calculate percentage
                if (previousTotalValue.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentage = daysGain.divide(previousTotalValue, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    daysGainPercentage = percentage.setScale(2, java.math.RoundingMode.HALF_UP) + "%";
                }
            }
        } catch (Exception e) {
            System.err.println("Exception in getMostRecentBeforeDate: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Format values for display
        stats.put("totalAssets", formatCurrency(totalAssets));
        stats.put("investments", formatCurrency(totalPortfolioValue));
        stats.put("daysGain", formatCurrency(daysGain));
        stats.put("daysGainPercentage", daysGainPercentage);
        stats.put("cash", formatCurrency(cash));
        
        return stats;
    }
    
    /**
     * Format BigDecimal to currency string
     * 
     * @param amount The amount to format
     * @return Formatted currency string
     */
    private String formatCurrency(BigDecimal amount) {
        return "$" + amount.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }
    
    /**
     * Convert PortfolioItem entity to PortfolioItemResponse DTO
     * 
     * @param portfolioItem The entity to convert
     * @return The response DTO
     */
    private PortfolioItemResponse convertToResponse(PortfolioItem portfolioItem) {
        return new PortfolioItemResponse(
                portfolioItem.getId(),
                portfolioItem.getTicker(),
                portfolioItem.getQuantity(),
                portfolioItem.getBuyPrice(),
                portfolioItem.getBuyDate(),
                portfolioItem.getTotalValue()
        );
    }
    
    /**
     * Convert PortfolioItemRequest DTO to PortfolioItem entity
     * 
     * @param request The request DTO to convert
     * @return The entity
     */
    private PortfolioItem convertToEntity(PortfolioItemRequest request) {
        PortfolioItem portfolioItem = new PortfolioItem();
        portfolioItem.setTicker(request.getTicker());
        portfolioItem.setQuantity(request.getQuantity());
        portfolioItem.setBuyPrice(request.getBuyPrice());
        portfolioItem.setBuyDate(request.getBuyDate());
        return portfolioItem;
    }
} 