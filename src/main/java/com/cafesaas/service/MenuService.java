package com.cafesaas.service;

import com.cafesaas.domain.*;
import com.cafesaas.dto.MenuDto.*;
import com.cafesaas.exception.ResourceNotFoundException;
import com.cafesaas.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @Transactional on the class means every public method runs inside
 * a transaction by default. Read-only methods override with
 * readOnly = true — this is an optimisation hint to Hibernate
 * (skips dirty-checking) and to the DB driver (can use a
 * read replica if configured).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public MenuItem create(CreateMenuItemRequest req) {
        MenuItem item = MenuItem.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .category(req.category())
                .prepTimeMinutes(req.prepTimeMinutes())
                .available(true)
                .build();
        // tenant_id is injected automatically by @PrePersist in TenantAwareEntity
        return menuItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getActiveMenu() {
        return menuItemRepository.findByAvailableTrueOrderByCategoryAscNameAsc();
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getByCategory(MenuCategory category) {
        return menuItemRepository.findByCategoryAndAvailableTrue(category);
    }

    public MenuItem update(String id, CreateMenuItemRequest req) {
        MenuItem item = findById(id);
        item.setName(req.name());
        item.setDescription(req.description());
        item.setPrice(req.price());
        item.setCategory(req.category());
        item.setPrepTimeMinutes(req.prepTimeMinutes());
        return menuItemRepository.save(item);
    }

    /**
     * Toggle — no request body needed. The controller just hits this endpoint
     * and the service flips the boolean. Simple and explicit.
     */
    public MenuItem toggleAvailability(String id) {
        MenuItem item = findById(id);
        item.setAvailable(!item.isAvailable());
        return menuItemRepository.save(item);
    }

    public void delete(String id) {
        menuItemRepository.delete(findById(id));
    }

    private MenuItem findById(String id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }
}