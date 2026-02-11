package org.example.backend.models;

import lombok.With;

import java.math.BigDecimal;

@With
public record Asset(String ticker, BigDecimal shares, String assetName, BigDecimal cost) {
}
