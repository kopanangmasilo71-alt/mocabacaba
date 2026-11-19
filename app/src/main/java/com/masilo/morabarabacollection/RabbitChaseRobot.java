package com.masilo.morabarabacollection;

import android.graphics.PointF;
import java.util.*;

/**
 * Enhanced Rabbit Chase AI - Supports all 6 levels with adaptive strategies
 * Level 1: 5 dogs, no move validation
 * Level 2: 4 dogs, no move validation
 * Level 3: 4 dogs, with move validation
 * Level 4: 3 dogs, no move validation
 * Level 5: 3 dogs, with move validation
 * Level 6: 2 dogs, no move validation
 */
public class RabbitChaseRobot {

    private final RabbitPanel panel;
    private Circle[] circles;
    private Piece rabbit;
    private final List<Piece> dogs = new ArrayList<>();
    private static final String BLACK = "B";
    private static final String WHITE = "W";
    private static final String NONE = "N";
    private String currentLevel;
    private int dogCount;

    public RabbitChaseRobot(RabbitPanel panel) {
        this.panel = panel;
        updateGameState();
    }

    public void updateGameState() {
        circles = panel.circleArray;
        dogs.clear();

        // Get current level and dog count
        currentLevel = GameData.getInstance().getRabbit_level();
        dogCount = getDogCountForLevel(currentLevel);

        for (Circle c : circles) {
            if(c == null) continue;
            if (c.getPiece() != null) {
                if (BLACK.equals(c.getPieceType())) rabbit = c.getPiece();
                else if (WHITE.equals(c.getPieceType())) dogs.add(c.getPiece());
            }
        }

   }

    public void makeRabbitMove() {
        updateGameState();
        if (rabbit == null) {
          return;
        }

        Circle current = getPieceCircle(rabbit);
        if (current == null) {
          return;
        }

        List<Circle> moves = getValidMoves(current);

        if (moves.isEmpty()) {
            panel.segment("Rabbit is trapped! Dogs win!");
           return;
        }

        Circle decision = pickBestMove(current, moves);
        if (decision != null) {
            movePiece(rabbit, decision);
       } else {
            // Fallback: choose first available move
            movePiece(rabbit, moves.get(0));
      }
    }

    private void movePiece(Piece p, Circle target) {
        panel.update(p.getMyCircle(), target, p);
        if (panel.animator != null) {
            panel.animator.moveAnimation(new PointF(target.getX(), target.getY()), p);
        }
    }

    private List<Circle> getValidMoves(Circle c) {
        List<Circle> moves = new ArrayList<>();
        for (int n : c.getNeighbour()) {
            if (n < circles.length && circles[n] != null && NONE.equals(circles[n].getPieceType())) {
                moves.add(circles[n]);
            }
        }
        return moves;
    }

    private Circle pickBestMove(Circle current, List<Circle> moves) {
        if (moves.isEmpty()) return null;
        if (moves.size() == 1) return moves.get(0);

        return moves.stream()
                .max(Comparator.comparingInt(m -> evaluateMove(current, m)))
                .orElse(moves.get(0));
    }

    /**
     * Enhanced Scoring Strategy for different levels:
     * - Levels 1-2: More aggressive, focus on mobility
     * - Levels 3-4: Balanced approach
     * - Levels 5-6: Defensive, avoid risks
     */
    private int evaluateMove(Circle from, Circle to) {
        int score = 0;

        // Base mobility score (higher for more open positions)
        score += mobilityScore(to) * getMobilityWeight();

        // Escape potential (bonus for positions with multiple escape routes)
        score += escapePotentialScore(to) * getEscapeWeight();

        // Distance from dogs (prefer positions farther from dogs)
        score += distanceScore(to) * getDistanceWeight();

        // Risk assessment (penalty for dangerous positions)
        score -= convergenceRisk(to, from) * getRiskWeight();

        // Level-specific bonuses
        score += levelSpecificBonus(from, to);

        return score;
    }

    /** More empty neighbors = safer */
    private int mobilityScore(Circle pos) {
        return getValidMoves(pos).size() * 15;
    }

    /** Bonus if multiple branching escape paths exist */
    private int escapePotentialScore(Circle pos) {
        int escape = 0;
        for (Circle c : getValidMoves(pos)) {
            if (getValidMoves(c).size() >= 2) escape += 10;
        }
        return escape;
    }

    /** Increase score based on summed distances from dogs */
    private int distanceScore(Circle pos) {
        int dist = 0;
        for (Piece d : dogs) {
            Circle dc = getPieceCircle(d);
            if(dc == null) continue;
            dist += Math.abs(pos.getIndex() - dc.getIndex());
        }
        return dist;
    }

    /** Identify moves moving toward dogs OR near convergence points */
    private int convergenceRisk(Circle to, Circle from) {
        int risk = 0;
        for (Piece d : dogs) {
            Circle dc = getPieceCircle(d);
            if(dc == null) continue;

            int oldDist = Math.abs(from.getIndex() - dc.getIndex());
            int newDist = Math.abs(to.getIndex() - dc.getIndex());

            if (newDist < oldDist) risk += 20;  // Moving closer to a dog

            // If dog blocks 2+ future escapes → huge risk zone
            if (blocksEscapeRoutes(dc, to)) risk += 30;

            // Additional risk if moving into corner with limited escape
            if (isCornerPosition(to.getIndex()) && getValidMoves(to).size() <= 2) {
                risk += 25;
            }
        }
        return risk;
    }

