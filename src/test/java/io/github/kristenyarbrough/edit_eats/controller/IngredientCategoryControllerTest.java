package io.github.kristenyarbrough.edit_eats.controller;

import io.github.kristenyarbrough.edit_eats.domain.IngredientCategory;
import io.github.kristenyarbrough.edit_eats.dto.request.CreateIngredientCategoryRequest;
import io.github.kristenyarbrough.edit_eats.service.IngredientCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngredientCategoryController.class)
class IngredientCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IngredientCategoryService ingredientCategoryService;

    @Test
    void shouldCreateIngredientCategory() throws Exception {

        CreateIngredientCategoryRequest request =
                new CreateIngredientCategoryRequest();

        request.setName("Dairy");

        IngredientCategory category = IngredientCategory.builder()
                .id(1L)
                .name("Dairy")
                .build();

        when(ingredientCategoryService.createIngredientCategory(any()))
                .thenReturn(category);

        mockMvc.perform(post("/api/ingredient-categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Dairy"));

        verify(ingredientCategoryService)
                .createIngredientCategory(any(CreateIngredientCategoryRequest.class));

    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {

        CreateIngredientCategoryRequest request =
                new CreateIngredientCategoryRequest();

        request.setName("");

        mockMvc.perform(post("/api/ingredient-categories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(ingredientCategoryService, never())
                .createIngredientCategory(any());

    }
}
