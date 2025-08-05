// src/main/java/com/yourapp/controller/AIAnalysisController.java
package com.portfolio.backend.controller;

import com.portfolio.backend.service.AIAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "http://localhost:3000") // Or your React app's domain
public class AIAnalysisController {

    @Autowired
    private AIAnalysisService aiAnalysisService;

    @GetMapping("/{symbol}")
    public Map<String, Object> getAnalysis(@PathVariable String symbol) {
        return aiAnalysisService.getAnalysis(symbol);
    }
}
