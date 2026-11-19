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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The activity that handles the stacked rabbit game panel
 */
public class RabbitActivity1 extends BaseGameActivity {

    private RabbitPannel1 gamePanel;
    private boolean gameOnPause = false;
    private Button resumeButton;
    private Button pauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.rabbit1);
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
        // Initialize views
        initViews();
        setupButtonListeners();
        setupGamePanel();

        // Ensure buttons start in the correct state
        if (resumeButton != null) resumeButton.setVisibility(View.GONE);
        if (pauseButton != null) pauseButton.setVisibility(View.VISIBLE);
    }

    private void initViews() {
        TextView movesTextView = findViewById(R.id.textView3);
        if (movesTextView != null) {
            int userMoves = 0;
            movesTextView.setText(String.valueOf(userMoves));
        }
    }

    private void setupButtonListeners() {
        resumeButton = findViewById(R.id.resume_button);
        pauseButton = findViewById(R.id.pause_button);
        Button exitButton = findViewById(R.id.exit_button);
        Button surrenderButton = findViewById(R.id.surrender);

        if (pauseButton != null) pauseButton.setOnClickListener(v -> pauseGame());
        if (resumeButton != null) resumeButton.setOnClickListener(v -> resumeGame());
        if (exitButton != null) exitButton.setOnClickListener(v -> showExitDialog());
        if (surrenderButton != null) surrenderButton.setOnClickListener(v -> showRestartDialog());
    }

    private void setupGamePanel() {
        gamePanel = findViewById(R.id.game);
        if (gamePanel != null) {
            gamePanel.setZOrderOnTop(false);
            SurfaceHolder surfaceHolder = gamePanel.getHolder();
            surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        } else {
            Toast.makeText(this, "Game panel not found!", Toast.LENGTH_SHORT).show();
        }
    }


    private void pauseGame() {
        if (gamePanel != null) {
            gamePanel.gamePaused();
            gameOnPause = true;
            if (pauseButton != null) pauseButton.setVisibility(View.GONE);
            if (resumeButton != null) resumeButton.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void resumeGame() {
        if (gamePanel != null) {
            gamePanel.gameResumed();
            gameOnPause = false;
            if (resumeButton != null) resumeButton.setVisibility(View.GONE);
            if (pauseButton != null) pauseButton.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Game Resumed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExitDialog() {
        ExitDialog exitDialog = new ExitDialog(this, "RABBIT1", this::startExit);
        exitDialog.show();
    }

    private void showRestartDialog() {
        RestartDialog restartDialog = new RestartDialog(this, "RABBIT1", this::startSurrender);
        restartDialog.show();
    }

    private void startExit() {
        if (gamePanel != null) {
            gamePanel.pause();
        }
        Intent intent = new Intent(RabbitActivity1.this, GameStart.class);
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
        finish();
    }

    private void startSurrender() {
        if (gamePanel != null) {
            gamePanel.gameSurrender();
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (gamePanel != null) {
            gamePanel.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gamePanel != null) {
            gamePanel.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gamePanel != null && !gameOnPause) {
            gamePanel.resume();
        }
    }
}
