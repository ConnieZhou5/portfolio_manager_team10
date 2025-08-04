package com.portfolio.backend.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BuyRequestTest {

    @Test
    void testDefaultConstructor() {
        // Act
        BuyRequest request = new BuyRequest();

        // Assert
        assertNull(request.getTicker());
        assertNull(request.getQuantity());
        assertNull(request.getPrice());
        assertNull(request.getTradeDate());
    }

    @Test
    void testParameterizedConstructor() {
        // Arrange
        String ticker = "AAPL";
        Integer quantity = 10;
        BigDecimal price = new BigDecimal("150.00");
        LocalDate tradeDate = LocalDate.now();

        // Act
        BuyRequest request = new BuyRequest(ticker, quantity, price, tradeDate);

        // Assert
        assertEquals(ticker, request.getTicker());
        assertEquals(quantity, request.getQuantity());
        assertEquals(price, request.getPrice());
        assertEquals(tradeDate, request.getTradeDate());
    }

    @Test
    void testSettersAndGetters() {
        // Arrange
        BuyRequest request = new BuyRequest();
        String ticker = "GOOGL";
        Integer quantity = 5;
        BigDecimal price = new BigDecimal("2800.00");
        LocalDate tradeDate = LocalDate.now();

        // Act
        request.setTicker(ticker);
        request.setQuantity(quantity);
        request.setPrice(price);
        request.setTradeDate(tradeDate);

        // Assert
        assertEquals(ticker, request.getTicker());
        assertEquals(quantity, request.getQuantity());
        assertEquals(price, request.getPrice());
        assertEquals(tradeDate, request.getTradeDate());
    }

    @Test
    void testGetTotalCost_WithValidValues() {
        // Arrange
        BuyRequest request = new BuyRequest("AAPL", 10, new BigDecimal("150.00"), LocalDate.now());

        // Act
        BigDecimal totalCost = request.getTotalCost();

        // Assert
        assertEquals(new BigDecimal("1500.00"), totalCost);
    }

    @Test
    void testGetTotalCost_WithNullPrice() {
        // Arrange
        BuyRequest request = new BuyRequest("AAPL", 10, null, LocalDate.now());

        // Act
        BigDecimal totalCost = request.getTotalCost();

        // Assert
        assertEquals(BigDecimal.ZERO, totalCost);
    }

    @Test
    void testGetTotalCost_WithNullQuantity() {
        // Arrange
        BuyRequest request = new BuyRequest("AAPL", null, new BigDecimal("150.00"), LocalDate.now());

        // Act
        BigDecimal totalCost = request.getTotalCost();

        // Assert
        assertEquals(BigDecimal.ZERO, totalCost);
    }

    @Test
    void testGetTotalCost_WithBothNullValues() {
        // Arrange
        BuyRequest request = new BuyRequest("AAPL", null, null, LocalDate.now());

        // Act
        BigDecimal totalCost = request.getTotalCost();

        // Assert
        assertEquals(BigDecimal.ZERO, totalCost);
    }

    @Test
    void testGetTotalCost_WithLargeNumbers() {
        // Arrange
        BuyRequest request = new BuyRequest("GOOGL", 100, new BigDecimal("2800.00"), LocalDate.now());

        // Act
        BigDecimal totalCost = request.getTotalCost();

        // Assert
        assertEquals(new BigDecimal("280000.00"), totalCost);
    }

    @Test
    void testGetTotalCost_WithDecimalPrice() {
        // Arrange
        BuyRequest request = new BuyRequest("TSLA", 5, new BigDecimal("250.50"), LocalDate.now());

        // Act
        BigDecimal totalCost = request.getTotalCost();

        // Assert
        assertEquals(new BigDecimal("1252.50"), totalCost);
    }

    @Test
    void testToString() {
        // Arrange
        BuyRequest request = new BuyRequest("AAPL", 10, new BigDecimal("150.00"), LocalDate.of(2024, 1, 15));

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("BuyRequest"));
        assertTrue(result.contains("ticker='AAPL'"));
        assertTrue(result.contains("quantity=10"));
        assertTrue(result.contains("price=150.00"));
        assertTrue(result.contains("totalCost=1500.00"));
    }

    @Test
    void testToString_WithNullValues() {
        // Arrange
        BuyRequest request = new BuyRequest(null, null, null, null);

        // Act
        String result = request.toString();

        // Assert
        assertTrue(result.contains("BuyRequest"));
        assertTrue(result.contains("ticker='null'"));
        assertTrue(result.contains("quantity=null"));
        assertTrue(result.contains("price=null"));
        assertTrue(result.contains("totalCost=0"));
    }

    @Test
    void testEqualsAndHashCode() {
        // Arrange
        BuyRequest request1 = new BuyRequest("AAPL", 10, new BigDecimal("150.00"), LocalDate.now());
        BuyRequest request2 = new BuyRequest("AAPL", 10, new BigDecimal("150.00"), LocalDate.now());
        BuyRequest request3 = new BuyRequest("GOOGL", 10, new BigDecimal("150.00"), LocalDate.now());

        // Act & Assert
        // Note: BuyRequest doesn't override equals/hashCode, so we're testing default behavior
        assertNotEquals(request1, request2); // Different objects
        assertNotEquals(request1, request3); // Different objects
    }

    @Test
    void testEquals_WithDifferentTypes() {
        // Arrange
        BuyRequest request = new BuyRequest("AAPL", 10, new BigDecimal("150.00"), LocalDate.now());
        String differentType = "not a BuyRequest";

        // Act & Assert
        assertNotEquals(request, differentType);
    }

} 