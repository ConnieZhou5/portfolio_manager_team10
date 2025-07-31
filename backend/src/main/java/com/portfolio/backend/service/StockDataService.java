package com.portfolio.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StockDataService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public StockDataService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch stock data for given symbols
     * 
     * @param symbols List of stock symbols
     * @return List of stock data maps
     */
    public List<Map<String, Object>> getStockData(List<String> symbols) {
        List<Map<String, Object>> responses = new ArrayList<>();
        
        for (String symbol : symbols) {
            try {
                Map<String, Object> stockData = fetchStockData(symbol);
                responses.add(stockData);
            } catch (Exception e) {
                Map<String, Object> errorData = new HashMap<>();
                errorData.put("symbol", symbol);
                errorData.put("name", null);
                errorData.put("price", null);
                errorData.put("currency", null);
                errorData.put("marketCap", null);
                errorData.put("error", "Failed to fetch data: " + e.getMessage());
                responses.add(errorData);
            }
        }
        
        return responses;
    }

    /**
     * Fetch stock data for a single symbol
     * 
     * @param symbol Stock symbol
     * @return Map containing stock data
     */
    private Map<String, Object> fetchStockData(String symbol) {
        String url = "/v8/finance/chart/" + symbol.toUpperCase() + "?interval=1d&range=1d";
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseStockData)
                .onErrorReturn(createErrorResponse(symbol))
                .block();
    }

    /**
     * Parse the JSON response from Yahoo Finance API
     * 
     * @param jsonResponse JSON response string
     * @return Map containing stock data
     */
    private Map<String, Object> parseStockData(String jsonResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode chartNode = rootNode.get("chart");
            
            if (chartNode == null) {
                return createErrorResponse("UNKNOWN");
            }
            
            JsonNode errorNode = chartNode.get("error");
            if (errorNode != null && !errorNode.isNull()) {
                return createErrorResponse("UNKNOWN");
            }
            
            JsonNode resultNode = chartNode.get("result").get(0);
            JsonNode metaNode = resultNode.get("meta");
            
            String symbol = metaNode.get("symbol").asText();
            String name = metaNode.has("shortName") ? metaNode.get("shortName").asText() : symbol;
            BigDecimal price = new BigDecimal(metaNode.get("regularMarketPrice").toString());
            String currency = metaNode.get("currency").asText();
            
            BigDecimal marketCap = null;
            if (metaNode.has("marketCap")) {
                marketCap = new BigDecimal(metaNode.get("marketCap").toString());
            }
            
            Map<String, Object> stockData = new HashMap<>();
            stockData.put("symbol", symbol);
            stockData.put("name", name);
            stockData.put("price", price);
            stockData.put("currency", currency);
            stockData.put("marketCap", marketCap);
            
            return stockData;
            
        } catch (Exception e) {
            return createErrorResponse("UNKNOWN");
        }
    }

    /**
     * Create error response map
     * 
     * @param symbol Stock symbol
     * @return Map containing error data
     */
    private Map<String, Object> createErrorResponse(String symbol) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("symbol", symbol);
        errorData.put("name", null);
        errorData.put("price", null);
        errorData.put("currency", null);
        errorData.put("marketCap", null);
        errorData.put("error", "Failed to fetch data");
        return errorData;
    }
} 