package com.cafesaas.service;

import com.cafesaas.domain.*;
import com.cafesaas.domain.Order;
import com.cafesaas.dto.OrderDto.*;
import com.cafesaas.exception.BusinessException;
import com.cafesaas.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository    orderRepository;
    @Mock MenuItemRepository menuItemRepository;

    @InjectMocks OrderService orderService;

    private MenuItem espresso;

    @BeforeEach
    void setUp() {
        espresso = MenuItem.builder()
                .name("Espresso")
                .price(new BigDecimal("120.00"))
                .available(true).build();
    }

    @Test
    void create_calculates_correct_total_for_multiple_items() {
        MenuItem cappuccino = MenuItem.builder()
                .name("Cappuccino")
                .price(new BigDecimal("160.00"))
                .available(true).build();

        when(menuItemRepository.findById("m1")).thenReturn(Optional.of(espresso));
        when(menuItemRepository.findById("m2")).thenReturn(Optional.of(cappuccino));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new CreateOrderRequest("T1", "Alice", List.of(
                new OrderItemRequest("m1", 2),   // 2 × 120 = 240
                new OrderItemRequest("m2", 1)    // 1 × 160 = 160
        ), null);

        Order order = orderService.create(req);

        // 240 + 160 = 400
        assertThat(order.getTotalAmount()).isEqualByComparingTo("400.00");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    void create_throws_when_menu_item_unavailable() {
        espresso.setAvailable(false);
        when(menuItemRepository.findById("m1")).thenReturn(Optional.of(espresso));

        assertThatThrownBy(() -> orderService.create(
                new CreateOrderRequest("T1", "Bob",
                        List.of(new OrderItemRequest("m1", 1)), null)
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("unavailable");
    }

    @Test
    void create_throws_when_menu_item_not_found() {
        when(menuItemRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(
                new CreateOrderRequest("T1", "Bob",
                        List.of(new OrderItemRequest("missing", 1)), null)
        )).isInstanceOf(com.cafesaas.exception.ResourceNotFoundException.class);
    }

    // ── State machine tests ───────────────────────────────────────────

    @Test
    void updateStatus_PENDING_to_PREPARING_is_valid() {
        Order order = Order.builder().status(OrderStatus.PENDING).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateStatus("o1", OrderStatus.PREPARING);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PREPARING);
    }

    @Test
    void updateStatus_PENDING_to_SERVED_is_invalid() {
        Order order = Order.builder().status(OrderStatus.PENDING).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus("o1", OrderStatus.SERVED))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_SERVED_is_terminal_cannot_transition() {
        Order order = Order.builder().status(OrderStatus.SERVED).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus("o1", OrderStatus.PENDING))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void updateStatus_CANCELLED_is_terminal_cannot_transition() {
        Order order = Order.builder().status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus("o1", OrderStatus.PREPARING))
                .isInstanceOf(BusinessException.class);
    }
}