package io.github.kristenyarbrough.edit_eats.dto.imported;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportedInstructionSection {

    private String name;
    private List<ImportedStep> steps;
    private List<ImportedInstructionSection> sections;

}
