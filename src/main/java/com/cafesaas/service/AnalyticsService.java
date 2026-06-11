package com.cafesaas.service;

import com.cafesaas.domain.OrderStatus;
import com.cafesaas.dto.AnalyticsDto.*;
import com.cafesaas.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final OrderRepository orderRepository;

    /**
     * Single dashboard call — aggregates 5 metrics in 4 queries.
     * In a production system you'd cache this in Redis with a
     * TTL of 60 seconds so it isn't re-computed on every page load.
     * For this project it's acceptably fast since data volume is small.
     */
    public DashboardStats getDashboard() {
        Instant now          = Instant.now();
        Instant startOfToday = LocalDate.now(ZoneOffset.UTC)
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant startOfWeek  = now.minus(Duration.ofDays(7));

        return new DashboardStats(
                orderRepository.countByStatus(OrderStatus.PENDING),
                orderRepository.countByStatus(OrderStatus.PREPARING),
                orderRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                        startOfToday, now,
                        org.springframework.data.domain.Pageable.unpaged()
                ).getTotalElements(),
                orderRepository.sumRevenue(startOfToday, now),
                orderRepository.sumRevenue(startOfWeek, now)
        );
    }
}