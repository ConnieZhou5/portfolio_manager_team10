package com.portfolio.backend.controller;

import com.portfolio.backend.service.StockDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stock-data")
@CrossOrigin(origins = "*")
public class StockDataController {

    @Autowired
    private StockDataService stockDataService;

    /**
     * POST /api/stock-data
     * Fetches real-time stock data for given symbols
     * 
     * @param request Map containing list of symbols
     * @return List of stock data maps
     */
    @PostMapping
    public ResponseEntity<List<Map<String, Object>>> getStockData(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> symbols = (List<String>) request.get("symbols");
            List<Map<String, Object>> stockData = stockDataService.getStockData(symbols);
            return ResponseEntity.ok(stockData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 