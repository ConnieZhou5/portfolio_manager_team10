package com.portfolio.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
                errorData.put("previousClose", null);
                errorData.put("dayGain", null);
                errorData.put("dayGainPercent", null);
                errorData.put("volume", null);
                errorData.put("dayLow", null);
                errorData.put("dayHigh", null);
                errorData.put("yearLow", null);
                errorData.put("yearHigh", null);
                errorData.put("marketStatus", null);
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
            
            BigDecimal previousClose = new BigDecimal(metaNode.get("chartPreviousClose").toString());
            BigDecimal dayGain = price.subtract(previousClose);
            BigDecimal dayGainPercent = previousClose.compareTo(BigDecimal.ZERO) > 0 ? 
                dayGain.divide(previousClose, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : 
                BigDecimal.ZERO;
            
            BigDecimal volume = new BigDecimal(metaNode.get("regularMarketVolume").toString());
            BigDecimal dayLow = new BigDecimal(metaNode.get("regularMarketDayLow").toString());
            BigDecimal dayHigh = new BigDecimal(metaNode.get("regularMarketDayHigh").toString());
            BigDecimal yearLow = new BigDecimal(metaNode.get("fiftyTwoWeekLow").toString());
            BigDecimal yearHigh = new BigDecimal(metaNode.get("fiftyTwoWeekHigh").toString());
            
            BigDecimal marketCap = null;
            if (metaNode.has("marketCap")) {
                marketCap = new BigDecimal(metaNode.get("marketCap").toString());
            }
            
            // Determine market status based on current time
            String marketStatus = determineMarketStatus();
            
            Map<String, Object> stockData = new HashMap<>();
            stockData.put("symbol", symbol);
            stockData.put("name", name);
            stockData.put("price", price);
            stockData.put("currency", currency);
            stockData.put("marketCap", marketCap);
            stockData.put("previousClose", previousClose);
            stockData.put("dayGain", dayGain);
            stockData.put("dayGainPercent", dayGainPercent);
            stockData.put("volume", volume);
            stockData.put("dayLow", dayLow);
            stockData.put("dayHigh", dayHigh);
            stockData.put("yearLow", yearLow);
            stockData.put("yearHigh", yearHigh);
            stockData.put("marketStatus", marketStatus);
            
            return stockData;
            
        } catch (Exception e) {
            return createErrorResponse("UNKNOWN");
        }
    }

    /**
     * Determine if the market is currently open based on New York time
     * 
     * @return "Market Open" or "Market Closed"
     */
    private String determineMarketStatus() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        int hour = now.getHour();
        int minute = now.getMinute();
        int dayOfWeek = now.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        
        // Market hours: Monday-Friday, 9:30 AM - 4:00 PM ET
        boolean isWeekday = dayOfWeek >= 1 && dayOfWeek <= 5;
        boolean isMarketHours = (hour > 9 || (hour == 9 && minute >= 30)) && hour < 16;
        
        return (isWeekday && isMarketHours) ? "Market Open" : "Market Closed";
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
        errorData.put("previousClose", null);
        errorData.put("dayGain", null);
        errorData.put("dayGainPercent", null);
        errorData.put("volume", null);
        errorData.put("dayLow", null);
        errorData.put("dayHigh", null);
        errorData.put("yearLow", null);
        errorData.put("yearHigh", null);
        errorData.put("marketStatus", null);
        errorData.put("error", "Failed to fetch data");
        return errorData;
    }
} 