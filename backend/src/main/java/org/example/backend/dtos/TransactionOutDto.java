package org.example.backend.dtos;

import org.example.backend.models.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionOutDto(String ticker, String assetname, BigDecimal cost, BigDecimal shares, Instant time, BigDecimal fee) {

    public TransactionOutDto(Transaction transaction){
        this(transaction.ticker(), transaction.assetname(), transaction.cost(), transaction.shares(), transaction.time(), transaction.fee());
    }
}
