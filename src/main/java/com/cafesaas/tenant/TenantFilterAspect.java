package com.cafesaas.tenant;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * AOP aspect — activates the Hibernate tenantFilter before every
 * method in the repository layer.
 *
 * The pointcut "execution(* com.cafesaas.repository..*(..))" matches
 * ANY method in any class inside the repository package.
 *
 * Why AOP instead of calling enableFilter() manually in each service?
 * Because if a developer adds a new service and forgets to call it,
 * cross-tenant data leaks silently. The aspect makes it impossible to
 * forget — the filter is always on.
 *
 * The filter must be activated per-Session (i.e. per EntityManager),
 * not per-application. That's why we unwrap the Hibernate Session
 * from the EntityManager every time.
 */
@Aspect
@Component
@RequiredArgsConstructor
public class TenantFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(* com.cafesaas.repository..*(..))")
    public void enableTenantFilter() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            entityManager.unwrap(Session.class)
                    .enableFilter("tenantFilter")
                    .setParameter("tenantId", tenantId);
        }
    }
}