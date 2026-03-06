package org.example.backend.components;

import org.example.backend.models.HistoricalEntry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LivePriceStore {
    private final Map<String, BigDecimal> prices = new ConcurrentHashMap<>();


    public void updatePrice(String symbol, BigDecimal price) {
        if (symbol.contains("BINANCE:")) {
            symbol = symbol.replace("BINANCE:", "");
            symbol = symbol.substring(0, symbol.length() - 4);
        }
        prices.put(symbol, price);
    }

    public void safeUpdatePrices(Map<String, HistoricalEntry> historicalEntries, LocalDate yesterday) {
        for (Map.Entry<String, HistoricalEntry> entry : historicalEntries.entrySet()) {
            if (!prices.containsKey(entry.getKey())) {
                updatePrice(entry.getKey(), entry.getValue().closePrice().get(yesterday));
            }
        }
    }

    public Map<String, BigDecimal> getAllPrices() {
        return prices;
    }
}
