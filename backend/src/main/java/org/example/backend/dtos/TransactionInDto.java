package org.example.backend.dtos;


import java.math.BigDecimal;
import java.time.Instant;

public record TransactionInDto(String ticker, String assetName, BigDecimal cost, BigDecimal shares, Instant timestamp,
                               BigDecimal fee) {


}
