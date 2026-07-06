package io.github.kristenyarbrough.edit_eats.dto.request;

import io.github.kristenyarbrough.edit_eats.domain.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateRecipeRequest {

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    private Integer prepMinutes;

    @NotNull
    @Min(0)
    private Integer cookMinutes;

    @NotNull
    @Min(1)
    private Integer servings;

    @NotNull
    private Difficulty difficulty;

    private String sourceUrl;
    private String imageUrl;
    private String storageInstructions;
    private String freezerInstructions;

    @NotEmpty
    @Valid
    private List<CreateRecipeStepRequest> steps;

    @NotEmpty
    @Valid
    private List<CreateRecipeIngredientRequest> ingredients;

}

//    @NotEmpty
//    @Valid
//    private List<RecipeIngredientRequest> recipeIngredientRequests;
//
//    @Data
//    public static class RecipeIngredientRequest {
//        @NotBlank
//        private String name;
//
//        private BigDecimal quantity;
//        private Unit unit;
//        private String preparation;
//        private Boolean optional = false;
//    }
//}
