package com.portfolio.backend.service;

import com.portfolio.backend.dto.BuyRequest;
import com.portfolio.backend.dto.PortfolioItemResponse;
import com.portfolio.backend.dto.TradeHistoryResponse;
import com.portfolio.backend.model.PortfolioItem;
import com.portfolio.backend.repository.PortfolioItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyServiceTest {

    @Mock
    private CashService cashService;

    @Mock
    private PortfolioService portfolioService;

    @Mock
    private TradeHistoryService tradeHistoryService;

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @InjectMocks
    private BuyService buyService;

    private BuyRequest validBuyRequest;
    private PortfolioItem mockPortfolioItem;
    private TradeHistoryResponse mockTradeHistoryResponse;

    @BeforeEach
    void setUp() {
        validBuyRequest = new BuyRequest("AAPL", 10, new BigDecimal("150.00"), LocalDate.now());
        mockPortfolioItem = new PortfolioItem("AAPL", 10, new BigDecimal("150.00"), LocalDate.now());
        mockTradeHistoryResponse = new TradeHistoryResponse();
        mockTradeHistoryResponse.setId(1L);
        mockTradeHistoryResponse.setTicker("AAPL");
        mockTradeHistoryResponse.setQuantity(10);
        mockTradeHistoryResponse.setPrice(new BigDecimal("150.00"));
        mockTradeHistoryResponse.setTradeType("BUY");
    }

    @Test
    void executeBuyTransaction_WithValidRequest_ReturnsSuccess() {
        // Arrange
        BigDecimal totalCost = new BigDecimal("1500.00");
        BigDecimal currentCash = new BigDecimal("2000.00");
        BigDecimal remainingCash = new BigDecimal("500.00");

        when(cashService.getCashBalance()).thenReturn(currentCash, remainingCash);
        when(cashService.subtractCash(totalCost)).thenReturn(true);
        when(portfolioItemRepository.findByTicker("AAPL")).thenReturn(List.of());
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(mockPortfolioItem);
        when(tradeHistoryService.addTrade(any())).thenReturn(mockTradeHistoryResponse);

        // Act
        Map<String, Object> result = buyService.executeBuyTransaction(validBuyRequest);

        // Assert
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Buy transaction completed successfully", result.get("message"));
        assertEquals(totalCost, result.get("totalCost"));
        assertEquals(remainingCash, result.get("remainingCash"));
        assertNotNull(result.get("portfolioItem"));
        assertNotNull(result.get("tradeRecord"));

        verify(cashService, times(2)).getCashBalance();
        verify(cashService).subtractCash(totalCost);
        verify(portfolioItemRepository).findByTicker("AAPL");
        verify(portfolioItemRepository).save(any(PortfolioItem.class));
        verify(tradeHistoryService).addTrade(any());
    }

    @Test
    void executeBuyTransaction_WithInsufficientFunds_ThrowsException() {
        // Arrange
        BigDecimal totalCost = new BigDecimal("1500.00");
        BigDecimal currentCash = new BigDecimal("1000.00");

        when(cashService.getCashBalance()).thenReturn(currentCash);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> buyService.executeBuyTransaction(validBuyRequest)
        );

        assertEquals("Insufficient funds. Required: $1500.00, Available: $1000.00", exception.getMessage());
        verify(cashService).getCashBalance();
        verify(cashService, never()).subtractCash(any());
    }

    @Test
    void executeBuyTransaction_WithNullTicker_ThrowsException() {
        // Arrange
        BuyRequest invalidRequest = new BuyRequest(null, 10, new BigDecimal("150.00"), LocalDate.now());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> buyService.executeBuyTransaction(invalidRequest)
        );

        assertEquals("Ticker cannot be null or empty", exception.getMessage());
        verify(cashService, never()).getCashBalance();
    }

    @Test
    void executeBuyTransaction_WithEmptyTicker_ThrowsException() {
        // Arrange
        BuyRequest invalidRequest = new BuyRequest("", 10, new BigDecimal("150.00"), LocalDate.now());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> buyService.executeBuyTransaction(invalidRequest)
        );

        assertEquals("Ticker cannot be null or empty", exception.getMessage());
        verify(cashService, never()).getCashBalance();
    }

    @Test
    void executeBuyTransaction_WithNullQuantity_ThrowsException() {
        // Arrange
        BuyRequest invalidRequest = new BuyRequest("AAPL", null, new BigDecimal("150.00"), LocalDate.now());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> buyService.executeBuyTransaction(invalidRequest)
        );

        assertEquals("Quantity must be positive", exception.getMessage());
        verify(cashService, never()).getCashBalance();
    }

    @Test
    void executeBuyTransaction_WithExistingTicker_UpdatesQuantity() {
        // Arrange
        BigDecimal totalCost = new BigDecimal("1500.00");
        BigDecimal currentCash = new BigDecimal("2000.00");
        BigDecimal remainingCash = new BigDecimal("500.00");

        PortfolioItem existingItem = new PortfolioItem("AAPL", 5, new BigDecimal("140.00"), LocalDate.now().minusDays(1));
        existingItem.setId(1L);

        when(cashService.getCashBalance()).thenReturn(currentCash, remainingCash);
        when(cashService.subtractCash(totalCost)).thenReturn(true);
        when(portfolioItemRepository.findByTicker("AAPL")).thenReturn(List.of(existingItem));
        when(portfolioItemRepository.save(any(PortfolioItem.class))).thenReturn(existingItem);
        when(tradeHistoryService.addTrade(any())).thenReturn(mockTradeHistoryResponse);

        // Act
        Map<String, Object> result = buyService.executeBuyTransaction(validBuyRequest);

        // Assert
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Buy transaction completed successfully", result.get("message"));

        verify(cashService, times(2)).getCashBalance();
        verify(cashService).subtractCash(totalCost);
        verify(portfolioItemRepository).findByTicker("AAPL");
        verify(portfolioItemRepository).save(any(PortfolioItem.class));
        verify(tradeHistoryService).addTrade(any());
    }
} 