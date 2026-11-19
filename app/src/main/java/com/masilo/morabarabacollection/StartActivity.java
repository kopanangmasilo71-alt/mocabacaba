package com.masilo.morabarabacollection;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.ads.AdView;

public class StartActivity extends BaseGameActivity {

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

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

        // Initialize AdManager and load banner only when ready
        // IDs are now correctly set in XML to avoid inflation errors
        AdManager.getInstance().init(this, () -> {
            if (mAdView != null) {
                runOnUiThread(() -> {
                    // Add a delay before loading the banner to eliminate low-impression requests from rapid navigation
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            mAdView.loadAd(AdManager.getInstance().getAdRequest());
                        }
                    }, 1500);
                });
            }
        });

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        Button startBtn = findViewById(R.id.btn_start_game);
        Button closeBtn = findViewById(R.id.btn_close_game);
        View settingsBtn = findViewById(R.id.btn_settings_main);

        View robotGameInclude = findViewById(R.id.include_robot_game);
        View normalGameInclude = findViewById(R.id.include_normal_game);
        View malulaGameInclude = findViewById(R.id.include_malula_game);

        // Navigation
        if (robotGameInclude != null) {
            robotGameInclude.findViewById(R.id.robot).setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                launchWithDoorEffect(GameActivity1.class);
            });
            robotGameInclude.findViewById(R.id.btn_info_robot).setOnClickListener(v ->
                    showGameInfoDialog(getString(R.string.title_robot_match), getString(R.string.desc_robot_match)));
        }

        if (normalGameInclude != null) {
            normalGameInclude.findViewById(R.id.normal).setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                launchWithDoorEffect(NormalGame.class);
            });
            normalGameInclude.findViewById(R.id.btn_info_normal).setOnClickListener(v ->
                    showGameInfoDialog(getString(R.string.title_local_pvp), getString(R.string.desc_local_pvp)));
        }

        if (malulaGameInclude != null) {
            malulaGameInclude.findViewById(R.id.action_malula).setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                launchWithDoorEffect(MalulaActivity.class);
            });
            malulaGameInclude.findViewById(R.id.btn_info_malula).setOnClickListener(v ->
                    showGameInfoDialog(getString(R.string.title_malula), getString(R.string.desc_malula)));
        }

        startBtn.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            launchWithDoorEffect(GameStart.class);
        });

        closeBtn.setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            finishAffinity(); // Closes all activities in the stack
        });

        if (settingsBtn != null) {
            settingsBtn.setOnClickListener(v -> {
                SoundManager.getInstance(this).playClick();
                launchWithDoorEffect(SettingsActivity.class);
            });
        }
    }

    private void refreshGameStats() {
        GameData data = GameData.getInstance();
        data.load(this);

        // 1. Robot Match Rating (Based on Current Level)
        View robotView = findViewById(R.id.include_robot_game);
        if (robotView != null) {
            RatingBar ratingBar = robotView.findViewById(R.id.rating_robot);
            if (ratingBar != null) {
                int levelNum = Integer.parseInt(data.getLevel().replace("LEVEL", ""));
                ratingBar.setRating(levelNum); // 1-6 stars
            }
        }

        // 2. Normal Game Rating (Based on Games Completed - 1 star per game)
        View normalView = findViewById(R.id.include_normal_game);
        if (normalView != null) {
            RatingBar ratingBar = normalView.findViewById(R.id.rating_normal);
            if (ratingBar != null) {
                float rating = (float) data.getNormalGames();
                ratingBar.setRating(Math.min(5.0f, rating));
            }
        }

        // 3. Malula Rating (Based on Games Completed - 1 star per game)
        View malulaView = findViewById(R.id.include_malula_game);
        if (malulaView != null) {
            RatingBar ratingBar = malulaView.findViewById(R.id.rating_malula);
            if (ratingBar != null) {
                float rating = (float) data.getMalulaGames();
                ratingBar.setRating(Math.min(5.0f, rating));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
        refreshGameStats();
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

    private void launchWithDoorEffect(Class<?> target) {
        Intent intent = new Intent(this, target);
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
    }

    public void showGameInfoDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
