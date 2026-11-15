package com.lifestylecoach.nutrition;

public interface NutritionDataProvider {

    NutritionResult findNutrition(
            String foodName,
            double quantity,
            String unit);
}