// A contract class defining the schema for the GamesLog table in the SQLite database.
package com.example.matchpairsmemorygame;

import android.provider.BaseColumns;

public final class GamesLogContract {
    // Private constructor to prevent instantiation of the contract class.
    private GamesLogContract() {}

    // Inner class defining the constants for the GamesLog table columns.
    public static class GamesLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "GamesLog"; // Name of the table
        public static final String COLUMN_GAME_ID = "gameID"; // Unique identifier for each game
        public static final String COLUMN_PLAY_DATE = "playDate"; // Date the game was played
        public static final String COLUMN_PLAY_TIME = "playTime"; // Time the game was played
        public static final String COLUMN_SCORE = "moves"; // Number of moves made in the game
        public static final String COLUMN_FINISH = "finish"; // Whether the game was finished (True/False)

    }
}