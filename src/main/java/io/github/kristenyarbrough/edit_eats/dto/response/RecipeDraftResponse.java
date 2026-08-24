package io.github.kristenyarbrough.edit_eats.dto.response;

import io.github.kristenyarbrough.edit_eats.domain.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeDraftResponse {

    private String name;
    private Integer prepMinutes;
    private Integer cookMinutes;
    private Integer servings;
    private Difficulty difficulty;
    private String sourceUrl;
    private String imageUrl;
    private String storageInstructions;
    private String freezerInstructions;

    private List<RecipeIngredientResponse> ingredients;
    private List<RecipeStepResponse> steps;
    private List<RecipeCategoryResponse> categories;

}
