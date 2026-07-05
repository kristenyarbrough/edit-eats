package io.github.kristenyarbrough.edit_eats.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_step",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_recipe_step_number",
        columnNames = {"recipe_id", "step_number"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(nullable = false)
    private Integer stepNumber;

    @Lob
    @Column(nullable = false)
    private String instruction;

}
