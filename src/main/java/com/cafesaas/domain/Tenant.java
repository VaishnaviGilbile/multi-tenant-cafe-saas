// src/main/java/com/cafesaas/domain/Tenant.java
package com.cafesaas.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * The Tenant entity does NOT extend TenantAwareEntity.
 * Tenants are platform-level, not tenant-scoped.
 * They live in a separate table with no tenant_id column.
 */
@Entity
@Table(name = "tenants")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Tenant {

    @Id
    @Column(length = 100)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String subdomain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    private String contactEmail;

    @Column(length = 50)
    private String timezone;

    @Column(length = 10)
    private String currency;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}