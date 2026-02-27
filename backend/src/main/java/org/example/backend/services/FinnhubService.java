package org.example.backend.services;

import org.example.backend.components.FinnhubWebSocketClient;
import org.example.backend.components.LivePriceStore;
import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.enums.AssetType;
import org.example.backend.exceptions.SymbolNotFoundException;
import org.example.backend.models.Asset;
import org.example.backend.models.FinnhubCryptoEntry;
import org.example.backend.models.FinnhubSearchResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class FinnhubService {

    private final FinnhubWebSocketClient finnhubWebSocketClient;
    private final LivePriceStore livePriceStore;
    private final AppUserService appUserService;
    public final RestClient restClient;
    private FinnhubCryptoEntry[] finnhubCryptoList;

    public FinnhubService(FinnhubWebSocketClient finnhubWebSocketClient, LivePriceStore livePriceStore, AppUserService appUserService, RestClient.Builder restClientBuilder) {
        this.finnhubWebSocketClient = finnhubWebSocketClient;
        this.livePriceStore = livePriceStore;
        this.appUserService = appUserService;
        this.restClient = restClientBuilder.baseUrl("https://finnhub.io/api/v1")
                .build();

    }

    public Map<String, BigDecimal> getLivePrices() {
        return livePriceStore.getAllPrices();
    }

    public void addSymbolsFromAssets(String id) {
        AppUserOutDto user = appUserService.getAppUser(id);
        if (!user.assets().isEmpty()) {
            for (Asset asset : user.assets()) {
                String ticker = asset.assetType() == AssetType.CRYPTO ? "BINANCE:" + asset.ticker() + "USDT" : asset.ticker();
                if (asset.assetType() == AssetType.STOCK && !isStockSymbolExisting(ticker)) {
                    throw new SymbolNotFoundException("Stock Symbol not found: " + ticker);
                } else if (asset.assetType() == AssetType.CRYPTO && !isCryptoSymbolExisting(ticker)) {
                    throw new SymbolNotFoundException("Crypto Symbol not found: " + ticker);
                }
                finnhubWebSocketClient.addSymbol(ticker);
            }
        }
    }

    public void addSymbol(String ticker, AssetType assetType) {
        boolean symbolExists = false;
        if (assetType == AssetType.STOCK) {
            symbolExists = isStockSymbolExisting(ticker);
        } else if (assetType == AssetType.CRYPTO) {
            ticker = "BINANCE:" + ticker + "USDT";
            symbolExists = isCryptoSymbolExisting(ticker);
        }
        if (!symbolExists) {
            throw new SymbolNotFoundException("Symbol not found: " + ticker);
        }
        finnhubWebSocketClient.addSymbol(ticker);
    }
    @Retryable(
            retryFor = {
                    HttpClientErrorException.TooManyRequests.class,
                    HttpServerErrorException.class,
                    ResourceAccessException.class
            },
            maxAttempts = 6,
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2
            )
    )
    boolean isStockSymbolExisting(String ticker) {
        FinnhubSearchResponse finnhubSearchResponse = restClient.get().uri("/search?q={ticker}&exchange=US&token={token}", ticker, System.getenv("FINNHUB_API_TOKEN"))
                .retrieve().body(FinnhubSearchResponse.class);
        if (finnhubSearchResponse == null || finnhubSearchResponse.result() == null || finnhubSearchResponse.result().isEmpty()) {
            return false;
        } else {
            return finnhubSearchResponse.result().getFirst().symbol().equals(ticker);
        }
    }
    @Retryable(
            retryFor = {
                    HttpClientErrorException.TooManyRequests.class,
                    HttpServerErrorException.class,
                    ResourceAccessException.class
            },
            maxAttempts = 6,
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2
            )
    )
    boolean isCryptoSymbolExisting(String ticker) {
        if (finnhubCryptoList == null || finnhubCryptoList.length == 0) {
            finnhubCryptoList = restClient.get().uri("/crypto/symbol?exchange=binance&token=" + System.getenv("FINNHUB_API_TOKEN"))
                    .retrieve().toEntity(FinnhubCryptoEntry[].class).getBody();
        }
        for (FinnhubCryptoEntry finnhubCryptoEntry : finnhubCryptoList) {
            if (finnhubCryptoEntry.symbol().equals(ticker)) {
                return true;
            }
        }
        return false;
    }

    public void removeSymbol(String symbol) {
        finnhubWebSocketClient.removeSymbol(symbol);
    }
}
