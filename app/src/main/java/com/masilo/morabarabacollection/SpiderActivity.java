package com.masilo.morabarabacollection;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

/**
 * the activity that handle the game and the UI
 */
public class SpiderActivity extends AppCompatActivity implements SpiderPanel.SpiderPanelListener {
    private SpiderPanel spiderPanel = null;
    private TextView whiteG;
    private TextView blackG;
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
    setContentView(R.layout.activity_spider);
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
        resumeButton = findViewById(R.id.resume_button);
        pauseButton = findViewById(R.id.pause_button);
        Button exitButton = findViewById(R.id.exit_button);
        Button surrenderButton = findViewById(R.id.surrender_button);

        whiteG = findViewById(R.id.goal_w);
        blackG = findViewById(R.id.goal_b);
        TextView cows = findViewById(R.id.allocating_cows);
        TextView gameMessages = findViewById(R.id.status_update);

        spiderPanel = findViewById(R.id.game_panel);
        if (spiderPanel != null) {
            spiderPanel.setGamePanelListener(this);
            spiderPanel.setGameMessages(gameMessages);
        }

        resumeButton.setOnClickListener(v -> enable());
        pauseButton.setOnClickListener(v -> disable());
        exitButton.setOnClickListener(v -> showExitDialog());
        surrenderButton.setOnClickListener(v -> showRestartDialog());
        cows.setText(R.string.you_own_white_cows);
    }

    private void showExitDialog() {
        ExitDialog exitDialog = new ExitDialog(this, this::startExit);
        exitDialog.show();
    }

    private void showRestartDialog() {
        RestartDialog restartDialog = new RestartDialog(this, this::startSurrender);
        restartDialog.show();
    }

    private void updateScore() {
        if (spiderPanel != null) {
            String x = spiderPanel.getWhitePieceKilled();
            String y = spiderPanel.getBlackPieceKilled();
            blackG.setText(x);
            whiteG.setText(y);
        }
    }

    @Override
    public void onUpdate(boolean black) {
        updateScore();
    }

    private void enable() {
        if (spiderPanel != null) {
            spiderPanel.resume();
            resumeButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        }
    }

    private void disable() {
        if (spiderPanel != null) {
            spiderPanel.pause();
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.VISIBLE);
        }
    }

    private void startExit() {
        finish();
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
    }

    private void startSurrender() {
        if (spiderPanel != null) {
            spiderPanel.restart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (spiderPanel != null) {
            spiderPanel.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (spiderPanel != null) {
            spiderPanel.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (spiderPanel != null) {
            spiderPanel.pause();
            spiderPanel.setGamePanelListener(null);
            spiderPanel = null;
        }
        super.onDestroy();
    }
}
