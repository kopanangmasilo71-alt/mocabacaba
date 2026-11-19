package com.masilo.morabarabacollection;

import androidx.annotation.NonNull;

import java.util.Arrays;

/**
 * A reusable class to manage goal checking (mills) and piece mobility for a board game.
 * It is designed to be independent of the game panel's implementation by using a
 * data provider interface.
 */
public class GoalManager {

    /**
     * An interface that a data provider (like a game panel) must implement to supply
     * the necessary data for goal and move checking.
     */
    public interface GoalDataProvider {
        Circle[] getCircleArray();
        int[][] getPatterns();
        Piece[] getWhitePieces();
        Piece[] getBlackPieces();
        String[][] getSegmentData();
        Circle getWhiteContainer();
        Circle getBlackContainer();
    }

    private final GoalDataProvider dataProvider;
    private final Circle[] circleArray;
    private final Piece[] whitePieces;
    private final Piece[] blackPieces;

    public Piece[] goalPieces = new Piece[3];
    public Piece[] goalPieces1 = new Piece[3];
    public Piece[] storePieces = new Piece[3];
    public Piece[] storePieces1 = new Piece[3];

    public GoalManager(GoalDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.circleArray = dataProvider.getCircleArray();
        this.whitePieces = dataProvider.getWhitePieces();
        this.blackPieces = dataProvider.getBlackPieces();
    }


    public int checkWhiteGoal(int b) {
        storePieces = null;
        int[][] patterns = dataProvider.getPatterns();
        String pieceType = "W";
        int patternIndex = -1;
        boolean whiteGoal = false;
        if (patterns == null) return -1;
        for (int i = 0; i < patterns.length; i++) {
            if (isMatchingPattern(patterns[i], b, pieceType)) {
                whiteGoal = true;
                patternIndex = i;
                break;
            }
        }
        if (whiteGoal) {
            storePieces = getGoalPieces(patterns[patternIndex]);
            return patternIndex;
        }
        return -1;
    }

    public boolean checkWhiteGoal1(int b, int state) {
        storePieces1 = null;
        int[][] patterns = dataProvider.getPatterns();
        String pieceType = "W";
        int patternIndex = -1;
        boolean foundGoal = false;
        if (patterns == null) return false;
        for (int i = state; i < patterns.length; i++) {
            if (isMatchingPattern(patterns[i], b, pieceType)) {
                foundGoal = true;
                patternIndex = i;
                break;
            }
        }
        if (foundGoal) {
            storePieces1 = getGoalPieces(patterns[patternIndex]);
            return true;
        }
        return false;
    }

    public int checkBlackGoal(int b) {
        goalPieces = null;
        int[][] patterns = dataProvider.getPatterns();
        String pieceType = "B";
        int patternIndex = -1;
        boolean blackGoal = false;
        if (patterns == null) return -1;
        for (int i = 0; i < patterns.length; i++) {
            if (isMatchingPattern(patterns[i], b, pieceType)) {
                blackGoal = true;
                patternIndex = i;
                break;
            }
        }
        if (blackGoal) {
            goalPieces = getGoalPieces(patterns[patternIndex]);
            return patternIndex;
        }
        return -1;
    }

    public boolean checkBlackGoal1(int b, int state) {
        goalPieces1 = null;
        int[][] patterns = dataProvider.getPatterns();
        String pieceType = "B";
        int patternIndex = -1;
        boolean foundGoal = false;
        if (patterns == null) return false;
        for (int i = state; i < patterns.length; i++) {
            if (isMatchingPattern(patterns[i], b, pieceType)) {
                foundGoal = true;
                patternIndex = i;
                break;
            }
        }
        if (foundGoal) {
            goalPieces1 = getGoalPieces(patterns[patternIndex]);
            return true;
        }
        return false;
    }

    private boolean isMatchingPattern(@NonNull int[] pattern, int circleIndex, String pieceType) {
        if (circleArray == null || pattern.length < 3 || circleIndex < 0 || circleIndex >= circleArray.length) return false;
        if (pattern[0] >= circleArray.length || pattern[1] >= circleArray.length || pattern[2] >= circleArray.length) return false;

        Circle c0 = circleArray[pattern[0]];
        Circle c1 = circleArray[pattern[1]];
        Circle c2 = circleArray[pattern[2]];
        if (c0 == null || c1 == null || c2 == null) return false;

        return (c0 == circleArray[circleIndex] ||
                c1 == circleArray[circleIndex] ||
                c2 == circleArray[circleIndex]) &&
                pieceType.equals(c0.getPieceType()) &&
                pieceType.equals(c1.getPieceType()) &&
                pieceType.equals(c2.getPieceType());
    }

    private Piece[] getGoalPieces(@NonNull int[] pattern) {
        return new Piece[]{
                circleArray[pattern[0]].getPiece(),
                circleArray[pattern[1]].getPiece(),
                circleArray[pattern[2]].getPiece()
        };
    }

    private boolean hasPieceInContainer(Piece[] pieces, Circle container) {
        if (container == null || pieces == null) return false;
        for (Piece piece : pieces) {
            if (piece != null && piece.isInContainer(container)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkBlackMovable() {
        if (hasPieceInContainer(blackPieces, dataProvider.getBlackContainer())) {
            return true;
        }
        String[][] segmentData = dataProvider.getSegmentData();
        if (segmentData == null) return false;
        for (String[] strings : segmentData) {
            if (Arrays.equals(strings, new String[]{"B", "N"}) || Arrays.equals(strings, new String[]{"N", "B"})) {
                return true;
            }
        }
        return false;
    }

    public boolean checkWhiteMovable() {
        if (hasPieceInContainer(whitePieces, dataProvider.getWhiteContainer())) {
            return true;
        }
        String[][] segmentData = dataProvider.getSegmentData();
        if (segmentData == null) return false;
        for (String[] strings : segmentData) {
            if (Arrays.equals(strings, new String[]{"N", "W"}) || Arrays.equals(strings, new String[]{"W", "N"})) {
                return true;
            }
        }
        return false;
    }
}
