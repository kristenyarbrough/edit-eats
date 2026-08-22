package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.response.ShoppingListCategoryResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.ShoppingListItemResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.ShoppingListResponse;
import io.github.kristenyarbrough.edit_eats.service.ShoppingListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShoppingListController.class)
final class ShoppingListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShoppingListService shoppingListService;

    @Test
    void shouldGetShoppingListForMealPlan() throws Exception {

        ShoppingListItemResponse item =
                ShoppingListItemResponse.builder()
                        .ingredientId(1L)
                        .ingredientName("Milk")
                        .ingredientCategoryId(2L)
                        .ingredientCategoryName("Dairy")
                        .quantity(new BigDecimal("1.5"))
                        .unit(Unit.L)
                        .build();

        ShoppingListCategoryResponse category =
                ShoppingListCategoryResponse.builder()
                        .categoryId(2L)
                        .categoryName("Dairy")
                        .items(List.of(item))
                        .build();

        ShoppingListResponse response =
                ShoppingListResponse.builder()
                        .categories(List.of(category))
                        .build();

        when(shoppingListService.generateShoppingList(1L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/meal-plans/1/shopping-list")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].categoryId").value(2))
                .andExpect(jsonPath("$.categories[0].categoryName").value("Dairy"))
                .andExpect(jsonPath("$.categories[0].items[0].ingredientId").value(1))
                .andExpect(jsonPath("$.categories[0].items[0].ingredientName").value("Milk"))
                .andExpect(jsonPath("$.categories[0].items[0].quantity").value(1.5))
                .andExpect(jsonPath("$.categories[0].items[0].unit").value("L"))
                .andExpect(jsonPath("$.categories[0].items[0].ingredientCategoryId").value(2))
                .andExpect(jsonPath("$.categories[0].items[0].ingredientCategoryName").value("Dairy"));

        verify(shoppingListService).generateShoppingList(1L);

    }

    @Test
    void shouldReturnEmptyShoppingListWhenMealPlanHasNoRecipes() throws Exception {

        ShoppingListResponse response =
                ShoppingListResponse.builder()
                        .categories(List.of())
                        .build();

        when(shoppingListService.generateShoppingList(1L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/meal-plans/1/shopping-list")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isEmpty());

        verify(shoppingListService).generateShoppingList(1L);

    }

    @Test
    void shouldReturnNotFoundWhenMealPlanDoesNotExist() throws Exception {

        when(shoppingListService.generateShoppingList(999L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: 999"
                ));

        mockMvc.perform(
                get("/api/meal-plans/999/shopping-list")
        )
                .andExpect(status().isNotFound());

        verify(shoppingListService)
                .generateShoppingList(999L);

    }

    @Test
    void shouldReturnBadRequestWhenIngredientUsesIncompatibleUnits() throws Exception {

        when(shoppingListService.generateShoppingList(1L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot combine units G and CUP for ingredient: Flour"
                ));

        mockMvc.perform(
                get("/api/meal-plans/1/shopping-list")
        )
                .andExpect(status().isBadRequest());

        verify(shoppingListService).generateShoppingList(1L);

    }

}
