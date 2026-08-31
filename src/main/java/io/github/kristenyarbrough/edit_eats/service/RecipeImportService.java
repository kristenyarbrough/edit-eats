package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedIngredient;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedStep;
import io.github.kristenyarbrough.edit_eats.dto.response.ImportedIngredientResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeDraftResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeIngredientResponse;
import io.github.kristenyarbrough.edit_eats.dto.response.RecipeStepResponse;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RecipeImportService {

    private final RecipeStructuredDataParser structuredDataParser;
    private final RecipePageFetcher pageFetcher;

    public RecipeDraftResponse importRecipeFromUrl(String url) {

        try {

            Document document = pageFetcher.fetch(url);

            Elements structuredDataScripts =
                    document.select("script[type=application/ld+json]");

            for (Element script : structuredDataScripts) {

                String json = script.data();

                if (json == null || json.isBlank()) {

                    continue;

                }

                try {

                    ImportedRecipe recipe = structuredDataParser.parse(json);

                    if (recipe.getName() != null
                            && recipe.getIngredients() != null
                            && !recipe.getIngredients().isEmpty()) {

                        ImportedRecipe importedRecipe = convertStructuredRecipe(recipe, url);

                        return toDraftResponse(importedRecipe);

                    }

                } catch (IllegalArgumentException e) {

                    // Not a recipe JSON-LD block.
                    // Try the next structured-data block.

                }

            }

            throw new IllegalArgumentException(
                    "No recipe structured data found at URL: " + url);

        } catch (IllegalArgumentException e) {

                throw e;

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Unable to import recipe from URL: " + url,
                    e
            );

        }

    }

    public RecipeDraftResponse importRecipeFromText(String text) {

        String[] lines = text.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toArray(String[]::new);

        String name = lines[0];

        Integer servings = null;
        Integer prepMinutes = null;
        Integer cookMinutes = null;
        Integer passiveMinutes = null;
        Integer totalMinutes = null;

        List<ImportedIngredient> ingredients = new ArrayList<>();
        List<ImportedStep> steps = new ArrayList<>();

        boolean readingIngredients = false;
        boolean readingMethod = false;

        for (int i = 1; i < lines.length; i++) {

            String line = lines[i];
            String heading = line.trim();

            if (heading.endsWith(":")) {

                heading = heading.substring(0, heading.length() - 1).trim();

            }

            if (heading.toLowerCase().startsWith("serves:")) {

                String value = heading.substring("serves:".length()).trim();
                servings = Integer.valueOf(value);
                continue;

            }

            if (isPrepTimeHeading(heading)) {

                prepMinutes = parseTimeValue(heading);
                continue;

            }

            if (isCookTimeHeading(heading)) {

                cookMinutes = parseTimeValue(heading);
                continue;

            }

            if (isPassiveTimeHeading(heading)) {

                passiveMinutes = parseTimeValue(heading);
                continue;

            }

            if (isTotalTimeHeading(heading)) {

                totalMinutes = parseTimeValue(heading);
                continue;
            }

            if (heading.toLowerCase().startsWith("serves")) {

                String value = heading.substring("serves".length()).trim();
                servings = Integer.valueOf(value);
                continue;

            }

            if (heading.equalsIgnoreCase("ingredients")) {

                readingIngredients = true;
                readingMethod = false;
                continue;

            }

            if (heading.equalsIgnoreCase("method")
                    || line.equalsIgnoreCase("instructions")) {

                readingIngredients = false;
                readingMethod = true;
                continue;

            }

            if (readingIngredients) {

                ingredients.add(parseIngredient(line));
                continue;

            }

            if (readingMethod) {

                steps.add(
                        ImportedStep.builder()
                                .stepNumber(steps.size() + 1)
                                .instruction(line)
                                .build()
                );

            }

        }

        ImportedRecipe importedRecipe = ImportedRecipe.builder()
                .name(name)
                .servings(servings)
                .prepMinutes(prepMinutes)
                .cookMinutes(cookMinutes)
                .passiveMinutes(passiveMinutes)
                .totalMinutes(totalMinutes)
                .ingredients(ingredients)
                .steps(steps)
                .build();

        return toDraftResponse(importedRecipe);

    }
