package com.portfolio.backend.controller;

import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.repository.PortfolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    /**
     * GET /api/portfolio
     * Retrieves all portfolio holdings
     * 
     * @return List of all portfolio items
     */
    @GetMapping
    public ResponseEntity<List<PortfolioItem>> getAllPortfolioItems() {
        try {
            List<PortfolioItem> items = portfolioItemRepository.findAll();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST /api/portfolio
     * Adds a new portfolio holding
     * 
     * @param portfolioItem The portfolio item to add
     * @return The saved portfolio item with generated ID
     */
    @PostMapping
    public ResponseEntity<PortfolioItem> addPortfolioItem(@RequestBody PortfolioItem portfolioItem) {
        try {
            // Set creation timestamp if not already set
            if (portfolioItem.getCreatedAt() == null) {
                portfolioItem.setCreatedAt(LocalDateTime.now());
            }
            
            // Validate required fields
            if (portfolioItem.getTicker() == null || portfolioItem.getTicker().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            if (portfolioItem.getQuantity() == null || portfolioItem.getQuantity() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            if (portfolioItem.getBuyPrice() == null || portfolioItem.getBuyPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            PortfolioItem savedItem = portfolioItemRepository.save(portfolioItem);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
            
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
            Optional<PortfolioItem> existingItem = portfolioItemRepository.findById(id);
            
            if (existingItem.isPresent()) {
                portfolioItemRepository.deleteById(id);
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
     * @return The portfolio item or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioItem> getPortfolioItemById(@PathVariable Long id) {
        try {
            Optional<PortfolioItem> item = portfolioItemRepository.findById(id);
            
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
     * @param portfolioItem The updated portfolio item data
     * @return The updated portfolio item or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<PortfolioItem> updatePortfolioItem(@PathVariable Long id, @RequestBody PortfolioItem portfolioItem) {
        try {
            Optional<PortfolioItem> existingItem = portfolioItemRepository.findById(id);
            
            if (existingItem.isPresent()) {
                PortfolioItem itemToUpdate = existingItem.get();
                
                // Update fields if provided
                if (portfolioItem.getTicker() != null) {
                    itemToUpdate.setTicker(portfolioItem.getTicker());
                }
                if (portfolioItem.getQuantity() != null) {
                    itemToUpdate.setQuantity(portfolioItem.getQuantity());
                }
                if (portfolioItem.getBuyPrice() != null) {
                    itemToUpdate.setBuyPrice(portfolioItem.getBuyPrice());
                }
                if (portfolioItem.getBuyDate() != null) {
                    itemToUpdate.setBuyDate(portfolioItem.getBuyDate());
                }
                
                PortfolioItem updatedItem = portfolioItemRepository.save(itemToUpdate);
                return ResponseEntity.ok(updatedItem);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
