package com.masilo.morabarabacollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlquerqueRobot {
    private final AlquerquePanel panel;
    private final Random random = new Random();
    public AlquerqueRobot(AlquerquePanel panel) {
        this.panel = panel;
    }
    public void makeMove() {
        // Placement phase
        if (panel.isPlacementPhase) {
            placePiece();
            return;
        }

        // Movement phase
        if (!panel.isRemovePiecePhase) {
            movePiece();
        }
    }
    private void placePiece() {
        List<Circle> emptyCircles = new ArrayList<>();
        for (Circle circle : panel.circleArray) {
            if (circle != null && circle.getPiece() == null) {
                emptyCircles.add(circle);
            }
        }

        if (!emptyCircles.isEmpty()) {
            Circle randomCircle = emptyCircles.get(random.nextInt(emptyCircles.size()));
            panel.circleClicked(randomCircle);
        }
    }
    private void movePiece() {
        List<Piece> robotPieces = new ArrayList<>();
        for (Piece piece : panel.player2Pieces) {
            if (piece != null && piece.getMyCircle() != null) {
                robotPieces.add(piece);
            }
        }

        if (robotPieces.isEmpty()) {
            return; // No pieces to move
        }

        // Find a piece that can move
        for (Piece piece : robotPieces) {
            Circle currentCircle = piece.getMyCircle();
            if (currentCircle != null && currentCircle.getNeighbour() != null) {
                for (int neighborIndex : currentCircle.getNeighbour()) {
                    Circle neighbor = panel.circleArray[neighborIndex];
                    if (neighbor != null && neighbor.getPiece() == null) {
                        // Found a valid move
                        panel.circleClicked(currentCircle);
                        panel.circleClicked(neighbor);
                        return;
                    }
                }
            }
        }
    }
    public void removePlayerPiece() {
        List<Piece> playerPieces = new ArrayList<>();
        for (Piece piece : panel.player1Pieces) {
            if (piece != null && piece.getMyCircle() != null && !panel.isPieceInMill(piece)) {
                playerPieces.add(piece);
            }
        }

        if (!playerPieces.isEmpty()) {
            Piece pieceToRemove = playerPieces.get(random.nextInt(playerPieces.size()));
            panel.circleClicked(pieceToRemove.getMyCircle());
        }
    }
}
