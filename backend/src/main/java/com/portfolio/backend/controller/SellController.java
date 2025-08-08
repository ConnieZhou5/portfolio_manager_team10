package com.portfolio.backend.controller;

import com.portfolio.backend.dto.SellRequest;
import com.portfolio.backend.service.SellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import com.portfolio.backend.util.DateUtil;

@RestController
@RequestMapping("/api/sell")
@CrossOrigin(origins = "*")
public class SellController {

    @Autowired
    private SellService sellService;

    /**
     * POST /api/sell
     * Execute a sell transaction
     * 
     * @param request The sell request
     * @return ResponseEntity with transaction result
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> executeSell(@RequestBody SellRequest request) {
        try {
            // Set trade date to today in EST timezone if not provided
            if (request.getTradeDate() == null) {
                request.setTradeDate(DateUtil.getCurrentDateInNYC());
            }

            Map<String, Object> result = sellService.executeSellTransaction(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "error", "An unexpected error occurred: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 