package com.portfolio.backend.service;

import com.portfolio.backend.dto.TradeHistoryRequest;
import com.portfolio.backend.dto.TradeHistoryResponse;
import com.portfolio.backend.model.TradeHistory;
import com.portfolio.backend.repository.TradeHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TradeHistoryService {

    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    /**
     * Get all trade history records
     * 
     * @return List of all trade history responses
     */
    public List<TradeHistoryResponse> getAllTradeHistory() {
        return tradeHistoryRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get trade history by ID
     * 
     * @param id The trade history ID
     * @return Optional containing the trade history response if found
     */
    public Optional<TradeHistoryResponse> getTradeHistoryById(Long id) {
        return tradeHistoryRepository.findById(id)
                .map(this::convertToResponse);
    }

    /**
     * Get all trades for a specific ticker
     * 
     * @param ticker The ticker symbol
     * @return List of trade history responses for the ticker
     */
    public List<TradeHistoryResponse> getTradesByTicker(String ticker) {
        return tradeHistoryRepository.findByTickerOrderByTradeDateDesc(ticker)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all trades by trade type
     * 
     * @param tradeType The trade type (BUY or SELL)
     * @return List of trade history responses for the trade type
     */
    public List<TradeHistoryResponse> getTradesByType(String tradeType) {
        TradeHistory.TradeType type = TradeHistory.TradeType.valueOf(tradeType.toUpperCase());
        return tradeHistoryRepository.findByTradeTypeOrderByTradeDateDesc(type)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Add a new trade record
     * 
     * @param request The trade history request
     * @return The saved trade history response
     * @throws IllegalArgumentException if validation fails
     */
    public TradeHistoryResponse addTrade(TradeHistoryRequest request) {
        validateTradeRequest(request);
        
        TradeHistory trade = convertToEntity(request);
        TradeHistory savedTrade = tradeHistoryRepository.save(trade);
        return convertToResponse(savedTrade);
    }

    /**
     * Update an existing trade record
     * 
     * @param id The trade history ID
     * @param request The updated trade history request
     * @return The updated trade history response
     * @throws IllegalArgumentException if trade not found
     */
    public TradeHistoryResponse updateTrade(Long id, TradeHistoryRequest request) {
        TradeHistory existingTrade = tradeHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found with id: " + id));

        validateTradeRequest(request);

        // Update fields
        existingTrade.setTradeDate(request.getTradeDate());
        existingTrade.setTicker(request.getTicker());
        existingTrade.setQuantity(request.getQuantity());
        existingTrade.setPrice(request.getPrice());
        existingTrade.setTradeType(TradeHistory.TradeType.valueOf(request.getTradeType().toUpperCase()));

        TradeHistory updatedTrade = tradeHistoryRepository.save(existingTrade);
        return convertToResponse(updatedTrade);
    }

    /**
     * Delete a trade record
     * 
     * @param id The trade history ID
     * @return true if deleted, false if not found
     */
    public boolean deleteTrade(Long id) {
        if (tradeHistoryRepository.existsById(id)) {
            tradeHistoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Validate trade request
     * 
     * @param request The trade history request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateTradeRequest(TradeHistoryRequest request) {
        if (request.getTicker() == null || request.getTicker().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker cannot be null or empty");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (request.getTradeDate() == null) {
            throw new IllegalArgumentException("Trade date cannot be null");
        }
        if (request.getTradeType() == null || request.getTradeType().trim().isEmpty()) {
            throw new IllegalArgumentException("Trade type cannot be null or empty");
        }
        try {
            TradeHistory.TradeType.valueOf(request.getTradeType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trade type must be 'BUY' or 'SELL'");
        }
    }

    /**
     * Convert TradeHistory entity to TradeHistoryResponse DTO
     * 
     * @param trade The entity to convert
     * @return The response DTO
     */
    private TradeHistoryResponse convertToResponse(TradeHistory trade) {
        return new TradeHistoryResponse(
                trade.getId(),
                trade.getTradeDate(),
                trade.getTicker(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getTradeType().toString(),
                trade.getTotalValue()
        );
    }

    /**
     * Convert TradeHistoryRequest DTO to TradeHistory entity
     * 
     * @param request The request DTO to convert
     * @return The entity
     */
    private TradeHistory convertToEntity(TradeHistoryRequest request) {
        return new TradeHistory(
                request.getTradeDate(),
                request.getTicker(),
                request.getQuantity(),
                request.getPrice(),
                TradeHistory.TradeType.valueOf(request.getTradeType().toUpperCase())
        );
    }
} 