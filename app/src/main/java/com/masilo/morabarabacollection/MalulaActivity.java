package com.masilo.morabarabacollection;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

/**
 * The activity that handles the game panel and the robot classes
 * Updated with entrance/exit animations and better lifecycle management
 */
public class MalulaActivity extends BaseGameActivity {
    private GamePanel1 gamePanel;
    private Button resumeButton;
    private Button pauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onCreate(savedInstanceState);
        // Fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game1);
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
        // Apply entrance animation
        findViewById(android.R.id.content).startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.doors_open_in)
        );
        // Initialize views
        TextView level = findViewById(R.id.textView3);
        resumeButton = findViewById(R.id.resume_button);
        pauseButton = findViewById(R.id.pause_button);
        Button exitButton = findViewById(R.id.exit_button);
        TextView whiteG = findViewById(R.id.goal_w);
        TextView blackG = findViewById(R.id.goal_b);
        level.bringToFront();
        // Set up game panel
        gamePanel = findViewById(R.id.game);
        gamePanel.setZOrderOnTop(false);
        SurfaceHolder surfaceHolder = gamePanel.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        
        // Set Malula flag BEFORE setting up the board/background
        if (gamePanel != null) {
            gamePanel.isMalulaStarted = true;
            gamePanel.setButtons1(exitButton, pauseButton, resumeButton);
        }

        if (gamePanel != null) {
            gamePanel.setButtons(whiteG, blackG);
        }
        if (gamePanel != null) {
            gamePanel.setBlack();
        }

        // Set level display
        if (gamePanel != null) {
            level.setText(gamePanel.getLevel());
        }

        // Button listeners
        pauseButton.setOnClickListener(v -> pauseGame());
        resumeButton.setOnClickListener(v -> resumeGame());
        exitButton.setOnClickListener(v -> showExitDialog());

        // Ensure buttons start in the correct state
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }
    private void pauseGame() {
        if (gamePanel != null) {
            gamePanel.gamePaused();
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.VISIBLE);
        }
    }
    private void resumeGame() {
        if (gamePanel != null) {
            gamePanel.gameResumed();
            resumeButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }
    private void showExitDialog() {
        ExitDialog exitDialog = new ExitDialog(this, this::startExit);
        exitDialog.show();
    }
    private void startExit() {
        if (gamePanel != null) gamePanel.pause();
        Intent intent = new Intent(this, GameStart.class);
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gamePanel != null) {
            gamePanel.gamePaused();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (gamePanel != null) {
            gamePanel.gameResumed();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gamePanel != null) {
            gamePanel.stop();
            gamePanel = null;
        }
    }
}