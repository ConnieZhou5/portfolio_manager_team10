package com.portfolio.backend.controller;

import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    /**
     * GET /api/portfolio
     * Retrieves all portfolio holdings
     * 
     * @return List of all portfolio items
     */
    @GetMapping
    public ResponseEntity<List<PortfolioItem>> getAllPortfolioItems() {
        try {
            List<PortfolioItem> items = portfolioService.getAllPortfolioItems();
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
            PortfolioItem savedItem = portfolioService.addPortfolioItem(portfolioItem);
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
     * @return The portfolio item or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioItem> getPortfolioItemById(@PathVariable Long id) {
        try {
            Optional<PortfolioItem> item = portfolioService.getPortfolioItemById(id);
            
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
            PortfolioItem updatedItem = portfolioService.updatePortfolioItem(id, portfolioItem);
            return ResponseEntity.ok(updatedItem);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
