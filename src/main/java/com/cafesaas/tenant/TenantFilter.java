package com.cafesaas.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Servlet filter — runs before Spring Security.
 * @Order(1) ensures this is the very first filter in the chain.
 *
 * Why a plain Filter instead of OncePerRequestFilter?
 * OncePerRequestFilter skips on async dispatches and error dispatches.
 * A plain Filter runs on EVERY dispatch — ensuring tenant context is
 * always present even on forwarded or error requests.
 */
@Component
@Order(1)
public class TenantFilter implements Filter {

    public static final String TENANT_HEADER = "X-Tenant-ID";

    private static final String[] PUBLIC_PATHS = {
            "/actuator/health",
            "/api/auth/register-tenant"
    };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  httpReq = (HttpServletRequest) req;
        HttpServletResponse httpRes = (HttpServletResponse) res;

        // Skip tenant resolution for truly public endpoints
        String path = httpReq.getRequestURI();
        for (String pub : PUBLIC_PATHS) {
            if (path.startsWith(pub)) {
                chain.doFilter(req, res);
                return;
            }
        }

        String tenantId = httpReq.getHeader(TENANT_HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            httpRes.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing required header: " + TENANT_HEADER);
            return;
        }

        TenantContext.setCurrentTenant(tenantId.trim());
        try {
            chain.doFilter(req, res);
        } finally {
            // ALWAYS runs — even on exceptions, even on async completions.
            // This is the leak prevention. Without it, the next request on
            // this pooled thread would start with the wrong tenant.
            TenantContext.clear();
        }
    }
}