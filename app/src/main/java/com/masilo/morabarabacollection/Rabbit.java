package com.masilo.morabarabacollection;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The activity that handles the rabbit game panel and the robot classes
 */
public class Rabbit extends BaseGameActivity {

    private RabbitPanel gamePanel;
    private boolean gameOnPause = false;
    private int userMoves = 0;
    private TextView movesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.rabbit);
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
    }

    private void initViews() {
        movesTextView = findViewById(R.id.textView3);
        if (movesTextView != null) {
            movesTextView.setText(String.valueOf(userMoves));
        }
    }

    private void setupButtonListeners() {
        Button resumeButton = findViewById(R.id.resume_button);
        Button pauseButton = findViewById(R.id.pause_button);
        Button exitButton = findViewById(R.id.exit_button);
        Button surrenderButton = findViewById(R.id.surrender);

        pauseButton.setOnClickListener(v -> pauseGame());
        resumeButton.setOnClickListener(v -> resumeGame());
        exitButton.setOnClickListener(v -> showExitDialog());
        surrenderButton.setOnClickListener(v -> showRestartDialog());
    }

    private void setupGamePanel() {
        gamePanel = findViewById(R.id.game);
        TextView gameMessages = findViewById(R.id.game_messages);
        if (gamePanel != null) {
            gamePanel.setZOrderOnTop(false);
            SurfaceHolder surfaceHolder = gamePanel.getHolder();
            surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            // Initialize the game panel with current rabbit level
            gamePanel.setBlack();
            gamePanel.setGameMessages(gameMessages);
        } else {
            Toast.makeText(this, "Game panel not found!", Toast.LENGTH_SHORT).show();
        }
    }

    public void incrementUserMoves() {
        userMoves++;
        runOnUiThread(() -> {
            if (movesTextView != null) {
                movesTextView.setText(String.valueOf(userMoves));
            }
        });
    }

    private void pauseGame() {
        if (gamePanel != null) {
            gamePanel.gamePaused();
            gameOnPause = true;
            // Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();
        }
    }

    private void resumeGame() {
        if (gamePanel != null) {
            gamePanel.gameResumed();
            gameOnPause = false;
            // Toast.makeText(this, "Game Resumed", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExitDialog() {
        ExitDialog exitDialog = new ExitDialog(this, "RABBIT", this::startExit);
        exitDialog.show();
    }

    private void showRestartDialog() {
        RestartDialog restartDialog = new RestartDialog(this, "RABBIT", this::startSurrender);
        restartDialog.show();
    }

    private void startExit() {
        if (gamePanel != null) {
            gamePanel.pause();
        }
        Intent intent = new Intent(Rabbit.this, GameStart.class);
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
        finish();
    }

    private void startSurrender() {
        if (gamePanel != null) {
            gamePanel.gameSurrender();
        }
    }

    public void onGameOver(String winner) {
        runOnUiThread(() -> {
            if (gamePanel != null) {
                gamePanel.showGameOverDialog(winner, userMoves);
            }
        });
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
