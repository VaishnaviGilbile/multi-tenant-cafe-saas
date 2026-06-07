package com.cafesaas.dto;

import com.cafesaas.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDto {

    public record CreateOrderRequest(
            String tableNumber,
            String customerName,
            @NotEmpty @Valid List<OrderItemRequest> items,
            String notes
    ) {}

    public record OrderItemRequest(
            @NotBlank String menuItemId,
            @Min(1)   int    quantity
    ) {}

    public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {}
}
