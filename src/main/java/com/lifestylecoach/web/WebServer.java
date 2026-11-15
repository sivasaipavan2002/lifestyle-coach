package com.lifestylecoach.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.lifestylecoach.ai.AIContextBuilder;
import com.lifestylecoach.ai.FoodAIService;
import com.lifestylecoach.ai.GeminiAIService;
import com.lifestylecoach.chat.ChatSession;
import com.lifestylecoach.model.UserProfile;
import com.lifestylecoach.nutrition.NutritionDataProvider;
import com.lifestylecoach.nutrition.USDADataProvider;
import com.lifestylecoach.nutrition.USDAFoodAnalysisService;
import com.lifestylecoach.storage.UserProfileRepository;
import com.lifestylecoach.web.dto.ChatRequest;
import com.lifestylecoach.web.dto.ChatResponse;
import com.lifestylecoach.web.dto.ErrorResponse;
import com.lifestylecoach.web.dto.ProfileResponse;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.HttpResponseException;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JavalinGson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Exposes the same coach (Gemini food parsing + USDA nutrition +
 * chat) that the console app uses, over a small HTTP API for the
 * React frontend under frontend/.
 *
 * One ChatSession per user name is kept in memory for the life of
 * the server -- this is a single-machine personal tool, not a
 * multi-instance service, so that is enough.
 */
public class WebServer {

    private static final int DEFAULT_PORT = 8080;
    private static final Path FRONTEND_BUILD_DIRECTORY = Path.of("frontend", "dist");

    private final UserProfileRepository userProfileRepository = new UserProfileRepository();
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    private final GeminiAIService geminiAIService;
    private final AIContextBuilder contextBuilder;
    private final FoodAIService foodAIService;
    private final USDAFoodAnalysisService usdaFoodAnalysisService;

    public WebServer() {

        this.geminiAIService = new GeminiAIService();
        this.contextBuilder = new AIContextBuilder();
        this.foodAIService = new FoodAIService(geminiAIService);

        String usdaApiKey = readUsdaApiKey();

        NutritionDataProvider nutritionDataProvider = new USDADataProvider(usdaApiKey);
        this.usdaFoodAnalysisService = new USDAFoodAnalysisService(nutritionDataProvider);
    }

    public static void main(String[] args) {

        int port = DEFAULT_PORT;

        String portEnv = System.getenv("WEB_PORT");

        if (portEnv != null && !portEnv.isBlank()) {
            port = Integer.parseInt(portEnv.trim());
        }

        new WebServer().start(port);
    }

    public void start(int port) {

        Gson gson = new GsonBuilder().create();

        Javalin app = Javalin.create(config -> {

            config.jsonMapper(new JavalinGson(gson, false));

            if (Files.isDirectory(FRONTEND_BUILD_DIRECTORY)) {

                config.staticFiles.add(staticFiles -> {
                    staticFiles.directory = FRONTEND_BUILD_DIRECTORY.toString();
                    staticFiles.location = Location.EXTERNAL;
                });
            }
        });

        registerRoutes(app);

        // Javalin has built-in plain-text handling for HttpResponseException
        // (NotFoundResponse, BadRequestResponse, ...) that runs before the
        // generic Exception.class mapper below, so it needs its own handler
        // to keep every error response in the same JSON shape.
        app.exception(HttpResponseException.class, (exception, ctx) ->
                ctx.status(exception.getStatus()).json(new ErrorResponse(exception.getMessage())));

        app.exception(Exception.class, (exception, ctx) -> {
            System.err.println("Unhandled error on " + ctx.method() + " " + ctx.path() + ": " + exception);
            exception.printStackTrace();
            ctx.status(500).json(new ErrorResponse("Something went wrong. Please try again."));
        });

        app.start(port);

        System.out.println("AI Personal Lifestyle Coach web API listening on http://localhost:" + port);
    }

    private void registerRoutes(Javalin app) {

        app.get("/api/health", ctx -> ctx.json(Map.of("status", "ok")));

        app.get("/api/profile/{name}", ctx -> {

            String name = ctx.pathParam("name");

            Optional<UserProfile> profile = userProfileRepository.load(name);

            if (profile.isEmpty()) {
                throw new NotFoundResponse("No profile found for '" + name + "'.");
            }

            ctx.json(profile.get());
        });

        app.post("/api/profile", ctx -> {

            UserProfile incoming = ctx.bodyAsClass(UserProfile.class);

            if (incoming.getName() == null || incoming.getName().isBlank()) {
                throw new BadRequestResponse("Profile must include a name.");
            }

            boolean alreadyExisted = userProfileRepository.load(incoming.getName()).isPresent();

            userProfileRepository.save(incoming);

            String analysis = null;

            if (!alreadyExisted) {

                String profilePrompt = contextBuilder.buildProfileAnalysisPrompt(incoming);
                analysis = geminiAIService.ask(profilePrompt);
            }

            ctx.status(alreadyExisted ? 200 : 201)
                    .json(new ProfileResponse(incoming, !alreadyExisted, analysis));
        });

        app.post("/api/chat", ctx -> {

            ChatRequest request = ctx.bodyAsClass(ChatRequest.class);

            if (request.getName() == null || request.getName().isBlank()) {
                throw new BadRequestResponse("Chat request must include a name.");
            }

            if (request.getMessage() == null || request.getMessage().isBlank()) {
                throw new BadRequestResponse("Chat request must include a message.");
            }

            ChatSession session = sessionFor(request.getName());

            String reply;

            // One ChatSession's conversation state isn't thread-safe, and
            // the same user could fire overlapping requests (a double
            // click, multiple tabs) -- serialize per-session access.
            synchronized (session) {
                reply = session.processMessage(request.getMessage().trim());
            }

            ctx.json(new ChatResponse(reply, session.getNutritionTotals()));
        });
    }

    private ChatSession sessionFor(String name) {

        String key = name.trim().toLowerCase(Locale.ROOT);

        return sessions.computeIfAbsent(key, ignored -> {

            UserProfile profile = userProfileRepository.load(name)
                    .orElseThrow(() -> new NotFoundResponse(
                            "No profile found for '" + name + "'. Create one first via POST /api/profile."));

            return new ChatSession(
                    profile,
                    geminiAIService,
                    contextBuilder,
                    foodAIService,
                    usdaFoodAnalysisService);
        });
    }

    private String readUsdaApiKey() {

        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        String usdaApiKey = dotenv.get("USDA_API_KEY");

        if (usdaApiKey == null || usdaApiKey.isBlank()) {
            usdaApiKey = System.getenv("USDA_API_KEY");
        }

        if (usdaApiKey == null || usdaApiKey.isBlank()) {
            throw new IllegalStateException(
                    "USDA_API_KEY was not found. Please check your .env file.");
        }

        return usdaApiKey;
    }
}
