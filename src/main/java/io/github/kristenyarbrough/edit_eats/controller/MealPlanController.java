package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.MealPlan;
import io.github.kristenyarbrough.edit_eats.domain.MealPlanRecipe;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.UpdateMealPlanRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.UpdateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanResponse;
import io.github.kristenyarbrough.edit_eats.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public MealPlanResponse getMealPlan(@PathVariable Long id) {
        return mealPlanService.getMealPlan(id);
    }

    @GetMapping
    public List<MealPlan> getMealPlans(
            @RequestParam(required = false) String name) {

        if (name == null || name.isBlank()) {

            return mealPlanService.getMealPlans();

        }

        return mealPlanService.findMealPlans(name);

    }

    @PostMapping("/{mealPlanId}/recipes")
    @ResponseStatus(HttpStatus.CREATED)
    public MealPlanRecipe addRecipeToMealPlan(
            @PathVariable Long mealPlanId,
            @Valid @RequestBody CreateMealPlanRecipeRequest request) {

        return mealPlanService.addRecipeToMealPlan(mealPlanId, request);

    }

    @PutMapping("/recipes/{mealPlanRecipeId}")
    public MealPlanRecipe updateMealPlanRecipe(
            @PathVariable Long mealPlanRecipeId,
            @Valid @RequestBody UpdateMealPlanRecipeRequest request) {

        return mealPlanService.updateMealPlanRecipe(
                mealPlanRecipeId, request
        );

    }

    @DeleteMapping("/recipes/{mealPlanRecipeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMealPlanRecipe(
            @PathVariable Long mealPlanRecipeId) {

        mealPlanService.deleteMealPlanRecipe(mealPlanRecipeId);

    }

    @PutMapping("/{id}")
    public MealPlan updateMealPlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMealPlanRequest request) {

        return mealPlanService.updateMealPlan(id, request);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMealPlan(@PathVariable Long id) {

        mealPlanService.deleteMealPlan(id);

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
