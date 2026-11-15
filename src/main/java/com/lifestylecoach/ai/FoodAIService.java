package com.lifestylecoach.ai;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.lifestylecoach.model.FoodEntry;
import com.lifestylecoach.model.food.FoodInputMethod;
import com.lifestylecoach.model.food.MealType;
import com.lifestylecoach.model.food.PortionType;

import java.util.ArrayList;
import java.util.List;

public class FoodAIService {

    private final GeminiAIService geminiAIService;
    private final Gson gson;

    public FoodAIService(GeminiAIService geminiAIService) {
        this.geminiAIService = geminiAIService;
        this.gson = new Gson();
    }

    public List<FoodEntry> analyzeFood(String foodDescription) {

        if (foodDescription == null ||
                foodDescription.isBlank()) {

            throw new IllegalArgumentException(
                    "Food description cannot be empty.");
        }

        String prompt = """
                You are the food-understanding component of an
                AI Personal Lifestyle Coach.

                Understand the user's food description.

                The user may write:
                - English
                - Telugu
                - English + Telugu
                - Informal English
                - Misspelled words
                - Short phrases

                Do not require a fixed format.

                Identify every food item that the user actually
                says they ate.

                For every food item determine:

                meal:
                BREAKFAST
                LUNCH
                SNACK
                DINNER
                OTHER

                foodName:
                The food name in clear English.

                quantity:
                If the user gives an exact amount, use it. For a
                food that is normally weighed rather than counted
                (chicken, rice, paneer, fish, vegetables, etc.), a
                bare number with no other unit (e.g. "150 chicken",
                "200 rice") means grams -- use that number directly
                as quantity with portionType GRAM and estimated
                false, do not treat it as a count of pieces.

                If the user does NOT give an exact amount, but the
                food is a common, well-known item (egg, chapati,
                banana, onion, carrot, a cup of rice, a bowl of dal,
                etc.), estimate its typical real-world weight in
                GRAMS using standard reference portion sizes (for
                example: 1 medium egg ~50g, 1 medium chapati ~35g,
                1 medium onion ~110g, 1 medium carrot ~60g). Convert
                the stated count into that estimated gram total,
                set portionType to GRAM, and set estimated to true.

                Only fall back to quantity 0 / portionType OTHER /
                estimated false when the food itself is too vague or
                unusual to estimate a reasonable weight for (e.g.
                "some snacks", an unfamiliar dish with no typical
                serving size).

                portionType:
                BOWL
                CUP
                PLATE
                GLASS
                PIECE
                SPOON
                HANDFUL
                SERVING
                GRAM
                MILLILITER
                OTHER

                estimated:
                true if you had to estimate the gram weight
                yourself (the user didn't give an exact amount).
                false if the user gave an exact quantity, or if
                quantity is 0 because it couldn't be estimated.

                preparation:
                Include preparation if mentioned.
                Examples:
                boiled
                fried
                grilled
                curry
                biryani

                If preparation is not known:
                use "Not specified".

                IMPORTANT RULES:

                1. Never invent a food that wasn't mentioned.
                2. Never assume a food was eaten if it was
                   only mentioned.
                3. If a word is unclear, keep it as an
                   uncertain food item rather than guessing.
                4. When estimating a weight, use realistic,
                   typical portion sizes -- do not exaggerate or
                   lowball them.
                5. Do not calculate calories.
                6. Do not calculate protein.
                7. Do not give a diet plan.
                8. Return ONLY valid JSON.
                9. Do not use Markdown.
                10. Do not put ``` around the JSON.

                Return exactly this structure:

                {
                  "foods": [
                    {
                      "meal": "BREAKFAST",
                      "foodName": "Eggs",
                      "quantity": 150,
                      "portionType": "GRAM",
                      "preparation": "Not specified",
                      "estimated": true
                    }
                  ],
                  "confidence": "HIGH",
                  "notes": "..."
                }

                USER FOOD DESCRIPTION:
                %s
                """.formatted(foodDescription);

        String jsonResponse = geminiAIService.ask(prompt);

        return convertJsonToFoodEntries(
                jsonResponse,
                foodDescription);
    }

    private List<FoodEntry> convertJsonToFoodEntries(
            String jsonResponse,
            String originalDescription) {

        String cleanedJson = cleanJsonResponse(jsonResponse);

        try {

            FoodAnalysisResult result = gson.fromJson(
                    cleanedJson,
                    FoodAnalysisResult.class);

            if (result == null ||
                    result.getFoods() == null) {

                throw new IllegalArgumentException(
                        "Gemini returned no food items.");
            }

            List<FoodEntry> foodEntries = new ArrayList<>();

            for (FoodAnalysisResult.FoodItem item : result.getFoods()) {

                FoodEntry entry = convertToFoodEntry(
                        item,
                        originalDescription);

                foodEntries.add(entry);
            }

            return foodEntries;

        } catch (JsonSyntaxException exception) {

            throw new IllegalArgumentException(
                    "Gemini returned invalid food data."
                            + "\nResponse:\n"
                            + jsonResponse,
                    exception);
        }
    }

    private FoodEntry convertToFoodEntry(
            FoodAnalysisResult.FoodItem item,
            String originalDescription) {

        MealType mealType = parseMealType(item.getMeal());

        PortionType portionType = parsePortionType(item.getPortionType());

        FoodInputMethod inputMethod = FoodInputMethod.NATURAL_LANGUAGE;

        return new FoodEntry(
                item.getFoodName(),
                item.getQuantity(),
                portionType,
                mealType,
                inputMethod,
                originalDescription,
                item.isEstimated());
    }

    private MealType parseMealType(String value) {

        if (value == null) {
            return MealType.OTHER;
        }

        try {

            return MealType.valueOf(
                    value.trim().toUpperCase());

        } catch (IllegalArgumentException exception) {

            return MealType.OTHER;
        }
    }

    private PortionType parsePortionType(String value) {

        if (value == null) {
            return PortionType.OTHER;
        }

        try {

            return PortionType.valueOf(
                    value.trim().toUpperCase());

        } catch (IllegalArgumentException exception) {

            return PortionType.OTHER;
        }
    }

    private String cleanJsonResponse(String response) {

        if (response == null ||
                response.isBlank()) {

            throw new IllegalArgumentException(
                    "Gemini returned an empty response.");
        }

        String cleaned = response.trim();

        if (cleaned.startsWith("```")) {

            cleaned = cleaned
                    .replaceFirst("^```json\\s*", "")
                    .replaceFirst("^```\\s*", "");

            int endingIndex = cleaned.lastIndexOf("```");

            if (endingIndex >= 0) {

                cleaned = cleaned.substring(
                        0,
                        endingIndex);
            }
        }

        return cleaned.trim();
    }
}