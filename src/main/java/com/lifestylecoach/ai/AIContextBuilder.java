package com.lifestylecoach.ai;

import com.lifestylecoach.model.FitnessProfile;
import com.lifestylecoach.model.HealthGoal;
import com.lifestylecoach.model.NutritionProfile;
import com.lifestylecoach.model.UserProfile;
import com.lifestylecoach.model.WorkProfile;

public class AIContextBuilder {

    public String buildUserProfileContext(UserProfile user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User profile cannot be null.");
        }

        StringBuilder context = new StringBuilder();

        // ==========================================
        // PERSONAL INFORMATION
        // ==========================================

        context.append("USER PROFILE\n");
        context.append("==============================\n");

        context.append("Name: ")
                .append(user.getName())
                .append("\n");

        context.append("Age: ")
                .append(user.getAge())
                .append("\n");

        context.append("Gender: ")
                .append(user.getGender())
                .append("\n");

        context.append("Height: ")
                .append(user.getHeightCm())
                .append(" cm\n");

        context.append("Weight: ")
                .append(user.getWeightKg())
                .append(" kg\n");

        // ==========================================
        // WORK & LIFESTYLE
        // ==========================================

        context.append("\n");
        context.append("WORK & LIFESTYLE\n");
        context.append("==============================\n");

        WorkProfile workProfile = user.getWorkProfile();

        if (workProfile != null) {

            context.append("Work Type: ")
                    .append(workProfile.getWorkType())
                    .append("\n");

            context.append("Working Hours: ")
                    .append(workProfile.getWorkHours())
                    .append(" hours/day\n");

            context.append("Shift: ")
                    .append(workProfile.getShift())
                    .append("\n");

            context.append("Activity Level: ")
                    .append(workProfile.getActivityLevel())
                    .append("\n");

        } else {

            context.append("Work information: Not provided\n");
        }

        // ==========================================
        // NUTRITION PROFILE
        // ==========================================

        context.append("\n");
        context.append("NUTRITION PROFILE\n");
        context.append("==============================\n");

        NutritionProfile nutritionProfile = user.getNutritionProfile();

        if (nutritionProfile != null) {

            context.append("Food Preference: ")
                    .append(nutritionProfile.getFoodPreference())
                    .append("\n");

            context.append("Food Allergies: ")
                    .append(nutritionProfile.getAllergies())
                    .append("\n");

            context.append("Foods Disliked: ")
                    .append(nutritionProfile.getDislikes())
                    .append("\n");

            context.append("Meals Per Day: ")
                    .append(nutritionProfile.getMealsPerDay())
                    .append("\n");

            context.append("Cooking Available: ")
                    .append(
                            nutritionProfile.isCookingAvailable()
                                    ? "Yes"
                                    : "No")
                    .append("\n");

        } else {

            context.append(
                    "Nutrition information: Not provided\n");
        }

        // ==========================================
        // FITNESS PROFILE
        // ==========================================

        context.append("\n");
        context.append("FITNESS PROFILE\n");
        context.append("==============================\n");

        FitnessProfile fitnessProfile = user.getFitnessProfile();

        if (fitnessProfile != null) {

            context.append("Fitness Level: ")
                    .append(fitnessProfile.getFitnessLevel())
                    .append("\n");

            context.append("Exercise Time: ")
                    .append(fitnessProfile.getExerciseMinutes())
                    .append(" minutes/day\n");

            context.append("Exercise Location: ")
                    .append(fitnessProfile.getExerciseLocation())
                    .append("\n");

            context.append("Available Equipment: ")
                    .append(fitnessProfile.getEquipment())
                    .append("\n");

        } else {

            context.append(
                    "Fitness information: Not provided\n");
        }

        // ==========================================
        // HEALTH GOAL
        // ==========================================

        context.append("\n");
        context.append("HEALTH GOAL\n");
        context.append("==============================\n");

        HealthGoal healthGoal = user.getHealthGoal();

        if (healthGoal != null) {

            context.append("Primary Goal: ")
                    .append(healthGoal.getPrimaryGoal())
                    .append("\n");

            context.append("Target: ")
                    .append(healthGoal.getTarget())
                    .append("\n");

        } else {

            context.append(
                    "Health goal: Not provided\n");
        }

        return context.toString();
    }

    public String buildProfileAnalysisPrompt(UserProfile user) {

        String userContext = buildUserProfileContext(user);

        return """
                You are an AI Personal Lifestyle Coach.

                Analyze the following user's profile.

                USER PROFILE
                ----------------
                %s

                Provide a practical personalized analysis covering:

                1. Overall lifestyle
                2. Important nutrition considerations
                3. Exercise considerations
                4. Lifestyle improvements
                5. The most important priority

                Do not invent information that is not present.

                Keep the guidance practical and easy to understand.

                If the information appears unusual or potentially
                incorrect, point it out rather than assuming it is correct.
                """.formatted(userContext);
    }
}