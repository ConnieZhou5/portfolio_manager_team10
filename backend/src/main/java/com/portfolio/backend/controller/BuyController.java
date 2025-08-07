package com.portfolio.backend.controller;

import com.portfolio.backend.dto.BuyRequest;
import com.portfolio.backend.service.BuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import com.portfolio.backend.util.DateUtil;

@RestController
@RequestMapping("/api/buy")
@CrossOrigin(origins = "*")
public class BuyController {

    @Autowired
    private BuyService buyService;

    /**
     * POST /api/buy
     * Execute a buy transaction
     * 
     * @param request The buy request
     * @return ResponseEntity with transaction result
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> executeBuy(@RequestBody BuyRequest request) {
        try {
            // Set trade date to today in EST timezone if not provided
            if (request.getTradeDate() == null) {
                request.setTradeDate(DateUtil.getCurrentDateInEST());
            }

            Map<String, Object> result = buyService.executeBuyTransaction(request);
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