package org.example.backend.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.example.backend.models.ChartData;
import org.example.backend.models.HistoricalEntry;
import org.example.backend.services.HistoricalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.util.List;

@RestController
@RequestMapping("/api/historical")
@RequiredArgsConstructor
public class HistoricalController {

    private final HistoricalService  historicalService;

    @GetMapping
    public List<HistoricalEntry> getAllHistoricalEntries() {
        return historicalService.getAllHistoricalEntries();
    }

    @GetMapping("/{ticker}")
    public HistoricalEntry getHistoricalEntryByTicker(@PathVariable String ticker) {
        return historicalService.getHistoricalEntryByTicker(ticker);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addHistoricalEntries(@AuthenticationPrincipal OAuth2User user) {
        historicalService.addHistoricalEntries(user.getAttribute("id").toString());
    }

    @GetMapping("/chart")
    public List<ChartData> getAllChartData() {
        return historicalService.getAllChartData();
    }

    @GetMapping("/chart/{ticker}")
    public List<ChartData> getChartDataByTicker(@PathVariable String ticker) {
        return historicalService.getChartDataByTicker(ticker);
    }

}
