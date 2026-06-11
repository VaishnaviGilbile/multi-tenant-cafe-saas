package com.cafesaas.service;

import com.cafesaas.domain.*;
import com.cafesaas.dto.MenuDto.*;
import com.cafesaas.exception.ResourceNotFoundException;
import com.cafesaas.repository.MenuItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock MenuItemRepository menuItemRepository;
    @InjectMocks MenuService menuService;

    @Test
    void create_saves_and_returns_item() {
        when(menuItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = menuService.create(new CreateMenuItemRequest(
                "Latte", "Smooth", new BigDecimal("150"), MenuCategory.HOT_DRINK, 4));

        assertThat(result.getName()).isEqualTo("Latte");
        assertThat(result.isAvailable()).isTrue();  // default
        verify(menuItemRepository).save(any());
    }

    @Test
    void toggleAvailability_flips_available_flag() {
        MenuItem item = MenuItem.builder().available(true).build();
        when(menuItemRepository.findById("m1")).thenReturn(Optional.of(item));
        when(menuItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MenuItem result = menuService.toggleAvailability("m1");
        assertThat(result.isAvailable()).isFalse();

        // Toggle again
        when(menuItemRepository.findById("m1")).thenReturn(Optional.of(result));
        MenuItem result2 = menuService.toggleAvailability("m1");
        assertThat(result2.isAvailable()).isTrue();
    }

    @Test
    void findById_throws_when_not_found() {
        when(menuItemRepository.findById("missing")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> menuService.toggleAvailability("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}