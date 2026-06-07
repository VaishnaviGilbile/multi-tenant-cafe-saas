package com.cafesaas.repository;

import com.cafesaas.domain.Order;
import com.cafesaas.domain.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByStatusIn(List<OrderStatus> statuses);

    Page<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(
            Instant start, Instant end, Pageable pageable);

    // COALESCE handles the case where there are no orders (SUM returns NULL)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE o.createdAt BETWEEN :start AND :end AND o.status <> 'CANCELLED'")
    BigDecimal sumRevenue(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);
}