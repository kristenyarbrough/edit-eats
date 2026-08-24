package io.github.kristenyarbrough.edit_eats.dto.imported;

import io.github.kristenyarbrough.edit_eats.domain.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportedRecipe {

    private String name;
    private Integer prepMinutes;
    private Integer cookMinutes;
    private Integer servings;
    private Difficulty difficulty;
    private String imageUrl;
    private String sourceUrl;

    private List<ImportedIngredient> ingredients;
    private List<ImportedStep> steps;

}
