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

public class GameActivity2 extends BaseGameActivity {
    private static final String TAG = "GameActivity2";
    private GamePanel2 gamePanel;
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
        setContentView(R.layout.activity_game2);
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
        GameData gd = GameData.getInstance();
        gd.incrementVariantGames();
        gd.save(this);
        gamePanel = findViewById(R.id.game);
        gamePanel.setZOrderOnTop(false);
        SurfaceHolder surfaceHolder = gamePanel.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        TextView level = findViewById(R.id.textView3);
        resumeButton = findViewById(R.id.resume_button);
        pauseButton = findViewById(R.id.pause_button);
        Button exitButton = findViewById(R.id.exit_button);
        Button surrenderButton = findViewById(R.id.surrender);
        TextView whiteG = findViewById(R.id.goal_w);
        TextView blackG = findViewById(R.id.goal_b);
        gamePanel.setButtons(whiteG,blackG);
        gamePanel.setBlack();
        gamePanel.setButtons1(exitButton,surrenderButton,pauseButton ,resumeButton);
        exitButton.setOnClickListener(v -> showExitDialog());
        surrenderButton.setOnClickListener(v -> showRestartDialog());
        pauseButton.setOnClickListener(v -> pauseGame());
        resumeButton.setOnClickListener(v -> resumeGame());
        level.setText(gamePanel.getLevel());

        // Initial state
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);
    }


    private void showExitDialog() {
        ExitDialog exitDialog = new ExitDialog(this, "NORMAL", this::startExit);
        exitDialog.show();
        if (exitDialog.getWindow() != null) {
            exitDialog.getWindow().getDecorView().startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.dialog_slide_fade_in)
            );
        }
    }
    private void showRestartDialog() {
        RestartDialog restartDialog = new RestartDialog(this, "NORMAL", this::startSurrender);
        restartDialog.show();
        if (restartDialog.getWindow() != null) {
            restartDialog.getWindow().getDecorView().startAnimation(
                    AnimationUtils.loadAnimation(this, R.anim.dialog_slide_fade_in)
            );
        }
    }
    private void startExit() {
        if (gamePanel != null) {
            gamePanel.pause();
        }
        proceedToGameStart();
    }

    private void proceedToGameStart() {
        Intent intent = new Intent(GameActivity2.this, GameStart.class);
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
        finish();
    }

    private void startSurrender() {
        if (gamePanel != null) {
            gamePanel.gameSurrender();
        }
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
        if (gamePanel != null) {
            gamePanel.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gamePanel != null) {
            gamePanel.resume();
        }
    }

}
