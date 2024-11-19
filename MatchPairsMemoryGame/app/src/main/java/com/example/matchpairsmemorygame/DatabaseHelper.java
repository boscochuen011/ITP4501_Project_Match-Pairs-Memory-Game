package com.example.matchpairsmemorygame;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.matchpairsmemorygame.GamesLogContract.GamesLogEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database version and name.
    private static final String DATABASE_NAME = "playgameslog.db";
    private static final int DATABASE_VERSION = 1;

    // SQL command to create the games log table.
    private static final String SQL_CREATE_GAMEINGLOG_TABLE =
            "CREATE TABLE IF NOT EXISTS " + GamesLogEntry.TABLE_NAME + " (" +
                    GamesLogEntry._ID + " INTEGER PRIMARY KEY," +
                    GamesLogEntry.COLUMN_PLAY_TIME + " TEXT," +
                    GamesLogEntry.COLUMN_SCORE + " INTEGER,"+
                    GamesLogEntry.COLUMN_GAME_ID + " TEXT," +
                    GamesLogEntry.COLUMN_PLAY_DATE + " TEXT," +
                    GamesLogEntry.COLUMN_FINISH + " TEXT)";


    // Constructor for DatabaseHelper
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create the table when the database is created.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_GAMEINGLOG_TABLE);
    }

    // If a newer database version exists, drop the old table and create a new one.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GamesLogEntry.TABLE_NAME);
        onCreate(db);
    }
}
