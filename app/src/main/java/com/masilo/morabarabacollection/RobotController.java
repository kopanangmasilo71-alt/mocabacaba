package com.masilo.morabarabacollection;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Robot Controller - Handles game logic for the robot and punishes missed mills.
 */
public class RobotController {
    private final GamePanel1 gamePanel;
    private final GameClickHandler clickHandler;
    private final Robot6 robot6;
    private final Robot5 robot5;
    private final Robot4 robot4;
    private final Robot3 robot3;
    private final Robot2 robot2;
    private final Robot1 robot1;

    // Captured BEFORE human moves to ensure we detect missed opportunities accurately
    private Circle preMovePlacementMill = null;
    private Circle preMoveProgressEmpty = null;
    private Circle preMoveProgressWhite = null;

    public Circle[] danger;

    public RobotController(GamePanel1 gamePanel, GameClickHandler clickHandler) {
        this.gamePanel = gamePanel;
        this.clickHandler = clickHandler;
        this.robot6 = gamePanel.getRobot6();
        this.robot5 = gamePanel.getRobot5();
        this.robot4 = gamePanel.getRobot4();
        this.robot3 = gamePanel.getRobot3();
        this.robot2 = gamePanel.getRobot2();
        this.robot1 = gamePanel.getRobot1();
    }

    /**
     * Captures mill opportunities BEFORE the human moves.
     * This is crucial for recognizing missed mills after the move is completed.
     */
    public void set() {
        // Capture potential mill for placement mode
        preMovePlacementMill = getBasicDangerousCircle();
        
        // Capture potential mill for progress mode
        Circle[] d = findDangerousPatternWithExternalWhite();
        if (d != null) {
            preMoveProgressEmpty = d[0];
            preMoveProgressWhite = d[1];
        } else {
            preMoveProgressEmpty = null;
            preMoveProgressWhite = null;
        }
    }

    public void startRobotControl() {
        setDanger(); // Update 'danger' for the Robot's internal AI logic

        Piece lastMoved = clickHandler.getClickedPiece();
        if (lastMoved == null) {
            helper();
            return;
        }

        // Check if the piece moved is now part of a mill
        boolean inMill = isPieceInMill(lastMoved);
        
        // 'are' flag: true if the human placed/moved OUTSIDE the dangerous pattern
        boolean notInSamePattern = false;
        if (preMovePlacementMill != null) {
            notInSamePattern = areCirclesNotInSameOrDangerousPattern(preMovePlacementMill, lastMoved.getMyCircle());
        }

        mill(inMill, notInSamePattern);
        helper();
    }

    /**
     * Core logic to punish human for missing mill opportunities.
     */
    public void mill(boolean inMill, boolean are) {
        // If human successfully made a mill, no punishment needed.
        if (inMill) {
            clearPreStates();
            return;
        }

        boolean isPlacement = clickHandler.isGameOnStart();
        if (isPlacement) {
            // Placement Mode: Punish if they had a mill spot but placed elsewhere.
            if (preMovePlacementMill != null && are) {
                handleMissedMillOpportunityStart();
            }
        } else {
            // Progress Mode: Punish if they moved the specific piece that could have made a mill elsewhere.
            if (preMoveProgressEmpty != null && preMoveProgressWhite != null) {
                Circle movedFrom = clickHandler.whereTheCircleWas;
                Circle movedTo = clickHandler.getClickedPiece().getMyCircle();
                
                // If the piece that COULD have moved into the mill spot was moved, but not to that spot.
                if (movedFrom == preMoveProgressWhite && movedTo != preMoveProgressEmpty) {
                    handleMissedMillOpportunity1(preMoveProgressEmpty, preMoveProgressWhite);
                }
            }
        }

        clearPreStates();
        clickHandler.resetMoveState();
    }

    private void clearPreStates() {
        preMovePlacementMill = null;
        preMoveProgressEmpty = null;
        preMoveProgressWhite = null;
    }

    private void helper() {
        new Handler().postDelayed(() -> {
            String level = gamePanel.getLevel();
            switch (level) {
                case "LEVEL2": robot2.start(false); break;
                case "LEVEL3": robot3.start(false); break;
                case "LEVEL4": robot4.start(false); break;
                case "LEVEL5": robot5.start(false); break;
                case "LEVEL6": robot6.start(false); break;
                default: robot1.start(false); break;
            }
        }, 1000);
    }

