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

    @OneToMany(mappedBy = "mealPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("mealDate ASC, mealType ASC")
    @Builder.Default
    private List<MealPlanRecipe> mealPlanRecipes = new ArrayList<>();

}
