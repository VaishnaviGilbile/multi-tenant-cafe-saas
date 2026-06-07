package com.cafesaas.dto;

import com.cafesaas.domain.MenuCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class MenuDto {

    public record CreateMenuItemRequest(
            @NotBlank                          String name,
            String                             description,
            @NotNull @DecimalMin("0.01")       BigDecimal price,
            @NotNull                           MenuCategory category,
            Integer                            prepTimeMinutes
    ) {}

    public record MenuItemResponse(
            String id, String name, String description,
            BigDecimal price, MenuCategory category,
            boolean available, Integer prepTimeMinutes, Instant createdAt
    ) {}
}