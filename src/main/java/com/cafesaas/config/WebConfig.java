// src/main/java/com/cafesaas/config/WebConfig.java
package com.cafesaas.config;

import com.cafesaas.security.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;


    // this is an in-memory rate limiter,
    // which means it resets on restart and doesn't work across multiple instances.

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/auth/**");  // only on auth endpoints
    }
}