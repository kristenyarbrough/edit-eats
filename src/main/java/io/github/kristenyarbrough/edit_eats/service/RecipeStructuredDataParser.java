package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.imported.*;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RecipeStructuredDataParser {

    private final JsonMapper jsonMapper;

    public ImportedRecipe parse(String json) {

        try {

            JsonNode root = jsonMapper.readTree(json);

            JsonNode recipe = findRecipeNode(root);

            System.out.println("prepTime: " + recipe.path("prepTime"));
            System.out.println("cookTime: " + recipe.path("cookTime"));
            System.out.println("totalTime: " + recipe.path("totalTime"));
            System.out.println("Recipe structured data: " + recipe.toPrettyString());

            if (recipe == null) {

                throw new IllegalArgumentException(
                        "No Recipe object found in structured data"
                );

            }

            Integer prepMinutes =
                    parseDuration(recipe.path("prepTime").asText(null));

            Integer cookMinutes =
                    parseDuration(recipe.path("cookTime").asText(null));

            Integer totalMinutes =
                    parseDuration(recipe.path("totalTime").asText(null));

            if (prepMinutes == null
                    && totalMinutes != null
                    && cookMinutes != null
                    && totalMinutes >= cookMinutes) {

                prepMinutes = totalMinutes - cookMinutes;

            }

            if (totalMinutes == null
                    && prepMinutes != null
                    && cookMinutes != null) {

                totalMinutes = prepMinutes + cookMinutes;

            }

            return ImportedRecipe.builder()
                    .name(recipe.path("name").asText(null))
                    .prepMinutes(prepMinutes)
                    .cookMinutes(cookMinutes)
                    .totalMinutes(totalMinutes)
                    .servings(parseServings(recipe.path("recipeYield")))
                    .imageUrl(parseImage(recipe.path("image")))
                    .sourceUrl(recipe.path("url").asText(null))
                    .ingredients(parseIngredients(recipe.path("recipeIngredient")))
                    .ingredientSections(parseIngredientSections(recipe.path("recipeIngredient")))
                    .steps(parseSteps(recipe.path("recipeInstructions")))
                    .instructionSections(parseInstructionSections(recipe.path("recipeInstructions")))
                    .build();

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Unable to parse recipe structured data",
                    e
            );

        }

    }

    private Integer parseDuration(String value) {

        if (value == null || value.isBlank()) {

            return null;

        }

        Matcher matcher = Pattern.compile(
                "^PT(?:(\\d+)H)?(?:(\\d+)M)?$",
                Pattern.CASE_INSENSITIVE
        ).matcher(value);

        boolean matches = matcher.matches();

        if (!matches) {
            return null;
        }

        int hours = matcher.group(1) == null
                ? 0
                : Integer.parseInt(matcher.group(1));

        int minutes = matcher.group(2) == null
                ? 0
                : Integer.parseInt(matcher.group(2));

        return hours * 60 + minutes;

    }

    private Integer parseServings(JsonNode node) {

        if (node == null || node.isNull()) {

            return null;

        }

        if (node.isTextual()) {

            Matcher matcher = Pattern.compile("\\d+").matcher(node.asText());

            if (!matcher.find()) {

                return null;

            }

            return Integer.parseInt(matcher.group());

        }

        if (node.isArray() && !node.isEmpty()) {

            return parseServings(node.get(0));

        }

        return null;

    }

    private String parseImage(JsonNode node) {

        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isTextual()) {

            return node.asText();

        }

        if (node.isArray() && !node.isEmpty()) {

            return parseImage(node.get(0));

        }

        if (node.isObject()) {

            JsonNode urlNode = node.get("url");

            if (urlNode != null && urlNode.isTextual()) {

                return urlNode.asText();

            }

        }

        return null;

    }

    private List<ImportedIngredient> parseIngredients(JsonNode node) {

        List<ImportedIngredient> ingredients = new ArrayList<>();

        if (!node.isArray()) {

            return ingredients;

        }

        for (JsonNode ingredient : node) {

            if (ingredient.isTextual()) {

                ingredients.addAll(parseIngredientText(ingredient.asText()));

            } else if (ingredient.isObject()
                    && !isIngredientSection(ingredient)) {

                ingredients.add(parseStructuredIngredient(ingredient));

            }

        }

        return ingredients;

    }

    private List<ImportedIngredientSection> parseIngredientSections(JsonNode node) {

        List<ImportedIngredientSection> sections = new ArrayList<>();

        if (!node.isArray()) {

            return sections;

        }

        for (JsonNode item : node) {

            if (isIngredientSection(item)) {

                sections.add(parseIngredientSection(item));

            }

        }

        return sections;

    }

    private ImportedIngredientSection parseIngredientSection(JsonNode node) {

        List<ImportedIngredient> ingredients = new ArrayList<>();
        List<ImportedIngredientSection> sections = new ArrayList<>();

        JsonNode items = node.path("itemListElement");

        if (items.isArray()) {

            for (JsonNode item : items) {

                if (isIngredientSection(item)) {

                    sections.add(parseIngredientSection(item));

                } else if (item.isTextual()) {

                    ingredients.addAll(parseIngredientText(item.asText()));

                } else if (item.isObject()) {

                    ingredients.add(parseStructuredIngredient(item));

                }

            }

        }

        return ImportedIngredientSection.builder()
                .name(node.path("name").asText(null))
                .ingredients(ingredients)
                .sections(sections)
                .build();

    }

    private boolean isIngredientSection(JsonNode node) {

        if (!node.isObject()) {

            return false;

        }

        JsonNode type = node.get("@type");

        return type != null
                && type.isTextual()
                && "ItemList".equalsIgnoreCase(type.asText());

    }

    private ImportedIngredient parseStructuredIngredient(JsonNode node) {

        String name = node.path("name").asText(null);

        BigDecimal quantity = null;
        JsonNode value = node.get("value");

        if (value != null && !value.isNull()) {

            try {

                quantity = new BigDecimal(value.asText());

            } catch (NumberFormatException ignored) {

                // Leave quantity as null if the value isn't numeric
            }

        }

        Unit unit = parseUnit(node.path("unitCode").asText(null));

        return ImportedIngredient.builder()
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .build();

    }

    private List<ImportedIngredient> parseIngredientText(String value) {

        if (value == null || value.isBlank()) {

            return List.of(
                    ImportedIngredient.builder()
                            .name(value)
                            .build()
            );

        }

        // Quantity + unit + ingredient name
        Matcher matcher = Pattern.compile(
                "^\\s*(\\d+(?:\\.\\d+)?|\\d+/\\d+)\\s*"
                            + "(g|kg|ml|l|tsp|teaspoon|teaspoons|tbsp|tablespoon|tablespoons"
                            + "|cup|cups|oz|ounce|ounces|lb|lbs|pound|pounds"
                            + "|each|clove|cloves)"
                            +"\\s+(.+?)\\s*$",
                Pattern.CASE_INSENSITIVE
        ).matcher(value);

        if (matcher.matches()) {

            BigDecimal quantity = parseQuantity(matcher.group(1));
            Unit unit = parseUnit(matcher.group(2));
            String name = matcher.group(3).trim();

            ImportedIngredient ingredient = ImportedIngredient.builder()
                    .quantity(quantity)
                    .unit(unit)
                    .build();

            // Extract a parenthesised preparation or note.
            Matcher noteMatcher = Pattern.compile(
                    "^(.*?)\\s+\\(+(.*?)\\)+\\s*$"
            ).matcher(name);

            if (noteMatcher.matches()) {

                name = noteMatcher.group(1).trim();
                ingredient.setPreparation(noteMatcher.group(2).trim());

            }

            return List.of(separatePreparationAndNote(ingredient, name));

        }

        // Quantity + ingredient name, with no unit
        matcher = Pattern.compile(
                "^\\s*(\\d+(?:\\.\\d+)?)\\s+(.+?)\\s*$"
        ).matcher(value);

        if (matcher.matches()) {

            BigDecimal quantity = new BigDecimal(matcher.group(1));
            String name = matcher.group(2).trim();

            ImportedIngredient ingredient = ImportedIngredient.builder()
                    .quantity(quantity)
                    .build();

            return List.of(separatePreparationAndNote(ingredient, name));

        }

        ImportedIngredient ingredient = ImportedIngredient.builder()
                .build();

        String name = value.trim();

        // Extract a note enclosed in parentheses.
        Matcher noteMatcher = Pattern.compile(
                "^(.*?)\\s*\\(([^()]*)\\)\\s*$"
        ).matcher(name);

        if (noteMatcher.matches()) {

            name = noteMatcher.group(1).trim();
            ingredient.setNote(noteMatcher.group(2).trim());

        }

        // Split combined ingredients when they share "to serve".
        Matcher servingMatcher = Pattern.compile(
                "^(.+?)\\s+and\\s+(.+?)\\s+to serve$",
                Pattern.CASE_INSENSITIVE
        ).matcher(name);

        if (servingMatcher.matches()) {

            return List.of(
                ImportedIngredient.builder()
                        .name(servingMatcher.group(1).trim())
                        .note("to serve")
                        .build(),
                ImportedIngredient.builder()
                        .name(servingMatcher.group(2).trim())
                        .note("to serve")
                        .build()
            );

        }

        return List.of(separatePreparationAndNote(ingredient, name));

    }

    private List<ImportedStep> parseSteps(JsonNode node) {

        List<ImportedStep> steps = new ArrayList<>();

        if (node.isTextual()) {

            steps.add(
                    ImportedStep.builder()
                            .stepNumber(1)
                            .instruction(node.asText())
                            .build()
            );

            return steps;

        }

        if (!node.isArray()) {

            return steps;

        }

        for (JsonNode step : node) {

            addStep(steps, step);

        }

        return steps;

    }

    private void addStep(List<ImportedStep> steps, JsonNode node) {

        if (isHowToSection(node)) {

            JsonNode sectionSteps = node.path("itemListElement");

            if (sectionSteps.isArray()) {

                for (JsonNode sectionStep: sectionSteps) {

                    addStep(steps, sectionStep);

                }

            }

            return;

        }

        String instruction = null;

        if (node.isTextual()) {

            instruction = node.asText();

        } else if (node.isObject()) {

            JsonNode text = node.get("text");

            if (text != null && text.isTextual()) {

                instruction = text.asText();

            }

        }

        if (instruction != null && !instruction.isBlank()) {

            steps.add(ImportedStep.builder()
                    .stepNumber(steps.size() + 1)
                    .instruction(instruction)
                    .build()
            );

        }

    }

    private BigDecimal parseQuantity(String value) {

        if (value.contains("/")) {

            String[] parts = value.split("/");

            return new BigDecimal(parts[0]).divide(
                    new BigDecimal(parts[1]),
                    10,
                    RoundingMode.HALF_UP
            );

        }

        return new BigDecimal(value);

    }

    private Unit parseUnit(String value) {

        if (value == null || value.isBlank()) {

            return null;

        }

        return switch (value.trim().toLowerCase()) {

            case "g", "gram", "grams", "grm" -> Unit.G;
            case "kg", "kilogram", "kilograms" -> Unit.KG;

            case "ml", "millilitre", "millilitres", "milliliter", "milliliters" -> Unit.ML;
            case "l", "litre", "litres", "liter", "liters" -> Unit.L;

            case "tsp", "teaspoon", "teaspoons" -> Unit.TSP;
            case "tbsp", "tablespoon", "tablespoons" -> Unit.TBSP;
            case "cup", "cups" -> Unit.CUP;

            case "oz", "ounce", "ounces" -> Unit.OZ;
            case "lb", "lbs", "pound", "pounds" -> Unit.LB;

            case "each" -> Unit.EACH;
            case "clove", "cloves" -> Unit.CLOVE;

            default -> null;

        };

    }

    private boolean isHowToSection(JsonNode node) {

        if (!node.isObject()) {

            return false;

        }

        JsonNode type = node.get("@type");

        return type != null && type.isTextual() && "HowToSection".equalsIgnoreCase(type.asText());

    }

    private JsonNode findRecipeNode(JsonNode root) {

        if (isRecipe(root)) {

            return root;

        }

        JsonNode graph= root.path("@graph");

        if (graph.isArray()) {

            for (JsonNode node : graph) {

                if (isRecipe(node)) {

                    return node;

                }

            }

        }

        return null;

    }

    private boolean isRecipe(JsonNode node) {

        JsonNode type = node.get("@type");

        if (type == null) {

            return false;

        }

        if (type.isTextual()) {

            return "Recipe".equalsIgnoreCase(type.asText());

        }

        if (type.isArray()) {

            for (JsonNode value : type) {

                if (value.isTextual()
                        && "Recipe".equalsIgnoreCase(value.asText())) {

                    return true;

                }

            }

        }

        return false;

    }

    private List<ImportedInstructionSection> parseInstructionSections(JsonNode node) {

        List<ImportedInstructionSection> sections = new ArrayList<>();

        if (!node.isArray()) {

            return sections;

        }

        for (JsonNode item : node) {

            if (isHowToSection(item)) {

                sections.add(parseInstructionSection(item));

            }

        }

        return sections;

    }

    private ImportedInstructionSection parseInstructionSection(JsonNode node) {

        List<ImportedStep> steps = new ArrayList<>();
        List<ImportedInstructionSection> sections = new ArrayList<>();

        JsonNode items = node.path("itemListElement");

        if (items.isArray()) {

            for (JsonNode item : items) {

                if (isHowToSection(item)) {

                    sections.add(parseInstructionSection(item));

                } else if (isHowToStep(item)) {

                    JsonNode text = item.get("text");

                    if (text != null && text.isTextual()) {

                        steps.add(ImportedStep.builder()
                                .stepNumber(steps.size() + 1)
                                .instruction(text.asText())
                                .build()
                        );

                    }

                }

            }

        }

        return ImportedInstructionSection.builder()
                .name(node.path("name").asText(null))
                .steps(steps)
                .sections(sections)
                .build();

    }

    private boolean isHowToStep(JsonNode node) {

        JsonNode type = node.get("@type");

        return type != null
                && type.isTextual()
                && "HowToStep".equalsIgnoreCase(type.asText());

    }

    private BigDecimal parseIngredientQuantity(JsonNode node) {

        if (node == null || node.isMissingNode() || node.isNull()) {

            return null;

        }

        try {

            return new BigDecimal(node.asText());

        } catch (NumberFormatException e) {

            return null;

        }

    }

    private ImportedIngredient separatePreparationAndNote(
            ImportedIngredient ingredient,
            String name
    ) {

        String cleanedName = name.trim();

        Matcher leadingPreparationMatcher = Pattern.compile(
                "^(beaten)\\s+(.+?)\\s+(to glaze)$",
                Pattern.CASE_INSENSITIVE
        ).matcher(cleanedName);

        if (leadingPreparationMatcher.matches()) {

            ingredient.setName(leadingPreparationMatcher.group(2).trim());
            ingredient.setPreparation(leadingPreparationMatcher.group(1).trim());
            ingredient.setNote(leadingPreparationMatcher.group(3).trim());
            return ingredient;

        }

        // Extract an extra usage note, such as "plus more for dusting".
        Matcher noteMatcher = Pattern.compile(
                "^(.*?)\\s+(plus more .+)$",
                Pattern.CASE_INSENSITIVE
        ).matcher(cleanedName);

        if (noteMatcher.matches()) {

            cleanedName = noteMatcher.group(1).trim();
            ingredient.setNote(noteMatcher.group(2).trim());

        }

        // Extract a preparation phrase from the remaining ingredient name.
        Matcher preparationMatcher = Pattern.compile(
                "^(.*?)\\s+(squeeze|cut into cubes|finely chopped|roughly chopped"
                        + "|chopped|diced|minced|grated|sliced|crushed"
                        + "|beaten|softened|melted|peeled|deseeded"
                        + "|finely sliced|thinly sliced)\\s*$",
                Pattern.CASE_INSENSITIVE
        ).matcher(cleanedName);

        if (preparationMatcher.matches()) {

            ingredient.setName(preparationMatcher.group(1).trim());
            ingredient.setPreparation(preparationMatcher.group(2).trim());

        } else if (cleanedName.toLowerCase().startsWith("squeeze ")) {

            ingredient.setName(cleanedName.substring("squeeze ".length()).trim());
            ingredient.setPreparation("squeeze");

        } else {

            ingredient.setName(cleanedName);

        }

        return ingredient;

    }


}