    public void setDanger() {
        danger = findDangerousPatternWithExternalWhite();
    }

    /**
     * Scans patterns for 2 white pieces and 1 empty spot reachable by another white piece.
     */
    public Circle[] findDangerousPatternWithExternalWhite() {
        for (int[] pattern : gamePanel.getPatterns()) {
            Circle emptyCircle = null;
            List<Circle> whitesInPattern = new ArrayList<>();
            for (int idx : pattern) {
                Circle c = gamePanel.circleArray[idx];
                if ("W".equals(c.getPieceType())) whitesInPattern.add(c);
                else if ("N".equals(c.getPieceType())) emptyCircle = c;
            }

            if (whitesInPattern.size() == 2 && emptyCircle != null) {
                int[] neighbors = emptyCircle.getNeighbour();
                if (neighbors != null) {
                    for (int nIdx : neighbors) {
                        if (isInPattern(nIdx, pattern)) continue;
                        Circle neighbor = gamePanel.circleArray[nIdx];
                        if ("W".equals(neighbor.getPieceType())) {
                            return new Circle[]{emptyCircle, neighbor};
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isInPattern(int idx, int[] pattern) {
        for (int i : pattern) if (i == idx) return true;
        return false;
    }

    public boolean areCirclesNotInSameOrDangerousPattern(Circle circleA, Circle circleB) {
        if (circleA == null || circleB == null) return true;
        for (int[] pattern : gamePanel.getPatterns()) {
            boolean hasA = false, hasB = false;
            int whiteCount = 0;
            for (int idx : pattern) {
                Circle c = gamePanel.circleArray[idx];
                if (c == circleA) hasA = true;
                if (c == circleB) hasB = true;
                if ("W".equals(c.getPieceType())) whiteCount++;
            }
            // If they are in the same pattern, check if it was dangerous (2 whites)
            if (hasA && hasB) return whiteCount < 2;
        }
        return true;
    }

    private boolean isPieceInMill(Piece piece) {
        if (piece == null) return false;
        Circle current = piece.getMyCircle();
        if (current == null) return false;
        String type = piece.isBlack() ? "B" : "W";
        int idx = current.getIndex();
        for (int[] pattern : gamePanel.getPatterns()) {
            boolean contains = false;
            for (int i : pattern) if (i == idx) { contains = true; break; }
            if (contains) {
                boolean match = true;
                for (int i : pattern) if (!gamePanel.circleArray[i].getPieceType().equals(type)) { match = false; break; }
                if (match) return true;
            }
        }
        return false;
    }

    private void handleMissedMillOpportunity1(Circle empty, Circle moving) {
        clickHandler.reverseLastHumanMove();
        clickHandler.forceMillMove2(empty, moving);
        applyMillPenalty(clickHandler.checkDoubleMill(empty));
    }

    private void handleMissedMillOpportunityStart() {
        clickHandler.reverseLastHumanMove();
        if (preMovePlacementMill != null) {
            clickHandler.forceMillMove1(preMovePlacementMill);
            applyMillPenalty(clickHandler.checkDoubleMill(preMovePlacementMill));
        }
    }

    private Circle getBasicDangerousCircle() {
        for (int[] pattern : gamePanel.getPatterns()) {
            int whiteCount = 0;
            int emptyIdx = -1;
            for (int idx : pattern) {
                String type = gamePanel.circleArray[idx].getPieceType();
                if ("W".equals(type)) whiteCount++;
                else if ("N".equals(type)) emptyIdx = idx;
            }
            if (whiteCount == 2 && emptyIdx != -1) return gamePanel.circleArray[emptyIdx];
        }
        return null;
    }

    private void applyMillPenalty(boolean isDoubleMill) {
        new Handler().postDelayed(this::executeRobotKill, 1000);
        if (isDoubleMill) new Handler().postDelayed(this::executeRobotKill, 2500);
    }

    public void executeRobotKill() {
        String level = gamePanel.getLevel();
        switch (level) {
            case "LEVEL2": robot2.start(true); break;
            case "LEVEL3": robot3.start(true); break;
            case "LEVEL4": robot4.start(true); break;
            case "LEVEL5": robot5.start(true); break;
            case "LEVEL6": robot6.start(true); break;
            default: robot1.start(true); break;
        }
    }


}
