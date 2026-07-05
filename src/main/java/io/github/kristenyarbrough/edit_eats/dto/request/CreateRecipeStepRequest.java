package io.github.kristenyarbrough.edit_eats.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRecipeStepRequest {

    @NotBlank
    private String instruction;

}
