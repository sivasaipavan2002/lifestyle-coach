package com.lifestylecoach.model;

import com.lifestylecoach.model.enums.ActivityLevel;
import com.lifestylecoach.model.enums.ShiftType;

public class WorkProfile {

    private String workType;
    private int workHours;
    private ShiftType shift;
    private ActivityLevel activityLevel;

    public WorkProfile(
            String workType,
            int workHours,
            ShiftType shift,
            ActivityLevel activityLevel) {

        this.workType = workType;
        this.workHours = workHours;
        this.shift = shift;
        this.activityLevel = activityLevel;
    }

    public String getWorkType() {
        return workType;
    }

    public int getWorkHours() {
        return workHours;
    }

    public ShiftType getShift() {
        return shift;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }
}