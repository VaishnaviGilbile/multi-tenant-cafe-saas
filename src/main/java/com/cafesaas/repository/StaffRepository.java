package com.cafesaas.repository;

import com.cafesaas.domain.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {

    // Hibernate tenantFilter is active — these queries are automatically tenant-scoped
    Optional<Staff> findByEmail(String email);

    List<Staff> findByActiveTrue();

    boolean existsByEmail(String email);
}