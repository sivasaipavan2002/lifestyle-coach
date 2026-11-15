package com.lifestylecoach.model;

import com.lifestylecoach.model.food.FoodInputMethod;
import com.lifestylecoach.model.food.MealType;
import com.lifestylecoach.model.food.PortionType;

public class FoodEntry {

    private final String foodName;

    private final double quantity;

    private final PortionType portionType;

    private final MealType mealType;

    private final FoodInputMethod inputMethod;

    private final String originalDescription;

    private final boolean estimated;

    public FoodEntry(
            String foodName,
            double quantity,
            PortionType portionType,
            MealType mealType,
            FoodInputMethod inputMethod,
            String originalDescription) {

        this(foodName, quantity, portionType, mealType, inputMethod, originalDescription, false);
    }

    public FoodEntry(
            String foodName,
            double quantity,
            PortionType portionType,
            MealType mealType,
            FoodInputMethod inputMethod,
            String originalDescription,
            boolean estimated) {

        this.foodName = foodName;
        this.quantity = quantity;
        this.portionType = portionType;
        this.mealType = mealType;
        this.inputMethod = inputMethod;
        this.originalDescription = originalDescription;
        this.estimated = estimated;
    }

    public String getFoodName() {
        return foodName;
    }

    public double getQuantity() {
        return quantity;
    }

    public PortionType getPortionType() {
        return portionType;
    }

    public MealType getMealType() {
        return mealType;
    }

    public FoodInputMethod getInputMethod() {
        return inputMethod;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public boolean isEstimated() {
        return estimated;
    }
}