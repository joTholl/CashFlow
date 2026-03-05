package org.example.backend.controllers;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.example.backend.services.FinnhubService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/live")
public class FinnhubController {

    private final FinnhubService  finnhubService;

    @GetMapping
    public Map<String, BigDecimal> getLivePrices() {
        return finnhubService.getLivePrices();
    }

    @PostMapping
    public void addSymbolsFromAssets (@AuthenticationPrincipal OAuth2User user) {
        finnhubService.addSymbolsFromAssets(user.getAttribute("id").toString());
    }

}
