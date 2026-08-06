package io.github.kristenyarbrough.edit_eats.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MealPlanResponse {

    private Long id;
    private String name;
    private LocalDate weekStarting;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;

}
