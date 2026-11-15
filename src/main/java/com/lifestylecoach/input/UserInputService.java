package com.lifestylecoach.input;

import java.util.Scanner;
import java.util.logging.Logger;

import com.lifestylecoach.model.FitnessProfile;
import com.lifestylecoach.model.HealthGoal;
import com.lifestylecoach.model.NutritionProfile;
import com.lifestylecoach.model.UserProfile;
import com.lifestylecoach.model.WorkProfile;
import com.lifestylecoach.model.enums.ActivityLevel;
import com.lifestylecoach.model.enums.ExerciseLocation;
import com.lifestylecoach.model.enums.FitnessLevel;
import com.lifestylecoach.model.enums.FoodPreference;
import com.lifestylecoach.model.enums.GoalType;
import com.lifestylecoach.model.enums.ShiftType;

public class UserInputService {

        private final Scanner scanner;
        private static final Logger LOGGER = Logger.getLogger(UserInputService.class.getName());

        public UserInputService(Scanner scanner) {
                this.scanner = scanner;
        }

        /**
         * Collects the rest of the profile when the name has
         * already been obtained (e.g. used to look up a saved profile).
         */
        public UserProfile collectUserProfile(String name) {

                // ==============================
                // PERSONAL INFORMATION
                // ==============================

                int age = readInt("Age: ");

                String gender = readString("Gender: ");

                double heightCm = readDouble("Height (cm): ");

                double weightKg = readDouble("Weight (kg): ");

                // ==============================
                // WORK & LIFESTYLE
                // ==============================

                LOGGER.info("");
                LOGGER.info("--- WORK & LIFESTYLE ---");

                String workType = readString(
                                "Work type (Desk / Field / Driver / Manual / Student / Other): ");

                int workHours = readInt(
                                "Working hours per day: ");

                ShiftType shift = readEnum(
                                "Shift (Day / Night / Rotating): ",
                                ShiftType.class);

                ActivityLevel activityLevel = readEnum(
                                "Daily activity level (Low / Moderate / High): ",
                                ActivityLevel.class);

                WorkProfile workProfile = new WorkProfile(
                                workType,
                                workHours,
                                shift,
                                activityLevel);

                // ==============================
                // NUTRITION
                // ==============================

                LOGGER.info("");
                LOGGER.info("--- NUTRITION ---");

                FoodPreference foodPreference = readEnum(
                                "Food preference (Vegetarian / Non-Vegetarian / Vegan / Eggetarian): ",
                                FoodPreference.class);

                String allergies = readString(
                                "Food allergies (enter None if none): ");

                String dislikes = readString(
                                "Foods you dislike (enter None if none): ");

                int mealsPerDay = readInt(
                                "Typical meals per day: ");

                boolean cookingAvailable = readBoolean(
                                "Do you have access to cooking? (Yes / No): ");

                NutritionProfile nutritionProfile = new NutritionProfile(
                                foodPreference,
                                allergies,
                                dislikes,
                                mealsPerDay,
                                cookingAvailable);

                // ==============================
                // FITNESS
                // ==============================

                LOGGER.info("");
                LOGGER.info("--- FITNESS ---");

                FitnessLevel fitnessLevel = readEnum(
                                "Fitness level (Beginner / Intermediate / Advanced): ",
                                FitnessLevel.class);

                int exerciseMinutes = readInt(
                                "Exercise time available per day (minutes): ");

                ExerciseLocation exerciseLocation = readEnum(
                                "Exercise location (Home / Gym / Outdoor): ",
                                ExerciseLocation.class);

                String equipment = readString(
                                "Available equipment (None / Basic / Full Gym): ");

                FitnessProfile fitnessProfile = new FitnessProfile(
                                fitnessLevel,
                                exerciseMinutes,
                                exerciseLocation,
                                equipment);

                // ==============================
                // GOAL
                // ==============================

                LOGGER.info("");
                LOGGER.info("--- GOAL ---");

                GoalType primaryGoal = readEnum(
                                "Main goal (Weight Management / Strength / Fitness / Healthy Eating): ",
                                GoalType.class);

                String target = readString(
                                "Describe your target: ");

                HealthGoal healthGoal = new HealthGoal(
                                primaryGoal,
                                target);

                // ==============================
                // CREATE USER PROFILE
                // ==============================

                return new UserProfile(name, age, gender, heightCm, weightKg, workProfile, nutritionProfile, fitnessProfile, healthGoal);
        }

        // ==============================
        // STRING INPUT
        // ==============================

        private String readString(String message) {

                LOGGER.info(message);

                return scanner.nextLine().trim();
        }

        // ==============================
        // INTEGER INPUT
        // ==============================

        private int readInt(String message) {

                while (true) {

                        LOGGER.info(message);

                        String input = scanner.nextLine().trim();

                        try {

                                return Integer.parseInt(input);

                        } catch (NumberFormatException exception) {

                                LOGGER.warning("Please enter a valid number.");
                        }
                }
        }

        // ==============================
        // DECIMAL INPUT
        // ==============================

        private double readDouble(String message) {

                while (true) {

                        LOGGER.info(message);

                        String input = scanner.nextLine().trim();

                        try {

                                return Double.parseDouble(input);

                        } catch (NumberFormatException exception) {

                                LOGGER.warning("Please enter a valid number.");
                        }
                }
        }

        // ==============================
        // YES / NO INPUT
        // ==============================

        private boolean readBoolean(String message) {

                while (true) {

                        LOGGER.info(message);

                        String input = scanner.nextLine().trim().toLowerCase();

                        if (input.equals("yes")
                                        || input.equals("y")) {

                                return true;
                        }

                        if (input.equals("no")
                                        || input.equals("n")) {

                                return false;
                        }

                        LOGGER.warning("Please enter Yes or No.");
                }
        }

        // ==============================
        // ENUM INPUT
        // ==============================

        private <T extends Enum<T>> T readEnum(
                        String message,
                        Class<T> enumType) {

                while (true) {

                        LOGGER.info(message);

                        String input = scanner.nextLine().trim().replace(" ", "_").replace("-", "_").toUpperCase();

                        try {

                                return Enum.valueOf(
                                                enumType,
                                                input);

                        } catch (IllegalArgumentException exception) {

                                LOGGER.warning("Invalid option. Please enter one of the available options.");
                        }
                }
        }
}