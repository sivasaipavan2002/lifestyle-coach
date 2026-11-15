package com.lifestylecoach.nutrition;

import com.lifestylecoach.model.FoodEntry;
import com.lifestylecoach.model.food.PortionType;

import java.util.ArrayList;
import java.util.List;

public class USDAFoodAnalysisService {

    private final NutritionDataProvider dataProvider;

    public USDAFoodAnalysisService(
            NutritionDataProvider dataProvider) {

        if (dataProvider == null) {
            throw new IllegalArgumentException(
                    "Nutrition data provider cannot be null.");
        }

        this.dataProvider = dataProvider;
    }

    /**
     * Analyze a list of FoodEntry objects using USDA.
     *
     * Foods with gram quantities are sent directly to USDA. By the
     * time entries reach this service, FoodAIService has already
     * converted common portions (PIECE, BOWL, PLATE, etc.) into an
     * estimated gram weight when the user didn't give an exact
     * amount -- entries still missing a gram quantity are the ones
     * that were too vague to estimate.
     */
    public List<NutritionResult> analyzeFoodEntries(
            List<FoodEntry> foodEntries) {

        if (foodEntries == null || foodEntries.isEmpty()) {
            return List.of();
        }

        List<NutritionResult> results = new ArrayList<>();

        for (FoodEntry foodEntry : foodEntries) {

            if (foodEntry == null) {
                continue;
            }

            if (!canSendToUSDA(foodEntry)) {
                continue;
            }

            NutritionResult result = dataProvider.findNutrition(
                    foodEntry.getFoodName(),
                    foodEntry.getQuantity(),
                    "grams");

            results.add(result);
        }

        return results;
    }

    /**
     * Checks whether the FoodEntry currently contains
     * a quantity that can safely be sent to USDA.
     */
    private boolean canSendToUSDA(
            FoodEntry foodEntry) {

        if (foodEntry.getQuantity() <= 0) {
            return false;
        }

        PortionType portionType = foodEntry.getPortionType();

        return portionType == PortionType.GRAM;
    }

    /**
     * Calculate total calories from USDA results.
     */
    public double calculateTotalCalories(
            List<NutritionResult> results) {

        double total = 0.0;

        if (results == null) {
            return total;
        }

        for (NutritionResult result : results) {

            if (result != null) {
                total += result.getCalories();
            }
        }

        return total;
    }

    /**
     * Calculate total protein.
     */
    public double calculateTotalProtein(
            List<NutritionResult> results) {

        double total = 0.0;

        if (results == null) {
            return total;
        }

        for (NutritionResult result : results) {

            if (result != null) {
                total += result.getProtein();
            }
        }

        return total;
    }

    /**
     * Calculate total carbohydrates.
     */
    public double calculateTotalCarbohydrates(
            List<NutritionResult> results) {

        double total = 0.0;

        if (results == null) {
            return total;
        }

        for (NutritionResult result : results) {

            if (result != null) {
                total += result.getCarbohydrates();
            }
        }

        return total;
    }

    /**
     * Calculate total fat.
     */
    public double calculateTotalFat(
            List<NutritionResult> results) {

        double total = 0.0;

        if (results == null) {
            return total;
        }

        for (NutritionResult result : results) {

            if (result != null) {
                total += result.getFat();
            }
        }

        return total;
    }

    /**
     * Calculate total fiber.
     */
    public double calculateTotalFiber(
            List<NutritionResult> results) {

        double total = 0.0;

        if (results == null) {
            return total;
        }

        for (NutritionResult result : results) {

            if (result != null) {
                total += result.getFiber();
            }
        }

        return total;
    }

}