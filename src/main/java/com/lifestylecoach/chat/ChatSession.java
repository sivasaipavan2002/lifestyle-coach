package com.lifestylecoach.chat;

import com.lifestylecoach.ai.AIContextBuilder;
import com.lifestylecoach.ai.FoodAIService;
import com.lifestylecoach.ai.GeminiAIService;
import com.lifestylecoach.model.FoodEntry;
import com.lifestylecoach.model.UserProfile;
import com.lifestylecoach.model.food.PortionType;
import com.lifestylecoach.nutrition.NutritionResult;
import com.lifestylecoach.nutrition.USDAFoodAnalysisService;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A single ongoing conversation with one user.
 *
 * Every message the user types is routed to either the food-logging
 * pipeline (Gemini parsing + USDA lookup) or a general conversational
 * reply, depending on what the message is about.
 */
public class ChatSession {

    private static final Set<String> EXIT_WORDS = Set.of("exit", "quit", "bye", "goodbye");

    private static final int MAX_HISTORY_TURNS = 10;

    private final UserProfile profile;
    private final GeminiAIService geminiAIService;
    private final FoodAIService foodAIService;
    private final USDAFoodAnalysisService usdaFoodAnalysisService;

    private final String profileContext;
    private final Deque<String> conversationHistory = new ArrayDeque<>();
    private final List<NutritionResult> loggedNutrition = new ArrayList<>();
    private List<FoodEntry> pendingFoodEntries = new ArrayList<>();

    public ChatSession(
            UserProfile profile,
            GeminiAIService geminiAIService,
            AIContextBuilder contextBuilder,
            FoodAIService foodAIService,
            USDAFoodAnalysisService usdaFoodAnalysisService) {

        if (profile == null) {
            throw new IllegalArgumentException("User profile cannot be null.");
        }

        this.profile = profile;
        this.geminiAIService = geminiAIService;
        this.foodAIService = foodAIService;
        this.usdaFoodAnalysisService = usdaFoodAnalysisService;
        this.profileContext = contextBuilder.buildUserProfileContext(profile);
    }

    public void start(Scanner scanner) {

        System.out.println();
        System.out.println("==========================================");
        System.out.println("        LET'S CHAT, " + profile.getName().toUpperCase());
        System.out.println("==========================================");
        System.out.println();
        System.out.println("Ask me anything, or tell me what you ate.");
        System.out.println("Type 'exit' when you're done.");

        while (true) {

            System.out.println();
            System.out.print("You: ");

            String message = scanner.nextLine();

            if (message == null || message.isBlank()) {
                continue;
            }

            message = message.trim();

            if (EXIT_WORDS.contains(message.toLowerCase(Locale.ROOT))) {
                System.out.println();
                System.out.println("Coach: Take care, " + profile.getName() + "! See you next time.");
                break;
            }

            try {

                String response = processMessage(message);

                System.out.println();
                System.out.println("Coach: " + response);

            } catch (Exception exception) {

                System.out.println();
                System.out.println("Coach: Sorry, something went wrong handling that: "
                        + exception.getMessage());
            }
        }
    }

