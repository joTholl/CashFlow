package org.example.backend.components;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LivePriceStore {
    private final Map<String, BigDecimal> prices = new ConcurrentHashMap<>();

    public void updatePrice(String symbol, BigDecimal price) {
        prices.put(symbol, price);
    }

    public Map<String, BigDecimal> getAllPrices() {
        return prices;
    }
}
