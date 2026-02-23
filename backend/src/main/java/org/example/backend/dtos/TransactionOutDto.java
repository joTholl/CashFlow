package org.example.backend.dtos;

import lombok.With;
import org.example.backend.enums.AssetType;
import org.example.backend.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@With
public record TransactionOutDto(String id, String ticker, String assetName, BigDecimal cost, BigDecimal shares,
                                LocalDateTime timestamp, BigDecimal fee, AssetType assetType) {

    public TransactionOutDto(Transaction transaction) {
        this(transaction.id(), transaction.ticker(), transaction.assetName(), transaction.cost(), transaction.shares(), transaction.timestamp().atZone(ZoneId.systemDefault()).toLocalDateTime(), transaction.fee(), transaction.assetType());
    }
}
