package org.example.backend.dtos;

import lombok.With;
import org.example.backend.models.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

@With
public record TransactionOutDto(String ticker, String assetName, BigDecimal cost, BigDecimal shares, Instant timestamp, BigDecimal fee) {

    public TransactionOutDto(Transaction transaction){
        this(transaction.ticker(), transaction.assetName(), transaction.cost(), transaction.shares(), transaction.timestamp(), transaction.fee());
    }
}
