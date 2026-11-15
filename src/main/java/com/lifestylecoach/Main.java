package com.lifestylecoach;

import java.util.Optional;
import java.util.Scanner;

import com.lifestylecoach.ai.AIContextBuilder;
import com.lifestylecoach.ai.FoodAIService;
import com.lifestylecoach.ai.GeminiAIService;
import com.lifestylecoach.chat.ChatSession;
import com.lifestylecoach.input.UserInputService;
import com.lifestylecoach.model.UserProfile;
import com.lifestylecoach.nutrition.NutritionDataProvider;
import com.lifestylecoach.nutrition.USDADataProvider;
import com.lifestylecoach.nutrition.USDAFoodAnalysisService;
import com.lifestylecoach.storage.UserProfileRepository;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {

        public static void main(String[] args) {

                Scanner scanner = new Scanner(System.in);

                try {

                        // ==========================================
                        // SERVICES
                        // ==========================================

                        UserInputService userInputService = new UserInputService(scanner);

                        GeminiAIService geminiAIService = new GeminiAIService();

                        AIContextBuilder contextBuilder = new AIContextBuilder();

                        FoodAIService foodAIService = new FoodAIService(geminiAIService);

                        UserProfileRepository userProfileRepository = new UserProfileRepository();

                        // ==========================================
                        // USDA SETUP
                        // ==========================================

                        Dotenv dotenv = Dotenv.configure()
                                        .directory(".")
                                        .ignoreIfMissing()
                                        .load();

                        String usdaApiKey = dotenv.get("USDA_API_KEY");

                        if (usdaApiKey == null ||
                                        usdaApiKey.isBlank()) {

                                usdaApiKey = System.getenv("USDA_API_KEY");
                        }

                        if (usdaApiKey == null ||
                                        usdaApiKey.isBlank()) {

                                throw new IllegalStateException(
                                                "USDA_API_KEY was not found. "
                                                                + "Please check your .env file.");
                        }

                        NutritionDataProvider nutritionDataProvider = new USDADataProvider(usdaApiKey);

                        USDAFoodAnalysisService usdaFoodAnalysisService = new USDAFoodAnalysisService(
                                        nutritionDataProvider);

                        // ==========================================
                        // APPLICATION HEADER
                        // ==========================================

                        System.out.println();
                        System.out.println(
                                        "==========================================");

                        System.out.println(
                                        "       AI PERSONAL LIFESTYLE COACH");

                        System.out.println(
                                        "==========================================");

                        // ==========================================
                        // IDENTIFY USER (load saved profile, or
                        // collect it for the first time)
                        // ==========================================

                        System.out.println();
                        System.out.print("What's your name? ");

                        String name = scanner.nextLine().trim();

                        Optional<UserProfile> savedProfile = userProfileRepository.load(name);

                        UserProfile user;

                        if (savedProfile.isPresent()) {

                                user = savedProfile.get();

                                System.out.println();
                                System.out.println("Welcome back, " + user.getName() + "!");

                        } else {

                                System.out.println();
                                System.out.println("I don't have a profile for you yet, "
                                                + "so let's set one up.");
                                System.out.println();

                                user = userInputService.collectUserProfile(name);

                                userProfileRepository.save(user);

                                // ==========================================
                                // FIRST-TIME PROFILE ANALYSIS
                                // ==========================================

                                String profilePrompt = contextBuilder.buildProfileAnalysisPrompt(user);

                                System.out.println();
                                System.out.println(
                                                "==========================================");

                                System.out.println(
                                                "       ANALYZING YOUR PROFILE");

                                System.out.println(
                                                "==========================================");

                                String profileResponse = geminiAIService.ask(profilePrompt);

                                System.out.println();
                                System.out.println(
                                                "========== LIFESTYLE ANALYSIS ==========");

                                System.out.println();

                                System.out.println(profileResponse);
                        }

                        // ==========================================
                        // FREE-FORM CHAT WITH THE AGENT
                        // ==========================================

                        ChatSession chatSession = new ChatSession(
                                        user,
                                        geminiAIService,
                                        contextBuilder,
                                        foodAIService,
                                        usdaFoodAnalysisService);

                        chatSession.start(scanner);

                } catch (Exception exception) {

                        System.out.println();
                        System.out.println(
                                        "==========================================");

                        System.out.println(
                                        "           APPLICATION ERROR");

                        System.out.println(
                                        "==========================================");

                        System.out.println();

                        System.out.println(
                                        exception.getMessage());

                        System.out.println();

                        exception.printStackTrace();

                } finally {

                        scanner.close();
                }
        }
}