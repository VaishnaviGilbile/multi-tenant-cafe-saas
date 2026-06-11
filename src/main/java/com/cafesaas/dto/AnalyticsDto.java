package com.cafesaas.dto;

import java.math.BigDecimal;

public class AnalyticsDto {
    public record DashboardStats(
            long       pendingOrders,
            long       preparingOrders,
            long       todayOrderCount,
            BigDecimal todayRevenue,
            BigDecimal weekRevenue
    ) {}
}