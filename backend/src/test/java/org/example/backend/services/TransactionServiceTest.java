package org.example.backend.services;

import org.example.backend.dtos.TransactionInDto;
import org.example.backend.models.Transaction;
import org.example.backend.repositories.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final HelperService helperService = mock(HelperService.class);
    private final AppUserService appUserService = mock(AppUserService.class);
    private final TransactionService transactionService = new TransactionService(transactionRepository, helperService, appUserService);

    private final Transaction transaction1 = new Transaction("zyx", "BTC", "Bitcoin", BigDecimal.valueOf(100), BigDecimal.valueOf(0.001), Instant.parse("2026-02-12T10:00:00.00Z"), BigDecimal.valueOf(0.1));
    private final Transaction transaction2 = new Transaction("abc", "ETH", "Ethereum", BigDecimal.valueOf(1000), BigDecimal.valueOf(0.33), Instant.parse("2026-02-12T11:00:00.00Z"), BigDecimal.valueOf(0.2));

    private final TransactionInDto tid1 = new TransactionInDto("BTC", "Bitcoin", BigDecimal.valueOf(100), BigDecimal.valueOf(0.001), Instant.parse("2026-02-12T10:00:00.00Z"), BigDecimal.valueOf(0.1));

    @Test
    void getAllTransactions_shouldReturnAllTransactions() {
        when(transactionRepository.findAll()).thenReturn(List.of(transaction1, transaction2));
        List<Transaction> transactions = transactionService.getAllTransactions();
        verify(transactionRepository).findAll();
        assertEquals(List.of(transaction1, transaction2), transactions);

    }

    @Test
    void getTransactionById_shouldReturnTransaction() {
        when(transactionRepository.findById("zyx")).thenReturn(Optional.of(transaction1));
        Transaction transaction = transactionService.getTransactionById("zyx");
        verify(transactionRepository).findById("zyx");
        assertEquals(transaction1, transaction);
    }

    @Test
    void getTransactionById_shouldThrowException_whenCalledWithWrongID() {
        when(transactionRepository.findById("zyx")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> transactionService.getTransactionById("zyx"));
        verify(transactionRepository).findById("zyx");
    }

    @Test
    void addTransaction_shouldAddTransaction() {
        when(helperService.getRandomId()).thenReturn("zyx");
        when(transactionRepository.save(transaction1)).thenReturn(transaction1);
        Transaction transaction = transactionService.addTransaction(tid1, "abc");
        verify(helperService).getRandomId();
        verify(transactionRepository).save(transaction1);
        verify(appUserService).addTransaction(transaction1, "abc");
        assertEquals(transaction1, transaction);
    }

    @Test
    void updateTransaction_shouldUpdateTransaction() {
        when(transactionRepository.findById("abc")).thenReturn(Optional.of(transaction2));
        Transaction transaction3 = transaction1.withId("abc");
        when(transactionRepository.save(transaction3)).thenReturn(transaction3);
        Transaction transaction = transactionService.updateTransaction("abc", tid1, "abc");
        verify(transactionRepository).findById("abc");
        verify(appUserService).subtractTransaction(transaction2, "abc");
        verify(appUserService).addTransaction(transaction3, "abc");
        verify(transactionRepository).save(transaction3);
        assertEquals(transaction3, transaction);
    }

    @Test
    void updateTransaction_shouldThrowException_whenCalledWithWrongId() {
        when(transactionRepository.findById("abc")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> transactionService.updateTransaction("abc", tid1, "abc"));
        verify(transactionRepository).findById("abc");
        verifyNoMoreInteractions(appUserService);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    void updateTransaction_shouldThrowException_whenTickerAndAssetnameNotMatch() {
        when(transactionRepository.findById("abc")).thenReturn(Optional.of(transaction2.withAssetName("Bitcoin")));
        assertThrows(IllegalArgumentException.class, () -> transactionService.updateTransaction("abc", tid1, "abc"));
        verify(transactionRepository).findById("abc");
        verifyNoMoreInteractions(appUserService);
        verifyNoMoreInteractions(transactionRepository);
    }

    @Test
    void deleteTransaction_shouldDeleteTransaction() {
        when(transactionRepository.findById("zyx")).thenReturn(Optional.of(transaction1));
        transactionService.deleteTransaction("zyx", "abc");
        verify(transactionRepository).findById("zyx");
        verify(appUserService).subtractTransaction(transaction1, "abc");
        verify(transactionRepository).deleteById("zyx");
    }

    @Test
    void deleteTransaction_shouldThrowException_whenCalledWithWrongId() {
        when(transactionRepository.findById("zyx")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> transactionService.deleteTransaction("zyx", "abc"));
        verify(transactionRepository).findById("zyx");
        verifyNoMoreInteractions(appUserService);
        verifyNoMoreInteractions(transactionRepository);

    }
}