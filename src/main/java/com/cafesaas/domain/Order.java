// src/main/java/com/cafesaas/domain/Order.java
package com.cafesaas.domain;

import com.cafesaas.tenant.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_order_tenant",  columnList = "tenant_id"),
                @Index(name = "idx_order_status",  columnList = "tenant_id,status"),
                @Index(name = "idx_order_created", columnList = "tenant_id,created_at")
        }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Order extends TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "table_number", length = 20)
    private String tableNumber;

    @Column(name = "customer_name")
    private String customerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "staff_id")
    private UUID staffId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}