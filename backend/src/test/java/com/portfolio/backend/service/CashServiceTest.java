package com.portfolio.backend.service;

import com.portfolio.backend.model.CashAccount;
import com.portfolio.backend.repository.CashAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private CashAccountRepository cashAccountRepository;

    @InjectMocks
    private CashService cashService;

    private CashAccount mockCashAccount;

    @BeforeEach
    void setUp() {
        mockCashAccount = new CashAccount(new BigDecimal("1000.00"));
    }

    @Test
    void getCashBalance_WhenAccountExists_ReturnsBalance() {
        // Arrange
        when(cashAccountRepository.findFirstByOrderByLastUpdatedDesc())
                .thenReturn(Optional.of(mockCashAccount));

        // Act
        BigDecimal result = cashService.getCashBalance();

        // Assert
        assertEquals(new BigDecimal("1000.00"), result);
        verify(cashAccountRepository).findFirstByOrderByLastUpdatedDesc();
    }

    @Test
    void getCashBalance_WhenNoAccountExists_ReturnsZero() {
        // Arrange
        when(cashAccountRepository.findFirstByOrderByLastUpdatedDesc())
                .thenReturn(Optional.empty());

        // Act
        BigDecimal result = cashService.getCashBalance();

        // Assert
        assertEquals(BigDecimal.ZERO, result);
        verify(cashAccountRepository).findFirstByOrderByLastUpdatedDesc();
    }

    @Test
    void initializeCashAccount_WhenNoExistingAccount_CreatesNewAccount() {
        // Arrange
        BigDecimal initialBalance = new BigDecimal("5000.00");
        when(cashAccountRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.empty());
        when(cashAccountRepository.save(any(CashAccount.class)))
                .thenReturn(mockCashAccount);

        // Act
        CashAccount result = cashService.initializeCashAccount(initialBalance);

        // Assert
        assertNotNull(result);
        verify(cashAccountRepository).findFirstByOrderByIdAsc();
        verify(cashAccountRepository).save(any(CashAccount.class));
    }

    @Test
    void initializeCashAccount_WhenExistingAccount_UpdatesBalance() {
        // Arrange
        BigDecimal newBalance = new BigDecimal("3000.00");
        when(cashAccountRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(mockCashAccount));
        when(cashAccountRepository.save(any(CashAccount.class)))
                .thenReturn(mockCashAccount);

        // Act
        CashAccount result = cashService.initializeCashAccount(newBalance);

        // Assert
        assertNotNull(result);
        verify(cashAccountRepository).findFirstByOrderByIdAsc();
        verify(cashAccountRepository).save(any(CashAccount.class));
    }

    @Test
    void addCash_WhenAccountExists_AddsAmount() {
        // Arrange
        BigDecimal amountToAdd = new BigDecimal("500.00");
        when(cashAccountRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(mockCashAccount));
        when(cashAccountRepository.save(any(CashAccount.class)))
                .thenReturn(mockCashAccount);

        // Act
        CashAccount result = cashService.addCash(amountToAdd);

        // Assert
        assertNotNull(result);
        verify(cashAccountRepository).findFirstByOrderByIdAsc();
        verify(cashAccountRepository).save(any(CashAccount.class));
    }

    @Test
    void addCash_WhenNoAccountExists_CreatesAccountAndAddsAmount() {
        // Arrange
        BigDecimal amountToAdd = new BigDecimal("500.00");
        when(cashAccountRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.empty());
        when(cashAccountRepository.save(any(CashAccount.class)))
                .thenReturn(mockCashAccount);

        // Act
        CashAccount result = cashService.addCash(amountToAdd);

        // Assert
        assertNotNull(result);
        verify(cashAccountRepository).findFirstByOrderByIdAsc();
        verify(cashAccountRepository).save(any(CashAccount.class));
    }

    @Test
    void subtractCash_WhenSufficientFunds_ReturnsTrue() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("500.00");
        when(cashAccountRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(mockCashAccount));
        when(cashAccountRepository.save(any(CashAccount.class)))
                .thenReturn(mockCashAccount);

        // Act
        boolean result = cashService.subtractCash(amountToSubtract);

        // Assert
        assertTrue(result);
        verify(cashAccountRepository).findFirstByOrderByIdAsc();
        verify(cashAccountRepository).save(any(CashAccount.class));
    }

    @Test
    void subtractCash_WhenInsufficientFunds_ReturnsFalse() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("1500.00"); // More than available
        when(cashAccountRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(mockCashAccount));

        // Act
        boolean result = cashService.subtractCash(amountToSubtract);

        // Assert
        assertFalse(result);
        verify(cashAccountRepository).findFirstByOrderByIdAsc();
        verify(cashAccountRepository, never()).save(any(CashAccount.class));
    }


    @Test
    void subtractCash_WithNegativeAmount_ReturnsTrue() {
        // Arrange
        BigDecimal amountToSubtract = new BigDecimal("-100.00");
        when(cashAccountRepository.findFirstByOrderByIdAsc())
                .thenReturn(Optional.of(mockCashAccount));
        when(cashAccountRepository.save(any(CashAccount.class)))
                .thenReturn(mockCashAccount);

        // Act
        boolean result = cashService.subtractCash(amountToSubtract);

        // Assert
        assertTrue(result);
        verify(cashAccountRepository).findFirstByOrderByIdAsc();
        verify(cashAccountRepository).save(any(CashAccount.class));
    }
} 