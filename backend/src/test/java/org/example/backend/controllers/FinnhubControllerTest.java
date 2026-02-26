package org.example.backend.controllers;

import org.example.backend.components.LivePriceStore;
import org.example.backend.enums.AssetType;
import org.example.backend.models.AppUser;
import org.example.backend.models.Asset;
import org.example.backend.repositories.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@AutoConfigureWebTestClient
class FinnhubControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MockRestServiceServer mockServer;
    @Autowired
    private LivePriceStore livePriceStore;
    @Autowired
    private AppUserRepository appUserRepository;




    private final Asset asset1 = new Asset("BTC", BigDecimal.valueOf(0.001), "Bitcoin", BigDecimal.valueOf(100), AssetType.CRYPTO);
    private final Asset asset2 = new Asset("AAPL", BigDecimal.valueOf(1.4), "Apple Inc.", BigDecimal.valueOf(100), AssetType.STOCK);
    private final AppUser appUser1 = new AppUser("abc", "Rainer Zufall", List.of(asset1, asset2));

    @Test
    void getLivePrices_shouldReturnPrices() throws Exception {
        livePriceStore.updatePrice("AAPL", BigDecimal.valueOf(200));
        livePriceStore.updatePrice("BINANCE:BTCUSDT", BigDecimal.valueOf(70000));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/live"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                                    {
                                    "AAPL": 200,
                                    "BINANCE:BTCUSDT": 70000
                                    }
                        """));
    }

    @Test
    void addSymbolsFromAssets_shouldAddSymbols() throws Exception {
        appUserRepository.save(appUser1);
        mockServer.expect(requestTo("https://finnhub.io/api/v1/crypto/symbol?exchange=binance&X-Finnhub-Token="))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                        {
                                "description": "Binance BTC/USDT",
                                "displaySymbol": "BTC/USDT",
                                "symbol": "BINANCE:BTCUSDT"
                            }
                            ]
                        """, MediaType.APPLICATION_JSON));
        mockServer.expect(requestTo("https://finnhub.io/api/v1/search?q=AAPL&exchange=US&X-Finnhub-Token="))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"count":1,
                        "result":
                        [
                        {
                                "description": "Apple Inc.",
                                "displaySymbol": "AAPL",
                                "symbol": "AAPL",
                                "type":"Common Stock"
                            }
                            ]
                        }""", MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/live").with(oidcLogin().userInfoToken(token-> token.claim("id", "abc"))))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }
}