package com.cafesaas.security;

import com.cafesaas.tenant.TenantFilter;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/**
 * Runs once per request (OncePerRequestFilter).
 * Validates the JWT and populates the Spring Security context.
 *
 * The cross-tenant check is the security-critical part:
 * a JWT for cafe-A should be rejected when used against cafe-B's endpoints,
 * even if the JWT itself is cryptographically valid.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtService.isValid(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        Claims claims      = jwtService.extractClaims(token);
        String userId      = claims.getSubject();
        String tokenTenant = claims.get("tenantId", String.class);
        String role        = claims.get("role", String.class);

        // SUPER_ADMIN can act across tenants — skip the check for them
        if (!Role.SUPER_ADMIN.name().equals(role)) {
            String headerTenant = request.getHeader(TenantFilter.TENANT_HEADER);
            if (!tokenTenant.equals(headerTenant)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Token tenant does not match X-Tenant-ID header");
                return;
            }
        }

        // Populate the Spring Security context so @PreAuthorize can see the role
        var auth = new UsernamePasswordAuthenticationToken(
                userId, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
    }
}