package org.example.backend.services;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.exceptions.UserNotLoggedInException;
import org.example.backend.repositories.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;

    public AppUserOutDto getAppUser(String id) {
        return new AppUserOutDto(appUserRepository.findById(id).orElseThrow(() -> new UserNotLoggedInException("User not logged in!")));
    }
}
