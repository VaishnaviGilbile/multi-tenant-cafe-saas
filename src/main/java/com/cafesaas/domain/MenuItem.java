// src/main/java/com/cafesaas/domain/MenuItem.java
package com.cafesaas.domain;

import com.cafesaas.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "menu_items",
        indexes = {
                @Index(name = "idx_menu_tenant",   columnList = "tenant_id"),
                @Index(name = "idx_menu_category", columnList = "tenant_id,category")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuItem extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private MenuCategory category;

    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    @Column(name = "image_url")
    private String imageUrl;
}