package org.example.backend.services;

import org.example.backend.models.*;
import org.example.backend.components.FinnhubWebSocketClient;
import org.example.backend.components.LivePriceStore;
import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.enums.AssetType;
import org.example.backend.exceptions.SymbolNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;


class FinnhubServiceTest {
    private final FinnhubWebSocketClient finnhubWebSocketClient = mock(FinnhubWebSocketClient.class);
    private final LivePriceStore livePriceStore = mock(LivePriceStore.class);
    private final AppUserService appUserService = mock(AppUserService.class);
    public RestClient.Builder restClientBuilder;
    private MockRestServiceServer mockServer;

    private FinnhubService finnhubService;

    private final AppUser appUser1 = new AppUser("abc", "Rainer Zufall", List.of(new Asset("BTC", BigDecimal.valueOf(0.01), "Bitcoin", BigDecimal.valueOf(1000), AssetType.CRYPTO)));
    private final AppUser appUser2 = new AppUser("abc", "Rainer Zufall", List.of(new Asset("MSFT", BigDecimal.valueOf(1), "Microsoft", BigDecimal.valueOf(200), AssetType.STOCK)));

    @BeforeEach
    void setUp() {
        restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        finnhubService = new FinnhubService(finnhubWebSocketClient, livePriceStore, appUserService, restClientBuilder);
    }

    @Test
    void getLivePrices_shouldReturnCorrectMap() {
        Map<String, BigDecimal> map = Map.of("AAPL", BigDecimal.valueOf(100), "BTC", BigDecimal.valueOf(100000));
        when(livePriceStore.getAllPrices()).thenReturn(map);
        assertEquals(map, finnhubService.getLivePrices());
    }

    @Test
    void addSymbolsFromAssets_shouldAddCryptoAssets() {
        when(appUserService.getAppUser("abc")).thenReturn(new AppUserOutDto(appUser1));
        mockServer.expect(requestTo("https://finnhub.io/api/v1/crypto/symbol?exchange=binance&X-Finnhub-Token=")).andRespond(withSuccess("""
                    [
                    {
                        "description": "Binance BTC/USDT",
                        "displaySymbol": "BTC/USDT",
                        "symbol": "BINANCE:BTCUSDT"
                    }
                    ]
                """, MediaType.APPLICATION_JSON));
        finnhubService.addSymbolsFromAssets("abc");
        verify(appUserService).getAppUser("abc");
        verify(finnhubWebSocketClient).addSymbol("BINANCE:BTCUSDT");
    }

    @Test
    void addSymbolsFromAssets_shouldAddStockAssets() {
        when(appUserService.getAppUser("abc")).thenReturn(new AppUserOutDto(appUser2));
        mockServer.expect(requestTo("https://finnhub.io/api/v1/search?q=MSFT&exchange=US&X-Finnhub-Token=")).andRespond(withSuccess("""
                {"count":1,
                "result":
                [
                {
                    "description": "Microsoft",
                    "displaySymbol": "MSFT",
                    "symbol": "MSFT",
                    "type":"Common Stock"
                }
                ]
                }
                """, MediaType.APPLICATION_JSON));
        finnhubService.addSymbolsFromAssets("abc");
        verify(appUserService).getAppUser("abc");
        verify(finnhubWebSocketClient).addSymbol("MSFT");
    }

    @Test
    void addSymbolsFromAssets_shouldThrowException_whenCryptoSymbolNotFound() {
        when(appUserService.getAppUser("abc")).thenReturn(new AppUserOutDto(appUser1));
        mockServer.expect(requestTo("https://finnhub.io/api/v1/crypto/symbol?exchange=binance&X-Finnhub-Token=")).andRespond(withSuccess("""
                [
                ]
                """, MediaType.APPLICATION_JSON));
        Exception e = assertThrows(SymbolNotFoundException.class, () -> finnhubService.addSymbolsFromAssets("abc"));
        assertEquals("Crypto Symbol not found: BINANCE:BTCUSDT", e.getMessage());
        verifyNoInteractions(finnhubWebSocketClient);
        verify(appUserService).getAppUser("abc");
    }

