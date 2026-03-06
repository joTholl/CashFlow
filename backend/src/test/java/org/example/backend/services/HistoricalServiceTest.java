package org.example.backend.services;

import java.util.*;

import org.example.backend.components.LivePriceStore;
import org.example.backend.models.*;
import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.dtos.TransactionOutDto;
import org.example.backend.enums.AssetType;
import org.example.backend.repositories.HistoricalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HistoricalServiceTest {

    private final HistoricalRepository historicalRepository = mock(HistoricalRepository.class);
    private final AppUserService appUserService = mock(AppUserService.class);
    private final TransactionService transactionService = mock(TransactionService.class);
    private final HelperService helperService = mock(HelperService.class);
    private final LivePriceStore livePriceStore = mock(LivePriceStore.class);
    private MockRestServiceServer mockServer;
    private HistoricalService historicalService;

    private final HistoricalEntry historicalEntry1 = new HistoricalEntry("BTC", Map.of(LocalDate.parse("2026-03-03"), BigDecimal.valueOf(61000), LocalDate.parse("2026-03-02"), BigDecimal.valueOf(60000), LocalDate.parse("2026-03-01"), BigDecimal.valueOf(59000)));
    private final HistoricalEntry historicalEntry2 = new HistoricalEntry("AAPL", Map.of(LocalDate.parse("2026-03-03"), BigDecimal.valueOf(410), LocalDate.parse("2026-03-02"), BigDecimal.valueOf(400), LocalDate.parse("2026-03-01"), BigDecimal.valueOf(390)));

    private final AppUserOutDto appUserOutDto1 = new AppUserOutDto("Rainer Zufall", List.of(new Asset("BTC", BigDecimal.valueOf(0.01), "Bitcoin", BigDecimal.valueOf(1000), AssetType.CRYPTO), new Asset("AAPL", BigDecimal.valueOf(0.5), "Apple Inc.", BigDecimal.valueOf(200), AssetType.STOCK)));

    private final TransactionOutDto tod1 = new TransactionOutDto("zyx", "BTC", "Bitcoin", BigDecimal.valueOf(100), BigDecimal.valueOf(0.001), LocalDateTime.parse("2026-03-01T10:00:00.00"), BigDecimal.valueOf(0.1), AssetType.CRYPTO);
    private final TransactionOutDto tod2 = new TransactionOutDto("abc", "AAPL", "Aple Inc.", BigDecimal.valueOf(400), BigDecimal.valueOf(2), LocalDateTime.parse("2026-03-01T11:00:00.00"), BigDecimal.valueOf(0.2), AssetType.STOCK);

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        historicalService = new HistoricalService(historicalRepository, appUserService, transactionService, helperService, restClientBuilder, livePriceStore);
    }

    @Test
    void getAllHistoricalEntries_shouldReturnHistoricalAllEntries() {
        when(historicalRepository.findAll()).thenReturn(List.of(historicalEntry1, historicalEntry2));
        List<HistoricalEntry> historicalEntries = historicalService.getAllHistoricalEntries();
        verify(historicalRepository, times(1)).findAll();
        assertEquals(List.of(historicalEntry1, historicalEntry2), historicalEntries);
    }

    @Test
    void getHistoricalEntryByTicker_shouldReturnHistoricalEntry() {
        when(historicalRepository.findById("BTC")).thenReturn(Optional.of(historicalEntry1));
        HistoricalEntry historicalEntry = historicalService.getHistoricalEntryByTicker("BTC");
        verify(historicalRepository, times(1)).findById("BTC");
        assertEquals(historicalEntry1, historicalEntry);
    }

    @Test
    void getHistoricalEntryByTicker_shouldThrowException_whenTickerNotFound() {
        when(historicalRepository.findById("BTC")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> historicalService.getHistoricalEntryByTicker("BTC"));
        verify(historicalRepository, times(1)).findById("BTC");
    }

    @Test
    void addHistoricalEntries_shouldAddAllAssets() {
        when(historicalRepository.findAll()).thenReturn(List.of(historicalEntry1, historicalEntry2));
        when(appUserService.getAppUser("abc")).thenReturn(appUserOutDto1);
        when(helperService.getLocalDateNow()).thenReturn(LocalDate.parse("2026-03-05"));
        mockServer.expect(requestTo("https://eodhd.com/api/eod/BTC-USD.CC?from=2025-03-05&to=2026-03-04&period=d&fmt=json&api_token=null"))
                .andRespond(withSuccess("""
                        [
                        {
                            "date":"2026-03-01",
                            "open":60050,
                            "high":60100,
                            "low":58800,
                            "close":59000,
                            "adjusted_close":60000,
                            "volume":264564656
                        },
                        {
                            "date":"2026-03-02",
                            "open":59000,
                            "high":60500,
                            "low":58200,
                            "close":60000,
                            "adjusted_close":59100,
                            "volume":26456456
                        },
                        {
                            "date":"2026-03-03",
                            "open":60000,
                            "high":60000,
                            "low":58200,
                            "close":61000,
                            "adjusted_close":60100,
                            "volume":26456456
                        }
                        ]
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("https://eodhd.com/api/eod/AAPL.US?from=2025-03-05&to=2026-03-04&period=d&fmt=json&api_token=null"))
                .andRespond(withSuccess("""
                        [
                        {
                            "date":"2026-03-01",
                            "open":420,
                            "high":450,
                            "low":380,
                            "close":390,
                            "adjusted_close":399,
                            "volume":658465
                        },
                        {
                            "date":"2026-03-02",
                            "open":400,
                            "high":410,
                            "low":380,
                            "close":400,
                            "adjusted_close":389,
                            "volume":5645156
                        },
                        {
                            "date":"2026-03-03",
                            "open":400,
                            "high":410,
                            "low":380,
                            "close":410,
                            "adjusted_close":389,
                            "volume":5645156
                        }
                        ]
                        """, MediaType.APPLICATION_JSON));
        historicalService.addHistoricalEntries("abc");
        verify(historicalRepository, times(1)).findAll();
        verify(appUserService, times(1)).getAppUser("abc");
        verify(historicalRepository, times(2)).save(argThat(entry ->
                entry.closePrice() != null && (entry.closePrice().size() == 365 || entry.closePrice().size() == 366)));
    }

    @Test
    void addHistoricalEntries_shouldNotAddAssets_whenAlreadyInDB() {
        when(historicalRepository.findAll()).thenReturn(List.of(historicalEntry1, historicalEntry2));
        when(appUserService.getAppUser("abc")).thenReturn(appUserOutDto1);
        when(helperService.getLocalDateNow()).thenReturn(LocalDate.parse("2026-03-03"));
        mockServer.expect(ExpectedCount.never(), requestTo("https://eodhd.com/api/eod/BTC-USD.CC?from=2025-03-03&to=2026-03-02&period=d&fmt=json&api_token=null"));
        mockServer.expect(ExpectedCount.never(), requestTo("https://eodhd.com/api/eod/AAPL.US?from=2025-03-03&to=2026-03-02&period=d&fmt=json&api_token=null"));
        historicalService.addHistoricalEntries("abc");
        verify(historicalRepository, times(1)).findAll();
        verify(appUserService, times(1)).getAppUser("abc");
        verify(historicalRepository, never()).save(any(HistoricalEntry.class));
    }


    @Test
    void getAllChartData_shouldReturnChartData() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(tod1, tod2));
        when(helperService.getLocalDateNow()).thenReturn(LocalDate.parse("2026-03-04"));
        when(historicalRepository.findAll()).thenReturn(List.of(historicalEntry1, historicalEntry2));
        List<ChartData> chartData = historicalService.getAllChartData();
        verify(transactionService, times(1)).getAllTransactions();
        assertTrue(chartData.size() == 365 || chartData.size() == 366);
        for (int i = 0; i < chartData.size(); i++) {
            if (i < chartData.size() - 3) {
                assertTrue(Objects.equals(chartData.get(i).invested(), BigDecimal.ZERO) && Objects.equals(chartData.get(i).value(), BigDecimal.ZERO));
            } else if (i == chartData.size() - 3) {
                assertEquals(0, BigDecimal.valueOf(839).compareTo(chartData.get(i).value()));
                assertEquals(0, BigDecimal.valueOf(500.3).compareTo(chartData.get(i).invested()));
            } else if (i == chartData.size() - 2) {
                assertEquals(0, BigDecimal.valueOf(860).compareTo(chartData.get(i).value()));
                assertEquals(0, BigDecimal.valueOf(500.3).compareTo(chartData.get(i).invested()));
            } else if (i == chartData.size() - 1) {
                assertEquals(0, BigDecimal.valueOf(881).compareTo(chartData.get(i).value()));
                assertEquals(0, BigDecimal.valueOf(500.3).compareTo(chartData.get(i).invested()));
            }
        }
    }

    @Test
    void getChartDataByTicker_shouldReturnChartData() {
        when(transactionService.getAllTransactions()).thenReturn(List.of(tod1, tod2));
        when(helperService.getLocalDateNow()).thenReturn(LocalDate.parse("2026-03-04"));
        when(historicalRepository.findAll()).thenReturn(List.of(historicalEntry1, historicalEntry2));
        List<ChartData> chartData = historicalService.getChartDataByTicker("BTC");
        verify(transactionService, times(1)).getAllTransactions();
        assertTrue(chartData.size() == 365 || chartData.size() == 366);
        for (int i = 0; i < chartData.size(); i++) {
            if (i < chartData.size() - 3) {
                assertTrue(Objects.equals(chartData.get(i).invested(), BigDecimal.ZERO) && Objects.equals(chartData.get(i).value(), BigDecimal.ZERO));
            } else if (i == chartData.size() - 3) {
                assertEquals(0, BigDecimal.valueOf(59).compareTo(chartData.get(i).value()));
                assertEquals(0, BigDecimal.valueOf(100.1).compareTo(chartData.get(i).invested()));
            } else if (i == chartData.size() - 2) {
                assertEquals(0, BigDecimal.valueOf(60).compareTo(chartData.get(i).value()));
                assertEquals(0, BigDecimal.valueOf(100.1).compareTo(chartData.get(i).invested()));
            } else if (i == chartData.size() - 1) {
                assertEquals(0, BigDecimal.valueOf(61).compareTo(chartData.get(i).value()));
                assertEquals(0, BigDecimal.valueOf(100.1).compareTo(chartData.get(i).invested()));
            }
        }
    }
}