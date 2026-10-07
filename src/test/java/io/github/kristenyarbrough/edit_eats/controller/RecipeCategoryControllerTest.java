package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.RecipeCategory;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeCategoryResponse;
import io.github.kristenyarbrough.edit_eats.service.RecipeCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeCategoryControllerTest {

    @Mock
    private RecipeCategoryService recipeCategoryService;

    @InjectMocks
    private RecipeCategoryController recipeCategoryController;

    @Test
    void shouldGetAvailableCategories() {

        RecipeCategoryResponse universalCategory = RecipeCategoryResponse.builder()
                .id(1L)
                .name("Dinner")
                .build();

        RecipeCategoryResponse userCategory = RecipeCategoryResponse.builder()
                .id(2L)
                .name("Quick Meals")
                .build();

        when(recipeCategoryService.getAvailableCategories())
                .thenReturn(List.of(universalCategory, userCategory));

        List<RecipeCategoryResponse> result = recipeCategoryController.getAvailableCategories();

        assertEquals(2, result.size());
        assertEquals("Dinner", result.get(0).getName());
        assertEquals("Quick Meals", result.get(1).getName());

        verify(recipeCategoryService).getAvailableCategories();

    }

    @Test
    void shouldCreateCategory() {

        RecipeCategoryResponse category = RecipeCategoryResponse.builder()
                .id(1L)
                .name("Quick Meals")
                .build();

        when(recipeCategoryService.createCategory("Quick Meals"))
                .thenReturn(category);

        RecipeCategoryResponse result = recipeCategoryController.createCategory("Quick Meals");

        assertEquals("Quick Meals", result.getName());

        verify(recipeCategoryService).createCategory("Quick Meals");

    }
}
