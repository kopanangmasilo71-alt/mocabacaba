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
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class ExitDialog extends Dialog {

    private final ExitDialogListener listener;
    private boolean isDismissing = false;
    private String gameType;

    public interface ExitDialogListener {
        void onExitConfirmed();
    }

    public ExitDialog(Context context, ExitDialogListener listener) {
        super(context);
        this.listener = listener;
        setContentView(R.layout.custom_exit_dialog_layout);
        initializeViews();
    }

    public ExitDialog(Context context, String gameType, ExitDialogListener listener) {
        super(context);
        this.gameType = gameType;
        this.listener = listener;
        setContentView(R.layout.custom_exit_dialog_layout);
        initializeViews();
    }

    private void initializeViews() {
        findViewById(R.id.dialog_message);
        Button yesButton = findViewById(R.id.btn_yes);
        Button noButton = findViewById(R.id.btn_no);
        MaterialButton unlockButton = findViewById(R.id.btn_unlock_next);

        // Customize the dialog's appearance
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Set click listeners for the buttons
        yesButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExitConfirmed();
            }
            dismiss();
        });
        noButton.setOnClickListener(v -> dismiss());

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
                            
                            // Exit the activity and return to the game selection screen
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

        // Add entry animation
        setOnShowListener(dialog -> {
            if (getWindow() != null) {
                View decorView = getWindow().getDecorView();
                decorView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.dialog_slide_fade_in));
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

        if (getWindow() != null) {
            View decorView = getWindow().getDecorView();
            Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.dialog_slide_fade_out);
            decorView.startAnimation(anim);
            
            // Dismiss after animation duration (250ms)
            new Handler(Looper.getMainLooper()).postDelayed(super::dismiss, 250);
        } else {
            super.dismiss();
        }
    }
}
