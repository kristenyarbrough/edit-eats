package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.Ingredient;
import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientRequest;
import io.github.kristenyarbrough.edit_eats.dto.response.IngredientResponse;
import io.github.kristenyarbrough.edit_eats.service.IngredientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngredientController.class)
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IngredientService ingredientService;

    @Test
    void shouldCreateIngredient() throws Exception {

        CreateIngredientRequest request = createRequest();

        IngredientCategory category = createCategory();

        Ingredient ingredient = Ingredient.builder()
                .id(1L)
                .name("Egg")
                .defaultUnit(Unit.EACH)
                .ingredientCategory(category)
                .build();

        when(ingredientService.createIngredient(any(CreateIngredientRequest.class)))
                .thenReturn(ingredient);

        mockMvc.perform(post("/api/ingredients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Egg"))
                .andExpect(jsonPath("$.defaultUnit").value("EACH"));

        verify(ingredientService).createIngredient(any(CreateIngredientRequest.class));

    }

    @Test
    void shouldGetAllIngredients() throws Exception {

        Ingredient butter = Ingredient.builder()
                .id(1L)
                .name("Butter")
                .build();

        Ingredient egg = Ingredient.builder()
                .id(2L)
                .name("Egg")
                .build();

        when(ingredientService.getAllIngredients())
                .thenReturn(List.of(butter, egg));

        mockMvc.perform(get("/api/ingredients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Butter"))
                .andExpect(jsonPath("$[1].name").value("Egg"));

        verify(ingredientService).getAllIngredients();
        verify(ingredientService, never()).findIngredients(anyString());

    }

    @Test
    void shouldSearchIngredients() throws Exception {

        Ingredient egg = Ingredient.builder()
                .id(1L)
                .name("Egg")
                .build();

        Ingredient eggplant = Ingredient.builder()
                .id(2L)
                .name("Eggplant")
                .build();

        when(ingredientService.findIngredients("egg"))
                .thenReturn(List.of(egg, eggplant));

        mockMvc.perform(get("/api/ingredients")
                    .param("name", "egg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Egg"))
                .andExpect(jsonPath("$[1].name").value("Eggplant"));

        verify(ingredientService).findIngredients("egg");
        verify(ingredientService, never()).getAllIngredients();

    }

    @Test
    void shouldReturnAllIngredientsWhenNameIsBlank() throws Exception {

        when(ingredientService.getAllIngredients())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ingredients")
                    .param("name", ""))
                .andExpect(status().isOk());

        verify(ingredientService).getAllIngredients();
        verify(ingredientService, never()).findIngredients(anyString());

    }

    @Test
    void shouldGetIngredient() throws Exception {

        IngredientCategory category = IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

        IngredientResponse response = IngredientResponse.builder()
                .id(1L)
                .name("Egg")
                .defaultUnit(Unit.EACH)
                .ingredientCategory(category)
                .build();

        when(ingredientService.getIngredient(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/ingredients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Egg"))
                .andExpect(jsonPath("$.defaultUnit").value("EACH"))
                .andExpect(jsonPath("$.ingredientCategory.id").value(1))
                .andExpect(jsonPath("$.ingredientCategory.name").value("Dairy"));

        verify(ingredientService).getIngredient(1L);

    }

    @Test
    void shouldReturn404WhenIngredientDoesNotExist() throws Exception {

        when(ingredientService.getIngredient(99L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ingredient not found: 99"));

        mockMvc.perform(get("/api/ingredients/99"))
                .andExpect(status().isNotFound());

        verify(ingredientService).getIngredient(99L);

    }

    private CreateIngredientRequest createRequest() {

        CreateIngredientRequest request = new CreateIngredientRequest();
        request.setName("Egg");
        request.setDefaultUnit(Unit.EACH);
        request.setIngredientCategoryId(1L);

        return request;

    }

    private IngredientCategory createCategory() {

        return IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

    }

}
