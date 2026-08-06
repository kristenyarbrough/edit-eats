package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.MealPlan;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanResponse;
import io.github.kristenyarbrough.edit_eats.service.MealPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealPlanController.class)
public class MealPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MealPlanService mealPlanService;

    @Test
    void shouldCreateMealPlan() throws Exception {

        CreateMealPlanRequest request = createRequest();

        MealPlan mealPlan = createMealPlan();

        when(mealPlanService.createMealPlan(any(CreateMealPlanRequest.class)))
                .thenReturn(mealPlan);

        mockMvc.perform(post("/api/meal-plans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekly Family Meals"));

        verify(mealPlanService).createMealPlan(any(CreateMealPlanRequest.class));

    }

    @Test
    void shouldReturnAllMealPlans() throws Exception {

        MealPlan first = createMealPlan();

        MealPlan second = MealPlan.builder()
                .id(2L)
                .name("Camping Weekend")
                .weekStarting(LocalDate.of(2026, 8, 10))
                .build();

        when(mealPlanService.getMealPlans())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/meal-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Weekly Family Meals"))
                .andExpect(jsonPath("$[1].name").value("Camping Weekend"));

        verify(mealPlanService).getMealPlans();

    }

    @Test
    void shouldGetMealPlan() throws Exception {

        MealPlanResponse response = MealPlanResponse.builder()
                .id(1L)
                .name("Weekly Family Meals")
                .weekStarting(LocalDate.of(2026, 8, 3))
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(mealPlanService.getMealPlan(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/meal-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekly Family Meals"));

        verify(mealPlanService).getMealPlan(1L);

    }

    @Test
    void shouldReturn404WhenMealPlanDoesNotExist() throws Exception {

        when(mealPlanService.getMealPlan(99L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: 99"));

        mockMvc.perform(get("/api/meal-plans/99"))
                .andExpect(status().isNotFound());

        verify(mealPlanService).getMealPlan(99L);

    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {

        CreateMealPlanRequest request = createRequest();
        request.setName("");

        assertInvalidRequest(request);

    }

    @Test
    void shouldReturn400WhenWeekStartingIsNull() throws Exception {

        CreateMealPlanRequest request = createRequest();
        request.setWeekStarting(null);

        assertInvalidRequest(request);
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

    private void assertInvalidRequest(CreateMealPlanRequest request) throws Exception {

        mockMvc.perform(post("/api/meal-plans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(mealPlanService, never()).createMealPlan(any());

    }
}
