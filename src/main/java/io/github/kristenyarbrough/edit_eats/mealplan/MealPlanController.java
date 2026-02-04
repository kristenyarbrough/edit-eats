package io.github.kristenyarbrough.edit_eats.mealplan;

import io.github.kristenyarbrough.edit_eats.service.RecipeService;
import io.github.kristenyarbrough.edit_eats.dto.ShoppingListItem;
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

    private final MealPlanRepository repository;
    private final RecipeService recipeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealPlan createOrReplace(@RequestBody MealPlanRequest req) {
        if (req.getWeekStarting() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "weekStarting is required");
        }

        MealPlan plan = repository.findByWeekStarting(req.getWeekStarting())
                .orElse(MealPlan.builder().weekStarting(req.getWeekStarting()).build());

        plan.setRecipeIds(req.getRecipeIds());
        return repository.save(plan);
    }

    @GetMapping("/{weekStarting}")
    public MealPlan get(@PathVariable String weekStarting) {
        LocalDate date = LocalDate.parse(weekStarting);
        return repository.findByWeekStarting(date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal plan not found"));
    }

    @GetMapping("/{weekStarting}/shopping-list")
    public List<ShoppingListItem> shoppingList(@PathVariable String weekStarting) {
        LocalDate date = LocalDate.parse(weekStarting);

        MealPlan plan = repository.findByWeekStarting(date)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal plan not found"));

        if (plan.getRecipeIds() == null || plan.getRecipeIds().isEmpty()) {
            return List.of();
        }

        return recipeService.generateShoppingListByRecipeIds(plan.getRecipeIds());
    }
}
