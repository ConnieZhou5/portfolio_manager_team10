package com.portfolio.backend.service;

import com.portfolio.backend.dto.PortfolioItemRequest;
import com.portfolio.backend.dto.PortfolioItemResponse;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioItemRepository portfolioItemRepository;

    @Mock
    private CashService cashService;

    @Mock
    private PortfolioDailyValueService portfolioDailyValueService;

    @InjectMocks
    private PortfolioService portfolioService;

    private PortfolioItem mockPortfolioItem;
    private PortfolioItemRequest mockRequest;
    private PortfolioItemResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockPortfolioItem = new PortfolioItem("AAPL", 10, new BigDecimal("150.00"), LocalDate.now());
        mockPortfolioItem.setId(1L);

        mockRequest = new PortfolioItemRequest();
        mockRequest.setTicker("AAPL");
        mockRequest.setQuantity(10);
        mockRequest.setBuyPrice(new BigDecimal("150.00"));
        mockRequest.setBuyDate(LocalDate.now());

        mockResponse = new PortfolioItemResponse();
        mockResponse.setId(1L);
        mockResponse.setTicker("AAPL");
        mockResponse.setQuantity(10);
        mockResponse.setBuyPrice(new BigDecimal("150.00"));
        mockResponse.setBuyDate(LocalDate.now());
        mockResponse.setTotalValue(new BigDecimal("1500.00"));
    }

    @Test
    void getAllPortfolioItems_ReturnsAllItems() {
        // Arrange
        PortfolioItem item1 = new PortfolioItem("AAPL", 10, new BigDecimal("150.00"), LocalDate.now());
        PortfolioItem item2 = new PortfolioItem("GOOGL", 5, new BigDecimal("2800.00"), LocalDate.now());
        List<PortfolioItem> items = Arrays.asList(item1, item2);

        when(portfolioItemRepository.findAll()).thenReturn(items);

        // Act
        List<PortfolioItemResponse> result = portfolioService.getAllPortfolioItems();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getTicker());
        assertEquals("GOOGL", result.get(1).getTicker());
        verify(portfolioItemRepository).findAll();
    }

    @Test
    void getAllPortfolioItems_WhenEmpty_ReturnsEmptyList() {
        // Arrange
        when(portfolioItemRepository.findAll()).thenReturn(List.of());

        // Act
        List<PortfolioItemResponse> result = portfolioService.getAllPortfolioItems();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(portfolioItemRepository).findAll();
    }

    @Test
    void deletePortfolioItem_WhenExists_ReturnsTrue() {
        // Arrange
        when(portfolioItemRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = portfolioService.deletePortfolioItem(1L);

        // Assert
        assertTrue(result);
        verify(portfolioItemRepository).existsById(1L);
        verify(portfolioItemRepository).deleteById(1L);
    }

    @Test
    void deletePortfolioItem_WhenNotExists_ReturnsFalse() {
        // Arrange
        when(portfolioItemRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = portfolioService.deletePortfolioItem(999L);

        // Assert
        assertFalse(result);
        verify(portfolioItemRepository).existsById(999L);
        verify(portfolioItemRepository, never()).deleteById(any());
    }

    @Test
    void getTotalPortfolioValue_ReturnsCorrectValue() {
        // Arrange
        BigDecimal expectedValue = new BigDecimal("5000.00");
        when(portfolioItemRepository.getTotalPortfolioValue()).thenReturn(Optional.of(expectedValue));

        // Act
        BigDecimal result = portfolioService.getTotalPortfolioValue();

        // Assert
        assertEquals(expectedValue, result);
        verify(portfolioItemRepository).getTotalPortfolioValue();
    }
} 