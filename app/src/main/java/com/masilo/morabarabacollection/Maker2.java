package com.masilo.morabarabacollection;

import android.os.Handler;

public class Maker2 {
    private final GamePanel2 myPanel;

    public Maker2(GamePanel2 myPanel) {
        this.myPanel = myPanel;
    }

    // Called after every move to check for game over or switch turns
    public void checkGameOver() {
        if (isGameOver()) {
            handleGameOver();
        } else {
            checkNextTurn();
        }
    }

    private boolean isGameOver() {
        return myPanel.whitePieceKilledCount >= 10 || myPanel.blackPieceKilledCount >= 10;
    }

    private void handleGameOver() {
        if (myPanel.whitePieceKilledCount >= 10) {
            myPanel.blackIsWinner = true;
        } else {
            myPanel.whiteIsWinner = true;
        }
        myPanel.gameOver(myPanel.blackIsWinner ? "Black" : "White");
    }

    private void checkNextTurn() {
        boolean whiteCanMove = canPlayerMove(myPanel.getWhitePieces());
        boolean blackCanMove = canPlayerMove(myPanel.getBlackPieces());

        if (!whiteCanMove || !blackCanMove) {
            myPanel.blackIsWinner = !whiteCanMove;
            myPanel.whiteIsWinner = !blackCanMove;
            myPanel.gameOver(myPanel.blackIsWinner ? "Black" : "White");
            return;
        }

        // Switch turns
        if (myPanel.gameClickHandler.gameInfoActivity.whitePlayerTurn) {
            switchTurnToBlack();
        } else {
            switchTurnToWhite();
        }
    }

    // Checks if the given set of pieces has any valid moves
    private boolean canPlayerMove(Piece[] pieces) {
        // If pieces are still in the container, the player can move.
        if ((pieces[0].isBlack() && myPanel.getBlackContainerCount() > 0) || 
            (!pieces[0].isBlack() && myPanel.getWhiteContainerCount() > 0)) {
            return true;
        }

        // Check pieces on the board for any possible move
        for (Piece piece : pieces) {
            if (piece.getMyCircle() != null && piece.getMyCircle().getNeighbour() != null) {
                for (int neighborIndex : piece.getMyCircle().getNeighbour()) {
                    if (myPanel.circleArray[neighborIndex].getPiece() == null) {
                        return true; // Found a valid move
                    }
                }
            }
        }
        return false; // No valid moves found
    }

    public void switchTurnToWhite() {
        myPanel.gameClickHandler.gameInfoActivity.blackPlayerTurn = false;
        myPanel.gameClickHandler.gameInfoActivity.whitePlayerTurn = true;
        myPanel.panelUpdater.updateBlock();
    }

    public void switchTurnToBlack() {
        myPanel.gameClickHandler.gameInfoActivity.whitePlayerTurn = false;
        myPanel.gameClickHandler.gameInfoActivity.blackPlayerTurn = true;
        myPanel.panelUpdater.updateBlock();
        
        // Trigger robot turn
        new Handler().postDelayed(() -> {
            if (myPanel.getRobot1() != null) {
                myPanel.getRobot1().start(false);
            }
        }, 1000);
    }
}
