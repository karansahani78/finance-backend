package com.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dto.request.LoginRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class TestTokenHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public TestTokenHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public String getAdminToken() throws Exception {
        return fetchToken("admin@finance.io", "password123");
    }

    public String getAnalystToken() throws Exception {
        return fetchToken("analyst@finance.io", "password123");
    }

    public String getViewerToken() throws Exception {
        return fetchToken("viewer@finance.io", "password123");
    }

    private String fetchToken(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
