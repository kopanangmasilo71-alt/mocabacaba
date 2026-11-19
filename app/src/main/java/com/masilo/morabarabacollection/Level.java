package com.masilo.morabarabacollection;

public class Level {
    private final int levelNumber;
    private final String gameType;
    private final boolean isUnlocked;
    private final boolean isCurrent;
    private final int points;
    private final int progress;

    public Level(int levelNumber, String gameType, boolean isUnlocked, boolean isCurrent, int points, int progress) {
        this.levelNumber = levelNumber;
        this.gameType = gameType;
        this.isUnlocked = isUnlocked;
        this.isCurrent = isCurrent;
        this.points = points;
        this.progress = progress;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public String getGameType() {
        return gameType;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public int getPoints() {
        return points;
    }

    public int getProgress() {
        return progress;
    }


}
