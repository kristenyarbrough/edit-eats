package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientCategoryRequest;
import io.github.kristenyarbrough.edit_eats.repository.IngredientCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientCategoryServiceTest {

    @Mock
    IngredientCategoryRepository ingredientCategoryRepository;

    @InjectMocks
    private IngredientCategoryService ingredientCategoryService;

    @Test
    void shouldCreateIngredientCategory() {

        CreateIngredientCategoryRequest request = createValidRequest();

        when(ingredientCategoryRepository.findByName("Dairy"))
                .thenReturn(Optional.empty());

        IngredientCategory savedCategory = IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

        when(ingredientCategoryRepository.save(any(IngredientCategory.class)))
                .thenReturn(savedCategory);

        IngredientCategory category =
                ingredientCategoryService.createIngredientCategory(request);

        assertEquals(1L, category.getId());
        assertEquals("Dairy", category.getName());

        verify(ingredientCategoryRepository).findByName("Dairy");
        verify(ingredientCategoryRepository).save(any(IngredientCategory.class));

    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {

        CreateIngredientCategoryRequest request = createValidRequest();

        IngredientCategory existingCategory = IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

        when(ingredientCategoryRepository.findByName("Dairy"))
                .thenReturn(Optional.of(existingCategory));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ingredientCategoryService.createIngredientCategory(request)
        );

        assertEquals(
                "Category 'Dairy' already exists.",
                exception.getMessage()
        );

        verify(ingredientCategoryRepository).findByName("Dairy");
        verify(ingredientCategoryRepository, never()).save(any());

    }
    private CreateIngredientCategoryRequest createValidRequest() {

        CreateIngredientCategoryRequest request =
                new CreateIngredientCategoryRequest();

        request.setName("Dairy");

        return request;

    }
}
