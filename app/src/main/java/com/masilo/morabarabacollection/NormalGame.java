
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

import androidx.annotation.NonNull;

/**
 * the activity that handles the game panel for a normal player vs player game
 */
public class NormalGame extends BaseGameActivity {
    private NormalPanel gamePanel = null;
    private Button resumeButton;
    private Button pauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.normal_activity);
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
        TextView level = findViewById(R.id.textView3);
        resumeButton = findViewById(R.id.resume_button);
        pauseButton = findViewById(R.id.pause_button);
        Button exitButton = findViewById(R.id.exit_button);

        TextView whiteG = findViewById(R.id.goal_w);
        TextView blackG = findViewById(R.id.goal_b);
        TextView gameMessages = findViewById(R.id.game_messages);

        pauseButton.setOnClickListener(v -> pauseGame());
        resumeButton.setOnClickListener(v -> resumeGame());
        exitButton.setOnClickListener(v -> showExitDialog());

        gamePanel = findViewById(R.id.game);
        gamePanel.setZOrderOnTop(false);
        SurfaceHolder surfaceHolder = gamePanel.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        gamePanel.setViews(whiteG, blackG, gameMessages);
        level.setText(R.string.normal_game);

        if (savedInstanceState != null) {
            gamePanel.restoreState(savedInstanceState);
        }

        // Initialize button visibility
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }

    private void pauseGame(){
        if (gamePanel != null) {
            gamePanel.gamePaused();
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.VISIBLE);
        }
    }

    private void resumeGame(){
        if (gamePanel != null) {
            gamePanel.gameResumed();
            resumeButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    private void showExitDialog() {
        ExitDialog exitDialog = new ExitDialog(this, "NORMAL", this::startExit);
        exitDialog.show();
    }

    private void startExit() {
        Intent intent = new Intent(NormalGame.this, GameStart.class);
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (gamePanel != null) {
            gamePanel.saveState(outState);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gamePanel != null) {
            gamePanel.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (gamePanel != null) {
            gamePanel.resume();
        }
    }
}
