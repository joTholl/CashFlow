package org.example.backend.models;

import lombok.With;

import java.math.BigDecimal;
import java.time.LocalDate;

@With
public record ChartData(LocalDate date, BigDecimal invested, BigDecimal value) {
}
