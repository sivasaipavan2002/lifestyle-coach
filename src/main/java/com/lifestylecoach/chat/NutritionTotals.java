package com.lifestylecoach.chat;

/**
 * Cumulative macros logged so far in a chat session.
 */
public class NutritionTotals {

    private final double calories;
    private final double protein;
    private final double carbohydrates;
    private final double fat;
    private final double fiber;

    public NutritionTotals(
            double calories,
            double protein,
            double carbohydrates,
            double fat,
            double fiber) {

        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.fiber = fiber;
    }

    public double getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public double getFat() {
        return fat;
    }

    public double getFiber() {
        return fiber;
    }
}
