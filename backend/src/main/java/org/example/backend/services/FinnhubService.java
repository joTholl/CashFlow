package org.example.backend.services;

import org.example.backend.components.FinnhubWebSocketClient;
import org.example.backend.components.LivePriceStore;
import org.example.backend.dtos.AppUserOutDto;
import org.example.backend.enums.AssetType;
import org.example.backend.exceptions.SymbolNotFoundException;
import org.example.backend.models.Asset;
import org.example.backend.models.FinnhubCryptoEntry;
import org.example.backend.models.FinnhubCryptoList;
import org.example.backend.models.FinnhubSearchResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class FinnhubService {

    private final FinnhubWebSocketClient finnhubWebSocketClient;
    private final LivePriceStore livePriceStore;
    private final AppUserService appUserService;
    public final RestClient restClient;

    public FinnhubService(FinnhubWebSocketClient finnhubWebSocketClient, LivePriceStore livePriceStore, AppUserService appUserService, RestClient.Builder restClientBuilder) {
        this.finnhubWebSocketClient = finnhubWebSocketClient;
        this.livePriceStore = livePriceStore;
        this.appUserService = appUserService;
        this.restClient = restClientBuilder.baseUrl("https://https://finnhub.io/api/v1").build();
    }

    public Map<String, BigDecimal> getLivePrices() {
        return livePriceStore.getAllPrices();
    }

    public void addSymbolsFromAssets(String id) {
        AppUserOutDto user = appUserService.getAppUser(id);
        if (!user.assets().isEmpty()) {
            for (Asset asset : user.assets()) {
                if (asset.ticker() == null || !isStockSymbolExisting(asset.ticker())) {
                    throw new SymbolNotFoundException("Symbol not found: " + asset.ticker());
                }
                finnhubWebSocketClient.addSymbol(asset.ticker());
            }
        }
    }

    public void addSymbol(String ticker, AssetType assetType) {
        boolean symbolExists = false;
        if (assetType == AssetType.STOCK) {
            symbolExists = isStockSymbolExisting(ticker);
        } else if (assetType == AssetType.CRYPTO) {
            symbolExists = isCryptoSymbolExisting(ticker);
        }
        if (!symbolExists) {
            throw new SymbolNotFoundException("Symbol not found: " + ticker);
        }
        finnhubWebSocketClient.addSymbol(ticker);
    }

    private boolean isStockSymbolExisting(String ticker) {
        FinnhubSearchResponse finnhubSearchResponse = restClient.get().uri("/search?q={symbol}&exchange=US", ticker).retrieve().body(FinnhubSearchResponse.class);
        if (finnhubSearchResponse == null || finnhubSearchResponse.result() == null) {
            return false;
        } else {
            return finnhubSearchResponse.result().getFirst().symbol().equals(ticker);
        }
    }

    private boolean isCryptoSymbolExisting(String ticker) {
        List<FinnhubCryptoEntry> finnhubCryptoList = restClient.get().uri("/crypto/symbol?exchange=binance").retrieve().body(FinnhubCryptoList.class).finnhubCryptoEntries();
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
