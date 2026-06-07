package com.cafesaas.tenant;

/**
 * Holds the current tenant ID for the duration of one HTTP request.
 *
 * ThreadLocal gives each thread its own isolated copy of the variable.
 * In a servlet container (Tomcat), each HTTP request runs on one thread,
 * so this is effectively "request-scoped storage" without needing Spring's
 * request scope or passing tenant ID through every method parameter.
 *
 * CRITICAL: The finally block in TenantFilter MUST call clear() after
 * every request, or a pooled thread will carry a stale tenantId into the
 * next request it handles.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}  // Utility class — not instantiable

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();  // remove() is safer than set(null) — avoids memory leaks
    }
}