package com.masilo.morabarabacollection;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.xml.KonfettiView;

public class EndGameDialog extends Dialog {
    private final String winnerName;
    private final int score;
    private final int moves;
    private final boolean showKonfetti;
    private String gameType = null;
    private final Class<?> destinationClass = GameStart.class;

    public EndGameDialog(Context context, String winnerName, int score, int moves, boolean showKonfetti) {
        super(context);
        this.winnerName = winnerName;
        this.score = score;
        this.moves = moves;
        this.showKonfetti = showKonfetti;
        commonInit();
    }

    public EndGameDialog(Context context, String winnerName, int score, int moves, boolean showKonfetti, String gameType) {
        super(context);
        this.winnerName = winnerName;
        this.score = score;
        this.moves = moves;
        this.showKonfetti = showKonfetti;
        this.gameType = gameType;
        commonInit();
    }

    private void commonInit() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        setContentView(R.layout.end_game_layout);
        initializeViews();
        makeDialogBigger();
    }

    private void initializeViews() {
        TextView winnerNameTextView = findViewById(R.id.winner_name_text_view);
        TextView scoreTextView = findViewById(R.id.score_text_view);
        TextView movesTextView = findViewById(R.id.moves_text_view);
        Button restartButton = findViewById(R.id.restart_button);
        Button exitButton = findViewById(R.id.exit_game_button);
        MaterialButton unlockButton = findViewById(R.id.btn_unlock_next);
        MaterialButton bonusButton = findViewById(R.id.btn_claim_bonus);
        KonfettiView konfettiView = findViewById(R.id.konfettiView);

        winnerNameTextView.setText(winnerName);
        scoreTextView.setText(String.format(Locale.getDefault(), "%d", score));
        movesTextView.setText(String.format(Locale.getDefault(), "%d", moves));

        // Always hide the manual claim button as ads are now automatic
        if (bonusButton != null) bonusButton.setVisibility(View.GONE);

        restartButton.setOnClickListener(v -> handleExitOrRestart(true));
        exitButton.setOnClickListener(v -> handleExitOrRestart(false));

        if (gameType != null) {
            if (unlockButton != null) {
                unlockButton.setVisibility(View.VISIBLE);
                unlockButton.setOnClickListener(v -> {
                    Activity activity = getActivity(getContext());
                    if (activity != null) {
                        AdManager.getInstance().showRewardedAd(activity, rewardItem -> {
                            GameData data = GameData.getInstance();
                            data.unlockNextLevel(gameType);
                            data.save(getContext());
                            Toast.makeText(getContext(), "Next Level Unlocked!", Toast.LENGTH_SHORT).show();
                            returnToMain();
                        });
                    }
                });
            }
        } else {
            if (unlockButton != null) unlockButton.setVisibility(View.GONE);
        }

        applyWinnerNameShader(winnerNameTextView);

        if (konfettiView != null && showKonfetti) {
            konfettiView.start(
                    new PartyFactory(new Emitter(100L, TimeUnit.MILLISECONDS).max(100))
                            .spread(360)
                            .shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE))
                            .colors(Arrays.asList(0xfce18a, 0xff726d, 0xf4306d, 0xb48def))
                            .setSpeedBetween(0f, 30f)
                            .position(new Position.Relative(0.5, 0.3))
                            .build()
            );
        }
    }

    private void handleExitOrRestart(boolean isRestart) {
        Activity activity = getActivity(getContext());
        // For games without levels (gameType == null), trigger ad automatically on exit/restart
        if (activity != null && gameType == null && AdManager.getInstance().isRewardedInterstitialLoaded()) {
            AdManager.getInstance().showRewardedInterstitialAd(
                activity, 
                rewardItem -> {
                    // Reward logic
                    Toast.makeText(getContext(), "Bonus XP Earned!", Toast.LENGTH_SHORT).show();
                },
                () -> {
                    // Completion logic (runs when ad is closed, skipped, or failed)
                    if (isRestart) {
                        restartGame();
                    } else {
                        returnToMain();
                    }
                }
            );
        } else {
            // Standard path if no ad is available or it's a level game
            if (isRestart) {
                restartGame();
            } else {
                returnToMain();
            }
        }
    }

    private void restartGame() {
        returnToMain();
    }

    private void returnToMain() {
        Activity activity = getActivity(getContext());
        if (activity != null) {
            Intent intent = new Intent(getContext(), destinationClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            dismiss();
            activity.finish();
        } else {
            dismiss();
        }
    }

    private Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private void applyWinnerNameShader(TextView winnerNameTextView) {
        String text = winnerNameTextView.getText().toString();
        float textWidth = winnerNameTextView.getPaint().measureText(text);
        float textSize = winnerNameTextView.getTextSize();
        
        if (textWidth <= 0) textWidth = 1.0f;
        if (textSize <= 0) textSize = 1.0f;

        LinearGradient shader = new LinearGradient(
                0, 0, textWidth, textSize,
                new int[]{
                        0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFA500,
                        0xFF800080, 0xFF008080, 0xFF808000, 0xFF800000
                },
                null,
                Shader.TileMode.CLAMP);
        winnerNameTextView.getPaint().setShader(shader);
        winnerNameTextView.invalidate();
    }

    private void makeDialogBigger() {
        Window window = getWindow();
        if (window != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.getDefaultDisplay().getMetrics(displayMetrics);

                int dialogWidth = (int) (displayMetrics.widthPixels * 0.85);
                int dialogHeight = WindowManager.LayoutParams.WRAP_CONTENT;

                window.setLayout(dialogWidth, dialogHeight);
            }
        }
    }
}
