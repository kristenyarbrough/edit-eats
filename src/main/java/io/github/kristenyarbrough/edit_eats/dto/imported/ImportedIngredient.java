package io.github.kristenyarbrough.edit_eats.dto.imported;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ImportedIngredient {

    private String name;
    private BigDecimal quantity;
    private Unit unit;

    private BigDecimal alternativeQuantity;
    private Unit alternativeUnit;

    private String preparation;
    private String note;
    private Boolean optional;
    private String section;

    private List<ImportedIngredientSubstitution> substitutions;

}
