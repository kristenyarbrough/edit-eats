package io.github.kristenyarbrough.edit_eats.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer prepMinutes;

    @Column(nullable = false)
    private Integer cookMinutes;

    @Column(nullable = false)
    private Integer passiveMinutes;

    @Column(nullable = false)
    private Integer servings;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    private String sourceUrl;
    private String imageUrl;

    @Lob
    private String storageInstructions;

    @Lob
    private String freezerInstructions;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;

    @Transient
    public int getTotalMinutes() {

        return getActiveMinutes()
                + valueOrZero(passiveMinutes);

    }

    @Transient
    public int getActiveMinutes() {

        return valueOrZero(prepMinutes)
                + valueOrZero(cookMinutes);

    }


    @Transient

    public String getFormattedTotalTime() {

        return formatTime(getTotalMinutes());

    }

    public String getFormattedActiveTime() {

        return formatTime(getActiveMinutes());

    }

    private int valueOrZero(Integer value) {

        return value == null ? 0 : value;

    }

    private String formatTime(Integer totalMinutes) {

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (hours == 0) {
            return minutes + " mins";
        }

        if (minutes == 0) {
            return hours + (hours == 1 ? " hr" : " hrs");
        }

        return hours + (hours == 1 ? " hr " : " hrs ") + minutes + " mins";

    }

}
    //private OffsetDateTime createdAt;
    //
    //@OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    //@Builder.Default
    //private List<RecipeIngredient> ingredients = new ArrayList<>();