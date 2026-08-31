package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedIngredient;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedStep;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeImportServiceTest {

    @Mock
    private RecipeStructuredDataParser structuredDataParser;

    @Mock
    private RecipePageFetcher pageFetcher;

    @InjectMocks
    private RecipeImportService recipeImportService;

    @Test
    void shouldImportRecipeFromUrl() {

        String jsonLd = """
                {
                    "@context": "https://schema.org",
                    "@type": "Recipe",
                    "name": "Chicken Curry",
                    "image": "https://example.com/chicken-curry.jpg",
                    "prepTime": "PT15M",
                    "cookTime": "PT30M",
                    "recipeYield": "4 servings",
                    "recipeIngredient": [
                        "500 g chicken breast",
                        "1 onion",
                        "400 ml coconut milk"
                    ],
                    "recipeInstructions": [
                        {
                            "@type": "HowToStep",
                            "text": "Cut the chicken into pieces."
                        },
                        {
                            "@type": "HowToStep",
                            "text": "Cook the onion until softened."
                        },
                        {
                            "@type": "HowToStep",
                            "text": "Add the chicken and coconut milk."
                        }
                    ]
                }
                """;

        Document document = Jsoup.parse("""
                <html>
                    <head>
                        <script type="application/ld+json">
                        %s
                        </script>
                    </head>
                </html>
                """.formatted(jsonLd));

        ImportedRecipe parsedRecipe = ImportedRecipe.builder()
                .name("Chicken Curry")
                .prepMinutes(15)
                .cookMinutes(30)
                .servings(4)
                .imageUrl("https://example.com/chicken-curry.jpg")
                .ingredients(List.of(
                        ImportedIngredient.builder()
                                .name("chicken breast")
                                .quantity(new BigDecimal("500"))
                                .unit(Unit.G)
                                .build(),
                        ImportedIngredient.builder()
                                .name("onion")
                                .quantity(new BigDecimal("1"))
                                .build(),
                        ImportedIngredient.builder()
                                .name("coconut milk")
                                .quantity(new BigDecimal("400"))
                                .unit(Unit.ML)
                                .build()
                ))
                .steps(List.of(
                        ImportedStep.builder()
                                .instruction("Cut the chicken into pieces.")
                                .build(),
                        ImportedStep.builder()
                                .instruction("Cook the onion until softened.")
                                .build(),
                        ImportedStep.builder()
                                .instruction("Add the chicken and coconut milk.")
                                .build()
                ))
                .build();

        when(pageFetcher.fetch("https://example.com/recipe"))
                .thenReturn(document);

        when(structuredDataParser.parse(anyString()))
                .thenReturn(parsedRecipe);

        ImportedRecipe result = recipeImportService.importRecipeFromUrl(
                "https://example.com/recipe"
        );

        verify(structuredDataParser).parse(anyString());

        assertEquals("Chicken Curry", result.getName());
        assertEquals(15, result.getPrepMinutes());
        assertEquals(30, result.getCookMinutes());
        assertEquals(45, result.getActiveMinutes());
        assertEquals(0, result.getPassiveMinutes());
        assertEquals(45, result.getTotalMinutes());
        assertEquals(4, result.getServings());
        assertEquals("https://example.com/recipe", result.getSourceUrl());
        assertEquals(3, result.getIngredients().size());
        assertEquals(3, result.getSteps().size());

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

    @Test
    void shouldImportRealisticRecipe() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Creamy Garlic Chicken
                
                Serves 4
                Prep Time: 15 minutes
                Cook Time: 25 minutes
                
                Ingredients
                
                4 chicken breasts
                1 tbsp olive oil
                1 tbsp butter
                4 cloves garlic, minced
                1 onion, finely chopped
                1 cup chicken stock
                1/2 cup heavy cream
                1/3 cup grated Parmesan
                1 tsp Italian seasoning
                1/2 tsp salt
                1/4 tsp black pepper
                1/2 cup baby spinach, optional
                
                Method
                
                Season the chicken breasts with salt and pepper.
                Heat the olive oil and butter in a large frying pan over medium-high heat.
                Add the chicken and cook for 5–6 minutes on each side, or until golden and cooked through. Remove from the pan and set aside.
                Add the onion and cook for 3–4 minutes until softened.
                Add the garlic and Italian seasoning and cook for 30 seconds.
                Pour in the chicken stock and scrape any browned bits from the bottom of the pan.
                Reduce the heat and stir in the cream and Parmesan.
                Return the chicken to the pan and simmer for 5 minutes.
                Stir through the spinach and cook until wilted.
                Serve immediately.
                """);

        assertEquals("Creamy Garlic Chicken", result.getName());
        assertEquals(4, result.getServings());

        assertEquals(12, result.getIngredients().size());
        assertEquals(10, result.getSteps().size());

        assertEquals(15, result.getPrepMinutes());
        assertEquals(25, result.getCookMinutes());

    }

    @Test
    void shouldParseTotalTime() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Chocolate Cake
                
                Serves: 8
                Total Time: 1 hour 15 minutes
                
                Ingredients
                200 g flour
                
                Method
                Mix ingredients.
                Bake.
                """);

        assertEquals(75, result.getTotalMinutes());

    }

    @Test
    void shouldParseHoursAbbreviation() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 1 hr
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(60, result.getTotalMinutes());

    }

    @Test
    void shouldParseHoursPluralAbbreviation() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 2 hrs
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(120, result.getTotalMinutes());

    }

    @Test
    void shouldParseMinutesAbbreviation() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 15 min
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(15, result.getTotalMinutes());

    }

    @Test
    void shouldParseMinutesPluralAbbreviation() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 15 mins
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(15, result.getTotalMinutes());

    }

    @Test
    void shouldParseHoursAndMinutesAbbreviations() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 1 hr 15 mins
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(75, result.getTotalMinutes());

    }

    @Test
    void shouldParseHoursPluralAndMinutesAbbreviations() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 1 hrs 15 min
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(75, result.getTotalMinutes());

    }

    @Test
    void shouldParseHoursPluralAndMinutesPluralAbbreviations() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 2 hrs 30 mins
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(150, result.getTotalMinutes());

    }

    @Test
    void shouldParseHoursFullAndMinutesAbbreviations() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 1 hour 15 min
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(75, result.getTotalMinutes());

    }

    @Test
    void shouldParseHoursAbbreviationAndMinutesFull() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Cake
                
                Total Time: 1 hr 15 minutes
                
                Ingredients
                1 cup flour
                
                Method
                Bake.
                """);

        assertEquals(75, result.getTotalMinutes());

    }

    @Test
    void shouldParsePrepCookAndTotalTime() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
                Chicken Curry
                
                Prep Time: 15 mins
                Cook Time: 45 mins
                Total Time: 1 hr
                
                Ingredients
                500 g chicken breast
                
                Method
                Cook the chicken.
                """);

        assertEquals(15, result.getPrepMinutes());
        assertEquals(45, result.getCookMinutes());
        assertEquals(60, result.getTotalMinutes());

    }

    @Test
    void shouldParseTimesWithoutColons() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Chicken Curry

            Prep Time 15 mins
            Cook Time 45 mins
            Total Time 1 hr

            Ingredients
            500 g chicken breast

            Method
            Cook the chicken.
            """);

        assertEquals(15, result.getPrepMinutes());
        assertEquals(45, result.getCookMinutes());
        assertEquals(60, result.getTotalMinutes());
    }

    @Test
    void shouldParsePassiveTime() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Marinated Chicken

            Prep Time: 15 mins
            Cook Time: 10 mins
            Marinating Time: 2 hrs
            Total Time: 2 hrs 25 mins

            Ingredients
            500 g chicken breast

            Method
            Mix the marinade.
            Marinate the chicken.
            Cook the chicken.
            """);

        assertEquals(15, result.getPrepMinutes());
        assertEquals(10, result.getCookMinutes());
        assertEquals(120, result.getPassiveMinutes());
        assertEquals(145, result.getTotalMinutes());

    }

    @Test
    void shouldParsePassiveTimeWithoutColon() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Marinated Chicken

            Prep Time 15 mins
            Cook Time 10 mins
            Marinating Time 2 hrs
            Total Time 2 hrs 25 mins

            Ingredients
            500 g chicken breast

            Method
            Mix the marinade.
            Marinate the chicken.
            Cook the chicken.
            """);

        assertEquals(120, result.getPassiveMinutes());

    }

    @Test
    void shouldImportRealisticRecipeChickenKorma() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Chicken Korma

            Serves 4

            Prep: 10 mins
            Cook: 25 mins

            Ingredients
            1 onion, chopped
            2 garlic cloves, roughly chopped
            thumb-sized piece ginger, roughly chopped
            4 tbsp korma paste
            4 skinless, boneless chicken breasts, cut into bite-sized pieces
            50g ground almonds, plus extra to serve (optional)
            4 tbsp sultanas
            400ml chicken stock
            ¼ tsp golden caster sugar
            150g pot 0% fat Greek yogurt
            small bunch coriander, chopped

            Method
            Put the onion, garlic and ginger in a food processor and whizz to a paste.
            Tip the paste into a large frying pan and cook for 5 mins.
            Add the korma paste and cook for 2 mins until aromatic.
            Stir the chicken into the sauce.
            Add the ground almonds, sultanas, chicken stock and sugar.
            Cover and simmer for 10 mins or until the chicken is cooked through.
            Remove from the heat and stir in the yogurt.
            Scatter over the coriander and serve.
            """);

        assertEquals("Chicken Korma", result.getName());
        assertEquals(4, result.getServings());
        assertEquals(10, result.getPrepMinutes());
        assertEquals(25, result.getCookMinutes());

        assertEquals(11, result.getIngredients().size());
        assertEquals(8, result.getSteps().size());

    }

    @Test
    void shouldParsePrepTime() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Chicken Curry
            
            Prep: 10 mins
            Cook: 30 mins
            
            Ingredients:
            500 g chicken
            
            Method:
            Cook chicken.
            """);

        assertEquals(10, result.getPrepMinutes());
        assertEquals(30, result.getCookMinutes());

    }

    @Test
    void shouldParsePreparationTime() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Chicken Curry
            
            Preparation Time: 10 minutes
            Cooking Time: 30 minutes
            
            Ingredients:
            500 g chicken
            
            Method:
            Cook chicken.
            """);

        assertEquals(10, result.getPrepMinutes());
        assertEquals(30, result.getCookMinutes());

    }

    @Test
    void shouldParsePrepTimeWithoutColon() {

        ImportedRecipe result = recipeImportService.importRecipeFromText("""
            Chicken Curry
            
            Prep 10 mins
            Cook 30 mins
            
            Ingredients:
            500 g chicken
            
            Method:
            Cook chicken.
            """);

        assertEquals(10, result.getPrepMinutes());
        assertEquals(30, result.getCookMinutes());

    }

    @Test
    void shouldPreservePassiveMinutesWhenProvided() {

        ImportedRecipe recipe = ImportedRecipe.builder()
                .name("Marinated Chicken")
                .prepMinutes(15)
                .cookMinutes(10)
                .passiveMinutes(120)
                .servings(4)
                .ingredients(List.of(
                        ImportedIngredient.builder()
                                .name("chicken breast")
                                .quantity(new BigDecimal("500"))
                                .unit(Unit.G)
                                .build()
                ))
                .build();

        when(pageFetcher.fetch("https://example.com/recipe"))
                .thenReturn(Jsoup.parse("""
                        <html>
                            <head>
                                <script type="application/ld+json">
                                {}
                                </script>
                            </head>
                        </html>
                        """));

        when(structuredDataParser.parse(anyString()))
                .thenReturn(recipe);

        ImportedRecipe result = recipeImportService.importRecipeFromUrl(
                "https://example.com/recipe"
        );

        assertEquals(15, result.getPrepMinutes());
        assertEquals(10, result.getCookMinutes());
        assertEquals(25, result.getActiveMinutes());
        assertEquals(120, result.getPassiveMinutes());
        assertEquals(145, result.getTotalMinutes());

    }

    @Test
    void shouldPreserveExplicitTotalMinutes() {

        ImportedRecipe recipe = ImportedRecipe.builder()
                .name("Chicken Curry")
                .prepMinutes(15)
                .cookMinutes(30)
                .passiveMinutes(10)
                .totalMinutes(100)
                .servings(4)
                .ingredients(List.of(
                        ImportedIngredient.builder()
                                .name("chicken breast")
                                .quantity(new BigDecimal("500"))
                                .unit(Unit.G)
                                .build()
                ))
                .build();

        when(pageFetcher.fetch("https://example.com/recipe"))
                .thenReturn(Jsoup.parse("""
                        <html>
                            <head>
                                <script type="application/ld+json">
                                {}
                                </script>
                            </head>
                        </html>
                        """));

        when(structuredDataParser.parse(anyString()))
                .thenReturn(recipe);

        ImportedRecipe result = recipeImportService.importRecipeFromUrl(
                "https://example.com/recipe"
        );

        assertEquals(45, result.getActiveMinutes());
        assertEquals(10, result.getPassiveMinutes());
        assertEquals(100, result.getTotalMinutes());

    }

    @Test
    void shouldCalculateActiveMinutesFromPrepTimeOnly() {

        ImportedRecipe recipe = ImportedRecipe.builder()
                .name("Simple Salad")
                .prepMinutes(15)
                .ingredients(List.of(
                        ImportedIngredient.builder()
                                .name("lettuce")
                                .quantity(new BigDecimal("50"))
                                .unit(Unit.G)
                                .build()
                ))
                .build();

        when(pageFetcher.fetch("https://example.com/recipe"))
                .thenReturn(Jsoup.parse("""
                        <html>
                            <head>
                                <script type="application/ld+json">
                                {}
                                </script>
                            </head>
                        </html>
                        """));

        when(structuredDataParser.parse(anyString()))
                .thenReturn(recipe);

        ImportedRecipe result = recipeImportService.importRecipeFromUrl(
                "https://example.com/recipe"
        );

        assertEquals(15, result.getActiveMinutes());
        assertEquals(0, result.getPassiveMinutes());
        assertEquals(15, result.getTotalMinutes());

    }

    @Test
    void shouldCalculateActiveMinutesFromCookTimeOnly() {

        ImportedRecipe recipe = ImportedRecipe.builder()
                .name("Roast Chicken")
                .cookMinutes(60)
                .ingredients(List.of(
                        ImportedIngredient.builder()
                                .name("whole chicken")
                                .quantity(new BigDecimal("1"))
                                .unit(Unit.EACH)
                                .build()
                ))
                .build();

        when(pageFetcher.fetch("https://example.com/recipe"))
                .thenReturn(Jsoup.parse("""
                        <html>
                            <head>
                                <script type="application/ld+json">
                                {}
                                </script>
                            </head>
                        </html>
                        """));

        when(structuredDataParser.parse(anyString()))
                .thenReturn(recipe);

        ImportedRecipe result = recipeImportService.importRecipeFromUrl(
                "https://example.com/recipe"
        );

        assertEquals(60, result.getCookMinutes());
        assertEquals(0, result.getPassiveMinutes());
        assertEquals(60, result.getTotalMinutes());

    }

}
