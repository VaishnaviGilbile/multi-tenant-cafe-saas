// src/main/java/com/cafesaas/service/OrderService.java
package com.cafesaas.service;

import com.cafesaas.domain.*;
import com.cafesaas.dto.OrderDto.*;
import com.cafesaas.exception.*;
import com.cafesaas.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository    orderRepository;
    private final MenuItemRepository menuItemRepository;

    public Order create(CreateOrderRequest req) {
        // Build order items — validate each menu item exists AND is available
        List<OrderItem> items = req.items().stream().map(itemReq -> {
            MenuItem menuItem = menuItemRepository.findById(itemReq.menuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Menu item not found: " + itemReq.menuItemId()));

            if (!menuItem.isAvailable()) {
                throw new BusinessException(
                        "'" + menuItem.getName() + "' is currently unavailable");
            }

            BigDecimal subtotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.quantity()));

            return OrderItem.builder()
                    .menuItem(menuItem)
                    .quantity(itemReq.quantity())
                    .unitPrice(menuItem.getPrice())
                    .subtotal(subtotal)
                    .build();
        }).toList();   // .toList() returns an unmodifiable list (Java 16+)

        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .tableNumber(req.tableNumber())
                .customerName(req.customerName())
                .status(OrderStatus.PENDING)
                .totalAmount(total)
                .notes(req.notes())
                .build();

        // Set the back-reference so the cascade save works correctly
        items.forEach(i -> i.setOrder(order));
        order.setItems(new java.util.ArrayList<>(items));

        // CascadeType.ALL on Order.items means saving Order also saves all OrderItems
        return orderRepository.save(order);
    }

    public Order updateStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        validateTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getActiveOrders() {
        return orderRepository.findByStatusIn(
                List.of(OrderStatus.PENDING, OrderStatus.PREPARING, OrderStatus.READY));
    }

    @Transactional(readOnly = true)
    public Page<Order> getHistory(Instant start, Instant end, Pageable pageable) {
        return orderRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(start, end, pageable);
    }

    /**
     * Explicit state machine using Java 21 switch expression.
     *
     * Why a switch expression instead of if-else chains?
     * - Exhaustive: the compiler forces you to handle every enum value.
     *   If you add a new OrderStatus later and forget to update this,
     *   the code won't compile.
     * - Readable: the legal transitions are obvious at a glance.
     * - No fall-through bugs: switch expressions don't fall through.
     *
     * Legal transitions:
     *   PENDING    → PREPARING or CANCELLED
     *   PREPARING  → READY     or CANCELLED
     *   READY      → SERVED
     *   SERVED     → (terminal — nothing)
     *   CANCELLED  → (terminal — nothing)
     */
    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING  -> next == OrderStatus.READY     || next == OrderStatus.CANCELLED;
            case READY      -> next == OrderStatus.SERVED;
            case SERVED, CANCELLED -> false;  // Terminal states — no transitions allowed
        };

        if (!valid) {
            throw new BusinessException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }
}