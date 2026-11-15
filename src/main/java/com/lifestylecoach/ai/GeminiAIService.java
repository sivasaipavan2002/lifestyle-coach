package com.lifestylecoach.ai;

import com.google.genai.Client;
import com.google.genai.errors.ServerException;
import com.google.genai.types.GenerateContentResponse;

import io.github.cdimascio.dotenv.Dotenv;

public class GeminiAIService {

    private static final String MODEL = "gemini-3.5-flash";

    private static final int MAX_ATTEMPTS = 3;

    private static final long INITIAL_BACKOFF_MILLIS = 1500;

    private final Client client;

    public GeminiAIService() {

        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        String apiKey = dotenv.get("GEMINI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY was not found. "
                            + "Please check your .env file.");
        }

        client = Client.builder()
                .apiKey(apiKey)
                .build();

        String keySuffix = apiKey.length() > 6 ? apiKey.substring(apiKey.length() - 6) : apiKey;

        System.out.println(
                "GeminiAIService using API key ending in ..." + keySuffix
                        + " (restart the server after editing .env -- it's only read once at startup)");
    }

    public String ask(String message) {

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                    "AI message cannot be empty.");
        }

        long backoffMillis = INITIAL_BACKOFF_MILLIS;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {

                GenerateContentResponse response = client.models.generateContent(
                        MODEL,
                        message,
                        null);

                return response.text();

            } catch (ServerException exception) {

                // Transient overload (e.g. HTTP 503). Retry a few
                // times with backoff before giving up.
                if (attempt == MAX_ATTEMPTS) {
                    throw exception;
                }

                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw exception;
                }

                backoffMillis *= 2;
            }
        }

        throw new IllegalStateException("Unreachable.");
    }
}