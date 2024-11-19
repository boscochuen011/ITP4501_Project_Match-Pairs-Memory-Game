package com.example.matchpairsmemorygame;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GameRankingActivity extends Activity {
    private static final String TAG = "GameRankingActivity";

    private ListView rankingListView;
    private RankingAdapter rankingAdapter;
    private DatabaseHelper dbHelper;

    // onCreate method called when the activity is created.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view of the activity using the activity_rank_records layout.
        setContentView(R.layout.activity_rank_records);

        // Initialize views and adapters.
        rankingListView = findViewById(R.id.recordsListView);
        rankingAdapter = new RankingAdapter();
        // Set the adapter for the rankingListView.
        rankingListView.setAdapter(rankingAdapter);
        // Initialize the dbHelper object.
        dbHelper = new DatabaseHelper(this);

        // Define the API URL and execute the DownloadRankingsTask to fetch rankings.
        String apiUrl = "https://ranking-mobileasignment-wlicpnigvf.cn-hongkong.fcapp.run";
        new DownloadRankingsTask().execute(apiUrl);
    }

    // Retrieves the user's best score from the local database.
    private int getOwnBestScore() {
        // Get a readable instance of the SQLite database.
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define the query to select the minimum score where the game was finished.
        String[] columns = {"MIN(" + GamesLogContract.GamesLogEntry.COLUMN_SCORE + ")"};
        String selection = GamesLogContract.GamesLogEntry.COLUMN_FINISH + " != ?";
        String[] selectionArgs = {"False"};

        // Execute the query and obtain a cursor to the result set.
        Cursor cursor = db.query(GamesLogContract.GamesLogEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        int ownScore = -1;
        // If there is at least one row in the result set, get the minimum score.
        if (cursor.moveToFirst()) {
            ownScore = cursor.getInt(0);
        }

        // Close the cursor and the database connection.
        cursor.close();
        db.close();

        // Return the user's best score.
        return ownScore;
    }
    private class DownloadRankingsTask extends AsyncTask<String, Void, List<Player>> {
        private int ownScore;

        @Override
        protected List<Player> doInBackground(String... urls) {
            String apiUrl = urls[0];
            List<Player> playerRankings = new ArrayList<>();
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String result = stringBuilder.toString();

                reader.close();
                inputStream.close();
                connection.disconnect();

                Log.d(TAG, "JSON response: " + result); // Log the response for debugging

                // Parse the JSON response and populate the player rankings list
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String name = jsonObject.getString("Name");
                    int score = jsonObject.getInt("Moves");

                    if (score > 0) { // Exclude players with a score of 0
                        playerRankings.add(new Player(name, score));
                    }
                }
                // Get own best score
                ownScore = getOwnBestScore();
                if (ownScore > 0) { // Exclude own score if it's 0
                    playerRankings.add(new Player("You", ownScore));
                }


                // Sort the player rankings by score in ascending order
                Collections.sort(playerRankings, new Comparator<Player>() {
                    @Override
                    public int compare(Player player1, Player player2) {
                        return Integer.compare(player1.getMoves(), player2.getMoves());
                    }
                });

                // Assign ranks to the players based on their score
                for (int i = 0; i < playerRankings.size(); i++) {
                    Player player = playerRankings.get(i);
                    player.setRank(i + 1);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return playerRankings;
        }

        @Override
        protected void onPostExecute(List<Player> playerRankings) {
            // Clear the rankingAdapter and add the fetched playerRankings.
            rankingAdapter.clear();
            rankingAdapter.addAll(playerRankings);

            int ownBestRank = -1;
            int ownBestScore = Integer.MAX_VALUE;
            // Iterate through the playerRankings list to find the user's best rank and score.
            for (int i = 0; i < playerRankings.size(); i++) {
                Player player = playerRankings.get(i);
                if ("You".equals(player.getName()) && player.getMoves() < ownBestScore) {
                    ownBestRank = player.getRank();
                    ownBestScore = player.getMoves();
                }
            }
            // Display a Toast message showing the user's ranking if found, otherwise show a "not found" message.
            if (ownBestRank != -1 && ownBestScore != Integer.MAX_VALUE) { // Check if a valid own best score was found
                Toast.makeText(GameRankingActivity.this,
                        String.format("your ranking ：%d", ownBestRank),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(GameRankingActivity.this,
                        "your ranking not found",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    // Custom ArrayAdapter class for displaying player rankings.
    private class RankingAdapter extends ArrayAdapter<Player> {
        private List<Player> playerList;

        public RankingAdapter() {
            super(GameRankingActivity.this, R.layout.ranking_item);
            playerList = new ArrayList<>();
        }
        // Getter for the playerList.
        public List<Player> getPlayerList() {
            return playerList;
        }
        // Add all players to the adapter and update the playerList.
        public void addAll(List<Player> players) {
            super.addAll(players);
            playerList.clear();
            playerList.addAll(players);
        }
        // Define the getView method to display the ranking data in the ListView.
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.ranking_item, parent, false);
            }
            // Get the player at the given position and populate the UI elements with the player's data.
            Player player = getItem(position);

            TextView rankTextView = convertView.findViewById(R.id.rankTextView);
            TextView nameTextView = convertView.findViewById(R.id.nameTextView);
            TextView scoreTextView = convertView.findViewById(R.id.movesTextView);

            rankTextView.setText(String.valueOf(player.getRank()));
            nameTextView.setText(player.getName());
            scoreTextView.setText(String.valueOf(player.getMoves()));

            return convertView;
        }
    }
    // onDestroy method is called when the activity is being destroyed.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
