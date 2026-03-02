package org.example.backend.models;

import java.math.BigDecimal;

public record EODHDResponse(String date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal adjusted_close, Long volume) {
}
