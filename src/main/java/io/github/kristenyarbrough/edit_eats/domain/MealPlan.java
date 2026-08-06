package io.github.kristenyarbrough.edit_eats.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_plan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // We store the Monday (or chosen start date) for the plan
    @Column(nullable = false)
    private LocalDate weekStarting;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;

//    @ElementCollection
//    @CollectionTable(name = "meal_plan_recipe_ids", joinColumns = @JoinColumn(name = "meal_plan_id"))
//    @Column(name = "recipe_id", nullable = false)
//    @Builder.Default
//    private List<Long> recipeIds = new ArrayList<>();
}
