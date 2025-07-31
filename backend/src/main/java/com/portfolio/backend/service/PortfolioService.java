package com.portfolio.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.backend.dto.PortfolioItemRequest;
import com.portfolio.backend.dto.PortfolioItemResponse;
import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.repository.PortfolioItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioItemRepository portfolioItemRepository;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public PortfolioService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://query1.finance.yahoo.com")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get all portfolio items
     * 
     * @return List of all portfolio item responses
     */
    public List<PortfolioItemResponse> getAllPortfolioItems() {
        return portfolioItemRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a portfolio item by ID
     * 
     * @param id The ID of the portfolio item
     * @return Optional containing the portfolio item response if found
     */
    public Optional<PortfolioItemResponse> getPortfolioItemById(Long id) {
        return portfolioItemRepository.findById(id)
                .map(this::convertToResponse);
    }

    /**
     * Add a new portfolio item with validation
     * 
     * @param request The portfolio item request
     * @return The saved portfolio item response
     * @throws IllegalArgumentException if validation fails
     */
    public PortfolioItemResponse addPortfolioItem(PortfolioItemRequest request) {
        validatePortfolioItemRequest(request);
        PortfolioItem portfolioItem = convertToEntity(request);
        PortfolioItem savedItem = portfolioItemRepository.save(portfolioItem);
        return convertToResponse(savedItem);
    }

    /**
     * Update an existing portfolio item
     * 
     * @param id The ID of the portfolio item to update
     * @param portfolioItem The updated portfolio item data
     * @return The updated portfolio item
     * @throws IllegalArgumentException if item not found
     */
    public PortfolioItemResponse updatePortfolioItem(Long id, PortfolioItemRequest request) {
        PortfolioItem existingItem = portfolioItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio item not found with id: " + id));

        // Update fields if provided
        if (request.getTicker() != null) {
            existingItem.setTicker(request.getTicker());
        }
        if (request.getQuantity() != null) {
            existingItem.setQuantity(request.getQuantity());
        }
        if (request.getBuyPrice() != null) {
            existingItem.setBuyPrice(request.getBuyPrice());
        }
        if (request.getBuyDate() != null) {
            existingItem.setBuyDate(request.getBuyDate());
        }

        PortfolioItem updatedItem = portfolioItemRepository.save(existingItem);
        return convertToResponse(updatedItem);
    }

    /**
     * Delete a portfolio item
     * 
     * @param id The ID of the portfolio item to delete
     * @return true if deleted, false if not found
     */
    public boolean deletePortfolioItem(Long id) {
        if (portfolioItemRepository.existsById(id)) {
            portfolioItemRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Validate portfolio item request data
     * 
     * @param request The portfolio item request to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePortfolioItemRequest(PortfolioItemRequest request) {
        if (request.getTicker() == null || request.getTicker().trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker is required and cannot be empty");
        }
        
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
        
        if (request.getBuyPrice() == null || request.getBuyPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Buy price must be greater than 0");
        }
    }

    /**
     * Get portfolio items by ticker
     * 
     * @param ticker The ticker symbol
     * @return List of portfolio item responses with the given ticker
     */
    public List<PortfolioItemResponse> getPortfolioItemsByTicker(String ticker) {
        return portfolioItemRepository.findByTicker(ticker)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get total portfolio value
     * 
     * @return Total value of all portfolio items
     */
    public BigDecimal getTotalPortfolioValue() {
        return portfolioItemRepository.getTotalPortfolioValue()
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get total value by ticker
     * 
     * @param ticker The ticker symbol
     * @return Total value of portfolio items with the given ticker
     */
    public BigDecimal getTotalValueByTicker(String ticker) {
        return portfolioItemRepository.getTotalValueByTicker(ticker)
                .orElse(BigDecimal.ZERO);
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
    
    /**
     * Convert PortfolioItem entity to PortfolioItemResponse DTO
     * 
     * @param portfolioItem The entity to convert
     * @return The response DTO
     */
    private PortfolioItemResponse convertToResponse(PortfolioItem portfolioItem) {
        return new PortfolioItemResponse(
                portfolioItem.getId(),
                portfolioItem.getTicker(),
                portfolioItem.getQuantity(),
                portfolioItem.getBuyPrice(),
                portfolioItem.getBuyDate(),
                portfolioItem.getTotalValue()
        );
    }
    
    /**
     * Convert PortfolioItemRequest DTO to PortfolioItem entity
     * 
     * @param request The request DTO to convert
     * @return The entity
     */
    private PortfolioItem convertToEntity(PortfolioItemRequest request) {
        PortfolioItem portfolioItem = new PortfolioItem();
        portfolioItem.setTicker(request.getTicker());
        portfolioItem.setQuantity(request.getQuantity());
        portfolioItem.setBuyPrice(request.getBuyPrice());
        portfolioItem.setBuyDate(request.getBuyDate());
        return portfolioItem;
    }
} 