package com.cafesaas.service;

import com.cafesaas.domain.Staff;
import com.cafesaas.dto.StaffDto.*;
import com.cafesaas.exception.BusinessException;
import com.cafesaas.repository.StaffRepository;
import com.cafesaas.security.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

    @Mock StaffRepository staffRepository;
    @Mock PasswordEncoder  passwordEncoder;
    @InjectMocks StaffService staffService;

    @Test
    void create_throws_when_email_exists_in_same_tenant() {
        when(staffRepository.existsByEmail("dup@cafe.com")).thenReturn(true);

        assertThatThrownBy(() -> staffService.create(
                new CreateStaffRequest("Priya", "dup@cafe.com",
                        "pass1234", Role.STAFF, null)
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void create_hashes_password_before_saving() {
        when(staffRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("$2a$hashed");
        when(staffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Staff result = staffService.create(
                new CreateStaffRequest("Ravi", "ravi@cafe.com",
                        "pass1234", Role.CASHIER, null));

        assertThat(result.getPasswordHash()).isEqualTo("$2a$hashed");
        // Raw password must never be stored
        assertThat(result.getPasswordHash()).doesNotContain("pass1234");
    }

    @Test
    void deactivate_sets_active_to_false() {
        Staff staff = Staff.builder().active(true).build();
        when(staffRepository.findById("s1")).thenReturn(Optional.of(staff));
        when(staffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Staff result = staffService.deactivate("s1");
        assertThat(result.isActive()).isFalse();
    }
}