package com.cafesaas.service;

import com.cafesaas.domain.Staff;
import com.cafesaas.dto.StaffDto.*;
import com.cafesaas.exception.*;
import com.cafesaas.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public Staff create(CreateStaffRequest req) {
        // existsByEmail is scoped to the current tenant by TenantFilterAspect
        // So the same email CAN exist in a different cafe — intentional
        if (staffRepository.existsByEmail(req.email())) {
            throw new BusinessException(
                    "Email already registered in this cafe: " + req.email());
        }

        return staffRepository.save(Staff.builder()
                .name(req.name())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(req.role())
                .phoneNumber(req.phoneNumber())
                .active(true)
                .build());
    }

    @Transactional(readOnly = true)
    public List<Staff> getAllActive() {
        return staffRepository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public Staff getById(String id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff member not found: " + id));
    }

    /**
     * Soft delete — we never hard-delete staff records.
     * An order might reference a staff ID for audit purposes.
     * Hard-deleting would break that reference or require ON DELETE SET NULL.
     * Soft delete preserves history while preventing login.
     */
    public Staff deactivate(String id) {
        Staff staff = getById(id);
        staff.setActive(false);
        return staffRepository.save(staff);
    }
}