package com.example.matchpairsmemorygame;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RecordsActivity extends AppCompatActivity {

    private ListView recordsListView;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private SimpleCursorAdapter recordsAdapter;
    private Button resetButton;

    // onCreate method is called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        // Initialize UI elements and set onClickListener for the resetButton
        recordsListView = findViewById(R.id.recordsListView);
        resetButton = findViewById(R.id.resetButton);
        dbHelper = new DatabaseHelper(this);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetRecords(); // Call the resetRecords method when the resetButton is clicked
            }
        });

        loadRecords(); // Load the records from the database
    }

    // Method to load the records from the database and display them in the ListView
    private void loadRecords() {
        db = dbHelper.getReadableDatabase();

        // Query the database to get the records sorted by play date and play time
        Cursor cursor = db.query(
                GamesLogContract.GamesLogEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                GamesLogContract.GamesLogEntry.COLUMN_PLAY_DATE + " DESC, " +
                        GamesLogContract.GamesLogEntry.COLUMN_PLAY_TIME + " DESC"
        );

        // Show a toast message if no records are found
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No records found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Define the columns to be displayed and their corresponding views in the record_item layout
        String[] fromColumns = {
                GamesLogContract.GamesLogEntry.COLUMN_PLAY_DATE,
                GamesLogContract.GamesLogEntry.COLUMN_PLAY_TIME,
                GamesLogContract.GamesLogEntry.COLUMN_SCORE,
                GamesLogContract.GamesLogEntry.COLUMN_FINISH
        };

        int[] toViews = {
                R.id.playDateTextView,
                R.id.playTimeTextView,
                R.id.movesTextView,
                R.id.finishTextView
        };

        // Create a SimpleCursorAdapter to display the records in the ListView
        recordsAdapter = new SimpleCursorAdapter(
                this,
                R.layout.record_item,
                cursor,
                fromColumns,
                toViews,
                0
        );

        // Set the adapter for the recordsListView
        recordsListView.setAdapter(recordsAdapter);
    }
    private void resetRecords() {
        db = dbHelper.getWritableDatabase();
        db.delete(GamesLogContract.GamesLogEntry.TABLE_NAME, null, null);

        // Update the ListView to reflect the changes
        recordsAdapter.changeCursor(null); // Set the current cursor to null
        recordsAdapter.notifyDataSetChanged(); // Notify the adapter that the data has changed

        loadRecords();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

