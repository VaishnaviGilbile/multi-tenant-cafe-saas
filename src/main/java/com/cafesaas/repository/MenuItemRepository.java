package com.cafesaas.repository;

import com.cafesaas.domain.MenuCategory;
import com.cafesaas.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

    List<MenuItem> findByAvailableTrue();

    List<MenuItem> findByCategoryAndAvailableTrue(MenuCategory category);

    List<MenuItem> findByAvailableTrueOrderByCategoryAscNameAsc();
}
