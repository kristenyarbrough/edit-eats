package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.MealPlan;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanResponse;
import io.github.kristenyarbrough.edit_eats.repository.MealPlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    @Test
    void shouldCreateMealPlan() {

        CreateMealPlanRequest request = createRequest();

        MealPlan savedMealPlan = createMealPlan();

        when(mealPlanRepository.save(any(MealPlan.class)))
                .thenReturn(savedMealPlan);

        MealPlan mealPlan = mealPlanService.createMealPlan(request);

        assertEquals(1L, mealPlan.getId());
        assertEquals("Weekly Family Meals", mealPlan.getName());
        assertEquals(LocalDate.of(2026, 8, 3), mealPlan.getWeekStarting());

        verify(mealPlanRepository).save(any(MealPlan.class));

    }

    @Test
    void shouldGetMealPlan() {

        MealPlan mealPlan = createMealPlan();

        when(mealPlanRepository.findById(1L))
                .thenReturn(Optional.of(mealPlan));

        MealPlanResponse response = mealPlanService.getMealPlan(1L);

        assertEquals(1L, response.getId());
        assertEquals("Weekly Family Meals", response.getName());
        assertEquals(LocalDate.of(2026, 8, 3), response.getWeekStarting());

        verify(mealPlanRepository).findById(1L);

    }

    @Test
    void shouldThrowExceptionWhenMealPlanDoesNotExist() {

        when(mealPlanRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> mealPlanService.getMealPlan(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Meal plan not found: 99", exception.getReason());

        verify(mealPlanRepository).findById(99l);

    }

    @Test
    void shouldReturnAllMealPlans() {

        MealPlan first = createMealPlan();

        MealPlan second = MealPlan.builder()
                .id(2L)
                .name("Camping Weekend")
                .weekStarting(LocalDate.of(2026, 8, 10))
                .build();

        when(mealPlanRepository.findAll())
                .thenReturn(List.of(first, second));

        List<MealPlan> mealPlans = mealPlanService.getMealPlans();

        assertEquals(2, mealPlans.size());
        assertEquals("Weekly Family Meals", mealPlans.get(0).getName());
        assertEquals("Camping Weekend", mealPlans.get(1).getName());

        verify(mealPlanRepository).findAll();

    }

    private CreateMealPlanRequest createRequest() {

        CreateMealPlanRequest request = new CreateMealPlanRequest();
        request.setName("Weekly Family Meals");
        request.setWeekStarting(LocalDate.of(2026, 8, 3));

        return request;

    }

    private MealPlan createMealPlan() {

        return MealPlan.builder()
                .id(1L)
                .name("Weekly Family Meals")
                .weekStarting(LocalDate.of(2026, 8, 3))
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

    }
}
