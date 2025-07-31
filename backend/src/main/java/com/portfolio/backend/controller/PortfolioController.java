package com.portfolio.backend.controller;

import com.portfolio.backend.dto.PortfolioItemRequest;
import com.portfolio.backend.dto.PortfolioItemResponse;
import com.portfolio.backend.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    /**
     * GET /api/portfolio
     * Retrieves all portfolio holdings
     * 
     * @return List of all portfolio item responses
     */
    @GetMapping
    public ResponseEntity<List<PortfolioItemResponse>> getAllPortfolioItems() {
        try {
            List<PortfolioItemResponse> items = portfolioService.getAllPortfolioItems();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/portfolio
     * Adds a new portfolio holding
     * 
     * @param request The portfolio item request
     * @return The saved portfolio item response with generated ID
     */
    @PostMapping
    public ResponseEntity<PortfolioItemResponse> addPortfolioItem(@RequestBody PortfolioItemRequest request) {
        try {
            PortfolioItemResponse savedItem = portfolioService.addPortfolioItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /api/portfolio/{id}
     * Deletes a portfolio holding by ID
     * 
     * @param id The ID of the portfolio item to delete
     * @return ResponseEntity with appropriate status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolioItem(@PathVariable Long id) {
        try {
            boolean deleted = portfolioService.deletePortfolioItem(id);
            
            if (deleted) {
                return ResponseEntity.noContent().build(); // 204 No Content
            } else {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/portfolio/{id}
     * Retrieves a specific portfolio holding by ID
     * 
     * @param id The ID of the portfolio item to retrieve
     * @return The portfolio item response or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioItemResponse> getPortfolioItemById(@PathVariable Long id) {
        try {
            Optional<PortfolioItemResponse> item = portfolioService.getPortfolioItemById(id);
            
            if (item.isPresent()) {
                return ResponseEntity.ok(item.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/portfolio/{id}
     * Updates an existing portfolio holding
     * 
     * @param id The ID of the portfolio item to update
     * @param request The updated portfolio item request
     * @return The updated portfolio item response or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<PortfolioItemResponse> updatePortfolioItem(@PathVariable Long id, @RequestBody PortfolioItemRequest request) {
        try {
            PortfolioItemResponse updatedItem = portfolioService.updatePortfolioItem(id, request);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/portfolio/stats
     * Retrieves portfolio statistics including total assets, investments, day's gain, and cash
     * 
     * @return Map containing portfolio statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getPortfolioStats() {
        try {
            Map<String, Object> stats = portfolioService.getPortfolioStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
