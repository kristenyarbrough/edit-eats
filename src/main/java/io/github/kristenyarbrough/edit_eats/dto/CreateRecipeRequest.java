package io.github.kristenyarbrough.edit_eats.dto;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateRecipeRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String method;

    private String sourceUrl;
    private String photoUrl;
    private String storageInstructions;
    private String freezerInstructions;

    @NotNull
    @Min(1)
    private Integer servings;

    @NotNull
    @Min(0)
    private Integer prepMinutes;

    @NotNull
    @Min(0)
    private Integer cookMinutes;

    @NotEmpty
    @Valid
    private List<Ingredient> ingredients;

    @Data
    public static class Ingredient {
        @NotBlank
        private String name;

        private BigDecimal quantity;
        private Unit unit;
        private String notes;
    }
}
