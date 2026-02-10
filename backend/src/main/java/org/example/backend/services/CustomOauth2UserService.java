package org.example.backend.services;

import lombok.RequiredArgsConstructor;
import org.example.backend.repositories.AppUserRepository;
import org.example.backend.models.AppUser;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        appUserRepository.findById(oAuth2User.getName())
                .orElseGet(()->createAppUser(oAuth2User));
        return oAuth2User;
    }

    private AppUser createAppUser(OAuth2User oAuth2User) {
        AppUser appUser = new AppUser(oAuth2User.getName(),oAuth2User.getAttribute("login"), new ArrayList<>());
        return appUserRepository.save(appUser);
    }
}
