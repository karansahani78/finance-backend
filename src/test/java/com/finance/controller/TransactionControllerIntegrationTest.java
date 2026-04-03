package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.BaseIntegrationTest;
import com.finance.TestTokenHelper;
import com.finance.dto.request.CreateTransactionRequest;
import com.finance.model.enums.TransactionCategory;
import com.finance.model.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransactionControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private TestTokenHelper tokenHelper;

    @BeforeEach
    void setUp() {
        tokenHelper = new TestTokenHelper(mockMvc, objectMapper);
    }

    @Test
    void adminCanCreateTransaction() throws Exception {
        String token = tokenHelper.getAdminToken();

        CreateTransactionRequest request = buildRequest(
                new BigDecimal("5000.00"), TransactionType.INCOME,
                TransactionCategory.SALARY, LocalDate.now().minusDays(1),
                "March salary"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", is(5000.00)))
                .andExpect(jsonPath("$.type", is("INCOME")))
                .andExpect(jsonPath("$.category", is("SALARY")));
    }

    @Test
    void viewerCannotCreateTransaction() throws Exception {
        String token = tokenHelper.getViewerToken();

        CreateTransactionRequest request = buildRequest(
                new BigDecimal("100.00"), TransactionType.EXPENSE,
                TransactionCategory.FOOD, LocalDate.now().minusDays(1),
                "Lunch"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCannotCreateTransaction() throws Exception {
        String token = tokenHelper.getAnalystToken();

        CreateTransactionRequest request = buildRequest(
                new BigDecimal("200.00"), TransactionType.EXPENSE,
                TransactionCategory.TRANSPORT, LocalDate.now().minusDays(2),
                "Cab fare"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCanListTransactions() throws Exception {
        String token = tokenHelper.getViewerToken();

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.page", is(0)));
    }

    @Test
    void listTransactionsSupportsTypeFilter() throws Exception {
        String token = tokenHelper.getAdminToken();

        mockMvc.perform(get("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "INCOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].type", everyItem(is("INCOME"))));
    }

    @Test
    void createTransactionWithFutureDateReturns400() throws Exception {
        String token = tokenHelper.getAdminToken();

        CreateTransactionRequest request = buildRequest(
                new BigDecimal("100.00"), TransactionType.EXPENSE,
                TransactionCategory.FOOD, LocalDate.now().plusDays(5),
                "Future expense"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.txnDate", notNullValue()));
    }

    @Test
    void createTransactionWithNegativeAmountReturns400() throws Exception {
        String token = tokenHelper.getAdminToken();

        CreateTransactionRequest request = buildRequest(
                new BigDecimal("-50.00"), TransactionType.EXPENSE,
                TransactionCategory.FOOD, LocalDate.now().minusDays(1),
                "Negative test"
        );

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount", notNullValue()));
    }

    @Test
    void unauthenticatedRequestReturns403() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTransactionByAdmin() throws Exception {
        String token = tokenHelper.getAdminToken();

        CreateTransactionRequest request = buildRequest(
                new BigDecimal("300.00"), TransactionType.EXPENSE,
                TransactionCategory.SHOPPING, LocalDate.now().minusDays(3),
                "Shopping spree"
        );

        String createBody = mockMvc.perform(post("/api/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(createBody).get("id").asLong();

        mockMvc.perform(delete("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transactions/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private CreateTransactionRequest buildRequest(
            BigDecimal amount, TransactionType type,
            TransactionCategory category, LocalDate date, String description
    ) {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setAmount(amount);
        req.setType(type);
        req.setCategory(category);
        req.setTxnDate(date);
        req.setDescription(description);
        return req;
    }
}
