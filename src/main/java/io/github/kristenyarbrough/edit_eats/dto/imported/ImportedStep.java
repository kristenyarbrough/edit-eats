package io.github.kristenyarbrough.edit_eats.dto.imported;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportedStep {

    private Integer stepNumber;
    private String instruction;

}
