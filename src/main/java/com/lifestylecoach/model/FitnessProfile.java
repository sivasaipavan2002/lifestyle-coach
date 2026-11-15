package com.lifestylecoach.model;

import com.lifestylecoach.model.enums.ExerciseLocation;
import com.lifestylecoach.model.enums.FitnessLevel;

public class FitnessProfile {

    private FitnessLevel fitnessLevel;
    private int exerciseMinutes;
    private ExerciseLocation exerciseLocation;
    private String equipment;

    public FitnessProfile(
            FitnessLevel fitnessLevel,
            int exerciseMinutes,
            ExerciseLocation exerciseLocation,
            String equipment) {

        this.fitnessLevel = fitnessLevel;
        this.exerciseMinutes = exerciseMinutes;
        this.exerciseLocation = exerciseLocation;
        this.equipment = equipment;
    }

    public FitnessLevel getFitnessLevel() {
        return fitnessLevel;
    }

    public int getExerciseMinutes() {
        return exerciseMinutes;
    }

    public ExerciseLocation getExerciseLocation() {
        return exerciseLocation;
    }

    public String getEquipment() {
        return equipment;
    }
}