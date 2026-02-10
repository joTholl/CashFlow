package org.example.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.services.AppUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appuser")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @GetMapping
    public AppUserOutDto getAppUser(@AuthenticationPrincipal OAuth2User user) {
        return appUserService.getAppUser(user.getAttribute("client_id"));
    }
}
