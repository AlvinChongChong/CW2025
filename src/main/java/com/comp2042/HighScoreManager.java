package com.comp2042;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Handles loading and saving the solo-mode high score to disk.
 * The score is stored in the user's home directory so it persists between runs.
 */
public class HighScoreManager {

    private static final String DIRECTORY_NAME = ".tetris";
    private static final String FILE_NAME = "highscore.dat";

    private final Path highScoreFile;

    public HighScoreManager() {
        String userHome = System.getProperty("user.home");
        Path directory = Paths.get(userHome, DIRECTORY_NAME);
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            // If we can't create the directory, fall back to current working directory.
            directory = Paths.get("").toAbsolutePath();
        }
        highScoreFile = directory.resolve(FILE_NAME);
    }

    /**
     * Loads the saved high score if available.
     *
     * @return the stored high score, or 0 if none exists or parsing fails
     */
    public int loadHighScore() {
        if (!Files.exists(highScoreFile)) {
            return 0;
        }
        try {
            String content = Files.readString(highScoreFile, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(content);
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Persists the provided high score value.
     *
     * @param newHighScore score to store
     */
    public void saveHighScore(int newHighScore) {
        try {
            Files.writeString(
                    highScoreFile,
                    String.valueOf(newHighScore),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
        }
    }
}

