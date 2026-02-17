package org.example.backend.controllers;

import org.example.backend.models.AppUser;
import org.example.backend.models.Asset;
import org.example.backend.models.Transaction;
import org.example.backend.repositories.AppUserRepository;
import org.example.backend.repositories.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

@AutoConfigureMockMvc
@SpringBootTest
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private AppUserRepository appUserRepository;

    private final Transaction transaction1 = new Transaction("zyx", "BTC", "Bitcoin", BigDecimal.valueOf(100), BigDecimal.valueOf(0.001), Instant.parse("2026-02-12T10:00:00Z"), BigDecimal.valueOf(0.1));
    private final Transaction transaction2 = new Transaction("abc", "ETH", "Ethereum", BigDecimal.valueOf(1000), BigDecimal.valueOf(0.33), Instant.parse("2026-02-12T11:00:00Z"), BigDecimal.valueOf(0.2));

    private final Asset asset1 = new Asset("BTC", BigDecimal.valueOf(0.001), "Bitcoin", BigDecimal.valueOf(100));
    private final AppUser appUser1 = new AppUser("abc", "Rainer Zufall", List.of(asset1));

    private final String transaction1JSON = """
            {
                "id": "zyx",
                "ticker": "BTC",
                "assetName": "Bitcoin",
                "cost": 100,
                "shares": 0.001,
                "timestamp": "2026-02-12T11:00:00",
                "fee": 0.1
            }
            """;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        appUserRepository.deleteAll();
        transactionRepository.save(transaction1);
        appUserRepository.save(appUser1);
    }

    @Test
    void getAllTransactions_shouldReturnAllTransactions() throws Exception {
        transactionRepository.save(transaction2);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("""
                        [
                          {
                            "id": "zyx",
                            "ticker": "BTC",
                            "assetName": "Bitcoin",
                            "cost": 100,
                            "shares": 0.001,
                            "timestamp": "2026-02-12T11:00:00",
                            "fee": 0.1
                          },
                          {
                            "id": "abc",
                            "ticker": "ETH",
                            "assetName": "Ethereum",
                            "cost": 1000,
                            "shares": 0.33,
                            "timestamp": "2026-02-12T12:00:00",
                            "fee": 0.2
                          }
                        ]
                        
                        """));
    }

    @Test
    void getTransactionById_shouldReturnTransaction() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions/zyx"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(transaction1JSON));
    }

    @Test
    void getTransactionById_shouldThrowException_whenCalledWithWrongId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions/ghwsh"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void addTransaction_shouldAddTransaction() throws Exception {
        transactionRepository.deleteAll();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/transactions")
                        .with(oidcLogin().userInfoToken(token -> token.claim("id", "abc")))
                        .contentType(APPLICATION_JSON).content(transaction1JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.ticker").value("BTC"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.assetName").value("Bitcoin"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cost").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fee").value(0.1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value("2026-02-12T11:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.shares").value(0.001));
    }

    @Test
    void addTransaction_shouldThrowException_whenCalledWithWrongUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transactions/zyx")
                        .with(oidcLogin().userInfoToken(token -> token.claim("id", "sdhg"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void updateTransaction_shouldUpdateTransaction() throws Exception {
        String newTransactionJSON = """
                {
                      "id": "zyx",
                      "ticker": "LTC",
                      "assetName": "Litecoin",
                      "cost": 100,
                      "shares": 0.001,
                      "timestamp": "2026-02-12T11:00:00",
                      "fee": 0.1
                }
                """;
        mockMvc.perform(MockMvcRequestBuilders.put("/api/transactions/zyx")
                        .with(oidcLogin().userInfoToken(token -> token.claim("id", "abc")))
                        .contentType(APPLICATION_JSON).content(newTransactionJSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(newTransactionJSON));
    }

    @Test
    void updateTransaction_shouldThrowException_whenCalledWithWrongId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions/ghwsh"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void updateTransaction_shouldThrowException_whenCalledWithWrongUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transactions/zyx")
                        .with(oidcLogin().userInfoToken(token -> token.claim("id", "sdhg"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void deleteTransaction_shouldDeleteTransaction() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transactions/zyx")
                        .with(oidcLogin().userInfoToken(token -> token.claim("id", "abc"))))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void deleteTransaction_shouldThrowException_whenCalledWithWrongId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/transactions/ghwsh"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void deleteTransaction_shouldThrowException_whenCalledWithWrongUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/transactions/zyx")
                        .with(oidcLogin().userInfoToken(token -> token.claim("id", "sdhg"))))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}