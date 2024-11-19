package com.example.matchpairsmemorygame;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Dialog loadingDialog;
    private Button playButton;
    private Button gameRankingButton;
    private Button yourRecordsButton;
    private VideoView backgroundVideoView;
    private Button closeButton;
    private Handler handler = new Handler();
    private Runnable navigateRunnable;

    // onCreate method is called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements and set onClickListeners
        playButton = findViewById(R.id.playButton);
        gameRankingButton = findViewById(R.id.gameRankingButton);
        yourRecordsButton = findViewById(R.id.yourRecordsButton);

        playButton.setOnClickListener(this);
        gameRankingButton.setOnClickListener(this);
        yourRecordsButton.setOnClickListener(this);

        closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(this);

        // Set up the VideoView for the background video
        backgroundVideoView = findViewById(R.id.backgroundVideoView);
        String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.background_video;
        backgroundVideoView.setVideoURI(Uri.parse(videoPath));
        backgroundVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            backgroundVideoView.start();
        });
    }

    // Method to show a custom loading dialog
    private void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(this) {
                @Override
                protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.custom_loading_dialog);
                    ImageView closeButton = findViewById(R.id.close_button);
                    closeButton.setOnClickListener(v -> {
                        dismiss();
                        if (navigateRunnable != null) {
                            handler.removeCallbacks(navigateRunnable);
                        }
                    });
                }
            };
            loadingDialog.setCancelable(false);
        }
        loadingDialog.show();
    }

    // Method to hide the custom loading dialog
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    // onResume method is called when the activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        // Start playing the background video if it's not playing
        if (backgroundVideoView != null && !backgroundVideoView.isPlaying()) {
            backgroundVideoView.start();
        }
    }

    // onPause method is called when the activity is paused
    @Override
    protected void onPause() {
        super.onPause();
        // Pause the background video if it's playing
        if (backgroundVideoView != null && backgroundVideoView.isPlaying()) {
            backgroundVideoView.pause();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.playButton) {
            showLoadingDialog();

            // Simulate loading task
            navigateRunnable = new Runnable() {
                @Override
                public void run() {
                    hideLoadingDialog();
                    // Perform the action after loading is complete, like starting a new Activity
                    Intent playIntent = new Intent(MainActivity.this, GameActivity.class);
                    startActivity(playIntent);
                }
            };
            handler.postDelayed(navigateRunnable, 2000);

        } else if (id == R.id.gameRankingButton) {

            showLoadingDialog();

            // Simulate loading task
            navigateRunnable = new Runnable() {
                @Override
                public void run() {
                    hideLoadingDialog();
                    // Perform the action after loading is complete, like starting a new Activity
                    Intent rankingIntent = new Intent(MainActivity.this, GameRankingActivity.class);
                    startActivity(rankingIntent);
                }
            };
            handler.postDelayed(navigateRunnable, 2000);

        } else if (id == R.id.yourRecordsButton) {

            showLoadingDialog();

            // Simulate loading task
            navigateRunnable = new Runnable() {
                @Override
                public void run() {
                    hideLoadingDialog();
                    // Perform the action after loading is complete, like starting a new Activity
                    Intent recordsIntent = new Intent(MainActivity.this, RecordsActivity.class);
                    startActivity(recordsIntent);
                }
            };
            handler.postDelayed(navigateRunnable, 2000);

        } else if (id == R.id.closeButton) {
            // Close the application
            finishAffinity();  // This will close all activities in the task, effectively closing the app
        }
    }
}
