package org.example.backend.models;

import org.example.backend.dtos.TransactionInDto;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(@Id String id, String ticker, String assetname, BigDecimal cost, BigDecimal shares,
                          Instant time, BigDecimal fee) {

    public Transaction(String id, TransactionInDto tid) {
        this(id, tid.ticker(), tid.assetname(), tid.cost(), tid.shares(), tid.time(), tid.fee());
    }

}
