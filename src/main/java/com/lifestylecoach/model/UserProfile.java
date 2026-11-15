package com.lifestylecoach.model;

public class UserProfile {

    private String name;
    private int age;
    private String gender;

    private double heightCm;
    private double weightKg;

    private WorkProfile workProfile;
    private NutritionProfile nutritionProfile;
    private FitnessProfile fitnessProfile;
    private HealthGoal healthGoal;

    public UserProfile(
            String name,
            int age,
            String gender,
            double heightCm,
            double weightKg,
            WorkProfile workProfile,
            NutritionProfile nutritionProfile,
            FitnessProfile fitnessProfile,
            HealthGoal healthGoal) {

        this.name = name;
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.workProfile = workProfile;
        this.nutritionProfile = nutritionProfile;
        this.fitnessProfile = fitnessProfile;
        this.healthGoal = healthGoal;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public WorkProfile getWorkProfile() {
        return workProfile;
    }

    public NutritionProfile getNutritionProfile() {
        return nutritionProfile;
    }

    public FitnessProfile getFitnessProfile() {
        return fitnessProfile;
    }

    public HealthGoal getHealthGoal() {
        return healthGoal;
    }
}