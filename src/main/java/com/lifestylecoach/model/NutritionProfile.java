package com.lifestylecoach.model;

import com.lifestylecoach.model.enums.FoodPreference;

public class NutritionProfile {

    private FoodPreference foodPreference;
    private String allergies;
    private String dislikes;
    private int mealsPerDay;
    private boolean cookingAvailable;

    public NutritionProfile(
            FoodPreference foodPreference,
            String allergies,
            String dislikes,
            int mealsPerDay,
            boolean cookingAvailable) {

        this.foodPreference = foodPreference;
        this.allergies = allergies;
        this.dislikes = dislikes;
        this.mealsPerDay = mealsPerDay;
        this.cookingAvailable = cookingAvailable;
    }

    public FoodPreference getFoodPreference() {
        return foodPreference;
    }

    public String getAllergies() {
        return allergies;
    }

    public String getDislikes() {
        return dislikes;
    }

    public int getMealsPerDay() {
        return mealsPerDay;
    }

    public boolean isCookingAvailable() {
        return cookingAvailable;
    }
}