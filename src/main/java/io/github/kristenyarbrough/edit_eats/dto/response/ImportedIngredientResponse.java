package io.github.kristenyarbrough.edit_eats.dto.response;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedIngredientSubstitution;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ImportedIngredientResponse {

    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private Unit unit;

    private BigDecimal alternativeQuantity;
    private Unit alternativeUnit;

    private String preparation;
    private String note;
    private Boolean optional;

    private List<ImportedIngredientSubstitutionResponse> substitutions;

}
