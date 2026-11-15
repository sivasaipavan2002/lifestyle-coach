package com.lifestylecoach.ai;

import java.util.List;

public class FoodAnalysisResult {

    private List<FoodItem> foods;
    private String confidence;
    private String notes;

    public List<FoodItem> getFoods() {
        return foods;
    }

    public String getConfidence() {
        return confidence;
    }

    public String getNotes() {
        return notes;
    }

    public static class FoodItem {

        private String meal;
        private String foodName;
        private double quantity;
        private String portionType;
        private String preparation;
        private boolean estimated;

        public String getMeal() {
            return meal;
        }

        public String getFoodName() {
            return foodName;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getPortionType() {
            return portionType;
        }

        public String getPreparation() {
            return preparation;
        }

        public boolean isEstimated() {
            return estimated;
        }
    }
}