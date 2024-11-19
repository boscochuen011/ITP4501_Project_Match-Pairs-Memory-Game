package com.example.matchpairsmemorygame;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.res.TypedArray;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GameActivity extends AppCompatActivity {

    private MediaPlayer backgroundMusicPlayer;
    private MediaPlayer soundEffectPlayer;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private int moves;
    private TextView scoreTextView;
    private TextView resultTextView;
    private Button continueButton;
    private List<ImageButton> gameButtons;
    private ImageButton selectedButton1;
    private ImageButton selectedButton2;
    private int playerHealth;
    private TextView healthTextView;
    private ProgressBar healthBar;  // Add this line

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        healthTextView = findViewById(R.id.healthTextView);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        moves = 0;
        backgroundMusicPlayer = MediaPlayer.create(this, R.raw.background_music);
        backgroundMusicPlayer.setLooping(true); // Optional: if you want the music to loop
        soundEffectPlayer = new MediaPlayer();
        scoreTextView = findViewById(R.id.scoreTextView);
        resultTextView = findViewById(R.id.resultTextView);
        continueButton = findViewById(R.id.continueButton);

        // Get the RelativeLayout
        RelativeLayout gameLayout = findViewById(R.id.gameLayout);

        // Set the random background image
        TypedArray backgroundImages = getResources().obtainTypedArray(R.array.background_images);
        int randomImageIndex = new Random().nextInt(backgroundImages.length());
        int randomImageResource = backgroundImages.getResourceId(randomImageIndex, -1);
        gameLayout.setBackgroundResource(randomImageResource);
        backgroundImages.recycle();

        playerHealth = 150;  // Initialize player's health.
        healthTextView = findViewById(R.id.healthTextView);  // Assuming you have a TextView in your layout to display player's health.
        healthTextView.setText("Health: " + playerHealth);

        healthBar = findViewById(R.id.healthBar);  // Find the health bar
        healthBar.setProgress(playerHealth);  // Set the initial progress of the health bar
        resetPlayerHealth();

        Button buttonStopMusic = findViewById(R.id.buttonStopMusic);
        buttonStopMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backgroundMusicPlayer.isPlaying()) {
                    backgroundMusicPlayer.pause();
                    backgroundMusicPlayer.seekTo(0); // Rewind the music to the beginning
                    buttonStopMusic.setText(R.string.start_music); // Change button text to "Start Music"
                } else {
                    backgroundMusicPlayer.start();
                    buttonStopMusic.setText(R.string.stop_music); // Change button text to "Stop Music"
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPlayerHealth(); // Reset player's health
                startNewGame();
            }
        });

        initializeGameButtons();
        startNewGame();

        Animation startGameAnim = AnimationUtils.loadAnimation(this, R.anim.start_game_anim);
        gameLayout.startAnimation(startGameAnim);
    }
    // Triggers a vibration pattern on the device using the specified pattern.
    private void vibrate(long[] pattern) {
        // Get the Vibrator system service using the application context.
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
// Check if the vibrator object is not null and if the device has a vibrator.
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
        dbHelper.close();
    }

    // Initializes the game buttons by finding each button in the layout and adding it to the gameButtons list.
    private void initializeGameButtons() {
        gameButtons = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            int buttonId = getResources().getIdentifier("button" + i, "id", getPackageName());
            ImageButton gameButton = findViewById(buttonId);
            gameButtons.add(gameButton);
        }
    }
    // Inserts a new game log entry into the database with the provided gameID, playDate, playTime, moves, and finish status.
    private void insertGameLog(String gameID, String playDate, String playTime, int moves, String finish) {
        // Put the gameID, playDate, playTime, moves, and finish status into the ContentValues object.
        ContentValues values = new ContentValues();
        values.put(GamesLogContract.GamesLogEntry.COLUMN_GAME_ID, gameID);
        values.put(GamesLogContract.GamesLogEntry.COLUMN_PLAY_DATE, playDate);
        values.put(GamesLogContract.GamesLogEntry.COLUMN_PLAY_TIME, playTime);
        values.put(GamesLogContract.GamesLogEntry.COLUMN_SCORE, moves);
        values.put(GamesLogContract.GamesLogEntry.COLUMN_FINISH, finish);

        // Log the ContentValues object for debugging purposes.
        Log.e("checking", String.valueOf(values));

        long newRowId = db.insert(GamesLogContract.GamesLogEntry.TABLE_NAME, null, values);

// Check if the newRowId is -1 (indicating a failed insertion) and log the result.
        if (newRowId == -1) {
            Log.e("Database Insertion", "Failed to insert row");
        } else {
            Log.d("Database Insertion", "Inserted row ID: " + newRowId);
        }
    }

    // Returns the current date as a formatted string.
    private String getCurrentDate() {
        // Create a new SimpleDateFormat object with the specified format and locale.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // Get the current date.
        Date date = new Date();
        // Format the date and return the result.
        return dateFormat.format(date);
    }

    // Returns the current time as a formatted string.
    private String getCurrentTime() {
        // Create a new SimpleDateFormat object with the specified format and locale.
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        // Get the current date (which includes the time).
        Date date = new Date();
        // Format the time and return the result.
        return timeFormat.format(date);
    }

    // Starts a new game by initializing the game state and setting up the game buttons.
    private void startNewGame() {
        resetPlayerHealth();  // Reset player's health at the start of a new game.
        moves = 0;
        scoreTextView.setText("Moves: 0");
        resultTextView.setVisibility(View.INVISIBLE);
        continueButton.setVisibility(View.INVISIBLE);
        selectedButton1 = null;
        selectedButton2 = null;

        // Generate the card values and assign them to the game buttons.
        List<Integer> cardValues = generateCardValues();
        assignCardValuesToButtons(cardValues);

        // Set up the game buttons.
        for (final ImageButton gameButton : gameButtons) {
            gameButton.setVisibility(View.VISIBLE);
            gameButton.setEnabled(true);
            gameButton.setImageResource(R.drawable.image);

            // Set an OnClickListener for the game button.
            gameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleGameButtonClick(gameButton);
                }
            });
        }
    }

    // Generates a list of card values to be assigned to the game buttons.
    private List<Integer> generateCardValues() {
        List<Integer> cardValues = new ArrayList<>();

        // Add card values to the list (each card value is added twice).
        for (int i = 1; i <= 4; i++) {
            cardValues.add(i);
            cardValues.add(i);
        }

        // Shuffle the card values.
        Collections.shuffle(cardValues);
        return cardValues;
    }

    // Assigns the card values to the game buttons.
    private void assignCardValuesToButtons(List<Integer> cardValues) {
        for (int i = 0; i < gameButtons.size(); i++) {
            ImageButton gameButton = gameButtons.get(i);
            int cardValue = cardValues.get(i);
            gameButton.setTag(cardValue);
        }
    }

    // Handles the logic for when a game button is clicked.
    private void handleGameButtonClick(final ImageButton gameButton) {
        // If two cards are already selected, do nothing.
        if (selectedButton1 != null && selectedButton2 != null) {
            return;
        }

        // Get the card value from the game button tag.
        final int cardValue = (int) gameButton.getTag();

        // Flip the card face-up with an animation.
        Animation flipInAnimation = AnimationUtils.loadAnimation(GameActivity.this, R.anim.flip_in);
        gameButton.setImageResource(getImageResource(cardValue));
        gameButton.startAnimation(flipInAnimation);

        // Handle the card selection logic.
        if (selectedButton1 == null) {
            // First card selection.
            selectedButton1 = gameButton;
            gameButton.setEnabled(false); // Disable the selected card temporarily.
        } else if (selectedButton2 == null) {
            // Second card selection.
            selectedButton2 = gameButton;
            disableAllButtons();

            // Check if the cards match or not and handle accordingly.
            if (selectedButton1.getTag().equals(selectedButton2.getTag())) {
                // Matching cards.
                handleMatchingCards();
                playSoundEffect(R.raw.match_sound);
            } else {
                // Non-matching cards.
                handleNonMatchingCards();
                playSoundEffect(R.raw.mismatch_sound);
            }

            // Increment the move counter and update the display.
            moves++;
            scoreTextView.setText("Moves: " + moves);
        }
    }
    // Plays a sound effect.
    private void playSoundEffect(int soundEffectResId) {
        soundEffectPlayer.release(); // Release any previously loaded sound effect.
        soundEffectPlayer = MediaPlayer.create(this, soundEffectResId); // Create a new MediaPlayer with the specified sound effect resource.
        soundEffectPlayer.start(); // Start playing the sound effect.
        soundEffectPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                soundEffectPlayer.release(); // Release the sound effect after it has finished playing.
            }
        });
    }

    // Releases all media player resources.
    private void releaseMediaPlayer() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.release(); // Release the background music player resources if it is not null.
            backgroundMusicPlayer = null; // Set the background music player reference to null.
        }
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release(); // Release the sound effect player resources if it is not null.
            soundEffectPlayer = null; // Set the sound effect player reference to null.
        }
    }

    // Disables all game buttons.
    private void disableAllButtons() {
        for (ImageButton gameButton : gameButtons) {
            gameButton.setEnabled(false); // Disable the current game button in the iteration.
        }
    }

    // Handles the animation and logic for matching cards.
    private void handleMatchingCards() {
        Animation rotateAnim = AnimationUtils.loadAnimation(GameActivity.this, R.anim.rotate_x); // Load the rotate animation.
        selectedButton1.startAnimation(rotateAnim); // Apply the animation to the first selected button.
        selectedButton2.startAnimation(rotateAnim); // Apply the animation to the second selected button.
        vibrate(new long[]{0, 500}); // Vibrate the device to indicate a match.

        Handler handler = new Handler(); // Create a new Handler to schedule a delayed action.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Make the matched cards invisible and reset the selected buttons.
                selectedButton1.setVisibility(View.INVISIBLE);
                selectedButton2.setVisibility(View.INVISIBLE);
                selectedButton1 = null;
                selectedButton2 = null;
                checkGameOver(); // Check if the game is over.
                enableAllButtons(); // Re-enable all buttons.
            }
        }, 1000); // Delay the execution of the Runnable by 1 second.
    }

    // Handles the animation and logic for non-matching cards.
    private void handleNonMatchingCards() {
        Animation rotateAnim = AnimationUtils.loadAnimation(GameActivity.this, R.anim.rotate_x); // Load the rotate animation.
        if (selectedButton1 != null) {
            selectedButton1.startAnimation(rotateAnim); // Apply the animation to the first selected button if it is not null.
        }
        if (selectedButton2 != null) {
            selectedButton2.startAnimation(rotateAnim); // Apply the animation to the second selected button if it is not null.
        }

        vibrate(new long[]{0, 100, 100, 100});
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (selectedButton1 != null) {
                    selectedButton1.setEnabled(true);
                    selectedButton1.setImageResource(R.drawable.image);
                }
                if (selectedButton2 != null) {
                    selectedButton2.setEnabled(true);
                    selectedButton2.setImageResource(R.drawable.image);
                }
                playerHealth -= 10;  // Decrease player's health by 10 on each mismatch.
                healthTextView.setText("Health: " + playerHealth);
                healthBar.setProgress(playerHealth);  // Update the progress of the health bar

                if (healthTextView != null) {  // Added a null check
                    healthTextView.setText("Health: " + playerHealth);
                }
                if (playerHealth <= 0) {
                    // Player lost all health, end the game.
                    resultTextView.setText("Game over! You lost all your health.");
                    resultTextView.setVisibility(View.VISIBLE);
                    continueButton.setVisibility(View.VISIBLE);
                    playSoundEffect(R.raw.game_over_sound);

                    // Make all the cards invisible
                    for (ImageButton gameButton : gameButtons) {
                        gameButton.setVisibility(View.INVISIBLE);
                    }

                    disableAllButtons();

                    String gameID = GameIdGenerator.generateShortGameId();
                    insertGameLog(gameID, getCurrentDate(), getCurrentTime(), moves, "False");
                } else {
                    selectedButton1 = null;
                    selectedButton2 = null;
                    enableAllButtons();
                }

            }
        }, 1000);
    }

    // Enables all visible game buttons.
    private void enableAllButtons() {
        for (ImageButton gameButton : gameButtons) { // Iterate through the gameButtons array.
            if (gameButton.getVisibility() == View.VISIBLE) { // Check if the current game button is visible.
                gameButton.setEnabled(true); // Enable the visible game button.
            }
        }
    }

    // Checks if the game is over and handles the end game logic.
    private void checkGameOver() {
        boolean isGameOver = true; // Start with the assumption that the game is over.

        for (ImageButton gameButton : gameButtons) { // Iterate through the gameButtons array.
            if (gameButton.getVisibility() == View.VISIBLE) { // Check if the current game button is visible.
                isGameOver = false; // If a visible button is found, set isGameOver to false and break the loop.
                break;
            }
        }

        if (isGameOver) { // If the game is over.
            // Update the resultTextView with the winning message and number of moves.
            resultTextView.setText("You Win! You finished in " + moves + " moves.");
            // Make the resultTextView and continueButton visible.
            resultTextView.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.VISIBLE);

            // Play the game over sound effect.
            playSoundEffect(R.raw.game_over_sound);

            // Generate a game ID and insert a game log with the relevant information.
            String gameID = GameIdGenerator.generateShortGameId();
            insertGameLog(gameID, getCurrentDate(), getCurrentTime(), moves, "True");
        }
    }

    // Resets the player's health and updates the healthTextView and healthBar.
    private void resetPlayerHealth() {
        playerHealth = 150; // Set the player's health to 150.
        if (healthTextView != null) {
            // Update the healthTextView with the current player's health.
            healthTextView.setText("Health: " + playerHealth);
        }
        if (healthBar != null) {
            // Update the healthBar progress with the current player's health.
            healthBar.setProgress(playerHealth);
        }
    }

    // Returns the image resource corresponding to a card value.
    private int getImageResource(int cardValue) {
        switch (cardValue) {
            case 1:
                return R.drawable.image1; // Return the image resource for card value 1.
            case 2:
                return R.drawable.image2; // Return the image resource for card value 2.
            case 3:
                return R.drawable.image3; // Return the image resource for card value 3.
            case 4:
                return R.drawable.image4; // Return the image resource for card value 4.
            default:
                return R.drawable.image; // Return the default image resource for other card values.
        }
    }
}
