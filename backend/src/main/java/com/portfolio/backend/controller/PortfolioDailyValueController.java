package com.portfolio.backend.controller;

import com.portfolio.backend.service.PortfolioDailyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/daily-values")
@CrossOrigin(origins = "*")
public class PortfolioDailyValueController {

    @Autowired
    private PortfolioDailyValueService portfolioDailyValueService;

    /**
     * POST /api/daily-values/save-snapshot
     * Saves a daily portfolio snapshot for testing
     * 
     * @return ResponseEntity with success message
     */
    @PostMapping("/save-snapshot")
    public ResponseEntity<Map<String, String>> saveDailySnapshot() {
        try {
            portfolioDailyValueService.saveTodaySnapshot();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Daily snapshot saved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 