    /**
     * Processes one user message and returns the coach's reply,
     * without printing anything -- used by both the console loop
     * and the web API.
     *
     * Tries to parse the message as food first (FoodAIService is
     * instructed to never invent a food item), and falls back to a
     * general conversational reply when nothing food-related was
     * found. This avoids a separate classification call to Gemini.
     */
    public String processMessage(String message) {

        String descriptionForParsing = message;

        if (!pendingFoodEntries.isEmpty()) {

            descriptionForParsing = "Earlier, mentioned without an exact gram amount: "
                    + describeEntries(pendingFoodEntries)
                    + ". Now the user adds: " + message;
        }

        List<FoodEntry> foodEntries = foodAIService.analyzeFood(descriptionForParsing);

        if (foodEntries.isEmpty()) {
            return handleChatMessage(message);
        }

        List<NutritionResult> nutritionResults = usdaFoodAnalysisService.analyzeFoodEntries(foodEntries);

        List<FoodEntry> stillPending = foodEntries.stream()
                .filter(entry -> !(entry.getQuantity() > 0 && entry.getPortionType() == PortionType.GRAM))
                .collect(Collectors.toList());

        pendingFoodEntries = stillPending;

        loggedNutrition.addAll(nutritionResults);

        String factsBlock = buildFoodFactsBlock(foodEntries, nutritionResults, stillPending);

        String prompt = """
                You are an AI Personal Lifestyle Coach chatting
                naturally with the user, like a messaging app --
                confident and decisive, the way a real human coach
                would be, not a form asking for missing fields.

                The user just talked about food. Below are the facts
                that were already computed for you -- do not print
                them as a raw report, weave them into a natural reply.
                Items marked "estimated typical portion" were sized
                using standard reference weights because the user
                didn't give exact grams; treat that as good enough
                and move on, you do not need the exact gram figure.

                %s

                NUTRITION LOGGED SO FAR TODAY
                ==============================
                %s

                RECENT CONVERSATION
                ==============================
                %s

                FOOD FACTS FOR THIS MESSAGE
                ==============================
                %s

                Now reply to the user:
                - Briefly confirm what you understood, in plain words.
                - Share the nutrition you were able to calculate,
                  using ONLY the numbers that literally appear in
                  "FOOD FACTS FOR THIS MESSAGE" / "NUTRITION LOGGED
                  SO FAR TODAY" above -- never state a calorie,
                  protein, carb, fat, or fiber number that isn't
                  written there. If that section says no nutrition
                  could be calculated yet, say so plainly (e.g. "I
                  can't total that up yet") instead of guessing a
                  figure -- you can still be decisive about what to
                  eat next even when you don't have exact macros.
                - If any figures used an estimated portion, mention
                  that in one short phrase (e.g. "assuming a medium
                  egg, ~50g each") -- do not turn it into a question.
                - Based on the profile, goals, and what's been logged
                  today, give a clear, specific recommendation: what
                  to eat, how much of it, or what to do next. Commit
                  to an answer instead of listing options and asking
                  the user to choose.
                - Only ask a question back if something is genuinely
                  ambiguous (e.g. you cannot tell what food was even
                  meant) -- never just to confirm a typical estimate.
                - Keep it short and conversational, not a report.
                - Reasonable portion-size assumptions and practical
                  suggestions are expected of you, but never invent
                  foods that weren't mentioned or exact nutrition
                  numbers that weren't actually computed above.

                USER MESSAGE
                ==============================
                %s
                """.formatted(
                profileContext,
                buildNutritionSummary(),
                String.join("\n", conversationHistory),
                factsBlock,
                message);

        String response = geminiAIService.ask(prompt);

        conversationHistory.addLast("User: " + message);
        conversationHistory.addLast("Coach: " + response);
        recordAndTrim();

        return response;
    }

    private String describeEntries(List<FoodEntry> entries) {

        return entries.stream()
                .map(FoodEntry::getFoodName)
                .collect(Collectors.joining(", "));
    }

    private String buildFoodFactsBlock(
            List<FoodEntry> foodEntries,
            List<NutritionResult> nutritionResults,
            List<FoodEntry> stillPending) {

        StringBuilder facts = new StringBuilder();

        facts.append("Understood items:\n");

        for (FoodEntry entry : foodEntries) {

            facts.append("- ")
                    .append(entry.getFoodName())
                    .append(" (meal: ")
                    .append(entry.getMealType())
                    .append(", quantity: ")
                    .append(entry.getQuantity() > 0 ? entry.getQuantity() : "unspecified")
                    .append(" ")
                    .append(entry.getPortionType())
                    .append(")\n");
        }

        if (nutritionResults.isEmpty()) {

            facts.append("\nNo nutrition could be calculated yet "
                    + "(no item had an exact gram amount).\n");

        } else {

            facts.append("\nCalculated nutrition:\n");

            Set<String> estimatedFoodNames = foodEntries.stream()
                    .filter(FoodEntry::isEstimated)
                    .map(entry -> entry.getFoodName().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());

            for (NutritionResult result : nutritionResults) {

                boolean isEstimated = estimatedFoodNames.contains(
                        result.getFoodName().toLowerCase(Locale.ROOT));

                facts.append(String.format(Locale.ROOT,
                        "- %s (%.0f %s%s): %.0f kcal, %.1fg protein, %.1fg carbs, %.1fg fat, %.1fg fiber\n",
                        result.getFoodName(),
                        result.getQuantity(),
                        result.getUnit(),
                        isEstimated ? ", estimated typical portion" : ", exact amount given",
                        result.getCalories(),
                        result.getProtein(),
                        result.getCarbohydrates(),
                        result.getFat(),
                        result.getFiber()));
            }
        }

        if (!stillPending.isEmpty()) {

            facts.append("\nItems still needing an exact gram amount: ")
                    .append(describeEntries(stillPending))
                    .append("\n");
        }

        return facts.toString();
    }

