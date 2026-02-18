package org.example.backend.controllers;

import org.example.backend.models.AppUser;
import org.example.backend.repositories.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class AppUserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AppUserRepository appUserRepository;

    private final AppUser appUser1 = new AppUser("abc", "Rainer Zufall", new ArrayList<>());
    private final AppUser appUser2 = new AppUser("cba", "Max Mustermann", new ArrayList<>());


    private final String appUserOut1JSON = """
            {
            "username": "Rainer Zufall",
            "assets": []
            }
            """;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
        appUserRepository.save(appUser1);
        appUserRepository.save(appUser2);
    }

    @Test
    void getAppUser_shouldReturnAppUser() throws Exception {
        mockMvc.perform(get("/api/appuser")
                        .with(oidcLogin().userInfoToken(token-> token.claim("id", "abc"))))
                .andExpect(status().isOk())
                .andExpect(content().json(appUserOut1JSON));
    }

    @Test
    void getAppUser_shouldReturnError401() throws Exception {
        mockMvc.perform(get("/api/appuser")
                .with(oidcLogin().userInfoToken(token-> token.claim("id", "hsdh"))))
                .andExpect(status().isUnauthorized());
    }
}