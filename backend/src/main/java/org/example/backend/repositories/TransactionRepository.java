package org.example.backend.repositories;

import org.example.backend.models.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> getTransactionsByTicker(String ticker);
}
