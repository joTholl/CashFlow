package org.example.backend.services;

import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.exceptions.UserNotLoggedInException;
import org.example.backend.models.AppUser;
import org.example.backend.repositories.AppUserRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppUserServiceTest {

    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final AppUserService appUserService = new AppUserService(appUserRepository);

    private final AppUser appUser1 = new AppUser("abc", "Rainer Zufall", new ArrayList<>());
    private final AppUserOutDto appUserOutDto1 = new AppUserOutDto(appUser1);

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
}