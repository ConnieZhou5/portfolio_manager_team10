package com.portfolio.backend.controller;

import com.portfolio.backend.service.PnLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pnl")
@CrossOrigin(origins = "*")
public class PnLController {

    @Autowired
    private PnLService pnLService;

    /**
     * GET /api/pnl/monthly
     * Get monthly P&L data for the last 7 months
     * 
     * @return Map containing monthly P&L data
     */
    @GetMapping("/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyPnLData() {
        try {
            Map<String, Object> pnLData = pnLService.getMonthlyPnLData();
            return ResponseEntity.ok(pnLData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 