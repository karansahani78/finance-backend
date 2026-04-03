package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.BaseIntegrationTest;
import com.finance.TestTokenHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DashboardControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private TestTokenHelper tokenHelper;

    @BeforeEach
    void setUp() {
        tokenHelper = new TestTokenHelper(mockMvc, objectMapper);
    }

    @Test
    void analystCanAccessSummary() throws Exception {
        String token = tokenHelper.getAnalystToken();

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome", notNullValue()))
                .andExpect(jsonPath("$.totalExpenses", notNullValue()))
                .andExpect(jsonPath("$.netBalance", notNullValue()))
                .andExpect(jsonPath("$.recentActivity", notNullValue()))
                .andExpect(jsonPath("$.incomeByCategory", notNullValue()))
                .andExpect(jsonPath("$.expenseByCategory", notNullValue()));
    }

    @Test
    void adminCanAccessSummary() throws Exception {
        String token = tokenHelper.getAdminToken();

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void viewerCannotAccessDashboard() throws Exception {
        String token = tokenHelper.getViewerToken();

        mockMvc.perform(get("/api/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotAccessDashboard() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isForbidden());
    }
}
