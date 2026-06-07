package com.cafesaas.tenant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import java.time.Instant;

/**
 * Every tenant-scoped table's JPA entity extends this class.
 *
 * Two mechanisms work together:
 *
 * 1. @PrePersist auto-injects tenant_id from TenantContext before insert.
 *    A developer can never forget to set it — it's automatic.
 *
 * 2. The Hibernate @Filter named "tenantFilter" adds a WHERE clause to
 *    every SELECT on entities that extend this class. The filter is
 *    dormant until explicitly enabled — TenantFilterAspect enables it
 *    before every repository call.
 *
 * Why @Filter instead of @Where?
 * @Where is static — the clause is hardcoded at class definition time.
 * @Filter is parameterised — you can pass the tenantId at runtime.
 * That's essential here: the tenant changes per-request.
 */
@MappedSuperclass
@Getter @Setter
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = String.class)
)
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private String tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.tenantId  = TenantContext.getCurrentTenant();  // Auto-set — never null if filter ran
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}