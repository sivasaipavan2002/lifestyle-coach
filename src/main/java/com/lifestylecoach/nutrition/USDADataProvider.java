package com.lifestylecoach.nutrition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class USDADataProvider implements NutritionDataProvider {

        private static final String BASE_URL = "https://api.nal.usda.gov/fdc/v1";

        private final String apiKey;
        private final HttpClient httpClient;

        public USDADataProvider(String apiKey) {

                if (apiKey == null || apiKey.isBlank()) {
                        throw new IllegalArgumentException(
                                        "USDA_API_KEY is missing.");
                }

                this.apiKey = apiKey;

                this.httpClient = HttpClient.newBuilder()
                                .connectTimeout(Duration.ofSeconds(15))
                                .build();
        }

        @Override
        public NutritionResult findNutrition(
                        String foodName,
                        double quantity,
                        String unit) {

                if (foodName == null || foodName.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Food name cannot be empty.");
                }

                if (quantity <= 0) {
                        throw new IllegalArgumentException(
                                        "Quantity must be greater than zero.");
                }

                if (unit == null || unit.isBlank()) {
                        throw new IllegalArgumentException(
                                        "Unit cannot be empty.");
                }

                try {

                        System.out.println();
                        System.out.println(
                                        "Searching USDA for: " + foodName);

                        int fdcId = searchFood(foodName);

                        if (fdcId <= 0) {

                                throw new RuntimeException(
                                                "USDA could not find a suitable food for: "
                                                                + foodName);
                        }

                        System.out.println(
                                        "USDA FDC ID found: " + fdcId);

                        return getNutrition(
                                        fdcId,
                                        foodName,
                                        quantity,
                                        unit);

                } catch (InterruptedException exception) {

                        Thread.currentThread().interrupt();

                        throw new RuntimeException(
                                        "USDA request was interrupted.",
                                        exception);

                } catch (IOException exception) {

                        throw new RuntimeException(
                                        "Could not connect to USDA FoodData Central: "
                                                        + exception.getMessage(),
                                        exception);
                }
        }

        /**
         * Search USDA FoodData Central and select
         * the most appropriate food result.
         */
        private int searchFood(
                        String foodName)
                        throws IOException, InterruptedException {

                String searchQuery = buildSearchQuery(foodName);

                String encodedFood = URLEncoder.encode(
                                searchQuery,
                                StandardCharsets.UTF_8);

                String url = BASE_URL
                                + "/foods/search"
                                + "?api_key="
                                + apiKey
                                + "&query="
                                + encodedFood
                                + "&pageSize=10";

                System.out.println(
                                "USDA search query: "
                                                + searchQuery);

                System.out.println(
                                "Calling USDA search API...");

                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .timeout(
                                                Duration.ofSeconds(30))
                                .GET()
                                .build();

                HttpResponse<String> response = httpClient.send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                System.out.println(
                                "USDA search HTTP status: "
                                                + response.statusCode());

                checkResponse(response);

                JsonObject root = JsonParser
                                .parseString(response.body())
                                .getAsJsonObject();

                if (!root.has("foods")) {

                        System.out.println(
                                        "USDA response does not contain foods.");

                        return -1;
                }

                JsonArray foods = root.getAsJsonArray("foods");

                if (foods == null || foods.isEmpty()) {

                        System.out.println(
                                        "USDA returned no matching foods.");

                        return -1;
                }

                return selectBestFood(
                                foods,
                                foodName);
        }

        /**
         * Build a better USDA search query.
         *
         * Plain "rice" is treated as cooked rice because
         * this application assumes that when a user simply
         * says they ate rice, they normally mean cooked rice.
         */
        private String buildSearchQuery(
                        String foodName) {

                String normalized = foodName.trim().toLowerCase();

                if (normalized.equals("rice")
                                || normalized.equals("white rice")
                                || normalized.equals("brown rice")) {

                        return normalized + " cooked";
                }

                if (normalized.contains("raw")
                                || normalized.contains("uncooked")) {

                        return normalized;
                }

                return foodName;
        }

        /**
         * Select the best USDA result instead of blindly
         * selecting the first result.
         */
        private int selectBestFood(
                        JsonArray foods,
                        String originalFoodName) {

                String normalizedFood = originalFoodName
                                .trim()
                                .toLowerCase();

                int bestFdcId = -1;
                int bestScore = Integer.MIN_VALUE;

                for (int i = 0; i < foods.size(); i++) {

                        JsonObject food = foods.get(i)
                                        .getAsJsonObject();

                        if (!food.has("fdcId")) {
                                continue;
                        }

                        String description = "";

                        if (food.has("description")
                                        && !food.get("description")
                                                        .isJsonNull()) {

                                description = food.get("description")
                                                .getAsString()
                                                .toLowerCase();
                        }

                        int score = calculateMatchScore(
                                        normalizedFood,
                                        description);

                        System.out.println(
                                        "USDA candidate: "
                                                        + description
                                                        + " | Score: "
                                                        + score);

                        if (score > bestScore) {

                                bestScore = score;

                                bestFdcId = food.get("fdcId")
                                                .getAsInt();
                        }
                }

                if (bestFdcId <= 0) {

                        System.out.println(
                                        "Could not select a suitable USDA result.");

                        return -1;
                }

                System.out.println(
                                "Selected USDA food with score: "
                                                + bestScore);

                return bestFdcId;
        }

        /**
         * Calculate a simple matching score.
         *
         * This is intentionally conservative. We want
         * USDA's actual food record, not an AI-generated
         * nutrition value.
         */
        private int calculateMatchScore(
                        String foodName,
                        String description) {

                int score = 0;

                if (description.contains(foodName)) {
                        score += 20;
                }

                /*
                 * Plain rice is treated as cooked rice.
                 */
                if (foodName.equals("rice")
                                || foodName.equals("white rice")
                                || foodName.equals("brown rice")) {

                        if (description.contains("cooked")) {
                                score += 50;
                        }

                        if (description.contains("boiled")) {
                                score += 20;
                        }

                        if (description.contains("raw")) {
                                score -= 50;
                        }

                        if (description.contains("uncooked")) {
                                score -= 50;
                        }
                }

                /*
                 * Avoid obviously irrelevant preparations
                 * when the user simply said rice.
                 */
                if (foodName.equals("rice")) {

                        if (description.contains("flour")) {
                                score -= 40;
                        }

                        if (description.contains("cereal")) {
                                score -= 20;
                        }

                        if (description.contains("cracker")) {
                                score -= 40;
                        }

                        if (description.contains("snack")) {
                                score -= 40;
                        }
                }

                return score;
        }

        private NutritionResult getNutrition(
                        int fdcId,
                        String foodName,
                        double quantity,
                        String unit)
                        throws IOException, InterruptedException {

                String url = BASE_URL
                                + "/food/"
                                + fdcId
                                + "?api_key="
                                + apiKey;

                System.out.println(
                                "Calling USDA food details API...");

                HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .timeout(
                                                Duration.ofSeconds(30))
                                .GET()
                                .build();

                HttpResponse<String> response = httpClient.send(
                                request,
                                HttpResponse.BodyHandlers.ofString());

                System.out.println(
                                "USDA food details HTTP status: "
                                                + response.statusCode());

                checkResponse(response);

                JsonObject food = JsonParser
                                .parseString(response.body())
                                .getAsJsonObject();

                double calories = getNutrient(
                                food,
                                "Energy");

                double protein = getNutrient(
                                food,
                                "Protein");

                double carbohydrates = getNutrient(
                                food,
                                "Carbohydrate, by difference");

                double fat = getNutrient(
                                food,
                                "Total lipid (fat)");

                double fiber = getNutrient(
                                food,
                                "Fiber, total dietary");

                /*
                 * USDA nutrient values used here are normally
                 * expressed per 100g.
                 */
                double grams = convertToGrams(
                                quantity,
                                unit);

                double multiplier = grams / 100.0;

                return new NutritionResult(
                                foodName,
                                quantity,
                                unit,
                                calories * multiplier,
                                protein * multiplier,
                                carbohydrates * multiplier,
                                fat * multiplier,
                                fiber * multiplier,
                                "USDA FoodData Central",
                                fdcId);
        }

        private double getNutrient(
                        JsonObject food,
                        String nutrientName) {

                if (!food.has("foodNutrients")) {
                        return 0.0;
                }

                JsonArray nutrients = food.getAsJsonArray(
                                "foodNutrients");

                for (int i = 0; i < nutrients.size(); i++) {

                        JsonObject nutrient = nutrients.get(i)
                                        .getAsJsonObject();

                        String name = null;

                        /*
                         * Some USDA responses contain:
                         *
                         * "nutrient": {
                         * "name": "Protein"
                         * }
                         */
                        if (nutrient.has("nutrient")
                                        && nutrient
                                                        .get("nutrient")
                                                        .isJsonObject()) {

                                JsonObject nutrientInfo = nutrient.getAsJsonObject(
                                                "nutrient");

                                if (nutrientInfo.has("name")) {

                                        name = nutrientInfo
                                                        .get("name")
                                                        .getAsString();
                                }
                        }

                        /*
                         * Other USDA responses expose
                         * nutrientName directly.
                         */
                        if (name == null
                                        && nutrient.has(
                                                        "nutrientName")) {

                                name = nutrient
                                                .get("nutrientName")
                                                .getAsString();
                        }

                        if (name == null) {
                                continue;
                        }

                        if (name.equalsIgnoreCase(
                                        nutrientName)) {

                                if (nutrient.has("amount")
                                                && !nutrient
                                                                .get("amount")
                                                                .isJsonNull()) {

                                        return nutrient
                                                        .get("amount")
                                                        .getAsDouble();
                                }
                        }
                }

                return 0.0;
        }

        private double convertToGrams(
                        double quantity,
                        String unit) {

                String normalized = unit.trim().toLowerCase();

                return switch (normalized) {

                        case "g",
                                        "gram",
                                        "grams" ->
                                quantity;

                        default ->
                                throw new IllegalArgumentException(
                                                "Currently USDA calculation "
                                                                + "requires a gram-based "
                                                                + "quantity. Received: "
                                                                + quantity
                                                                + " "
                                                                + unit);
                };
        }

        private void checkResponse(
                        HttpResponse<String> response) {

                int status = response.statusCode();

                if (status == 200) {
                        return;
                }

                if (status == 400) {

                        throw new RuntimeException(
                                        "USDA API rejected the request "
                                                        + "(HTTP 400).");
                }

                if (status == 403) {

                        throw new RuntimeException(
                                        "USDA API rejected the API key "
                                                        + "(HTTP 403). Check "
                                                        + "USDA_API_KEY in .env.");
                }

                if (status == 404) {

                        throw new RuntimeException(
                                        "USDA food was not found "
                                                        + "(HTTP 404).");
                }

                if (status == 429) {

                        throw new RuntimeException(
                                        "USDA API rate limit reached "
                                                        + "(HTTP 429).");
                }

                throw new RuntimeException(
                                "USDA API request failed. "
                                                + "HTTP status: "
                                                + status);
        }
}