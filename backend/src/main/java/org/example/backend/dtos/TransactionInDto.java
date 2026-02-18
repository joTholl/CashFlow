package org.example.backend.dtos;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionInDto(String ticker, String assetName, BigDecimal cost, BigDecimal shares, LocalDateTime timestamp,
                               BigDecimal fee) {


}
