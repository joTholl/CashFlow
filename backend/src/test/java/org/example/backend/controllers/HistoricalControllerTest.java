package org.example.backend.controllers;

import java.time.*;

import org.example.backend.enums.AssetType;
import org.example.backend.models.AppUser;
import org.example.backend.models.Asset;
import org.example.backend.models.HistoricalEntry;
import org.example.backend.models.Transaction;
import org.example.backend.repositories.AppUserRepository;
import org.example.backend.repositories.HistoricalRepository;
import org.example.backend.repositories.TransactionRepository;
import org.example.backend.services.HelperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.MockServerRestClientCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@AutoConfigureMockMvc
class HistoricalControllerTest {

    @TestConfiguration()
    static class TestContextConfiguration {
        private final MockServerRestClientCustomizer customizer = new MockServerRestClientCustomizer();
        private final RestClient.Builder customizedBuilder = RestClient.builder();

        public TestContextConfiguration() {
            customizer.customize(customizedBuilder);
        }

        @Bean
        public RestClient.Builder restClientBuilder() {
            return customizedBuilder;
        }

        @Bean
        public MockRestServiceServer mockRestServiceServer() {
            return customizer.getServer(customizedBuilder);
        }

    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private HistoricalRepository historicalRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private HelperService helperService;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    public MockRestServiceServer mockServer;
    @MockitoBean
    private Clock clock;

    private final HistoricalEntry historicalEntry1 = new HistoricalEntry("BTC", Map.of(LocalDate.parse("2026-03-02"), BigDecimal.valueOf(60000), LocalDate.parse("2026-03-01"), BigDecimal.valueOf(59000)));
    private final HistoricalEntry historicalEntry2 = new HistoricalEntry("AAPL", Map.of(LocalDate.parse("2026-03-02"), BigDecimal.valueOf(400), LocalDate.parse("2026-03-01"), BigDecimal.valueOf(390)));

    private final AppUser appUser1 = new AppUser("abc", "Rainer Zufall", List.of(new Asset("BTC", BigDecimal.valueOf(0.01), "Bitcoin", BigDecimal.valueOf(1000), AssetType.CRYPTO), new Asset("AAPL", BigDecimal.valueOf(0.5), "Apple Inc.", BigDecimal.valueOf(200), AssetType.STOCK)));

    private final Transaction transaction1 = new Transaction("zyx", "BTC", "Bitcoin", BigDecimal.valueOf(100), BigDecimal.valueOf(0.001), Instant.parse("2026-03-01T10:00:00.00Z"), BigDecimal.valueOf(0.1), AssetType.CRYPTO);
    private final Transaction transaction2 = new Transaction("abc", "AAPL", "Apple Inc.", BigDecimal.valueOf(400), BigDecimal.valueOf(2), Instant.parse("2026-03-01T11:00:00.00Z"), BigDecimal.valueOf(0.2), AssetType.STOCK);

    @BeforeEach
    void setUp() {
        historicalRepository.deleteAll();
        transactionRepository.deleteAll();
        appUserRepository.deleteAll();
        historicalRepository.save(historicalEntry1);
        historicalRepository.save(historicalEntry2);
        transactionRepository.save(transaction1);
        transactionRepository.save(transaction2);
        appUserRepository.save(appUser1);
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
        mockServer.reset();
        when(clock.instant()).thenReturn(Instant.parse("2026-03-03T00:00:00.00Z"));
        when(clock.getZone()).thenReturn(ZoneOffset.systemDefault());
    }

    @Test
    void getAllHistoricalEntries_shouldReturnAllHistoricalEntries() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/historical"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        [
                        {
                            "ticker": "BTC",
                            "closePrice": {
                                "2026-03-01": 59000,
                                "2026-03-02": 60000
                           }
                        },
                        {
                            "ticker": "AAPL",
                            "closePrice": {
                                "2026-03-01": 390,
                                "2026-03-02": 400
                            }
                        }
                        ]
                        """));
    }

    @Test
    void getHistoricalEntryByTicker_shouldReturnHistoricalEntry() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/historical/BTC"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        {
                        "ticker": "BTC",
                        "closePrice": {
                            "2026-03-01": 59000,
                            "2026-03-02": 60000
                        }
                        }
                        """));
    }

    @Test
    void getHistoricalEntryByTicker_shouldThrowException_whenTickerNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/historical/HDBI"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void addHistoricalEntries_shouldAddEntries() throws Exception {
        System.out.println(helperService.getLocalDateNow());
        historicalRepository.deleteAll();
        mockServer.expect(requestTo("https://eodhd.com/api/eod/BTC-USD.CC?from=2025-03-03&to=2026-03-02&period=d&fmt=json&api_token=null"))
                .andRespond(withSuccess("""
                        [
                        {
                            "date":"2026-03-01",
                            "open":60050,
                            "high":60100,
                            "low":59800,
                            "close":60000,
                            "adjusted_close":60000,
                            "volume":264564656
                        },
                        {
                            "date":"2026-03-02",
                            "open":60000,
                            "high":60000,
                            "low":58200,
                            "close":59000,
                            "adjusted_close":59100,
                            "volume":26456456
                        }
                        ]
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("https://eodhd.com/api/eod/AAPL.US?from=2025-03-03&to=2026-03-02&period=d&fmt=json&api_token=null"))
                .andRespond(withSuccess("""
                        [
                        {
                            "date":"2026-03-01",
                            "open":420,
                            "high":450,
                            "low":380,
                            "close":400,
                            "adjusted_close":399,
                            "volume":658465
                        },
                        {
                            "date":"2026-03-02",
                            "open":400,
                            "high":410,
                            "low":380,
                            "close":390,
                            "adjusted_close":389,
                            "volume":5645156
                        }
                        ]
                        """, MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/historical")
                        .with(oidcLogin().userInfoToken(token -> token.claim("id", "abc"))))
                .andExpect(MockMvcResultMatchers.status().isCreated());
        assertEquals(2, historicalRepository.findAll().size());
        assertTrue(historicalRepository.findAll().getFirst().closePrice().size() == 364 || historicalRepository.findAll().getFirst().closePrice().size() == 365);
        assertTrue(historicalRepository.findAll().getLast().closePrice().size() == 364 || historicalRepository.findAll().getLast().closePrice().size() == 365);
    }

    @Test
    void getAllChartData_shouldReturnAllChartData() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/historical/chart"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(364))
                .andExpect(MockMvcResultMatchers.jsonPath("$[363].value").value(860))
                .andExpect(MockMvcResultMatchers.jsonPath("$[363].invested").value(500.3));
    }

    @Test
    void getChartDataByTicker_shouldReturnChartDataFromTicker() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/historical/chart/BTC"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(364))
                .andExpect(MockMvcResultMatchers.jsonPath("$[363].value").value(60))
                .andExpect(MockMvcResultMatchers.jsonPath("$[363].invested").value(100.1));
    }
}