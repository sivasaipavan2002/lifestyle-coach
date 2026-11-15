package com.lifestylecoach.nutrition;

public class NutritionResult {

    private final String foodName;
    private final double quantity;
    private final String unit;

    private final double calories;
    private final double protein;
    private final double carbohydrates;
    private final double fat;
    private final double fiber;

    private final String source;
    private final int fdcId;

    public NutritionResult(
            String foodName,
            double quantity,
            String unit,
            double calories,
            double protein,
            double carbohydrates,
            double fat,
            double fiber,
            String source,
            int fdcId) {

        this.foodName = foodName;
        this.quantity = quantity;
        this.unit = unit;
        this.calories = calories;
        this.protein = protein;
        this.carbohydrates = carbohydrates;
        this.fat = fat;
        this.fiber = fiber;
        this.source = source;
        this.fdcId = fdcId;
    }

    public String getFoodName() {
        return foodName;
    }

    public double getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
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

    public String getSource() {
        return source;
    }

    public int getFdcId() {
        return fdcId;
    }

    @Override
    public String toString() {

        return "NutritionResult{" +
                "foodName='" + foodName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", calories=" + calories +
                ", protein=" + protein +
                ", carbohydrates=" + carbohydrates +
                ", fat=" + fat +
                ", fiber=" + fiber +
                ", source='" + source + '\'' +
                ", fdcId=" + fdcId +
                '}';
    }
}