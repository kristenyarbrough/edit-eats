package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.domain.Unit;
import io.github.kristenyarbrough.edit_eats.dto.imported.*;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
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
                    .servings(parseServings(recipe.path("recipeYield")
                            .asText(null)))
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

    private Integer parseServings(String value) {

        if (value == null || value.isBlank()) {

            return null;

        }

        Matcher matcher = Pattern.compile("\\d+").matcher(value);

        if (!matcher.find()) {

            return null;

        }

        return Integer.parseInt(matcher.group());

    }

    private String parseImage(JsonNode node) {

        if (node.isTextual()) {

            return node.asText();

        }

        if (node.isArray() && !node.isEmpty()) {

            JsonNode first = node.get(0);

            if (first.isTextual()) {

                return first.asText();

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

                ingredients.add(parseIngredientText(ingredient.asText()));

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

        JsonNode items = node.path("itemListElement");

        if (items.isArray()) {

            for (JsonNode item : items) {

                if (item.isTextual()) {

                    ingredients.add(parseIngredientText(item.asText()));

                } else if (item.isObject()) {

                    ingredients.add(parseStructuredIngredient(item));

                }

            }

        }

        return ImportedIngredientSection.builder()
                .name(node.path("name").asText(null))
                .ingredients(ingredients)
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

    private ImportedIngredient parseIngredientText(String value) {

        if (value == null || value.isBlank()) {

            return ImportedIngredient.builder()
                    .name(value)
                    .build();

        }

        // Quantity + unit + ingredient name
        Matcher matcher = Pattern.compile(
                "^\\s*(\\d+(?:\\.\\d+)?)\\s*"
                            + "(g|kg|ml|l|tsp|teaspoon|teaspoons|tbsp|tablespoon|tablespoons"
                            + "|cup|cups|oz|ounce|ounces|lb|lbs|pound|pounds"
                            + "|each|clove|cloves)"
                            +"\\s+(.+?)\\s*$",
                Pattern.CASE_INSENSITIVE
        ).matcher(value);

        if (matcher.matches()) {

            BigDecimal quantity = new BigDecimal(matcher.group(1));
            Unit unit = parseUnit(matcher.group(2));
            String name = matcher.group(3).trim();

            return ImportedIngredient.builder()
                    .name(name)
                    .quantity(quantity)
                    .unit(unit)
                    .build();

        }

        // Quantity + ingredient name, with no unit
        matcher = Pattern.compile(
                "^\\s*(\\d+(?:\\.\\d+)?)\\s+(.+?)\\s*$"
        ).matcher(value);

        if (matcher.matches()) {

            BigDecimal quantity = new BigDecimal(matcher.group(1));
            String name = matcher.group(2).trim();

            return ImportedIngredient.builder()
                    .name(name)
                    .quantity(quantity)
                    .build();

        }

        return ImportedIngredient.builder()
                .name(value.trim())
                .build();

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

        if (instruction != null) {

            steps.add(ImportedStep.builder()
                    .stepNumber(steps.size() + 1)
                    .instruction(instruction)
                    .build()
            );

        }

        if (node.isTextual()) {

            steps.add(
                    ImportedStep.builder()
                            .stepNumber(steps.size() + 1)
                            .instruction(node.asText())
                            .build()
            );

        }

    }

    private BigDecimal parseQuantity(String value) {

        if (value == null || value.isBlank()) {

            return null;

        }

        try {

            return new BigDecimal(value);

        } catch (NumberFormatException e) {

            return null;

        }

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

}