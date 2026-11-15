package com.lifestylecoach.web.dto;

import com.lifestylecoach.chat.NutritionTotals;

public class ChatResponse {

    private final String reply;
    private final NutritionTotals nutrition;

    public ChatResponse(String reply, NutritionTotals nutrition) {
        this.reply = reply;
        this.nutrition = nutrition;
    }

    public String getReply() {
        return reply;
    }

    public NutritionTotals getNutrition() {
        return nutrition;
    }
}
