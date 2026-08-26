package io.github.kristenyarbrough.edit_eats.dto.response;

import io.github.kristenyarbrough.edit_eats.domain.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RecipeResponse {

    private Long id;

    private String name;

    private Integer prepMinutes;
    private Integer cookMinutes;
    private Integer passiveMinutes;
    private Integer activeMinutes;
    private Integer totalMinutes;
    private String formattedTotalTime;
    private String formattedActiveTime;

    private Integer servings;

    private Difficulty difficulty;

    private String sourceUrl;
    private String imageUrl;

    private String storageInstructions;
    private String freezerInstructions;

    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

    private List<RecipeIngredientResponse> ingredients;

    private List<RecipeStepResponse> steps;

    private List<RecipeCategoryResponse> categories;

}
