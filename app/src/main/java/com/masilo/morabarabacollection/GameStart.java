package com.masilo.morabarabacollection;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.ads.AdView;

import java.util.Objects;

/**
 * Start screen of the game with a modern UI design.
 */
public class GameStart extends BaseGameActivity {

    private AlertDialog activeDialog;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_start);
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
            // Add a delay before loading the banner to eliminate low-impression requests from rapid navigation
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    mAdView.loadAd(AdManager.getInstance().getAdRequest());
                }
            }, 1500);
        }

        setupButtons();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
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

    private void setupButtons() {
        // Game Modes
        findViewById(R.id.action_spider).setOnClickListener(v -> launchWithDoorEffect(SpiderActivity.class));
        
        findViewById(R.id.action_rabbit).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            startSelectedGame(Rabbit.class);
        });

        findViewById(R.id.action_rabbit_stacked).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            startSelectedGame(RabbitActivity1.class);
        });

        findViewById(R.id.action_rabbit_multiplayer).setOnClickListener(v -> {
            SoundManager.getInstance(this).playClick();
            startSelectedGame(RabbitMultiplayerActivity.class);
        });

        findViewById(R.id.action_variant).setOnClickListener(v -> launchWithDoorEffect(GameActivity2.class));
        findViewById(R.id.action_alquerque).setOnClickListener(v -> launchWithDoorEffect(AlquerqueActivity.class));

        // Info Buttons
        findViewById(R.id.btn_info_spider).setOnClickListener(v ->
                showGameInfoDialog(getString(R.string.title_spider), getString(R.string.desc_spider)));

        findViewById(R.id.btn_info_rabbit).setOnClickListener(v ->
                showGameInfoDialog(getString(R.string.title_rabbit_hunt), getString(R.string.desc_rabbit_hunt)));

        findViewById(R.id.btn_info_rabbit_stacked).setOnClickListener(v ->
                showGameInfoDialog(getString(R.string.title_rabbit_stacked), getString(R.string.desc_rabbit_stacked)));
                
        findViewById(R.id.btn_info_rabbit_multiplayer).setOnClickListener(v ->
                showGameInfoDialog(getString(R.string.title_rabbit_pvp), getString(R.string.desc_rabbit_pvp)));

        findViewById(R.id.btn_info_variant).setOnClickListener(v ->
                showGameInfoDialog(getString(R.string.title_square_logic), getString(R.string.desc_square_logic)));

        findViewById(R.id.btn_info_alquerque).setOnClickListener(v ->
                showGameInfoDialog(getString(R.string.title_alquerque), getString(R.string.desc_alquerque)));

        // Menu Buttons
        findViewById(R.id.exit_button).setOnClickListener(v -> launchWithDoorEffect(StartActivity.class));
        findViewById(R.id.help_button).setOnClickListener(v -> showTransitionDialog(R.layout.tutorial_dialog));
        findViewById(R.id.score_board).setOnClickListener(v -> launchWithDoorEffect(ScoreBoardActivity.class));
        findViewById(R.id.about_us_button).setOnClickListener(v -> showTransitionDialog(R.layout.about_us));
        findViewById(R.id.settings_button).setOnClickListener(v -> launchWithDoorEffect(SettingsActivity.class));
    }

    private void showTransitionDialog(int layoutRes) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View view = getLayoutInflater().inflate(layoutRes, null);
        builder.setView(view);

        activeDialog = builder.create();
        Window window = activeDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        activeDialog.setOnShowListener(dialog ->
                view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dialog_slide_fade_in))
        );

        // Close button if exists
        View closeBtn = view.findViewById(R.id.close_button);
        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> closeDialog());
        }

        // Read Full Policy button if exists (for About Us dialog)
        View readPolicyBtn = view.findViewById(R.id.read_full_policy_button);
        if (readPolicyBtn != null) {
            readPolicyBtn.setOnClickListener(v -> {
                closeDialog();
                new Handler().postDelayed(() -> showTransitionDialog(R.layout.privacy_dialog), 400);
            });
        }

        activeDialog.show();
    }

    private void closeDialog() {
        if (activeDialog != null && activeDialog.isShowing()) {
            new Handler().postDelayed(() -> activeDialog.dismiss(), 300);
            View root = Objects.requireNonNull(activeDialog.getWindow()).getDecorView();
            root.startAnimation(AnimationUtils.loadAnimation(this, R.anim.dialog_slide_fade_out));
        }
    }

    private void updateUI() {
        GameData gd = GameData.getInstance();
        TextView levelText = findViewById(R.id.level_text);
        
        RatingBar ratingRabbit = findViewById(R.id.rating_rabbit);
        RatingBar ratingRabbitStacked = findViewById(R.id.rating_rabbit_stacked);
        RatingBar ratingRabbitMultiplayer = findViewById(R.id.rating_rabbit_multiplayer);
        RatingBar ratingVariant = findViewById(R.id.rating_variant);
        
        RatingBar ratingSpider = findViewById(R.id.rating_spider);
        RatingBar ratingAlquerque = findViewById(R.id.rating_alquerque);

        if (levelText != null) {
            int rabbitLevel = 0;
            int rabbitStackedLevel = 0;
            try {
                rabbitLevel = Integer.parseInt(gd.getRabbit_level().replace("LEVEL", ""));
                rabbitStackedLevel = Integer.parseInt(gd.getRabbit_1_level().replace("LEVEL", ""));
            } catch (NumberFormatException e) {
                // Ignore
            }
            int highestLevel = Math.max(rabbitLevel, rabbitStackedLevel);
            levelText.setText(getString(R.string.level_format, highestLevel));

            if (ratingRabbit != null) ratingRabbit.setRating(rabbitLevel);
            if (ratingRabbitStacked != null) ratingRabbitStacked.setRating(rabbitStackedLevel);
            
            // Dummy ratings for others or calculate based on different stats if available
            if (ratingSpider != null) ratingSpider.setRating(3);
            if (ratingAlquerque != null) ratingAlquerque.setRating(2);
            if (ratingVariant != null) ratingVariant.setRating(4);
            if (ratingRabbitMultiplayer != null) ratingRabbitMultiplayer.setRating(5);
        }
    }

    private void startSelectedGame(Class<?> gameClass) {
        Intent intent = new Intent(this, gameClass);
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
    }

    private void launchWithDoorEffect(Class<?> target) {
        Intent intent = new Intent(this, target);
        startActivity(intent);
        overridePendingTransition(R.anim.doors_open_in, R.anim.doors_open_out_left);
    }
}
