package io.github.kristenyarbrough.edit_eats.service;

import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedIngredient;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedRecipe;
import io.github.kristenyarbrough.edit_eats.dto.imported.ImportedStep;
import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

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

            System.out.println("RECIPE NODE = " + recipe);
            System.out.println("PREP TIME = [" + recipe.path("prepTime").asText(null) + "]");
            System.out.println("COOK TIME = [" + recipe.path("cookTime").asText(null) + "]");


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
                    .steps(parseSteps(recipe.path("recipeInstructions")))
                    .build();

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Unable to parse recipe structured data",
                    e
            );

        }

    }

    private Integer parseDuration(String value) {

        System.out.println("parseDuration INPUT = [" + value + "]");

        if (value == null || value.isBlank()) {

            return null;

        }

        Matcher matcher = Pattern.compile(
                "^PT(?:(\\d+)H)?(?:(\\d+)M)?$",
                Pattern.CASE_INSENSITIVE
        ).matcher(value);

        boolean matches = matcher.matches();

        System.out.println("parseDuration MATCHES = " + matches);

        if (!matches) {
            return null;
        }

        int hours = matcher.group(1) == null
                ? 0
                : Integer.parseInt(matcher.group(1));

        int minutes = matcher.group(2) == null
                ? 0
                : Integer.parseInt(matcher.group(2));

        System.out.println("hours = " + hours + ", minutes = " + minutes);

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

                ingredients.add(
                        ImportedIngredient.builder()
                                .name(ingredient.asText())
                                .build()
                );

            } else if (ingredient.isObject()) {

                String name = ingredient.path("name").asText(null);

                if (name != null) {

                    ingredients.add(
                            ImportedIngredient.builder()
                                    .name(name)
                                    .build()
                    );

                }

            }

        }

        return ingredients;

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

            if (isHowToSection(step)) {

                JsonNode sectionSteps = step.path("itemListElement");

                if (sectionSteps.isArray()) {

                    for (JsonNode sectionStep: sectionSteps) {

                        addStep(steps, sectionStep);

                    }

                }
            } else {

                addStep(steps, step);

            }

        }

        return steps;

    }

//    private void parseInstructionNode(JsonNode node, List<ImportedStep> steps) {
//
//        if (node.isTextual()) {
//
//            steps.add(
//                    ImportedStep.builder()
//                            .stepNumber(steps.size() + 1)
//                            .instruction(node.asText())
//                            .build()
//            );
//
//            return;
//
//        }
//
//        if (!node.isObject()) {
//
//            return;
//
//        }
//
//        String type = node.path("@type").asText();
//
//        if ("HowToSection".equalsIgnoreCase(type)) {
//
//            parseInstructionNode(
//                    node.path("itemListElement"),
//                    steps
//            );
//
//            return;
//
//        }
//
//        JsonNode text = node.get("text");
//
//        if (text != null && text.isTextual()) {
//
//            steps.add(
//                    ImportedStep.builder()
//                            .stepNumber(steps.size() + 1)
//                            .instruction(text.asText())
//                            .build()
//            );
//        }
//
//    }

    private void addStep(List<ImportedStep> steps, JsonNode node) {

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

}