package org.example.backend.models;

import lombok.With;
import org.example.backend.dtos.TransactionInDto;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;

@With
public record Transaction(@Id String id, String ticker, String assetName, BigDecimal cost, BigDecimal shares,
                          Instant timestamp, BigDecimal fee) {

    public Transaction(String id, TransactionInDto tid) {
        this(id, tid.ticker(), tid.assetName(), tid.cost(), tid.shares(), tid.timestamp(), tid.fee());
    }

}
