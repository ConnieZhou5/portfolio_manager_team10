package com.portfolio.backend.service;

import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.repository.PortfolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    /**
     * Get all portfolio items
     * 
     * @return List of all portfolio items
     */
    public List<PortfolioItem> getAllPortfolioItems() {
        return portfolioItemRepository.findAll();
    }

    /**
     * Get a portfolio item by ID
     * 
     * @param id The ID of the portfolio item
     * @return Optional containing the portfolio item if found
     */
    public Optional<PortfolioItem> getPortfolioItemById(Long id) {
        return portfolioItemRepository.findById(id);
    }

    /**
     * Add a new portfolio item with validation
     * 
     * @param portfolioItem The portfolio item to add
     * @return The saved portfolio item
     * @throws IllegalArgumentException if validation fails
     */
    public PortfolioItem addPortfolioItem(PortfolioItem portfolioItem) {
        validatePortfolioItem(portfolioItem);
        return portfolioItemRepository.save(portfolioItem);
    }

    /**
     * Update an existing portfolio item
     * 
     * @param id The ID of the portfolio item to update
     * @param portfolioItem The updated portfolio item data
     * @return The updated portfolio item
     * @throws IllegalArgumentException if item not found
     */
    public PortfolioItem updatePortfolioItem(Long id, PortfolioItem portfolioItem) {
        PortfolioItem existingItem = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio item not found with id: " + id));

        if (portfolioItem.getTicker() != null) {
            existingItem.setTicker(portfolioItem.getTicker());
        }
        if (portfolioItem.getQuantity() != null) {
            existingItem.setQuantity(portfolioItem.getQuantity());
        }
        if (portfolioItem.getBuyPrice() != null) {
            existingItem.setBuyPrice(portfolioItem.getBuyPrice());
        }
        if (portfolioItem.getBuyDate() != null) {
            existingItem.setBuyDate(portfolioItem.getBuyDate());
        }

        return portfolioItemRepository.save(existingItem);
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
     * Validate portfolio item data
     * 
     * @param portfolioItem The portfolio item to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePortfolioItem(PortfolioItem portfolioItem) {
        if (portfolioItem.getTicker() == null || portfolioItem.getTicker().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker is required and cannot be empty");
        }
        
        if (portfolioItem.getQuantity() == null || portfolioItem.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        if (portfolioItem.getBuyPrice() == null || portfolioItem.getBuyPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Buy price must be greater than 0");
        }
    }

    /**
     * Get portfolio items by ticker
     * 
     * @param ticker The ticker symbol
     * @return List of portfolio items with the given ticker
     */
    public List<PortfolioItem> getPortfolioItemsByTicker(String ticker) {
        return portfolioItemRepository.findByTicker(ticker);
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
} 