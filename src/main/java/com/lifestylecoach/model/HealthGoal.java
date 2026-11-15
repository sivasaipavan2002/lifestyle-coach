package com.lifestylecoach.model;

import com.lifestylecoach.model.enums.GoalType;

public class HealthGoal {

    private GoalType primaryGoal;
    private String target;

    public HealthGoal(GoalType primaryGoal, String target) {
        this.primaryGoal = primaryGoal;
        this.target = target;
    }

    public GoalType getPrimaryGoal() {
        return primaryGoal;
    }

    public String getTarget() {
        return target;
    }
}