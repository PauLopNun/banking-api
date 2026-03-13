package com.gft.banking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gft.banking.api.dto.AccountDTO;
import com.gft.banking.api.dto.AuthDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("dev")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;
    private String otherToken;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        token = registerAndGetToken("testuser-" + suffix, "1234");
        otherToken = registerAndGetToken("otheruser-" + suffix, "1234");
    }

    private String registerAndGetToken(String username, String password) throws Exception {
        AuthDTO.AuthRequest authRequest = new AuthDTO.AuthRequest();
        authRequest.setUsername(username);
        authRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    void shouldCreateAccountAndReturnCreated() throws Exception {
        AccountDTO.CreateAccountRequest request = new AccountDTO.CreateAccountRequest();
        request.setOwnerName("Pau López");
        request.setInitialBalance(new BigDecimal("1000"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerName").value("Pau López"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnBadRequestWhenOwnerNameIsBlank() throws Exception {
        AccountDTO.CreateAccountRequest request = new AccountDTO.CreateAccountRequest();
        request.setOwnerName("");
        request.setInitialBalance(new BigDecimal("1000"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void shouldReturnOnlyAccountsOfAuthenticatedUser() throws Exception {
        AccountDTO.CreateAccountRequest request = new AccountDTO.CreateAccountRequest();
        request.setOwnerName("Pau López");
        request.setInitialBalance(new BigDecimal("1000"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // otheruser no debe ver la cuenta de testuser
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // testuser sí la ve
        mockMvc.perform(get("/api/accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ownerName").value("Pau López"));
    }

    @Test
    void shouldReturnNotFoundWhenAccessingAnotherUsersAccount() throws Exception {
        AccountDTO.CreateAccountRequest request = new AccountDTO.CreateAccountRequest();
        request.setOwnerName("Cuenta de Pau");
        request.setInitialBalance(new BigDecimal("500"));

        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        // otheruser intenta acceder — debe recibir error
        mockMvc.perform(get("/api/accounts/" + id)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAccountWithZeroBalance() throws Exception {
        AccountDTO.CreateAccountRequest request = new AccountDTO.CreateAccountRequest();
        request.setOwnerName("Cuenta temporal");
        request.setInitialBalance(BigDecimal.ZERO);

        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(delete("/api/accounts/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}