package com.cafesaas.security;

public enum Role {
    SUPER_ADMIN,    // SaaS operator — crosses tenants
    TENANT_ADMIN,   // Cafe owner — full access to their cafe
    MANAGER,        // Shift manager
    STAFF,          // Barista/waiter
    CASHIER         // POS only
}