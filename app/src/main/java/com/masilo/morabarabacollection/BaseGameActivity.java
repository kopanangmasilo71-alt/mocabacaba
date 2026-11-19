package com.masilo.morabarabacollection;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * Base activity for all game screens. Handles global settings like language and sound.
 */
public abstract class BaseGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply locale before super.onCreate to ensure UI is inflated with correct language
        GameData gameData = GameData.getInstance();
        gameData.load(this);
        applyLocale(this, gameData.getLanguage());
        
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-load and re-apply in case settings changed
        GameData.getInstance().load(this);
        applyLocale(this, GameData.getInstance().getLanguage());

        // Preload full-screen ads only for actual gameplay screens to minimize wasted requests
        if (!(this instanceof StartActivity || this instanceof GameStart || 
              this instanceof SettingsActivity || this instanceof ScoreBoardActivity)) {
            AdManager.getInstance().loadRewardedAd(this);
            AdManager.getInstance().loadRewardedInterstitialAd(this);
        }
    }

    /**
     * Applies the specified language locale to the context.
     */
    protected void applyLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        context.createConfigurationContext(config);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    protected void playClickSound() {
        SoundManager.getInstance(this).playClick();
    }

    /**
     * Shows a themed information dialog with a custom layout.
     */
    protected void showGameInfoDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_info, null);
        builder.setView(view);

        TextView titleView = view.findViewById(R.id.dialog_title);
        TextView messageView = view.findViewById(R.id.dialog_message);
        View closeBtn = view.findViewById(R.id.close_button);

        if (titleView != null) titleView.setText(title);
        if (messageView != null) messageView.setText(message);

        final AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if (closeBtn != null) {
            closeBtn.setOnClickListener(v -> {
                playClickSound();
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}
