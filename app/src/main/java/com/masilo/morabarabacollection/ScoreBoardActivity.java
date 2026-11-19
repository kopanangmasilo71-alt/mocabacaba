package com.masilo.morabarabacollection;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class ScoreBoardActivity extends BaseGameActivity implements LevelAdapter.OnLevelClickListener {

    private GameData gameData;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // BaseGameActivity handles orientation, flags, and locale.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_board);
        // Hide system bars for immersive feel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        mAdView = findViewById(R.id.adView);
        if (mAdView != null) {
            mAdView.setVisibility(android.view.View.GONE); // Hide and do not load to eliminate low-impression requests on short visits
        }

        gameData = GameData.getInstance();
        gameData.load(this);
        setupRecyclerViews();
        setupButtonListeners();
        updatePlayerInfo();
    }

    private void setupRecyclerViews() {
        // Robotic AI Levels
        RecyclerView roboticAiRecyclerView = findViewById(R.id.robotic_ai_recycler_view);
        roboticAiRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Level> roboticAiLevels = createLevels("ROBOTIC_AI", gameData.getLevel());
        LevelAdapter roboticAiAdapter = new LevelAdapter(roboticAiLevels, this);
        roboticAiRecyclerView.setAdapter(roboticAiAdapter);

        // Rabbit Levels
        RecyclerView rabbitRecyclerView = findViewById(R.id.rabbit_recycler_view);
        rabbitRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Level> rabbitLevels = createLevels("RABBIT", gameData.getRabbit_level());
        LevelAdapter rabbitAdapter = new LevelAdapter(rabbitLevels, this);
        rabbitRecyclerView.setAdapter(rabbitAdapter);

        // Rabbit Stacked Levels
        RecyclerView rabbit1RecyclerView = findViewById(R.id.rabbit_1_recycler_view);
        rabbit1RecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        List<Level> rabbit1Levels = createLevels("RABBIT_STACKED", gameData.getRabbit_1_level());
        LevelAdapter rabbit1Adapter = new LevelAdapter(rabbit1Levels, this);
        rabbit1RecyclerView.setAdapter(rabbit1Adapter);
    }

    private List<Level> createLevels(String gameType, String currentLevelStr) {
        List<Level> levels = new ArrayList<>();
        int currentLevelNum = 0;
        try {
            currentLevelNum = Integer.parseInt(currentLevelStr.replace("LEVEL", ""));
        } catch (NumberFormatException e) {
            // Handle case where level format is unexpected
        }

        for (int i = 1; i <= 6; i++) {
            boolean isUnlocked = i <= currentLevelNum;
            boolean isCurrent = i == currentLevelNum;
            int points = i * 1000;
            int progress = isUnlocked ? (i == currentLevelNum ? 50 : 100) : 0;
            levels.add(new Level(i, gameType, isUnlocked, isCurrent, points, progress));
        }
        return levels;
    }

    private void setupButtonListeners() {
        findViewById(R.id.play_again_button).setOnClickListener(v -> {
            String lastPlayed = gameData.getLastPlayed();
            if (lastPlayed.isEmpty()) {
                Toast.makeText(this, "No game played yet!", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] parts = lastPlayed.split(":");
            String gameType = parts[0];
            String level = parts[1];

            if (gameType.equals("RABBIT")) {
                gameData.setRabbit_level(level);
                gameData.save(this);
                startLevelWithDoorAnimation(new Intent(this, Rabbit.class));
            } else if (gameType.equals("RABBIT_STACKED")) {
                gameData.setRabbit_1_level(level);
                gameData.save(this);
                startLevelWithDoorAnimation(new Intent(this, RabbitActivity1.class));
            } else {
                gameData.setLevel(level);
                gameData.save(this);
                startLevelWithDoorAnimation(new Intent(this, GameActivity1.class));
            }
        });

        findViewById(R.id.reset_button).setOnClickListener(v -> {
            gameData.reset();
            gameData.save(this);
            setupRecyclerViews();
            updatePlayerInfo();
            Toast.makeText(this, "Reset Successfully", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        findViewById(R.id.settings_button).setOnClickListener(v -> launchWithDoorEffect(new Intent(this, SettingsActivity.class)));
    }

    private void updatePlayerInfo() {
        TextView currentLevelTextView = findViewById(R.id.current_level_text);
        int roboticLevel = 0;
        int rabbitLevel = 0;
        int rabbit1Level = 0;
        try {
            roboticLevel = Integer.parseInt(gameData.getLevel().replace("LEVEL", ""));
            rabbitLevel = Integer.parseInt(gameData.getRabbit_level().replace("LEVEL", ""));
            rabbit1Level = Integer.parseInt(gameData.getRabbit_1_level().replace("LEVEL", ""));
        } catch (NumberFormatException e) {
            // Handle parsing errors
        }
        int highestLevel = Math.max(Math.max(roboticLevel, rabbitLevel), rabbit1Level);
        currentLevelTextView.setText(String.valueOf(highestLevel));
    }

    @Override
    public void onLevelClick(Level level) {
        if (level.isUnlocked()) {
            String lastPlayed = level.getGameType() + ":LEVEL" + level.getLevelNumber();
            gameData.setLastPlayed(lastPlayed);
            if (level.getGameType().equals("RABBIT")) {
                gameData.setRabbit_level("LEVEL" + level.getLevelNumber());
                gameData.save(this);
                startLevelWithDoorAnimation(new Intent(this, Rabbit.class));
            } else if (level.getGameType().equals("RABBIT_STACKED")) {
                gameData.setRabbit_1_level("LEVEL" + level.getLevelNumber());
                gameData.save(this);
                startLevelWithDoorAnimation(new Intent(this, RabbitActivity1.class));
            } else {
                gameData.setLevel("LEVEL" + level.getLevelNumber());
                gameData.save(this);
                startLevelWithDoorAnimation(new Intent(this, GameActivity1.class));
            }
        } else {
            Toast.makeText(this, "Complete Level " + (level.getLevelNumber() - 1) + " to unlock!", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchWithDoorEffect(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
    }

    private void startLevelWithDoorAnimation(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        gameData.load(this);
        setupRecyclerViews();
        updatePlayerInfo();
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
