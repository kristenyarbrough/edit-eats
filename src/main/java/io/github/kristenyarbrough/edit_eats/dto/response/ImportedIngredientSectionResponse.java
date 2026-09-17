package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportedIngredientSectionResponse {

    private String name;
    private List<ImportedIngredientResponse> ingredients;
    private List<ImportedIngredientSectionResponse> sections;

}
