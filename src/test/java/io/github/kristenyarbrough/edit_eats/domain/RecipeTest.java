package io.github.kristenyarbrough.edit_eats.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class RecipeTest {

    @Test
    void shouldCalculateTotalMinutes() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(10)
                .cookMinutes(20)
                .build();

        assertEquals(30, recipe.getTotalMinutes());

    }

    @Test
    void shouldCalculateTotalMinutesWhenPrepMinutesIsNull() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(null)
                .cookMinutes(20)
                .build();

        assertEquals(20, recipe.getTotalMinutes());

    }

    @Test
    void shouldCalculateTotalMinutesWhenCookMinutesIsNull() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(15)
                .cookMinutes(null)
                .build();

        assertEquals(15, recipe.getTotalMinutes());

    }

    @Test
    void shouldReturnZeroWhenPrepAndCookMinutesAreNull() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(null)
                .cookMinutes(null)
                .build();

        assertEquals(0, recipe.getTotalMinutes());
    }

    @Test
    void shouldFormatMinutesOnly() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(20)
                .cookMinutes(25)
                .build();

        assertEquals("45 mins", recipe.getFormattedTotalTime());

    }

    @Test
    void shouldFormatOneHour() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(30)
                .cookMinutes(30)
                .build();

        assertEquals("1 hr", recipe.getFormattedTotalTime());

    }

    @Test
    void shouldFormatMultipleWholeHours() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(60)
                .cookMinutes(60)
                .build();

        assertEquals("2 hrs", recipe.getFormattedTotalTime());

    }

    @Test
    void shouldFormatOneHourAndMinutes() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(45)
                .cookMinutes(45)
                .build();

        assertEquals("1 hr 30 mins", recipe.getFormattedTotalTime());

    }

    @Test
    void shouldFormatMultipleHoursAndMinutes() {

        Recipe recipe = Recipe.builder()
                .prepMinutes(75)
                .cookMinutes(60)
                .build();

        assertEquals("2 hrs 15 mins", recipe.getFormattedTotalTime());

    }
}
