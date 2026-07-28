package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.dto.response.RecipeResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import io.github.kristenyarbrough.edit_eats.domain.Difficulty;
import io.github.kristenyarbrough.edit_eats.domain.Recipe;
import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeIngredientRequest;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeRequest;

import io.github.kristenyarbrough.edit_eats.dto.request.CreateRecipeStepRequest;
import io.github.kristenyarbrough.edit_eats.service.RecipeService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeService recipeService;

    @Test
    void shouldCreateRecipe() throws Exception {

        CreateRecipeRequest request = createRequest();

        Recipe recipe = Recipe.builder()
                .id(1L)
                .name("Scrambled Eggs")
                .prepMinutes(2)
                .cookMinutes(5)
                .servings(2)
                .difficulty(Difficulty.EASY)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(recipeService.createRecipe(any(CreateRecipeRequest.class)))
                .thenReturn(recipe);

        mockMvc.perform(post("/api/recipes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Scrambled Eggs"))
                .andExpect(jsonPath("$.servings").value(2))
                .andExpect(jsonPath("$.difficulty").value("EASY"));

        verify(recipeService).createRecipe(any(CreateRecipeRequest.class));

    }

    @Test
    void shouldGetRecipe() throws Exception {

        RecipeResponse response = RecipeResponse.builder()
                .id(1L)
                .name("Scrambled Eggs")
                .prepMinutes(2)
                .cookMinutes(5)
                .servings(2)
                .totalMinutes(7)
                .formattedTotalTime("7 mins")
                .difficulty(Difficulty.EASY)
                .createdAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .build();

        when(recipeService.getRecipe(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/recipes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Scrambled Eggs"))
                .andExpect(jsonPath("$.prepMinutes").value(2))
                .andExpect(jsonPath("$.cookMinutes").value(5))
                .andExpect(jsonPath("$.servings").value(2))
                .andExpect(jsonPath("$.difficulty").value("EASY"))
                .andExpect(jsonPath("$.totalMinutes").value(7))
                .andExpect(jsonPath("$.formattedTotalTime").value("7 mins"));

        verify(recipeService).getRecipe(1L);

    }

    @Test
    void shouldReturn404WhenRecipeDoesNotExist() throws Exception {

        when(recipeService.getRecipe(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Recipe not found: 99"));

        mockMvc.perform(get("/api/recipes/99"))
                .andExpect(status().isNotFound());

        verify(recipeService).getRecipe(99L);
    }

    private CreateRecipeRequest createRequest() {

        CreateRecipeRequest request = new CreateRecipeRequest();

        request.setName("Scrambled Eggs");
        request.setPrepMinutes(2);
        request.setCookMinutes(5);
        request.setServings(2);
        request.setDifficulty(Difficulty.EASY);

        request.setIngredients(List.of(createIngredient()));
        request.setSteps(List.of(createStep()));

        return request;
    }

    private CreateRecipeIngredientRequest createIngredient() {

        CreateRecipeIngredientRequest request = new CreateRecipeIngredientRequest();

        request.setIngredientId(1L);
        request.setQuantity(new BigDecimal("4"));
        request.setUnit(Unit.EACH);
        request.setOptional(false);

        return request;

    }

    private CreateRecipeStepRequest createStep() {

        CreateRecipeStepRequest request = new CreateRecipeStepRequest();

        request.setInstruction("Whisk eggs.");

        return request;

    }

}
