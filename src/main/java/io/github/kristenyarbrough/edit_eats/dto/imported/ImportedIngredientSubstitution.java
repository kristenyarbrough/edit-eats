package io.github.kristenyarbrough.edit_eats.dto.imported;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ImportedIngredientSubstitution {

    private String name;
    private BigDecimal quantity;
    private Unit unit;
    private String preparation;
    private String note;

}
