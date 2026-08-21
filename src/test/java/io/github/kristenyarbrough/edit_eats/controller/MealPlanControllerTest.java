package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.*;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.UpdateMealPlanRecipeRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.UpdateMealPlanRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.MealPlanRecipeResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                .startDate(LocalDate.of(2026, 8, 10))
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

        MealPlanRecipeResponse meal = MealPlanRecipeResponse.builder()
                .id(1L)
                .recipeId(1L)
                .recipeName("Lasagne")
                .mealDate(LocalDate.of(2026, 8, 3))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        MealPlanResponse response = MealPlanResponse.builder()
                .id(1L)
                .name("Weekly Family Meals")
                .startDate(LocalDate.of(2026, 8, 3))
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .meals(List.of(meal))
                .build();

        when(mealPlanService.getMealPlan(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/meal-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Weekly Family Meals"))
                .andExpect(jsonPath("$.meals.length()").value(1))
                .andExpect(jsonPath("@.meals[0].recipeId").value(1))
                .andExpect(jsonPath("$.meals[0].recipeName").value("Lasagne"))
                .andExpect(jsonPath("$.meals[0].mealDate").value("2026-08-03"))
                .andExpect(jsonPath("$.meals[0].mealType").value("DINNER"))
                .andExpect(jsonPath("$.meals[0].servings").value(4));

        verify(mealPlanService).getMealPlan(1L);

    }

    @Test
    void shouldReturnMealPlanWithNoMeals() throws Exception{

        MealPlanResponse response = MealPlanResponse.builder()
                .id(1L)
                .name("Weekly Family Meals")
                .startDate(LocalDate.of(2026, 8, 3))
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .meals(List.of())
                .build();

        when(mealPlanService.getMealPlan(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/meal-plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.meals").isArray())
                .andExpect(jsonPath("$.meals.length()").value(0));

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
    void shouldReturn400WhenStartDateIsNull() throws Exception {

        CreateMealPlanRequest request = createRequest();
        request.setStartDate(null);

        assertInvalidRequest(request);
    }

    @Test
    void shouldReturn400WhenDurationDaysIsZero() throws Exception {

        CreateMealPlanRequest request = createRequest();
        request.setDurationDays(0);

        assertInvalidRequest(request);

    }

    @Test
    void shouldSearchMealPlansByName() throws Exception {

        MealPlan mealPlan = createMealPlan();

        when(mealPlanService.findMealPlans("family"))
                .thenReturn(List.of(mealPlan));

        mockMvc.perform(get("/api/meal-plans")
                        .param("name", "family"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Weekly Family Meals"));

        verify(mealPlanService).findMealPlans("family");
        verify(mealPlanService, never()).getMealPlans();

    }

    @Test
    void shouldReturnAllMealPlansWhenNameIsBlank() throws Exception {

        MealPlan mealPlan = createMealPlan();

        when(mealPlanService.getMealPlans())
                .thenReturn(List.of(mealPlan));

        mockMvc.perform(get("/api/meal-plans")
                    .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(mealPlanService).getMealPlans();
        verify(mealPlanService, never()).findMealPlans(anyString());

    }

    @Test
    void shouldAddRecipeToMealPlan() throws Exception {

        MealPlan mealPlan = createMealPlan();
        Recipe recipe = createRecipe();

        MealPlanRecipe mealPlanRecipe = MealPlanRecipe.builder()
                .id(1L)
                .mealPlan(mealPlan)
                .recipe(recipe)
                .mealDate(LocalDate.of(2026, 8, 3))
                .mealType(MealType.DINNER)
                .servings(4)
                .build();

        when(mealPlanService.addRecipeToMealPlan(
                eq(1L),
                any(CreateMealPlanRecipeRequest.class)))
                .thenReturn(mealPlanRecipe);

        mockMvc.perform(post("/api/meal-plans/1/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "recipeId": 1,
                                "mealDate": "2026-08-03",
                                "mealType": "DINNER",
                                "servings": 4
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.recipe.id").value(1))
                .andExpect(jsonPath("$.mealDate").value("2026-08-03"))
                .andExpect(jsonPath("$.mealType").value("DINNER"))
                .andExpect(jsonPath("$.servings").value(4));

        verify(mealPlanService).addRecipeToMealPlan(
                eq(1L),
                any(CreateMealPlanRecipeRequest.class));

    }

    @Test
    void shouldReturn400WhenRecipeIdIsMissing() throws  Exception {

        mockMvc.perform(post("/api/meal-plans/1/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "mealDate": "2026-08-03",
                                "mealType": "DINNER",
                                "servings": 4
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mealPlanService);

    }

    @Test
    void shouldReturn400WhenServingsIsZero() throws Exception {

        mockMvc.perform(post("/api/meal-plans/1/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "recipeId": 1,
                                "mealDate": "2026-08-03",
                                "mealType": "DINNER",
                                "servings": 0
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mealPlanService);

    }

    @Test
    void shouldReturn400WhenMealDateIsMissing() throws Exception {

        mockMvc.perform(post("/api/meal-plans/1/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "recipeId": 1,
                                "mealType": "DINNER",
                                "servings": 4
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mealPlanService);
    }

    @Test
    void shouldUpdateMealPlanRecipe() throws Exception {

        MealPlanRecipe updatedMeal = MealPlanRecipe.builder()
                .id(1L)
                .mealDate(LocalDate.of(2026, 8, 5))
                .mealType(MealType.LUNCH)
                .servings(6)
                .build();

        when(mealPlanService.updateMealPlanRecipe(
                eq(1L),
                any(UpdateMealPlanRecipeRequest.class)))
                .thenReturn(updatedMeal);

        mockMvc.perform(put("/api/meal-plans/recipes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "recipeId": 2,
                                "mealDate": "2026-08-05",
                                "mealType": "LUNCH",
                                "servings": 6
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mealDate").value("2026-08-05"))
                .andExpect(jsonPath("$.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.servings").value(6));

        verify(mealPlanService).updateMealPlanRecipe(
                eq(1L),
                any(UpdateMealPlanRecipeRequest.class));

    }

    @Test
    void shouldRejectUpdateWhenRecipeIdIsMissing() throws Exception {

        mockMvc.perform(put("/api/meal-plans/recipes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "mealDate": "2026-08-05",
                                "mealType": "LUNCH",
                                "servings": 6
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mealPlanService);

    }

    @Test
    void shouldRejectUpdateWhenServingsIsZero() throws Exception {

        mockMvc.perform(put("/api/meal-plans/recipes/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "recipeId": 2,
                                "mealDate": "2026-08-05",
                                "mealType": "LUNCH",
                                "servings": 0
                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mealPlanService);

    }

    @Test
    void shouldDeleteMealPlanRecipe() throws Exception {

        doNothing()
                .when(mealPlanService)
                .deleteMealPlanRecipe(1L);

        mockMvc.perform(delete("/api/meal-plans/recipes/1"))
                .andExpect(status().isNoContent());

        verify(mealPlanService).deleteMealPlanRecipe(1L);

    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentMealPlanRecipe() throws Exception {

        doThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Meal plan recipe not found: 99"))
                .when(mealPlanService)
                .deleteMealPlanRecipe(99L);

        mockMvc.perform(delete("/api/meal-plans/recipes/99"))
                .andExpect(status().isNotFound());

        verify(mealPlanService).deleteMealPlanRecipe(99L);

    }

    @Test
    void shouldUpdateMealPlan() throws Exception {

        UpdateMealPlanRequest request = new UpdateMealPlanRequest();
        request.setName("Updated Family Meals");
        request.setStartDate(LocalDate.of(2026, 8, 10));
        request.setDurationDays(14);

        MealPlan mealPlan = MealPlan.builder()
                .id(1L)
                .name("Updated Family Meals")
                .startDate(LocalDate.of(2026, 8, 10))
                .durationDays(14)
                .createdAt(LocalDateTime.now().minusDays(1))
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(mealPlanService.updateMealPlan(eq(1L), any(UpdateMealPlanRequest.class)))
                .thenReturn(mealPlan);

        mockMvc.perform(put("/api/meal-plans/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Family Meals"))
                .andExpect(jsonPath("$.startDate").value("2026-08-10"))
                .andExpect(jsonPath("$.durationDays").value(14));

        verify(mealPlanService).updateMealPlan(
                eq(1L),
                any(UpdateMealPlanRequest.class)
        );

    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingMealPlan() throws Exception {

        UpdateMealPlanRequest request = new UpdateMealPlanRequest();
        request.setName("Updated Family Meals");
        request.setStartDate(LocalDate.of(2026, 8, 10));
        request.setDurationDays(14);

        when(mealPlanService.updateMealPlan(
                eq(99L),
                any(UpdateMealPlanRequest.class)))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Meal plan not found: 99"));

        mockMvc.perform(put("/api/meal-plans/99")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(mealPlanService).updateMealPlan(
                eq(99L),
                any(UpdateMealPlanRequest.class)
        );

    }

    @Test
    void shouldReturn400WhenUpdatingMealPlanWithInvalidRequest() throws Exception {

        UpdateMealPlanRequest request = new UpdateMealPlanRequest();
        request.setName("");
        request.setStartDate(null);

        mockMvc.perform(put("/api/meal-plans/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mealPlanService);

    }

    @Test
    void shouldDeleteMealPlan() throws Exception {

        doNothing()
                .when(mealPlanService)
                .deleteMealPlan(1L);

        mockMvc.perform(delete("/api/meal-plans/1"))
                .andExpect(status().isNoContent());

        verify(mealPlanService).deleteMealPlan(1L);

    }

    @Test
    void shouldReturn404WhenDeletingNonExistentMealPlan() throws Exception {

        doThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Meal plan not found: 99"))
                .when(mealPlanService)
                .deleteMealPlan(99L);

        mockMvc.perform(delete("/api/meal-plans/99"))
                .andExpect(status().isNotFound());

        verify(mealPlanService).deleteMealPlan(99L);

    }

    private CreateMealPlanRequest createRequest() {

        CreateMealPlanRequest request = new CreateMealPlanRequest();
        request.setName("Weekly Family Meals");
        request.setStartDate(LocalDate.of(2026, 8, 3));
        request.setDurationDays(7);

        return request;

    }

    private MealPlan createMealPlan() {

        return MealPlan.builder()
                .id(1L)
                .name("Weekly Family Meals")
                .startDate(LocalDate.of(2026, 8, 3))
                .durationDays(7)
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

    private Recipe createRecipe() {

        return Recipe.builder()
                .id(1L)
                .name("Lasagne")
                .prepMinutes(20)
                .cookMinutes(50)
                .servings(6)
                .difficulty(Difficulty.EASY)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();
    }

}
