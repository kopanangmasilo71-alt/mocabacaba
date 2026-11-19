package com.masilo.morabarabacollection;

import android.graphics.PointF;

/**
 * Updates the game state for the fused extended board.
 * Simplified for Rabbit game (no mills needed).
 */
public class PanelUpdater1 {

    public Circle[] circleArray;
    public Piece[] blackPieces;
    public Piece[] whitePieces;

    public PanelUpdater1(Piece[] blackPieces, Piece[] whitePieces, Circle[] circleArray) {
        this.blackPieces = blackPieces;
        this.whitePieces = whitePieces;
        this.circleArray = circleArray;
    }

    public void start(Circle previous, Circle target, Piece moving) {
        // Update positions
        if (previous != null) {
            previous.resetPiece();
        }
        if (target != null) {
            target.setMyPiece(moving, moving.isBlack() ? "B" : "W");
            moving.setMyCircle(target);
            moving.setPosition(new PointF(target.getX(), target.getY()));
        }
    }
}
