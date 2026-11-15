package com.lifestylecoach.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lifestylecoach.model.UserProfile;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * File-based persistence for UserProfile, keyed by a normalized name.
 *
 * This is a placeholder for a real database / authentication system.
 * Each user's profile is stored as a JSON file under data/users/.
 */
public class UserProfileRepository {

    private static final Path STORAGE_DIRECTORY = Path.of("data", "users");

    private final Gson gson;

    public UserProfileRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public Optional<UserProfile> load(String name) {

        Path file = fileFor(name);

        if (!Files.exists(file)) {
            return Optional.empty();
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {

            UserProfile profile = gson.fromJson(reader, UserProfile.class);

            return Optional.ofNullable(profile);

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not read saved profile for '" + name + "'.",
                    exception);
        }
    }

    public void save(UserProfile profile) {

        if (profile == null || profile.getName() == null || profile.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Cannot save a profile without a name.");
        }

        Path file = fileFor(profile.getName());

        try {

            Files.createDirectories(STORAGE_DIRECTORY);

            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {

                gson.toJson(profile, writer);
            }

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Could not save profile for '" + profile.getName() + "'.",
                    exception);
        }
    }

    private Path fileFor(String name) {
        return STORAGE_DIRECTORY.resolve(normalize(name) + ".json");
    }

    private String normalize(String name) {

        String normalized = name.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "Name must contain at least one letter or digit.");
        }

        return normalized;
    }
}
