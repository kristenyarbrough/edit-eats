package io.github.kristenyarbrough.edit_eats.dto.imported;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportedIngredientSection {

    private String name;
    private List<ImportedIngredient> ingredients;
    private List<ImportedIngredientSection> sections;

}
