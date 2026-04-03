package com.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.BaseIntegrationTest;
import com.finance.TestTokenHelper;
import com.finance.dto.request.CreateUserRequest;
import com.finance.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private TestTokenHelper tokenHelper;

    @BeforeEach
    void setUp() {
        tokenHelper = new TestTokenHelper(mockMvc, objectMapper);
    }

    @Test
    void adminCanCreateUser() throws Exception {
        String token = tokenHelper.getAdminToken();

        CreateUserRequest request = buildCreateRequest("viewer");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(request.getEmail())))
                .andExpect(jsonPath("$.role", is("VIEWER")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    void viewerCannotAccessUserEndpoints() throws Exception {
        String token = tokenHelper.getViewerToken();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCannotAccessUserEndpoints() throws Exception {
        String token = tokenHelper.getAnalystToken();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUserWithDuplicateEmailReturns409() throws Exception {
        String token = tokenHelper.getAdminToken();
        CreateUserRequest request = buildCreateRequest("duptest");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createUserWithShortPasswordReturns400() throws Exception {
        String token = tokenHelper.getAdminToken();

        CreateUserRequest request = new CreateUserRequest();
        request.setName("Test User");
        request.setEmail("short@finance.io");
        request.setPassword("short");
        request.setRole(Role.VIEWER);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password", notNullValue()));
    }

    @Test
    void adminCanListUsersWithPagination() throws Exception {
        String token = tokenHelper.getAdminToken();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.size", is(10)));
    }

    @Test
    void adminCanSearchUsers() throws Exception {
        String token = tokenHelper.getAdminToken();

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .param("search", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email", containsString("admin")));
    }

    @Test
    void softDeletedUserNotReturnedInList() throws Exception {
        String token = tokenHelper.getAdminToken();
        CreateUserRequest request = buildCreateRequest("deleteme");

        String body = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(delete("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private CreateUserRequest buildCreateRequest(String prefix) {
        String unique = prefix + "-" + UUID.randomUUID().toString().substring(0, 6);
        CreateUserRequest req = new CreateUserRequest();
        req.setName("Test " + prefix);
        req.setEmail(unique + "@finance.io");
        req.setPassword("securepass99");
        req.setRole(Role.VIEWER);
        return req;
    }
}
