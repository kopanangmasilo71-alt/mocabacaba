package com.masilo.morabarabacollection;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.content.pm.ActivityInfo;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;


/**
 * Activity responsible for the Alquerque game screen.
 */
public class AlquerqueActivity extends BaseGameActivity {

    // Main game rendering / logic panel
    private AlquerquePanel alquerquePanel;

    // UI controls
    private AppCompatButton resumeButton;
    private AppCompatButton pauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        // Inflate activity layout first to ensure DecorView is initialized
        setContentView(R.layout.activity_alquerque);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Deprecated in API 30
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        // Initialize game panel
        alquerquePanel = findViewById(R.id.alquerque_panel);

        // Initialize UI buttons
        resumeButton = findViewById(R.id.resume_button);
        pauseButton = findViewById(R.id.pause_button);
        AppCompatButton exitButton = findViewById(R.id.exit_button);
        AppCompatButton surrenderButton = findViewById(R.id.surrender_button);

        TextView goalB = findViewById(R.id.goal_b);
        TextView goalW = findViewById(R.id.goal_w);
        TextView gameMessages = findViewById(R.id.status_update);

        // Link scores to the panel
        if (alquerquePanel != null) {
            alquerquePanel.setViews(goalW, goalB, gameMessages);
        }

        // Resume game button
        resumeButton.setOnClickListener(v -> {
            playClickSound();
            resumeGame();
        });

        // Pause game button
        pauseButton.setOnClickListener(v -> {
            playClickSound();
            pauseGame();
        });

        // Exit game button
        exitButton.setOnClickListener(v -> {
            playClickSound();
            showExitDialog();
        });

        // Surrender game button
        surrenderButton.setOnClickListener(v -> {
            playClickSound();
            showRestartDialog();
        });
        
        // Ensure buttons start in the correct state
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    /**
     * Called by the game panel when the game ends.
     */
    public void onGameOver(String winner, int moves) {
        EndGameDialog endGameDialog =
                new EndGameDialog(this, winner, 1, moves, true);
        endGameDialog.setCancelable(false);
        endGameDialog.show();
    }

    private void showExitDialog() {
        ExitDialog exitDialog = new ExitDialog(this, this::startExit);
        exitDialog.show();
        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow()
                    .getDecorView()
                    .startAnimation(
                            AnimationUtils.loadAnimation(
                                    this,
                                    R.anim.dialog_slide_fade_in
                            )
                    );
        }
    }

    private void showRestartDialog() {
        RestartDialog restartDialog =
                new RestartDialog(this, this::startSurrender);
        restartDialog.show();
        if (restartDialog.getWindow() != null) {
            restartDialog.getWindow()
                    .getDecorView()
                    .startAnimation(
                            AnimationUtils.loadAnimation(
                                    this,
                                    R.anim.dialog_slide_fade_in
                            )
                    );
        }
    }

    private void startExit() {
        if (alquerquePanel != null) {
            alquerquePanel.pause();
        }
        Intent intent =
                new Intent(AlquerqueActivity.this, GameStart.class);
        startActivity(intent);
        overridePendingTransition(
                R.anim.doors_open_in,
                R.anim.doors_open_out_left
        );
        finish();
    }

    private void startSurrender() {
        if (alquerquePanel != null) {
            alquerquePanel.gameSurrender();
        }
    }

    private void pauseGame() {
        if (alquerquePanel != null) {
            alquerquePanel.gamePaused();
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.VISIBLE);
        }
    }

    private void resumeGame() {
        if (alquerquePanel != null) {
            alquerquePanel.gameResumed();
            resumeButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alquerquePanel != null) {
            alquerquePanel.gamePaused();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alquerquePanel != null) {
            alquerquePanel.gameResumed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alquerquePanel != null) {
            alquerquePanel.stop();
        }
    }
}
