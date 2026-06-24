// src/main/java/com/cafesaas/security/RateLimitInterceptor.java
package com.cafesaas.security;

import jakarta.servlet.http.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Max 10 requests per minute per IP on auth endpoints
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = request.getRemoteAddr();
        requestCounts.putIfAbsent(ip, new AtomicInteger(0));

        int count = requestCounts.get(ip).incrementAndGet();
        if (count > 10) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Too many requests — try again later");
            return false;
        }

        // Reset counter every 60 seconds
        Executors.newSingleThreadScheduledExecutor()
                .schedule(() -> requestCounts.remove(ip), 60, TimeUnit.SECONDS);

        return true;
    }
}