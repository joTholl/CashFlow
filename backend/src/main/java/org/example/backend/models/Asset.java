package org.example.backend.models;

import lombok.With;
import org.example.backend.enums.AssetType;

import java.math.BigDecimal;

@With
public record Asset(String ticker, BigDecimal shares, String assetName, BigDecimal cost, AssetType assetType) {
}
