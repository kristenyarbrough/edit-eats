package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.MealPlan;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanResponse;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;

    public MealPlan createMealPlan(CreateMealPlanRequest request) {

        MealPlan mealPlan = MealPlan.builder()
                .name(request.getName())
                .weekStarting(request.getWeekStarting())
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        return mealPlanRepository.save(mealPlan);

    }

    public List<MealPlan> getMealPlans() {

        return mealPlanRepository.findAll();
    }

    public MealPlanResponse getMealPlan(Long id) {

        MealPlan mealPlan = mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: " + id));

        return MealPlanResponse.builder()
                .id(mealPlan.getId())
                .name(mealPlan.getName())
                .weekStarting(mealPlan.getWeekStarting())
                .createdAt(mealPlan.getCreatedAt())
                .lastModifiedAt(mealPlan.getLastModifiedAt())
                .build();

    }
}
