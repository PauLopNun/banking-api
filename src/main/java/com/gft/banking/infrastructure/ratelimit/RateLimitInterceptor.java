package com.gft.banking.infrastructure.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.time.Duration;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${rate.limit.capacity:60}")
    private long capacity;

    @Value("${rate.limit.refill-tokens:60}")
    private long refillTokens;

    @Value("${rate.limit.refill-duration-seconds:60}")
    private long refillDurationSeconds;

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofSeconds(refillDurationSeconds))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket getBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createBucket());
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {

        String ip = request.getRemoteAddr();
        Bucket bucket = getBucket(ip);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("X-Rate-Limit-Remaining", "0");
        response.addHeader("Retry-After", String.valueOf(Math.max(1, refillDurationSeconds)));
        response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Has superado el límite de peticiones. Espera un minuto.\"}");
        return false;
    }
}