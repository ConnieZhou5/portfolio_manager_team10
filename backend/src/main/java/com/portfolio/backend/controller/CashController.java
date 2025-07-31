package com.portfolio.backend.controller;

import com.portfolio.backend.service.CashService;
import com.portfolio.backend.model.CashAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/cash")
@CrossOrigin(origins = "*")
public class CashController {

    @Autowired
    private CashService cashService;

    /**
     * GET /api/cash
     * Gets the current cash balance
     * 
     * @return Map containing cash balance
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCashBalance() {
        try {
            BigDecimal balance = cashService.getCashBalance();
            Map<String, Object> response = new HashMap<>();
            response.put("balance", balance);
            response.put("formattedBalance", "$" + balance.setScale(2, java.math.RoundingMode.HALF_UP));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/cash/add
     * Adds cash to the account
     * 
     * @param request Map containing "amount" field
     * @return Map containing updated cash balance
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCash(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            CashAccount updatedAccount = cashService.addCash(amount);
            
            Map<String, Object> response = new HashMap<>();
            response.put("balance", updatedAccount.getBalance());
            response.put("formattedBalance", "$" + updatedAccount.getBalance().setScale(2, java.math.RoundingMode.HALF_UP));
            response.put("message", "Cash added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * POST /api/cash/initialize
     * Initializes cash account with initial balance
     * 
     * @param request Map containing "initialBalance" field
     * @return Map containing cash account details
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeCashAccount(@RequestBody Map<String, Object> request) {
        try {
            BigDecimal initialBalance = new BigDecimal(request.get("initialBalance").toString());
            CashAccount cashAccount = cashService.initializeCashAccount(initialBalance);
            
            Map<String, Object> response = new HashMap<>();
            response.put("balance", cashAccount.getBalance());
            response.put("formattedBalance", "$" + cashAccount.getBalance().setScale(2, java.math.RoundingMode.HALF_UP));
            response.put("message", "Cash account initialized successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 