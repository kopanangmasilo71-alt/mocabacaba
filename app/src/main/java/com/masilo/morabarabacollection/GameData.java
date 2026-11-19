package com.masilo.morabarabacollection;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * GameData
 * Responsible for storing and managing all global game settings and progress data.
 * Implements Singleton pattern and uses SharedPreferences for persistence.
 */
public class GameData {

    private static GameData instance;
    private static final String PREF_NAME = "game_data_prefs";

    // ==============================
    // Game Progress
    // ==============================
    private String level = "LEVEL1";
    private int highScore = 0;
    private String lastPlayed = "";
    //-------------------
    //RABBIT
    //---------------------------------------------------
    private String rabbit_level = "LEVEL1";
    private String rabbit_1_level = "LEVEL1"; // New level tracking for RabbitPannel1
    
    // New counters for other games
    private int spiderGames = 0;
    private int normalGames = 0;
    private int malulaGames = 0;
    private int rabbitMultiplayerGames = 0;
    private int variantGames = 0;

    // ==============================
    // Settings
    // ==============================
    private boolean soundEffects = true;
    private String difficultyLevel = "Normal";
    private String language = "en";


    // ==============================
    // Performance
    // ==============================
    private int animationSpeed = 100; // percentage (50–200)

    // ==============================
    // Constructor
    // ==============================
    private GameData() {}

    // ==============================
    // Singleton Accessor
    // ==============================
    public static synchronized GameData getInstance() {
        if (instance == null) {
            instance = new GameData();
        }
        return instance;
    }

    // ==============================
    // Getters and Setters
    // ==============================

    // Level
    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }

    // Last Played
    public String getLastPlayed() {
        return lastPlayed;
    }
    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    // Sound Effects
    public boolean isSoundEffects() {
        return soundEffects;
    }
    public void setSoundEffects(boolean soundEffects) {
        this.soundEffects = soundEffects;
    }

    // Difficulty Level
    public String getDifficultyLevel() {
        return difficultyLevel;
    }
    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    // Language
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }

    // Animation Speed
    public int getAnimationSpeed() {
        return animationSpeed;
    }
    public void setAnimationSpeed(int animationSpeed) {
        if (animationSpeed < 50) animationSpeed = 50;
        if (animationSpeed > 200) animationSpeed = 200;
        this.animationSpeed = animationSpeed;
    }
    
    // New Getters/Setters for games
    public int getSpiderGames() { return spiderGames; }
    public void incrementSpiderGames() { this.spiderGames++; }

    public int getNormalGames() { return normalGames; }
    public void incrementNormalGames() { this.normalGames++; }

    public int getMalulaGames() { return malulaGames; }
    public void incrementMalulaGames() { this.malulaGames++; }

    public int getRabbitMultiplayerGames() { return rabbitMultiplayerGames; }
    public void incrementRabbitMultiplayerGames() { this.rabbitMultiplayerGames++; }

    public int getVariantGames() { return variantGames; }
    public void incrementVariantGames() { this.variantGames++; }

    // ==============================
    // Level Unlocking Logic
    // ==============================

    public void unlockNextLevel(String gameType) {
        if (gameType == null) return;
        switch (gameType) {
            case "NORMAL":
                level = getNextLevelString(level);
                break;
            case "RABBIT":
                rabbit_level = getNextLevelString(rabbit_level);
                break;
            case "RABBIT1":
                rabbit_1_level = getNextLevelString(rabbit_1_level);
                break;
        }
    }

    private String getNextLevelString(String currentLevel) {
        if (currentLevel == null || !currentLevel.startsWith("LEVEL")) return "LEVEL1";
        try {
            int currentNum = Integer.parseInt(currentLevel.substring(5));
            return "LEVEL" + (currentNum + 1);
        } catch (NumberFormatException e) {
            return "LEVEL1";
        }
    }

    // ==============================
    // Persistence
    // ==============================

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("level", level);
        editor.putString("rabbit_level", rabbit_level);
        editor.putString("rabbit_1_level", rabbit_1_level);
        editor.putString("lastPlayed", lastPlayed);
        editor.putInt("highScore", highScore);
        editor.putBoolean("soundEffects", soundEffects);
        editor.putString("difficultyLevel", difficultyLevel);
        editor.putString("language", language);
        editor.putInt("animationSpeed", animationSpeed);
        
        editor.putInt("spiderGames", spiderGames);
        editor.putInt("normalGames", normalGames);
        editor.putInt("malulaGames", malulaGames);
        editor.putInt("rabbitMultiplayerGames", rabbitMultiplayerGames);
        editor.putInt("variantGames", variantGames);

        editor.apply();
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        rabbit_level = prefs.getString("rabbit_level", "LEVEL1");
        rabbit_1_level = prefs.getString("rabbit_1_level", "LEVEL1");
        level = prefs.getString("level", "LEVEL1");
        lastPlayed = prefs.getString("lastPlayed", "");
        highScore = prefs.getInt("highScore", 0);
        soundEffects = prefs.getBoolean("soundEffects", true);
        difficultyLevel = prefs.getString("difficultyLevel", "Normal");
        language = prefs.getString("language", "en");
        animationSpeed = prefs.getInt("animationSpeed", 100);
        
        spiderGames = prefs.getInt("spiderGames", 0);
        normalGames = prefs.getInt("normalGames", 0);
        malulaGames = prefs.getInt("malulaGames", 0);
        rabbitMultiplayerGames = prefs.getInt("rabbitMultiplayerGames", 0);
        variantGames = prefs.getInt("variantGames", 0);
    }

    // ==============================
    // Reset to Defaults (ALL)
    // ==============================
    public void reset() {
        level = "LEVEL1";
        rabbit_level = "LEVEL1";
        rabbit_1_level = "LEVEL1";
        lastPlayed = "";
        highScore = 0;
        difficultyLevel = "Normal";
        language = "en";
        animationSpeed = 100;
        
        spiderGames = 0;
        normalGames = 0;
        malulaGames = 0;
        rabbitMultiplayerGames = 0;
        variantGames = 0;
    }

    public String getRabbit_level() {
        return rabbit_level;
    }

    public void setRabbit_level(String rabbit_level) {
        this.rabbit_level = rabbit_level;
    }

    public String getRabbit_1_level() {
        return rabbit_1_level;
    }

    public void setRabbit_1_level(String rabbit_1_level) {
        this.rabbit_1_level = rabbit_1_level;
    }
}
