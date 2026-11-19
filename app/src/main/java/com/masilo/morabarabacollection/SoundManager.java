package com.masilo.morabarabacollection;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;

import java.util.HashSet;
import java.util.Set;

public class SoundManager {
    private static SoundManager instance;

    private SoundPool soundPool;
    private final int clickSound;
    private final Set<Integer> loadedSounds = new HashSet<>();
    private ToneGenerator toneGenerator;

    private SoundManager(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attrs)
                .build();

        clickSound = soundPool.load(context, R.raw.click, 1);

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0) {
                synchronized (loadedSounds) {
                    loadedSounds.add(sampleId);
                }
            }
        });
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) instance = new SoundManager(context.getApplicationContext());
        return instance;
    }

    public void playClick() {
        if (soundPool != null && isLoaded(clickSound) && GameData.getInstance().isSoundEffects()) {
            soundPool.play(clickSound, 1f, 1f, 1, 0, 1f);
        }
    }

    /** Light hop for rabbit; lower thud for dogs (procedural until custom SFX are added). */
    public void playChaseStep(boolean rabbit) {
        if (!GameData.getInstance().isSoundEffects()) return;
        ensureToneGenerator();
        if (toneGenerator == null) return;
        try {
            int tone = rabbit ? ToneGenerator.TONE_PROP_BEEP2 : ToneGenerator.TONE_PROP_ACK;
            toneGenerator.startTone(tone, rabbit ? 35 : 45);
        } catch (RuntimeException ignored) {
            // ToneGenerator can fail on some devices when busy
        }
    }

    public void playInvalidMove() {
        if (!GameData.getInstance().isSoundEffects()) return;
        ensureToneGenerator();
        if (toneGenerator == null) return;
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 80);
        } catch (RuntimeException ignored) {
        }
    }

    private void ensureToneGenerator() {
        if (toneGenerator == null) {
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 55);
            } catch (RuntimeException ignored) {
                toneGenerator = null;
            }
        }
    }

    private boolean isLoaded(int soundId) {
        synchronized (loadedSounds) {
            return loadedSounds.contains(soundId);
        }
    }

    public void release() {
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        synchronized (loadedSounds) {
            loadedSounds.clear();
        }
        instance = null;
    }
}
