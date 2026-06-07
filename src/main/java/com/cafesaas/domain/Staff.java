// src/main/java/com/cafesaas/domain/Staff.java
package com.cafesaas.domain;

import com.cafesaas.security.Role;
import com.cafesaas.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Extends TenantAwareEntity — gets tenant_id auto-set on insert
 * and gets filtered by the Hibernate tenantFilter on SELECT.
 *
 * The UNIQUE constraint is on (tenant_id, email), not just email.
 * This is correct for SaaS: the same person can work at two different
 * cafes with the same email address.
 */
@Entity
@Table(
        name = "staff",
        indexes = {
                @Index(name = "idx_staff_tenant",       columnList = "tenant_id"),
                @Index(name = "idx_staff_tenant_email", columnList = "tenant_id,email", unique = true)
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Staff extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
}