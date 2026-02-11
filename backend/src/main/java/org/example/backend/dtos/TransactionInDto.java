package org.example.backend.dtos;


import java.math.BigDecimal;
import java.time.Instant;

public record TransactionInDto(String ticker, String assetname, BigDecimal cost, BigDecimal shares, Instant time, BigDecimal fee) {


}
