package io.github.kristenyarbrough.edit_eats.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recipe_ingredient_section")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredientSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_section_id")
    private RecipeIngredientSection parentSection;

    @Column(nullable = false)
    private Integer sortOrder;

}