//
//    public RecipeDraftResponse createDraftFromUrl(String url) {
//
//        ImportedRecipe recipe = importRecipeFromUrl(url);
//
//        return toDraftResponse(recipe);
//
//    }
//
//    public RecipeDraftResponse createDraftFromText(String text) {
//
//        ImportedRecipe recipe = importRecipeFromText(text);
//
//        return toDraftResponse(recipe);
//
//    }

    private ImportedIngredient parseIngredient(String line) {

        String[] parts = line.split("\\s+", 4);

        BigDecimal quantity = null;
        Unit unit = null;
        String ingredientText;

        // Example:
        // 1 ¼ cups flour
        if (parts.length >= 4
                && isQuantity(parts[0] + " " + parts[1])
                && isUnit(parts[2])) {

            quantity = parseQuantity(parts[0] + " " + parts[1]);
            unit = parseUnit(parts[2]);

            ingredientText = line
                    .substring(parts[0].length()
                            + 1 + parts[1].length()
                            + 1 + parts[2].length()).trim();

        // Examples:
        // 500 g chicken
        // 2 tbsp butter (melted)
        } else if (parts.length >= 3
                && isQuantity(parts[0])
                && isUnit(parts[1])) {

            quantity = parseQuantity(parts[0]);
            unit = parseUnit(parts[1]);

            ingredientText = line.substring(parts[0].length()).trim();

            ingredientText = ingredientText.substring(parts[1].length()).trim();

        // Example:
        // 1 onion
        } else if (parts.length >= 2
                && isQuantity(parts[0])) {

            quantity = parseQuantity(parts[0]);
            ingredientText = line.substring(parts[0].length()).trim();

        } else {

            ingredientText = line;

        }

        ParsedIngredientText parsed = parseIngredientText(ingredientText);

        return ImportedIngredient.builder()
                .quantity(quantity)
                .unit(unit)
                .name(parsed.name())
                .preparation(parsed.preparation())
                .optional(parsed.optional())
                .build();

    }

    private boolean isQuantity(String value) {

        value = value.trim();

        // Decimal or whole number
        if (value.matches("\\d+(\\.\\d+)?")) {

            return true;

        }

        // Simple ASCII fraction: 1/2
        if (value.matches("\\d+/\\d+")) {

            return true;

        }

        // Mixed ASCII fraction: 1 1/2
        if (value.matches("\\d+\\s+\\d+/\\d+")) {

            return true;

        }

        // Unicode fraction: ½, ¾, ⅔, etc.
        if (value.length() == 1
                && UNICODE_FRACTIONS.containsKey(value.charAt(0))) {

            return true;

        }

        // Attached Unicode mixed fraction: 1½, 2¾, etc.
        if (value.matches("\\d+[½⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞]")) {

            return true;

        }

        // Spaced Unicode mixed fraction: 1 ½, 2 ¾, etc.
        return value.matches("\\d+\\s+[½⅓⅔¼¾⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞]");

    }

        private boolean isUnit(String value) {

            try {

                parseUnit(value);
                return true;

            } catch (IllegalArgumentException e) {

                return false;

            }

        }

    private BigDecimal parseQuantity(String value) {

        value = value.trim();

        for (Map.Entry<Character, String> entry : UNICODE_FRACTIONS.entrySet()) {

            if (value.indexOf(entry.getKey()) >= 0) {

                String fraction = entry.getValue();

                String wholePart = value
                        .replace(String.valueOf(entry.getKey()), "")
                        .trim();

                BigDecimal fractionValue = parseFraction(fraction);

                if (wholePart.isEmpty()) {

                    return fractionValue;

                }

                return new BigDecimal(wholePart)
                        .add(fractionValue);

            }

        }

        if (value.matches("\\d+\\s+\\d+/\\d+")) {

            String[] parts = value.split("\\s+");

            return new BigDecimal(parts[0])
                    .add(parseFraction(parts[1]));

        }

        // Simple fraction: 1/2
        if (value.matches("\\d+/\\d+")) {

            return parseFraction(value);

        }

        return new BigDecimal(value);

    }

    private BigDecimal parseFraction(String value) {

        String[] parts = value.split("/");

        BigDecimal numerator = new BigDecimal(parts[0]);
        BigDecimal denominator = new BigDecimal(parts[1]);

        return numerator.divide(
                denominator,
                10,
                RoundingMode.HALF_UP
        );

    }

    private Unit parseUnit(String value) {

        return switch (value.toLowerCase()) {

            case "g", "gram", "grams" -> Unit.G;
            case "kg", "kilogram", "kilograms", "kilo", "kilos" -> Unit.KG;
            case "ml", "millilitre", "millilitres", "milliliter", "milliliters" -> Unit.ML;
            case "l", "litre", "litres", "liter", "liters" -> Unit.L;
            case "tsp", "ts", "teaspoon", "teaspoons" -> Unit.TSP;
            case "tbsp", "tbs", "tbl", "tablespoon", "tablespoons" -> Unit.TBSP;
            case "cup", "cups", "c" -> Unit.CUP;
            case "oz", "ounce", "ounces" -> Unit.OZ;
            case "lb", "lbs", "pound", "pounds" -> Unit.LB;
            case "clove", "cloves" -> Unit.CLOVE;
            default -> throw new IllegalArgumentException("Unknown recipe unit: " + value);

        };

    }

    private ParsedIngredientText parseIngredientText(String ingredientText) {

        String trimmed = ingredientText.trim();

        // Parenthesised optional
        if (endsWithIgnoreCase(trimmed, "(optional)")) {

            String name = trimmed.substring(0, trimmed.length() - "(optional)".length()).trim();

            return new ParsedIngredientText(
                    name,
                    null,
                    true
            );
        }

        // Comma optional
        if (endsWithIgnoreCase(trimmed, ", optional")) {

            String name = trimmed.substring(0, trimmed.length() - ", optional".length()).trim();

            return new ParsedIngredientText(
                    name,
                    null,
                    true
            );
        }

        // Optional without punctuation
        if (endsWithIgnoreCase(trimmed, " optional")) {

            String name = trimmed.substring(0, trimmed.length() - " optional".length()).trim();

            return new ParsedIngredientText(
                    name,
                    null,
                    true
            );
        }

        int commaIndex = trimmed.indexOf(',');

        if (commaIndex >= 0) {

            String name = trimmed.substring(0, commaIndex).trim();
            String preparation = trimmed.substring(commaIndex + 1).trim();

            return new ParsedIngredientText(
                    name,
                    preparation.isEmpty() ? null : preparation,
                    false
            );
        }

        int openParenthesis = trimmed.indexOf('(');
        int closeParenthesis = trimmed.lastIndexOf(')');

        if (openParenthesis >= 0
                && closeParenthesis > openParenthesis) {

            String name = trimmed.substring(0, openParenthesis).trim();
            String preparation = trimmed
                    .substring(openParenthesis + 1, closeParenthesis).trim();

            return new ParsedIngredientText(
                    name,
                    preparation.isEmpty() ? null : preparation,
                    false
            );

        }

        if (trimmed.regionMatches(
                true,
                trimmed.length() - " to taste".length(),
                " to taste",
                0,
                " to taste".length())) {

            String name = trimmed
                    .substring(0, trimmed.length() - " to taste".length()).trim();

            return new ParsedIngredientText(
                    name,
                    "to taste",
                    false
            );

        }

        return new ParsedIngredientText(
                trimmed,
                null,
                false
        );


    }

    private boolean isPrepTimeHeading(String line) {

        return line.matches("(?i)^prep(?:aration)?(?:\\s+time)?\\s*:?.*\\d+\\s*(?:hours?|hrs?\\.?|minutes?|mins?\\.?)\\s*$"
        );

    }

    private boolean isCookTimeHeading(String line) {

        return line.matches(
                "(?i)^cook(?:ing)?(?:\\s+time)?\\s*:?.*\\d+\\s*(?:hours?|hrs?\\.?|minutes?|mins?\\.?)\\s*$");

    }

    private boolean isPassiveTimeHeading(String line) {

        return line.matches(
                "(?i)^(?:passive|rest(?:ing)?|marinat(?:e|ing)|chill(?:ing)?|cool(?:ing)?|" +
                "proof(?:ing)?|ris(?:e|ing)|soak(?:ing)?)\\s*(time)?\\s*:?.*\\d+\\s*(?:hours?|hrs?\\.?|minutes?|mins?\\.?)\\s*$"
        );

    }

    private boolean isTotalTimeHeading(String line) {

        return line.matches(
                "(?i)^total(?:\\s+time)?\\s*:?.*\\d+\\s*(?:hours?|hrs?\\.?|minutes?|mins?\\.?)\\s*$");

    }

    private Integer parseTime(String value) {

        Matcher matcher = Pattern.compile(
                "(?:(\\d+)\\s*(?:hours?|hrs?\\.?)\\s*)?(?:(\\d+)\\s*(?:minutes?|mins?\\.?)\\s*)?",
                Pattern.CASE_INSENSITIVE
        ).matcher(value.trim());

        if (!matcher.matches()) {

            return null;

        }

        int hours = matcher.group(1) != null
                ? Integer.parseInt(matcher.group(1))
                : 0;

        int minutes = matcher.group(2) != null
                ? Integer.parseInt(matcher.group(2))
                : 0;

        // Don't accept a string containing no time value
        if (hours == 0 && minutes == 0) {

            return null;

        }

        return hours * 60 + minutes;

    }

    private Integer parseTimeValue(String line) {

        int colonIndex = line.indexOf(':');

        if (colonIndex >= 0) {

            return parseTime(line.substring(colonIndex + 1).trim());

        }

        Matcher matcher = Pattern.compile(
                "(?i)^(?:prep(?:aration)?|cook|passive|rest(?:ing)?|marinat(?:e|ing)|" +
                        "chill(?:ing)?|cool(?:ing)?|proof(?:ing)?|ris(?:e|ing)|soak(?:ing)?|" +
                        "total)(?:\\s+time)?\\s+(.+)$"
        ).matcher(line);

        if (matcher.matches()) {

            return parseTime(matcher.group(1).trim());

        }

        return null;

    }

    private boolean endsWithIgnoreCase(String value, String suffix) {

        return value.length() >= suffix.length()
                && value.regionMatches(
                    true,
                    value.length() - suffix.length(),
                    suffix,
                    0,
                    suffix.length()
        );
    }

    private static final Map<Character, String> UNICODE_FRACTIONS = Map.ofEntries(
            Map.entry('½', "1/2"),
            Map.entry('⅓', "1/3"),
            Map.entry('⅔', "2/3"),
            Map.entry('¼', "1/4"),
            Map.entry('¾', "3/4"),
            Map.entry('⅕', "1/5"),
            Map.entry('⅖', "2/5"),
            Map.entry('⅗', "3/5"),
            Map.entry('⅘', "4/5"),
            Map.entry('⅙', "1/6"),
            Map.entry('⅚', "5/6"),
            Map.entry('⅛', "1/8"),
            Map.entry('⅜', "3/8"),
            Map.entry('⅝', "5/8"),
            Map.entry('⅞', "7/8")
    );

    private record ParsedIngredientText(
            String name,
            String preparation,
            boolean optional
    ) {

    }

    private RecipeDraftResponse toDraftResponse(ImportedRecipe recipe) {

        List<ImportedIngredientResponse> ingredientResponses =
                recipe.getIngredients().stream()
                        .map(ingredient -> ImportedIngredientResponse.builder()
                                .ingredientName(ingredient.getName())
                                .quantity(ingredient.getQuantity())
                                .unit(ingredient.getUnit())
                                .preparation(ingredient.getPreparation())
                                .optional(ingredient.getOptional())
                                .build())
                        .toList();

        List<RecipeStepResponse> stepResponses =
                recipe.getSteps().stream()
                        .map(step -> RecipeStepResponse.builder()
                                .stepNumber(step.getStepNumber())
                                .instruction(step.getInstruction())
                                .build())
                        .toList();

        return RecipeDraftResponse.builder()
                .name(recipe.getName())
                .prepMinutes(recipe.getPrepMinutes())
                .cookMinutes(recipe.getCookMinutes())
                .activeMinutes(recipe.getActiveMinutes())
                .passiveMinutes(recipe.getPassiveMinutes())
                .totalMinutes(recipe.getTotalMinutes())
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty())
                .sourceUrl(recipe.getSourceUrl())
                .imageUrl(recipe.getImageUrl())
                .ingredients(ingredientResponses)
                .steps(stepResponses)
                .build();

    }

    private ImportedRecipe convertStructuredRecipe(ImportedRecipe recipe, String sourceUrl) {

        Integer prepMinutes = recipe.getPrepMinutes();
        Integer cookMinutes = recipe.getCookMinutes();
        Integer activeMinutes = recipe.getActiveMinutes();
        Integer passiveMinutes = recipe.getPassiveMinutes();
        Integer totalMinutes = recipe.getTotalMinutes();

        if (activeMinutes == null
                && (prepMinutes != null
                || cookMinutes != null)) {

            activeMinutes = (prepMinutes == null ? 0 : prepMinutes)
                    + (cookMinutes == null ? 0 : cookMinutes);
        }

        if (passiveMinutes == null) {

            passiveMinutes = 0;

        }

        if (totalMinutes == null) {

            totalMinutes = (activeMinutes == null ? 0 : activeMinutes)
                    + passiveMinutes;

        }

        return ImportedRecipe.builder()
                .name(recipe.getName())
                .prepMinutes(prepMinutes)
                .cookMinutes(cookMinutes)
                .activeMinutes(activeMinutes)
                .passiveMinutes(passiveMinutes)
                .totalMinutes(totalMinutes)
                .servings(recipe.getServings())
                .difficulty(recipe.getDifficulty())
                .imageUrl(recipe.getImageUrl())
                .sourceUrl(sourceUrl)
                .ingredients(recipe.getIngredients())
                .steps(recipe.getSteps())
                .build();

    }

    private ImportedIngredientResponse toIngredientResponse(
            ImportedIngredient ingredient) {

        return ImportedIngredientResponse.builder()
                .ingredientName(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .unit(ingredient.getUnit())
                .preparation(ingredient.getPreparation())
                .optional(ingredient.getOptional())
                .build();

    }

}
