package com.masilo.morabarabacollection;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdView;

public class SettingsActivity extends BaseGameActivity {

    private GameData gameData;
    private AdView mAdView;
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        mAdView = findViewById(R.id.adView);
        if (mAdView != null) {
            mAdView.setVisibility(View.GONE); // Hide and do not load to eliminate low-impression requests on short visits
        }

        gameData = GameData.getInstance();
        gameData.load(this);

        setupSettings();
    }

    private void setupSettings() {
        SeekBar animationSpeedSeekBar = findViewById(R.id.animation_speed_seekbar);
        TextView speedLabel = findViewById(R.id.animation_speed_label);
        SwitchMaterial soundSwitch = findViewById(R.id.sound_effects_switch);
        Spinner difficultySpinner = findViewById(R.id.difficulty_spinner);
        Spinner languageSpinner = findViewById(R.id.language_spinner);
        View privacyButton = findViewById(R.id.privacy_options_button);
        View privacyCard = findViewById(R.id.privacy_card);


        // Difficulty Spinner setup
        ArrayAdapter<CharSequence> diffAdapter = ArrayAdapter.createFromResource(this,
                R.array.difficulty_levels, android.R.layout.simple_spinner_item);
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(diffAdapter);

        // Language Spinner setup
        ArrayAdapter<CharSequence> langAdapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(langAdapter);


        // Load existing values
        animationSpeedSeekBar.setProgress(gameData.getAnimationSpeed());
        speedLabel.setText(String.format("%d%%", gameData.getAnimationSpeed()));
        soundSwitch.setChecked(gameData.isSoundEffects());
        difficultySpinner.setSelection(diffAdapter.getPosition(gameData.getDifficultyLevel()));

        // Set Language selection
        String currentLangCode = gameData.getLanguage();
        String[] langCodes = getResources().getStringArray(R.array.language_codes);
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLangCode)) {
                languageSpinner.setSelection(i);
                break;
            }
        }

        // Animation speed label update
        animationSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                speedLabel.setText(String.format("%d%%", progress));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Privacy options
        // We show the button if required, OR if it's potentially needed.
        // Google checks often depend on the RequirementStatus being exactly REQUIRED.
        if (AdManager.getInstance().isPrivacyOptionsRequired()) {
            Log.d(TAG, "Privacy options are required, showing button.");
            privacyCard.setVisibility(View.VISIBLE);
            privacyButton.setOnClickListener(v -> AdManager.getInstance().showPrivacyOptionsForm(this));
        } else {
            Log.d(TAG, "Privacy options not required by SDK. Hiding card.");
            // To pass Play Store audits, we MUST show it if status is REQUIRED.
            // If the user isn't seeing it, it might be because they are in a region where it's NOT_REQUIRED.
            privacyCard.setVisibility(View.GONE);
        }

        findViewById(R.id.save_button).setOnClickListener(v -> {
            gameData.setSoundEffects(soundSwitch.isChecked());
            gameData.setDifficultyLevel(difficultySpinner.getSelectedItem().toString());
            gameData.setAnimationSpeed(animationSpeedSeekBar.getProgress());

            int langIndex = languageSpinner.getSelectedItemPosition();
            String selectedLangCode = getResources().getStringArray(R.array.language_codes)[langIndex];
            gameData.setLanguage(selectedLangCode);

            gameData.save(this);

            // Apply language change
            applyLocale(this, selectedLangCode);

            Toast.makeText(this, getString(R.string.save), Toast.LENGTH_SHORT).show();
            recreate(); // Restart activity to apply language immediately
        });

        findViewById(R.id.reset_button).setOnClickListener(v -> {
            gameData.reset();
            gameData.save(this);
            setupSettings();
            Toast.makeText(this, "Settings Reset", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.back_button).setOnClickListener(v -> finish());
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
