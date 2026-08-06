package io.github.kristenyarbrough.edit_eats.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateMealPlanRequest {

    @NotBlank
    private String name;

    @NotNull
    private LocalDate weekStarting;

//    private List<Long> recipeIds;
}
