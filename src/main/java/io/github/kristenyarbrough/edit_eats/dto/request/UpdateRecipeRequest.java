package io.github.kristenyarbrough.edit_eats.dto.request;

import io.github.kristenyarbrough.edit_eats.domain.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateRecipeRequest {

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
    private List<CreateRecipeStepRequest> steps = new ArrayList<>();

    @NotEmpty
    @Valid
    private List<CreateRecipeIngredientRequest> ingredients = new ArrayList<>();

    @Valid
    private List<CreateRecipeCategoryRequest> categories = new ArrayList<>();

}
