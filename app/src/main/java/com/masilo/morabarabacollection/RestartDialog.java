package com.masilo.morabarabacollection;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.Objects;

public class RestartDialog extends Dialog {

    private final RestartDialogListener listener;
    private boolean isDismissing = false;
    private String gameType;

    public interface RestartDialogListener {
        void onRestartConfirmed();
    }

    public RestartDialog(Context context, RestartDialogListener listener) {
        super(context);
        this.listener = listener;
        setContentView(R.layout.custom_restart_dialog_layout);
        initializeViews();
    }

    public RestartDialog(Context context, String gameType, RestartDialogListener listener) {
        super(context);
        this.gameType = gameType;
        this.listener = listener;
        setContentView(R.layout.custom_restart_dialog_layout);
        initializeViews();
    }

    private void initializeViews() {
        TextView titleTextView = findViewById(R.id.dialog_title);
        Button yesButton = findViewById(R.id.btn_yes);
        Button noButton = findViewById(R.id.btn_no);
        MaterialButton unlockButton = findViewById(R.id.btn_unlock_next);

        // Customize the dialog's appearance
        Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        // Set click listeners
        if (yesButton != null) {
            yesButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRestartConfirmed();
                }
                dismiss();
            });
        }

        if (noButton != null) {
            noButton.setOnClickListener(v -> dismiss());
        }

        if (unlockButton != null) {
            if (gameType == null) {
                unlockButton.setVisibility(View.GONE);
            } else {
                unlockButton.setVisibility(View.VISIBLE);
                unlockButton.setOnClickListener(v -> {
                    Activity activity = getActivity(getContext());
                    if (activity != null) {
                        AdManager.getInstance().showRewardedAd(activity, rewardItem -> {
                            GameData data = GameData.getInstance();
                            data.unlockNextLevel(gameType);
                            data.save(getContext());
                            Toast.makeText(getContext(), "Next Level Unlocked!", Toast.LENGTH_SHORT).show();
                            
                            // Return to menu
                            Intent intent = new Intent(getContext(), GameStart.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            getContext().startActivity(intent);

                            dismiss();
                            activity.finish();
                        });
                    }
                });
            }
        }

        // Apply animations
        if (titleTextView != null) {
            titleTextView.setText(R.string.confirm_restart);
        }

        // Add entry animation
        setOnShowListener(dialog -> {
            try {
                View decorView = getWindow().getDecorView();
                decorView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dialog_slide_fade_in));
            } catch (Exception ignored) {

            }
        });
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


    @Override
    public void dismiss() {
        if (isDismissing) return;
        isDismissing = true;

        try {
            View decorView = Objects.requireNonNull(getWindow()).getDecorView();
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_slide_fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Use a more reliable way to dismiss
                    new Handler(Looper.getMainLooper()).post(() -> {
                        try {
                            RestartDialog.super.dismiss();
                        } catch (Exception e) {
                            // Fallback if super.dismiss() fails
                        }
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            decorView.startAnimation(anim);

            // Safety timeout: if animation doesn't end in 500ms, dismiss anyway
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isShowing()) {
                    try {
                        RestartDialog.super.dismiss();
                    } catch (Exception ignored) {}
                }
            }, 500);

        } catch (Exception e) {
            // If animation logic fails, just dismiss normally
            RestartDialog.super.dismiss();
        }
    }
}
