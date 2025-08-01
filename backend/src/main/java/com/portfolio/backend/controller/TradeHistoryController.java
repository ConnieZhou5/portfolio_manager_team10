package com.portfolio.backend.controller;

import com.portfolio.backend.dto.TradeHistoryRequest;
import com.portfolio.backend.dto.TradeHistoryResponse;
import com.portfolio.backend.service.TradeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trade-history")
@CrossOrigin(origins = "*")
public class TradeHistoryController {

    @Autowired
    private TradeHistoryService tradeHistoryService;

    /**
     * GET /api/trade-history
     * Retrieves all trade history records
     * 
     * @return List of all trade history responses
     */
    @GetMapping
    public ResponseEntity<List<TradeHistoryResponse>> getAllTradeHistory() {
        try {
            List<TradeHistoryResponse> trades = tradeHistoryService.getAllTradeHistory();
            return ResponseEntity.ok(trades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/trade-history/{id}
     * Retrieves a specific trade history record by ID
     * 
     * @param id The trade history ID
     * @return The trade history response or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TradeHistoryResponse> getTradeHistoryById(@PathVariable Long id) {
        try {
            Optional<TradeHistoryResponse> trade = tradeHistoryService.getTradeHistoryById(id);
            
            if (trade.isPresent()) {
                return ResponseEntity.ok(trade.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/trade-history/ticker/{ticker}
     * Retrieves all trades for a specific ticker
     * 
     * @param ticker The ticker symbol
     * @return List of trade history responses for the ticker
     */
    @GetMapping("/ticker/{ticker}")
    public ResponseEntity<List<TradeHistoryResponse>> getTradesByTicker(@PathVariable String ticker) {
        try {
            List<TradeHistoryResponse> trades = tradeHistoryService.getTradesByTicker(ticker);
            return ResponseEntity.ok(trades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/trade-history/type/{tradeType}
     * Retrieves all trades by trade type (BUY or SELL)
     * 
     * @param tradeType The trade type
     * @return List of trade history responses for the trade type
     */
    @GetMapping("/type/{tradeType}")
    public ResponseEntity<List<TradeHistoryResponse>> getTradesByType(@PathVariable String tradeType) {
        try {
            List<TradeHistoryResponse> trades = tradeHistoryService.getTradesByType(tradeType);
            return ResponseEntity.ok(trades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/trade-history
     * Adds a new trade record
     * 
     * @param request The trade history request
     * @return The saved trade history response
     */
    @PostMapping
    public ResponseEntity<TradeHistoryResponse> addTrade(@RequestBody TradeHistoryRequest request) {
        try {
            TradeHistoryResponse savedTrade = tradeHistoryService.addTrade(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTrade);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/trade-history/{id}
     * Updates an existing trade record
     * 
     * @param id The trade history ID
     * @param request The updated trade history request
     * @return The updated trade history response or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<TradeHistoryResponse> updateTrade(@PathVariable Long id, @RequestBody TradeHistoryRequest request) {
        try {
            TradeHistoryResponse updatedTrade = tradeHistoryService.updateTrade(id, request);
            return ResponseEntity.ok(updatedTrade);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/trade-history/{id}
     * Deletes a trade record by ID
     * 
     * @param id The trade history ID
     * @return ResponseEntity with appropriate status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrade(@PathVariable Long id) {
        try {
            boolean deleted = tradeHistoryService.deleteTrade(id);
            
            if (deleted) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 