package io.github.kristenyarbrough.edit_eats.dto.imported;

import io.github.kristenyarbrough.edit_eats.domain.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ImportedRecipe {

    private String name;
    private Integer prepMinutes;
    private Integer cookMinutes;
    private Integer activeMinutes;
    private Integer passiveMinutes;
    private Integer totalMinutes;
    private Integer servings;
    private Difficulty difficulty;
    private String imageUrl;
    private String sourceUrl;

    @Builder.Default
    private List<ImportedIngredient> ingredients = new ArrayList<>();

    @Builder.Default
    private List<ImportedIngredientSection> ingredientSections = new ArrayList<>();

    @Builder.Default
    private List<ImportedStep> steps = new ArrayList<>();

    @Builder.Default
    private List<ImportedInstructionSection> instructionSections = new ArrayList<>();

}
