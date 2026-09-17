package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeInstructionSectionResponse {

    private String name;
    private List<RecipeStepResponse> steps;
    private List<RecipeInstructionSectionResponse> sections;

}