    private boolean blocksEscapeRoutes(Circle dog, Circle rabbitPos) {
        int blockedRoutes = 0;
        for (Circle m : getValidMoves(rabbitPos)) {
            if (Math.abs(m.getIndex() - dog.getIndex()) <= 1) {
                blockedRoutes++;
            }
        }
        return blockedRoutes >= 2;
    }

    private boolean isCornerPosition(int index) {
        int[] corners = {0, 2, 22, 24};
        for (int corner : corners) {
            if (index == corner) return true;
        }
        return false;
    }

    /** Level-specific strategy adjustments */

    private int getMobilityWeight() {
        switch(currentLevel) {
            case "LEVEL1": return 1; // Focus on mobility in early levels
            case "LEVEL2": return 1;
            case "LEVEL3": return 2; // Higher weight with move validation
            case "LEVEL4": return 1;
            case "LEVEL5": return 2; // Higher weight with move validation
            case "LEVEL6": return 3; // Maximum mobility focus with few dogs
            default: return 1;
        }
    }

    private int getEscapeWeight() {
        switch(currentLevel) {
            case "LEVEL1": return 1;
            case "LEVEL2": return 1;
            case "LEVEL3": return 2; // More important with move validation
            case "LEVEL4": return 1;
            case "LEVEL5": return 2;
            case "LEVEL6": return 3; // Critical with only 2 dogs
            default: return 1;
        }
    }

    private int getDistanceWeight() {
        switch(currentLevel) {
            case "LEVEL1": return 1; // Less important with many dogs
            case "LEVEL2": return 1;
            case "LEVEL3": return 2;
            case "LEVEL4": return 2; // More important with fewer dogs
            case "LEVEL5": return 2;
            case "LEVEL6": return 3; // Very important with only 2 dogs
            default: return 1;
        }
    }

    private int getRiskWeight() {
        switch(currentLevel) {
            case "LEVEL1": return 1; // Can take more risks with many escape routes
            case "LEVEL2": return 1;
            case "LEVEL3": return 2; // Be more cautious with move validation
            case "LEVEL4": return 2;
            case "LEVEL5": return 3; // Very cautious with move validation
            case "LEVEL6": return 2; // Balanced risk with few dogs
            default: return 1;
        }
    }

    private int levelSpecificBonus(Circle from, Circle to) {
        int bonus = 0;

        switch(currentLevel) {
            case "LEVEL1": // 5 dogs - prioritize center control
                if (isCenterPosition(to.getIndex())) bonus += 25;
                break;

            case "LEVEL2": // 4 dogs - balanced approach
                if (isEdgePosition(to.getIndex())) bonus += 15;
                break;

            case "LEVEL3": // 4 dogs with validation - avoid corners
                if (!isCornerPosition(to.getIndex())) bonus += 20;
                break;

            case "LEVEL4": // 3 dogs - aggressive positioning
                if (getValidMoves(to).size() > 2) bonus += 30;
                break;

            case "LEVEL5": // 3 dogs with validation - safe mobility
                if (getValidMoves(to).size() >= 3) bonus += 40;
                break;

            case "LEVEL6": // 2 dogs - maximum mobility and distance
                if (getValidMoves(to).size() >= 4) bonus += 50;
                if (distanceScore(to) > 30) bonus += 30;
                break;
        }

        return bonus;
    }

    private boolean isCenterPosition(int index) {
        int[] centerPositions = {6, 7, 8, 11, 12, 13, 16, 17, 18};
        for (int pos : centerPositions) {
            if (index == pos) return true;
        }
        return false;
    }

    private boolean isEdgePosition(int index) {
        return index % 5 == 0 || index % 5 == 4 || index < 5 || index > 19;
    }

    private int getDogCountForLevel(String level) {
        switch(level) {
            case "LEVEL1": return 5;
            case "LEVEL2": return 4;
            case "LEVEL3": return 4;
            case "LEVEL4": return 3;
            case "LEVEL5": return 3;
            case "LEVEL6": return 2;
            default: return 3;
        }
    }

    private Circle getPieceCircle(Piece p) {
        for (Circle c : circles) {
            if (c != null && c.getPiece() == p) return c;
        }
        return null;
    }

    public boolean isRabbitTrapped() {
        updateGameState();
        Circle rc = getPieceCircle(rabbit);
        if (rc == null) return true;
        return getValidMoves(rc).isEmpty();
    }

    public String getAIStatus() {
        if (rabbit == null) return "AI: Rabbit not found";

        Circle rc = getPieceCircle(rabbit);
        if (rc == null) return "AI: Rabbit position unknown";

        int mobility = getValidMoves(rc).size();
        return String.format("AI: Level %s | Pos: %d | Mobility: %d | Dogs: %d",
                currentLevel.replace("LEVEL", ""), rc.getIndex(), mobility, dogs.size());
    }

    public void start(boolean rabbitTurn) {
        if (rabbitTurn) {
           makeRabbitMove();
        }
    }
}