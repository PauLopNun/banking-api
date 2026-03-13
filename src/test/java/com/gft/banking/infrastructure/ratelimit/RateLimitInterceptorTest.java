package com.gft.banking.infrastructure.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("dev")
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "rate.limit.capacity=1",
        "rate.limit.refill-tokens=1",
        "rate.limit.refill-duration-seconds=60"
})
class RateLimitInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldIncludeRateLimitHeader() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"x\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Rate-Limit-Remaining"));
    }

    @Test
    void shouldReturn429WhenRateLimitExceeded() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"x\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"x\",\"password\":\"x\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("X-Rate-Limit-Remaining", "0"))
                .andExpect(header().exists("Retry-After"));
    }
}