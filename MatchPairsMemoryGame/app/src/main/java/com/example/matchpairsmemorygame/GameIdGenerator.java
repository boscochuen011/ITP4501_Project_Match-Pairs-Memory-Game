// A utility class for generating unique game IDs.
package com.example.matchpairsmemorygame;

import java.util.UUID;

public class GameIdGenerator {

    // Generates a short game ID by combining a "G" prefix with the first 8 characters of a randomly generated UUID.
    public static String generateShortGameId() {
        UUID uuid = UUID.randomUUID(); // Generate a random UUID.
        String shortUUID = uuid.toString().substring(0, 8); // Extract the first 8 characters of the UUID string.
        return "G" + shortUUID; // Return the generated game ID by concatenating the "G" prefix with the shortened UUID.
    }
}