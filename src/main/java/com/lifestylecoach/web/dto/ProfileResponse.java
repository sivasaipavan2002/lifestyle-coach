package com.lifestylecoach.web.dto;

import com.lifestylecoach.model.UserProfile;

public class ProfileResponse {

    private final UserProfile profile;
    private final boolean created;
    private final String analysis;

    public ProfileResponse(UserProfile profile, boolean created, String analysis) {
        this.profile = profile;
        this.created = created;
        this.analysis = analysis;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public boolean isCreated() {
        return created;
    }

    public String getAnalysis() {
        return analysis;
    }
}
