package org.example.backend.services;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtos.TransactionInDto;
import org.example.backend.dtos.TransactionOutDto;
import org.example.backend.models.Transaction;
import org.example.backend.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final HelperService helperService;
    private final AppUserService appUserService;
    private final FinnhubService finnhubService;
    private static final String TRANSACTION_NOT_FOUND = "Transaction not found";


    public List<TransactionOutDto> getAllTransactions() {
        List<Transaction>  transactions = transactionRepository.findAll();
        List<TransactionOutDto> tods = new ArrayList<>();
        for(Transaction transaction : transactions) {
            tods.add(new TransactionOutDto(transaction));
        }
        return tods;
    }

    public TransactionOutDto getTransactionById(String id) {
        return new TransactionOutDto(transactionRepository.findById(id).orElseThrow(() -> new NoSuchElementException(TRANSACTION_NOT_FOUND)));
    }

    public TransactionOutDto addTransaction(TransactionInDto tid, String userId) {
        Transaction transaction = transactionRepository.save(new Transaction(helperService.getRandomId(), tid));
        appUserService.addTransaction(transaction, userId);
        finnhubService.addSymbol(tid.ticker(),tid.assetType());
        return new TransactionOutDto(transaction);
    }

    public TransactionOutDto updateTransaction(String transactionId, TransactionInDto tid, String userId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new NoSuchElementException(TRANSACTION_NOT_FOUND));
        Transaction updateTransaction = new Transaction(transactionId, tid);
        if (transaction.ticker().equals(tid.ticker()) && !transaction.assetName().equals(tid.assetName()) ||
                !transaction.ticker().equals(tid.ticker()) && transaction.assetName().equals(tid.assetName())) {
            throw new IllegalArgumentException("Ticker and Assetname have both to change or none of them!");
        } else {
            appUserService.subtractTransaction(transaction, userId);
            finnhubService.removeSymbol(transaction.ticker());
            appUserService.addTransaction(updateTransaction, userId);
            finnhubService.addSymbol(tid.ticker(),tid.assetType());
        }
        return new TransactionOutDto(transactionRepository.save(updateTransaction));
    }

    public void deleteTransaction(String transactionId, String userId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new NoSuchElementException(TRANSACTION_NOT_FOUND));
        appUserService.subtractTransaction(transaction, userId);
        finnhubService.removeSymbol(transaction.ticker());
        transactionRepository.deleteById(transactionId);
    }


}
