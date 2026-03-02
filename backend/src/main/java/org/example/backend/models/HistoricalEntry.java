package org.example.backend.models;

import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record HistoricalEntry(@Id String ticker, Map<LocalDate, BigDecimal> closePrice) {
}
