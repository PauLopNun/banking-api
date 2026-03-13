package com.gft.banking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gft.banking.api.dto.AccountDTO;
import com.gft.banking.api.dto.AuthDTO;
import com.gft.banking.api.dto.TransferDTO;
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
class TransferIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String tokenA;
    private String tokenB;
    private Long accountAId;
    private Long accountBId;

    @BeforeEach
    void setUp() throws Exception {
        tokenA = registerAndGetToken("userA", "1234");
        tokenB = registerAndGetToken("userB", "1234");
        accountAId = createAccount(tokenA, "Cuenta A", new BigDecimal("1000"));
        accountBId = createAccount(tokenB, "Cuenta B", new BigDecimal("500"));
    }

    private String registerAndGetToken(String username, String password) throws Exception {
        AuthDTO.AuthRequest request = new AuthDTO.AuthRequest();
        request.setUsername(username);
        request.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private Long createAccount(String token, String ownerName, BigDecimal balance) throws Exception {
        AccountDTO.CreateAccountRequest request = new AccountDTO.CreateAccountRequest();
        request.setOwnerName(ownerName);
        request.setInitialBalance(balance);

        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    @Test
    void shouldTransferSuccessfully() throws Exception {
        TransferDTO.TransferRequest request = new TransferDTO.TransferRequest();
        request.setFromAccountId(accountAId);
        request.setToAccountId(accountBId);
        request.setAmount(new BigDecimal("200"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenA)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(200))
                .andExpect(jsonPath("$.fromAccountId").value(accountAId))
                .andExpect(jsonPath("$.toAccountId").value(accountBId));
    }

    @Test
    void shouldReturnBadRequestWhenInsufficientFunds() throws Exception {
        TransferDTO.TransferRequest request = new TransferDTO.TransferRequest();
        request.setFromAccountId(accountAId);
        request.setToAccountId(accountBId);
        request.setAmount(new BigDecimal("9999"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenA)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Saldo insuficiente"));
    }

    @Test
    void shouldReturnBadRequestWhenTransferringToSameAccount() throws Exception {
        TransferDTO.TransferRequest request = new TransferDTO.TransferRequest();
        request.setFromAccountId(accountAId);
        request.setToAccountId(accountAId);
        request.setAmount(new BigDecimal("100"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenA)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No puedes transferir a tu propia cuenta"));
    }

    @Test
    void shouldReturnBadRequestWhenAmountIsZero() throws Exception {
        TransferDTO.TransferRequest request = new TransferDTO.TransferRequest();
        request.setFromAccountId(accountAId);
        request.setToAccountId(accountBId);
        request.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenA)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnTransferHistory() throws Exception {
        TransferDTO.TransferRequest request = new TransferDTO.TransferRequest();
        request.setFromAccountId(accountAId);
        request.setToAccountId(accountBId);
        request.setAmount(new BigDecimal("100"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenA)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/transfers/history/" + accountAId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].amount").value(100));
    }

    @Test
    void shouldReturnBadRequestWhenTransferringFromAnotherUsersAccount() throws Exception {
        TransferDTO.TransferRequest request = new TransferDTO.TransferRequest();
        request.setFromAccountId(accountAId);
        request.setToAccountId(accountBId);
        request.setAmount(new BigDecimal("50"));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + tokenB)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cuenta origen no encontrada"));
    }

    @Test
    void shouldReturnBadRequestWhenGettingAnotherUsersHistory() throws Exception {
        mockMvc.perform(get("/api/transfers/history/" + accountAId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cuenta no encontrada con id: " + accountAId));
    }
}
