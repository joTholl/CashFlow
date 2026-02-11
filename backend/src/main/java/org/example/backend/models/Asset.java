package org.example.backend.models;

import java.math.BigDecimal;

public record Asset(String ticker, BigDecimal shares, String assetName, BigDecimal cost) {
}
