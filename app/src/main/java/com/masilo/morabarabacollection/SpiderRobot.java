package com.masilo.morabarabacollection;

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpiderRobot {
    private final SpiderPanel panel;
    private final Random random = new Random();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public SpiderRobot(SpiderPanel panel) {
        this.panel = panel;
    }

    public void triggerMove() {
        handler.postDelayed(() -> {
            if (panel.getCurrentPhase() == SpiderPanel.GamePhase.CAPTURING_OPPONENT) {
                killPiece();
            } else if (panel.getCurrentPhase() == SpiderPanel.GamePhase.SELECTING || panel.getCurrentPhase() == SpiderPanel.GamePhase.MOVING) {
                performBestMove();
            }
        }, 1000);
    }

    private void killPiece() {
        Piece[] whitePieces = panel.getWhitePieces();
        List<Piece> killable = new ArrayList<>();
        for (Piece p : whitePieces) {
            if (p != null && !panel.isInGraveyard(p)) {
                killable.add(p);
            }
        }
        if (!killable.isEmpty()) {
            panel.killPiece(killable.get(random.nextInt(killable.size())));
        }
    }

    private void performBestMove() {
        // 1. Placement Phase: Try to place from container if available
        Piece pieceInContainer = null;
        for (Piece p : panel.getBlackPieces()) {
            if (p.getMyCircle() == panel.getBlackContainer()) {
                pieceInContainer = p;
                break;
            }
        }

        if (pieceInContainer != null) {
            Circle target = findBestCircleForPlacement();
            if (target != null) {
                panel.robotExecuteMove(pieceInContainer, target);
                return;
            }
        }

        // 2. Movement Phase: Try to move a piece on the board
        List<Move> validMoves = new ArrayList<>();
        for (Piece p : panel.getBlackPieces()) {
            if (p.getMyCircle() != panel.getBlackContainer() && !panel.isInGraveyard(p)) {
                for (Circle c : panel.getCircleArray()) {
                    if (panel.isValidMove(p, c)) {
                        validMoves.add(new Move(p, c));
                    }
                }
            }
        }

        if (!validMoves.isEmpty()) {
            Move bestMove = findBestMove(validMoves);
            panel.robotExecuteMove(bestMove.piece, bestMove.target);
        }
    }

    private Circle findBestCircleForPlacement() {
        // Priority 1: Form a mill
        Circle target = findMillFormingCircle("B");
        if (target != null) return target;
        
        // Priority 2: Block opponent mill
        target = findMillFormingCircle("W");
        if (target != null) return target;

        // Priority 3: Random empty circle
        List<Circle> emptyCircles = new ArrayList<>();
        for (Circle c : panel.getCircleArray()) {
            if (c.getPiece() == null) emptyCircles.add(c);
        }
        if (!emptyCircles.isEmpty()) return emptyCircles.get(random.nextInt(emptyCircles.size()));
        
        return null;
    }

    private Circle findMillFormingCircle(String playerTag) {
        for (int[] pattern : panel.getPatterns()) {
            int count = 0;
            int emptyIndex = -1;
            for (int i : pattern) {
                Circle c = panel.getCircleArray()[i];
                if (playerTag.equals(c.getPieceType())) {
                    count++;
                } else if (c.getPiece() == null) {
                    emptyIndex = i;
                }
            }
            if (count == 2 && emptyIndex != -1) {
                return panel.getCircleArray()[emptyIndex];
            }
        }
        return null;
    }

    private Move findBestMove(List<Move> moves) {
        // Try to find a move that forms a mill
        for (Move move : moves) {
            if (willFormMill(move)) return move;
        }
        return moves.get(random.nextInt(moves.size()));
    }

    private boolean willFormMill(Move move) {
        Circle oldCircle = move.piece.getMyCircle();
        Circle target = move.target;
        
        String oldTag = target.getPieceType();
        Piece oldPiece = target.getPiece();
        
        // Simulating move
        target.setMyPiece(move.piece, "B");
        if (oldCircle != null) oldCircle.setPieceType("N");
        
        boolean formed = false;
        for (int[] pattern : panel.getPatterns()) {
            int targetIndex = -1;
            for(int i=0; i<panel.getCircleArray().length; i++) {
                if(panel.getCircleArray()[i] == target) { targetIndex = i; break; }
            }
            boolean involvesTarget = false;
            for(int i : pattern) if(i == targetIndex) involvesTarget = true;
            
            if (involvesTarget) {
                if ("B".equals(panel.getCircleArray()[pattern[0]].getPieceType()) &&
                    "B".equals(panel.getCircleArray()[pattern[1]].getPieceType()) &&
                    "B".equals(panel.getCircleArray()[pattern[2]].getPieceType())) {
                    formed = true;
                    break;
                }
            }
        }
        
        // Revert
        target.setMyPiece(oldPiece, oldTag);
        if (oldCircle != null) oldCircle.setPieceType("B");
        
        return formed;
    }

    private static class Move {
        Piece piece;
        Circle target;
        Move(Piece p, Circle c) { this.piece = p; this.target = c; }
    }
}
