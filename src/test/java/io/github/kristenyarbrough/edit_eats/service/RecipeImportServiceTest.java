package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedIngredient;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import io.github.kristenyarbrough.edit_eats.dto.response.ImportedRecipeResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecipeImportServiceTest {

    private final RecipeImportService recipeImportService = new RecipeImportService();

    @Test
    void shouldImportRecipeFromUrl() {

        ImportedRecipe result = recipeImportService.importRecipeFromUrl(
                "https://example.com/recipe"
        );

        assertNull(result);

    }

    @Test
    void shouldImportRecipeFromText() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Chicken Curry
                
                Serves: 4
                
                Ingredients:
                500 g chicken breast
                1 onion
                2 tsp curry powder
                
                Method:
                Cut the chicken into pieces.
                Cook the onion until softened.
                Add the chicken and curry powder and cook until done.
                """);

        assertEquals("Chicken Curry", result.getName());
        assertEquals(4, result.getServings());
        assertEquals(3, result.getIngredients().size());
        assertEquals("chicken breast", result.getIngredients().get(0).getName());
        assertEquals("onion", result.getIngredients().get(1).getName());
        assertEquals("curry powder", result.getIngredients().get(2).getName());
        assertEquals(3, result.getSteps().size());

    }

    @Test
    void shouldParseFractionalIngredientQuantity() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Pancakes
                
                Serves: 4
                
                Ingredients:
                1/2 cup flour
                1 1/2 cups milk
                3/4 tsp salt
                
                Method:
                Mix ingredients.
                Cook pancakes.
                """);

        assertEquals(0, new BigDecimal("0.5")
                .compareTo(result.getIngredients().get(0).getQuantity())
        );
        assertEquals(Unit.CUP, result.getIngredients().get(0).getUnit());

        assertEquals(0, new BigDecimal("1.5")
                .compareTo(result.getIngredients().get(1).getQuantity())
        );
        assertEquals(Unit.CUP, result.getIngredients().get(1).getUnit());

        assertEquals(0, new BigDecimal("0.75")
                .compareTo(result.getIngredients().get(2).getQuantity())
        );
        assertEquals(Unit.TSP, result.getIngredients().get(2).getUnit());

    }

    @Test
    void shouldParseUnicodeFractionalIngredientQuantity() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Serves: 4
                
                Ingredients:
                ½ cup sugar
                ¾ tsp salt
                ¼ cup butter
                
                Method:
                Mix ingredients.
                Bake.
                """);

        assertEquals(0, new BigDecimal("0.5")
                .compareTo(result.getIngredients().get(0).getQuantity())
        );
        assertEquals(0, new BigDecimal("0.75")
                .compareTo(result.getIngredients().get(1).getQuantity())
        );
        assertEquals(0, new BigDecimal("0.25")
                .compareTo(result.getIngredients().get(2).getQuantity())
        );

    }

    @Test
    void shouldParseMixedNumberWithUnicodeFraction() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Serves: 4
                
                Ingredients:
                1¼ cups flour
                
                Method:
                Mix ingredients.
                """);

        assertEquals(0, new BigDecimal("1.25")
                .compareTo(result.getIngredients().get(0).getQuantity())
        );

    }

    @Test
    void shouldParseMixedNumberWithSpaceAndUnicodeFraction() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Cake

            Serves: 4

            Ingredients:
            1 ¼ cups flour

            Method:
            Mix ingredients.
            """);

        assertEquals(
                0, new BigDecimal("1.25")
                        .compareTo(result.getIngredients().get(0).getQuantity())
        );

    }

    @Test
    void shouldParseMixedNumberWithAsciiFraction() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Cake

            Serves: 4

            Ingredients:
            1 1/4 cups flour

            Method:
            Mix ingredients.
            """);

        assertEquals(
                0, new BigDecimal("1.25")
                        .compareTo(result.getIngredients().get(0).getQuantity())
        );

    }

    @Test
    void shouldParseMixedNumberWithAttachedUnicodeFraction() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Ingredients:
                1¼ cups flour
                
                Method:
                Mix ingredients.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals(0, new BigDecimal("1.25")
                .compareTo(ingredient.getQuantity()));
        assertEquals(Unit.CUP, ingredient.getUnit());
        assertEquals("flour", ingredient.getName());

    }

    @Test
    void shouldParseMixedNumbersWithSpacedUnicodeFraction() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Ingredients:
                1 ¼ cups flour
                
                Method:
                Mix ingredients.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals(0, new BigDecimal("1.25")
                .compareTo(ingredient.getQuantity()));
        assertEquals(Unit.CUP, ingredient.getUnit());
        assertEquals("flour", ingredient.getName());

    }

    @Test
    void shouldImportRecipeWithHeadingsWithoutColons() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Chicken Curry
                
                Serves 4
                
                Ingredients
                500 g chicken breast
                1 onion
                2 tsp curry powder
                
                Method
                Cut the chicken into pieces.
                Cook the onion until softened.
                Add the chicken and curry powder and cook until done.
                """);

        assertEquals("Chicken Curry", result.getName());
        assertEquals(4, result.getServings());
        assertEquals(3, result.getIngredients().size());
        assertEquals(3, result.getSteps().size());

    }

    @Test
    void shouldParseIngredientPreparation() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Chicken Curry
                
                Ingredients:
                3 cloves garlic, minced
                1 onion, finely chopped
                500 g chicken breast, diced
                
                Method:
                Cook the ingredients.
                """);

        ImportedIngredient garlic = result.getIngredients().get(0);
        assertEquals(3, garlic.getQuantity().intValue());
        assertEquals(Unit.CLOVE, garlic.getUnit());
        assertEquals("garlic", garlic.getName());
        assertEquals("minced", garlic.getPreparation());

        ImportedIngredient onion = result.getIngredients().get(1);
        assertEquals("onion", onion.getName());
        assertEquals("finely chopped", onion.getPreparation());

        ImportedIngredient chicken = result.getIngredients().get(2);
        assertEquals("chicken breast", chicken.getName());
        assertEquals("diced", chicken.getPreparation());

    }

    @Test
    void shouldParseIngredientWithoutQuantityOrUnit() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Soup
                
                Ingredients:
                salt
                pepper, to taste
                
                Method:
                Season to taste.
                """);

        ImportedIngredient salt = result.getIngredients().get(0);
        assertNull(salt.getQuantity());
        assertNull(salt.getUnit());
        assertEquals("salt", salt.getName());

        ImportedIngredient pepper = result.getIngredients().get(1);
        assertNull(pepper.getQuantity());
        assertNull(pepper.getUnit());
        assertEquals("pepper", pepper.getName());
        assertEquals("to taste", pepper.getPreparation());

    }

    @Test
    void shouldParseCommonUnitAliases() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Test Recipe
                
                Ingredients:
                500 grams chicken
                250 millilitres milk
                2 tablespoons oil
                3 teaspoons salt
                4 cloves garlic
                
                Method:
                Cook.
                """);

        assertEquals(Unit.G, result.getIngredients().get(0).getUnit());
        assertEquals(Unit.ML, result.getIngredients().get(1).getUnit());
        assertEquals(Unit.TBSP, result.getIngredients().get(2).getUnit());
        assertEquals(Unit.TSP, result.getIngredients().get(3).getUnit());
        assertEquals(Unit.CLOVE, result.getIngredients().get(4).getUnit());

    }

    @Test
    void shouldRecogniseQuantityWithoutUnit() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Salad
                
                Ingredients:
                1 onion
                salt
                
                Method:
                Chop onion.
                """);

        assertEquals(0, new BigDecimal("1")
                .compareTo(result.getIngredients().get(0).getQuantity()));
        assertEquals("onion", result.getIngredients().get(0).getName());
        assertNull(result.getIngredients().get(1).getQuantity());
        assertEquals("salt", result.getIngredients().get(1).getName());

    }

    @Test
    void shouldParseSpacedUnicodeFraction(){

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Ingredients:
                1 ¼ cups flour
                
                Method:
                Mix.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals(0, new BigDecimal("1.25")
                .compareTo(ingredient.getQuantity()));
        assertEquals(Unit.CUP, ingredient.getUnit());
        assertEquals("flour", ingredient.getName());

    }

    @Test
    void shouldParseOunces() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Brownies
                
                Ingredients:
                8 oz chocolate
                
                Method:
                Melt chocolate.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals(0, new BigDecimal("8").compareTo(ingredient.getQuantity()));
        assertEquals(Unit.OZ, ingredient.getUnit());
        assertEquals("chocolate", ingredient.getName());

    }

    @Test
    void shouldParsePounds() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Beef Stew
                
                Ingredients:
                2 lb beef
                
                Method:
                Cook beef.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals(0, new BigDecimal("2")
                .compareTo(ingredient.getQuantity()));
        assertEquals(Unit.LB, ingredient.getUnit());
        assertEquals("beef", ingredient.getName());

    }

    @Test
    void shouldParsePreparation() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Chicken
                
                Ingredients:
                2 cloves garlic, minced
                1 onion, finely diced
                2 tbsp olive oil, divided
                
                Method:
                Cook ingredients.
                """);

        ImportedIngredient garlic = result.getIngredients().get(0);

        assertEquals(2, garlic.getQuantity().intValue());
        assertEquals(Unit.CLOVE, garlic.getUnit());
        assertEquals("garlic", garlic.getName());
        assertEquals("minced", garlic.getPreparation());

        ImportedIngredient onion = result.getIngredients().get(1);

        assertEquals("onion", onion.getName());
        assertEquals("finely diced", onion.getPreparation());

        ImportedIngredient oil = result.getIngredients().get(2);

        assertEquals(Unit.TBSP, oil.getUnit());
        assertEquals("olive oil", oil.getName());
        assertEquals("divided", oil.getPreparation());

    }

    @Test
    void shouldParseToTaste() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Soup
                
                Ingredients:
                salt to taste
                pepper, to taste
                
                Method:
                Season soup.
                """);

        ImportedIngredient salt = result.getIngredients().get(0);

        assertNull(salt.getQuantity());
        assertNull(salt.getUnit());
        assertEquals("salt", salt.getName());
        assertEquals("to taste", salt.getPreparation());

        ImportedIngredient pepper = result.getIngredients().get(1);

        assertNull(pepper.getQuantity());
        assertNull(pepper.getUnit());
        assertEquals("pepper", pepper.getName());
        assertEquals("to taste", pepper.getPreparation());

    }

    @Test
    void shouldParseParenthesisPreparation() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Ingredients:
                2 tbsp butter (melted)
                
                Method:
                Mix ingredients.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals("butter", ingredient.getName());
        assertEquals("melted", ingredient.getPreparation());

    }

    @Test
    void shouldParseCommaPreparationWithoutUnit() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Salad
                
                Ingredients:
                1 onion, finely diced
                
                Method:
                Mix ingredients.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals(0, new BigDecimal("1")
                .compareTo(ingredient.getQuantity()));
        assertNull(ingredient.getUnit());
        assertEquals("onion", ingredient.getName());
        assertEquals("finely diced", ingredient.getPreparation());

    }

    @Test
    void shouldParseIngredientToTaste() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Pasta

            Ingredients:
            Salt, to taste

            Method:
            Cook pasta.
            """);

        ImportedIngredient ingredient =
                result.getIngredients().get(0);

        assertNull(ingredient.getQuantity());
        assertNull(ingredient.getUnit());
        assertEquals("Salt", ingredient.getName());
        assertEquals("to taste", ingredient.getPreparation());
    }

    @Test
    void shouldParseQuantityWithToTaste() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Pasta

            Ingredients:
            ½ tsp salt, to taste

            Method:
            Cook pasta.
            """);

        ImportedIngredient ingredient =
                result.getIngredients().get(0);

        assertEquals(
                0,
                new BigDecimal("0.5")
                        .compareTo(ingredient.getQuantity())
        );
        assertEquals(Unit.TSP, ingredient.getUnit());
        assertEquals("salt", ingredient.getName());
        assertEquals("to taste", ingredient.getPreparation());
    }

    @Test
    void shouldParseToTasteWithoutComma() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Pasta

            Ingredients:
            Salt to taste

            Method:
            Cook pasta.
            """);

        ImportedIngredient ingredient =
                result.getIngredients().get(0);

        assertNull(ingredient.getQuantity());
        assertNull(ingredient.getUnit());
        assertEquals("Salt", ingredient.getName());
        assertEquals("to taste", ingredient.getPreparation());
    }

    @Test
    void shouldParseOptionalIngredientWithParentheses() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Pasta
                
                Ingredients:
                1/2 cup basil (optional)
                
                Method:
                Cook pasta.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals("basil", ingredient.getName());
        assertNull(ingredient.getPreparation());
        assertEquals(true, ingredient.getOptional());

    }

    @Test
    void shouldParseOptionalIngredientWithComma() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Pasta
                
                Ingredients:
                1/2 cup basil, optional
                
                Method:
                Cook pasta.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals("basil", ingredient.getName());
        assertNull(ingredient.getPreparation());
        assertEquals(true, ingredient.getOptional());

    }

    @Test
    void shouldParseOptionalIngredientWithoutPunctuation() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Pasta
                
                Ingredients:
                1/2 cup basil optional
                
                Method:
                Cook pasta.
                """);

        ImportedIngredient ingredient = result.getIngredients().get(0);

        assertEquals("basil", ingredient.getName());
        assertNull(ingredient.getPreparation());
        assertEquals(true, ingredient.getOptional());

    }

}
