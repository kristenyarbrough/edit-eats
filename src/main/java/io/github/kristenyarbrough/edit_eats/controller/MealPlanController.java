package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.MealPlan;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanResponse;
import io.github.kristenyarbrough.edit_eats.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealPlan createMealPlan(
            @Valid @RequestBody CreateMealPlanRequest request) {
        return mealPlanService.createMealPlan(request);
    }

    @GetMapping
    public List<MealPlan> getMealPlans() {
        return mealPlanService.getMealPlans();
    }

    @GetMapping("/{id}")
    public MealPlanResponse getMealPlan(@PathVariable Long id) {
        return mealPlanService.getMealPlan(id);
    }

//    @GetMapping("/{weekStarting}/shopping-list")
//    public List<ShoppingListItem> shoppingList(@PathVariable String weekStarting) {
//        LocalDate date = LocalDate.parse(weekStarting);
//
//        MealPlan plan = repository.findByWeekStarting(date)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal plan not found"));
//
//        if (plan.getRecipeIds() == null || plan.getRecipeIds().isEmpty()) {
//            return List.of();
//        }
//
//        return recipeService.generateShoppingListByRecipeIds(plan.getRecipeIds());
//    }
}
