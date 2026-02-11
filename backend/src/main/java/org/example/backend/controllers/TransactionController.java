package org.example.backend.controllers;

import lombok.RequiredArgsConstructor;
import org.example.backend.dtos.TransactionInDto;
import org.example.backend.dtos.TransactionOutDto;
import org.example.backend.services.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public List<TransactionOutDto> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/{id}")
    public TransactionOutDto getTransactionById(@PathVariable String id) {
        return transactionService.getTransactionById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionOutDto addTransaction(@RequestBody TransactionInDto tid, @AuthenticationPrincipal OAuth2User user) {
        return transactionService.addTransaction(tid, user.getAttribute("id").toString());
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionOutDto addTransaction(@PathVariable String id, @RequestBody TransactionInDto tid, @AuthenticationPrincipal OAuth2User user) {
        return transactionService.updateTransaction(id, tid, user.getAttribute("id").toString());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable String id, @AuthenticationPrincipal OAuth2User user) {
        transactionService.deleteTransaction(id, user.getAttribute("id").toString());
    }


}