    @Test
    void addSymbolsFromAssets_shouldThrowException_whenStockSymbolNotFound() {
        when(appUserService.getAppUser("abc")).thenReturn(new AppUserOutDto(appUser2));
        mockServer.expect(requestTo("https://finnhub.io/api/v1/search?q=MSFT&exchange=US&X-Finnhub-Token=")).andRespond(withSuccess("""
                {"count":0,
                "result":
                [
                ]
                }
                """, MediaType.APPLICATION_JSON));
        Exception e = assertThrows(SymbolNotFoundException.class, () -> finnhubService.addSymbolsFromAssets("abc"));
        verify(appUserService).getAppUser("abc");
        verifyNoInteractions(finnhubWebSocketClient);
        assertEquals("Stock Symbol not found: MSFT", e.getMessage());
    }

    @Test
    void addSymbol_shouldAddStockSymbol() {
        mockServer.expect(requestTo("https://finnhub.io/api/v1/search?q=MSFT&exchange=US&X-Finnhub-Token=")).andRespond(withSuccess("""
                {"count":1,
                "result":
                [
                {
                    "description": "Microsoft",
                    "displaySymbol": "MSFT",
                    "symbol": "MSFT",
                    "type":"Common Stock"
                }
                ]
                }
                """, MediaType.APPLICATION_JSON));
        finnhubService.addSymbol("MSFT", AssetType.STOCK);
        verify(finnhubWebSocketClient).addSymbol("MSFT");
    }

    @Test
    void addSymbol_shouldAddCryptoSymbol() {
        mockServer.expect(requestTo("https://finnhub.io/api/v1/crypto/symbol?exchange=binance&X-Finnhub-Token=")).andRespond(withSuccess("""
                    [
                    {
                        "description": "Binance BTC/USDT",
                        "displaySymbol": "BTC/USDT",
                        "symbol": "BINANCE:BTCUSDT"
                    }
                    ]
                """, MediaType.APPLICATION_JSON));
        finnhubService.addSymbol("BTC", AssetType.CRYPTO);
        verify(finnhubWebSocketClient).addSymbol("BINANCE:BTCUSDT");
    }

    @Test
    void addSymbol_shouldThrowException_whenCryptoSymbolNotFound() {
        mockServer.expect(requestTo("https://finnhub.io/api/v1/crypto/symbol?exchange=binance&X-Finnhub-Token=")).andRespond(withSuccess("""
                [
                ]
                """, MediaType.APPLICATION_JSON));
        Exception e = assertThrows(SymbolNotFoundException.class, () -> finnhubService.addSymbol("BTC", AssetType.CRYPTO));
        verifyNoInteractions(finnhubWebSocketClient);
        assertEquals("Symbol not found: BINANCE:BTCUSDT", e.getMessage());
    }

    @Test
    void addSymbol_shouldThrowException_whenStockSymbolNotFound() {
        mockServer.expect(requestTo("https://finnhub.io/api/v1/search?q=MSFT&exchange=US&X-Finnhub-Token=")).andRespond(withSuccess("""
                {"count":0,
                "result":
                [
                ]
                }
                """, MediaType.APPLICATION_JSON));
        Exception e = assertThrows(SymbolNotFoundException.class, () -> finnhubService.addSymbol("MSFT", AssetType.STOCK));
        verifyNoInteractions(finnhubWebSocketClient);
        assertEquals("Symbol not found: MSFT", e.getMessage());
    }

    @Test
    void removeSymbol_shouldRemoveSymbol() {
        finnhubService.removeSymbol("MSFT");
        verify(finnhubWebSocketClient).removeSymbol("MSFT");
    }
}