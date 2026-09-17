package io.github.kristenyarbrough.edit_eats.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateRecipeInstructionSectionRequest {

    @NotBlank
    private String name;

    @Valid
    private List<CreateRecipeStepRequest> steps = new ArrayList<>();

    @Valid
    private List<CreateRecipeInstructionSectionRequest> sections = new ArrayList<>();

}
