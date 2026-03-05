package org.example.backend.components;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class LivePriceStoreTest {

    LivePriceStore livePriceStore = new LivePriceStore();

    @Test
    void updatePrice_shouldAddSymbolAndPrice() {
        Map<String, BigDecimal> priceMap = new ConcurrentHashMap<>();
        priceMap.put("AAPL", BigDecimal.valueOf(100));
        livePriceStore.updatePrice("AAPL", BigDecimal.valueOf(100));
        assertEquals(priceMap,livePriceStore.getAllPrices());
    }

    @Test
    void updatePrice_shouldChangePriceOfExistingSymbol() {
        Map<String, BigDecimal> priceMap = new ConcurrentHashMap<>();
        priceMap.put("AAPL", BigDecimal.valueOf(50));
        livePriceStore.updatePrice("AAPL", BigDecimal.valueOf(100));
        livePriceStore.updatePrice("AAPL", BigDecimal.valueOf(50));
        assertEquals(priceMap,livePriceStore.getAllPrices());
    }
}