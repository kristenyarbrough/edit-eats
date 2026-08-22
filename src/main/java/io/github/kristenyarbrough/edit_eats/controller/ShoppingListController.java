package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.dto.response.ShoppingListResponse;
import io.github.kristenyarbrough.edit_eats.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meal-plans")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping("/{mealPlanId}/shopping-list")
    public ShoppingListResponse getShoppingList(
            @PathVariable Long mealPlanId) {

        return shoppingListService.generateShoppingList(mealPlanId);

    }
}
