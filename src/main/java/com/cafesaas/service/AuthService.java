package com.cafesaas.service;

import com.cafesaas.domain.*;
import com.cafesaas.dto.AuthDto.*;
import com.cafesaas.exception.BusinessException;
import com.cafesaas.repository.*;
import com.cafesaas.security.*;
import com.cafesaas.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StaffRepository  staffRepository;
    private final TenantRepository tenantRepository;
    private final JwtService       jwtService;
    private final PasswordEncoder  passwordEncoder;

    /**
     * Registers a brand-new cafe and creates its first TENANT_ADMIN account.
     * This is the only endpoint where TenantContext is set MANUALLY because
     * the TenantFilter skips /api/auth/register-tenant.
     * We need it for @PrePersist to work on the Staff entity.
     */
    @Transactional
    public LoginResponse registerTenant(RegisterTenantRequest req) {
        if (tenantRepository.existsById(req.tenantId())) {
            throw new BusinessException("Tenant ID already taken: " + req.tenantId());
        }
        if (tenantRepository.existsBySubdomain(req.subdomain())) {
            throw new BusinessException("Subdomain already taken: " + req.subdomain());
        }

        Tenant tenant = Tenant.builder()
                .id(req.tenantId())
                .name(req.tenantName())
                .subdomain(req.subdomain())
                .status(TenantStatus.TRIAL)
                .planType(PlanType.STARTER)
                .currency("INR")
                .timezone("Asia/Kolkata")
                .build();
        tenantRepository.save(tenant);

        // Manually set context so @PrePersist on Staff can inject tenant_id
        TenantContext.setCurrentTenant(tenant.getId());
        try {
            Staff admin = Staff.builder()
                    .name(req.adminName())
                    .email(req.adminEmail())
                    .passwordHash(passwordEncoder.encode(req.adminPassword()))
                    .role(Role.TENANT_ADMIN)
                    .active(true)
                    .build();
            staffRepository.save(admin);

            String token = jwtService.generateToken(admin.getId().toString(), tenant.getId(), admin.getRole());
            return new LoginResponse(token, admin.getId().toString(), tenant.getId(), admin.getRole().name());
        } finally {
            TenantContext.clear(); // Must clear even if save throws
        }
    }

    /**
     * Login — TenantFilter has already run and set TenantContext.
     * The Hibernate filter on StaffRepository means findByEmail
     * only searches within the current tenant automatically.
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Staff staff = staffRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException("Invalid email or password"));

        if (!staff.isActive()) {
            throw new BusinessException("Account is deactivated");
        }
        if (!passwordEncoder.matches(req.password(), staff.getPasswordHash())) {
            throw new BusinessException("Invalid email or password");
        }

        String tenantId = TenantContext.getCurrentTenant();
        String token = jwtService.generateToken(staff.getId().toString(), tenantId, staff.getRole());
        return new LoginResponse(token, staff.getId().toString(), tenantId, staff.getRole().name());
    }
}