    private String handleChatMessage(String message) {

        String prompt = """
                You are an AI Personal Lifestyle Coach having an
                ongoing conversation with this user -- talk like a
                real, decisive human coach, not a form collecting
                missing fields.

                Use the profile and food logged so far as context.
                Do not invent facts about the user or their food that
                weren't provided, but when they ask what to do, give
                a clear, specific, actionable suggestion (what to eat,
                how much, what workout to do) instead of just asking
                clarifying questions back. Make a reasonable, clearly
                stated assumption when something minor is missing,
                rather than blocking your answer on it. Keep the reply
                conversational, concise, and practical.

                This app does not track exercise or calories burned --
                only the food nutrition totals shown below are ever
                actually computed. If the user mentions a workout or
                activity (e.g. a walk), you may acknowledge it and
                encourage it, but do not state a specific calorie-burn
                number or add it to "today's totals" as if it were
                logged -- that would be a fabricated, not a real,
                figure. Likewise, never state an exact calorie/protein/
                carb/fat/fiber number unless it is the one given in
                "NUTRITION LOGGED SO FAR TODAY" below.

                %s

                NUTRITION LOGGED SO FAR TODAY
                ==============================
                %s

                RECENT CONVERSATION
                ==============================
                %s

                USER MESSAGE
                ==============================
                %s
                """.formatted(
                profileContext,
                buildNutritionSummary(),
                String.join("\n", conversationHistory),
                message);

        String response = geminiAIService.ask(prompt);

        conversationHistory.addLast("User: " + message);
        conversationHistory.addLast("Coach: " + response);
        recordAndTrim();

        return response;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public NutritionTotals getNutritionTotals() {

        return new NutritionTotals(
                usdaFoodAnalysisService.calculateTotalCalories(loggedNutrition),
                usdaFoodAnalysisService.calculateTotalProtein(loggedNutrition),
                usdaFoodAnalysisService.calculateTotalCarbohydrates(loggedNutrition),
                usdaFoodAnalysisService.calculateTotalFat(loggedNutrition),
                usdaFoodAnalysisService.calculateTotalFiber(loggedNutrition));
    }

    private String buildNutritionSummary() {

        if (loggedNutrition.isEmpty()) {
            return "No food logged yet in this session.";
        }

        return String.format(Locale.ROOT,
                "Calories: %.2f kcal, Protein: %.2f g, Carbohydrates: %.2f g, Fat: %.2f g, Fiber: %.2f g",
                usdaFoodAnalysisService.calculateTotalCalories(loggedNutrition),
                usdaFoodAnalysisService.calculateTotalProtein(loggedNutrition),
                usdaFoodAnalysisService.calculateTotalCarbohydrates(loggedNutrition),
                usdaFoodAnalysisService.calculateTotalFat(loggedNutrition),
                usdaFoodAnalysisService.calculateTotalFiber(loggedNutrition));
    }

    private void recordAndTrim() {

        while (conversationHistory.size() > MAX_HISTORY_TURNS) {
            conversationHistory.removeFirst();
        }
    }
}
