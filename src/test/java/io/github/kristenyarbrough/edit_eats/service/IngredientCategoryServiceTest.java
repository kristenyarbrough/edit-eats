package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.domain.User;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientCategoryRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.IngredientCategoryResponse;
import io.github.kristenyarbrough.edit_eats.repository.IngredientCategoryRepository;
import io.github.kristenyarbrough.edit_eats.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientCategoryServiceTest {

    @Mock
    IngredientCategoryRepository ingredientCategoryRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    private IngredientCategoryService ingredientCategoryService;

    @Test
    void shouldCreateIngredientCategory() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        CreateIngredientCategoryRequest request = createValidRequest();

        when(ingredientCategoryRepository.existsByNameAndUserId("Dairy", 1L))
                .thenReturn(false);

        IngredientCategory savedCategory = IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .user(user)
                .build();

        when(ingredientCategoryRepository.save(any(IngredientCategory.class)))
                .thenReturn(savedCategory);

        IngredientCategoryResponse category =
                ingredientCategoryService.createIngredientCategory(request);

        assertEquals(1L, category.getId());
        assertEquals("Dairy", category.getName());
        assertEquals(true, category.isCustom());

        verify(ingredientCategoryRepository).existsByNameAndUserId("Dairy", 1L);

        ArgumentCaptor<IngredientCategory> categoryCaptor =
                ArgumentCaptor.forClass(IngredientCategory.class);
        verify(ingredientCategoryRepository).save(categoryCaptor.capture());

        assertEquals("Dairy", categoryCaptor.getValue().getName());
        assertEquals(user.getId(), categoryCaptor.getValue().getUser().getId());

    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        CreateIngredientCategoryRequest request = createValidRequest();

        when(ingredientCategoryRepository.existsByNameAndUserId("Dairy", 1L))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ingredientCategoryService.createIngredientCategory(request)
        );

        assertEquals(
                "Category 'Dairy' already exists.",
                exception.getMessage()
        );

        verify(ingredientCategoryRepository).existsByNameAndUserId("Dairy", 1L);
        verify(ingredientCategoryRepository, never()).save(any());

    }

    @Test
    void shouldRejectCustomCategoryWhenStandardCategoryAlreadyExists() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        CreateIngredientCategoryRequest request = createValidRequest();

        when(ingredientCategoryRepository.existsByNameAndUserId("Dairy", 1L))
                .thenReturn(false);

        when(ingredientCategoryRepository.existsByNameAndUserIdIsNull("Dairy"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ingredientCategoryService.createIngredientCategory(request)
        );

        assertEquals("Category 'Dairy' already exists.", exception.getMessage());

        verify(ingredientCategoryRepository, never()).save(any());

    }

    @Test
    void shouldAllowSameCustomCategoryNameForDifferentUsers() {

        User user = createUser();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        CreateIngredientCategoryRequest request = createValidRequest();

        when(ingredientCategoryRepository.existsByNameAndUserId("Dairy", 1L))
                .thenReturn(false);

        when(ingredientCategoryRepository.existsByNameAndUserIdIsNull("Dairy"))
                .thenReturn(false);

        IngredientCategory savedCategory = IngredientCategory.builder()
                .id(2L)
                .name("Dairy")
                .user(user)
                .build();

        when(ingredientCategoryRepository.save(any(IngredientCategory.class)))
                .thenReturn(savedCategory);

        IngredientCategoryResponse category = ingredientCategoryService.createIngredientCategory(request);

        assertEquals("Dairy", category.getName());
        assertEquals(true, category.isCustom());

        verify(ingredientCategoryRepository).existsByNameAndUserId("Dairy", user.getId());
        verify(ingredientCategoryRepository).existsByNameAndUserIdIsNull("Dairy");
        verify(ingredientCategoryRepository).save(any(IngredientCategory.class));

    }

    @Test
    void shouldReturnStandardAndCurrentUserCustomCategories() {

        User user = createUser();

        IngredientCategory standardCategory = IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

        IngredientCategory customCategory = IngredientCategory.builder()
                .id(2L)
                .name("Baking Supplies")
                .user(user)
                .build();

        when(userRepository.findByUsername("development-user"))
                .thenReturn(Optional.of(user));

        when(ingredientCategoryRepository.findByUserIdOrUserIsNullOrderByNameAsc(user.getId()))
                .thenReturn(List.of(standardCategory, customCategory));

        List<IngredientCategoryResponse> categories = ingredientCategoryService.getAllIngredientCategories();

        assertEquals(2, categories.size());
        assertEquals("Dairy", categories.get(0).getName());
        assertEquals("Baking Supplies", categories.get(1).getName());

        assertEquals(false, categories.get(0).isCustom());
        assertEquals(true, categories.get(1).isCustom());

        verify(ingredientCategoryRepository).findByUserIdOrUserIsNullOrderByNameAsc(user.getId());

    }

    private CreateIngredientCategoryRequest createValidRequest() {

        CreateIngredientCategoryRequest request =
                new CreateIngredientCategoryRequest();

        request.setName("Dairy");

        return request;

    }

    private User createUser() {

        return User.builder()
                .id(1L)
                .username("development-user")
                .build();

    }

}
