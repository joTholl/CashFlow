package org.example.backend.services;

import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.exceptions.UserNotLoggedInException;
import org.example.backend.models.AppUser;
import org.example.backend.models.Asset;
import org.example.backend.models.Transaction;
import org.example.backend.repositories.AppUserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final AppUserService appUserService = new AppUserService(appUserRepository);

    private final AppUser appUser1 = new AppUser("abc", "Rainer Zufall", new ArrayList<>());
    private final AppUser appUser2 = new AppUser("abc", "Rainer Zufall", List.of(new Asset("BTC", BigDecimal.valueOf(0.01), "Bitcoin", BigDecimal.valueOf(1000))));
    private final AppUserOutDto appUserOutDto1 = new AppUserOutDto(appUser1);

    private final Transaction transaction1 = new Transaction("zyx", "BTC", "Bitcoin", BigDecimal.valueOf(100), BigDecimal.valueOf(0.001), Instant.parse("2026-02-12T10:00:00.00Z"), BigDecimal.valueOf(0.1));

    @Test
    void getAppUser_shouldReturnAppUser() {
        when(appUserRepository.findById("abc")).thenReturn(Optional.of(appUser1));
        AppUserOutDto appUserOutDto = appUserService.getAppUser("abc");
        verify(appUserRepository).findById("abc");
        assertEquals(appUserOutDto, appUserOutDto1);
    }

    @Test
    void getAppUser_shouldThrowException_whenUserNotLoggedIn() {
        when(appUserRepository.findById("abc")).thenReturn(Optional.empty());
        assertThrows(UserNotLoggedInException.class, () -> appUserService.getAppUser("abc"));
        verify(appUserRepository).findById("abc");
    }

    @Test
    void addTransaction_shouldAddTransaction_andCreateAsset() {
        when(appUserRepository.findById("abc")).thenReturn(Optional.of(appUser1));
        AppUser appUser = appUser2.withAssets(List.of(appUser2.assets().getFirst().withCost(BigDecimal.valueOf(100.1)).withShares(BigDecimal.valueOf(0.001))));
        appUserService.addTransaction(transaction1, "abc");
        verify(appUserRepository).findById("abc");
        verify(appUserRepository).save(appUser);
    }

    @Test
    void addTransaction_shouldAddTransaction_andAddToAsset() {
        when(appUserRepository.findById("abc")).thenReturn(Optional.of(appUser2));
        AppUser appUser = appUser2.withAssets(List.of(appUser2.assets().getFirst().withCost(BigDecimal.valueOf(1100.1)).withShares(BigDecimal.valueOf(0.011))));
        appUserService.addTransaction(transaction1, "abc");
        verify(appUserRepository).findById("abc");
        verify(appUserRepository).save(appUser);
    }

    @Test
    void addTransaction_shouldThrowException_whenUserNotLoggedIn() {
        when(appUserRepository.findById("abc")).thenReturn(Optional.empty());
        assertThrows(UserNotLoggedInException.class, () -> appUserService.addTransaction(transaction1, "abc"));
        verify(appUserRepository).findById("abc");
        verify(appUserRepository, times(0)).save(any());
    }

    @Test
    void subtractTransaction_shouldSubtractTransaction_andSubtractToAsset() {
        when(appUserRepository.findById("abc")).thenReturn(Optional.of(appUser2));
        AppUser appUser = appUser2.withAssets(List.of(appUser2.assets().getFirst().withCost(BigDecimal.valueOf(899.9)).withShares(BigDecimal.valueOf(0.009))));
        appUserService.subtractTransaction(transaction1, "abc");
        verify(appUserRepository).findById("abc");
        verify(appUserRepository).save(appUser);
    }

    @Test
    void subtractTransaction_shouldSubtractTransaction_andDeleteAsset() {
        AppUser appUser = appUser2.withAssets(List.of(appUser2.assets().getFirst().withCost(BigDecimal.valueOf(100.1)).withShares(BigDecimal.valueOf(0.001))));
        when(appUserRepository.findById("abc")).thenReturn(Optional.of(appUser));
        appUserService.subtractTransaction(transaction1, "abc");
        verify(appUserRepository).findById("abc");
        verify(appUserRepository).save(appUser.withAssets(new ArrayList<>()));
    }

    @Test
    void subtractTransaction_shouldThrowException_whenUserNotLoggedIn() {
        when(appUserRepository.findById("abc")).thenReturn(Optional.empty());
        assertThrows(UserNotLoggedInException.class, () -> appUserService.subtractTransaction(transaction1, "abc"));
        verify(appUserRepository).findById("abc");
        verify(appUserRepository, times(0)).save(any());
    }

    @Test
    void subtractTransaction_shouldThrowException_whenNotEnoughShares() {
        AppUser appUser = appUser2.withAssets(List.of(appUser2.assets().getFirst().withCost(BigDecimal.valueOf(100.1)).withShares(BigDecimal.valueOf(0.0001))));
        when(appUserRepository.findById("abc")).thenReturn(Optional.of(appUser));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> appUserService.subtractTransaction(transaction1, "abc"));
        verify(appUserRepository).findById("abc");
        verify(appUserRepository, times(0)).save(any());
        assertEquals("Not enough shares to subtract!", e.getMessage());
    }

    @Test
    void subtractTransaction_shouldThrowException_whenAssetNotFound() {
        when(appUserRepository.findById("abc")).thenReturn(Optional.of(appUser1));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> appUserService.subtractTransaction(transaction1, "abc"));
        verify(appUserRepository).findById("abc");
        verify(appUserRepository, times(0)).save(any());
        assertEquals("Asset not found!", e.getMessage());
    }
}