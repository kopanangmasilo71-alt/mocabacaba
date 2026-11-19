package com.masilo.morabarabacollection;

public class Maker1 {
    private final NormalPanel myPanel;

    public Maker1(NormalPanel myPanel) {
        this.myPanel = myPanel;
    }

    // Called after every move
    public void checkGameOver(boolean isRobot) {
        if (isGameOver()) {
            handleGameOver();
        } else {
            checkNextTurn(isRobot);
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
        myPanel.gameOver(myPanel.blackIsWinner ? "ROBOT" : "YOU");
    }

    private void checkNextTurn(boolean isRobot) {
        boolean whiteCanMove = myPanel.goalManager.checkWhiteMovable();
        boolean blackCanMove = myPanel.goalManager.checkBlackMovable();

        // Decide whose turn next
        if (!isRobot) {
            if (blackCanMove) {
                // Black cannot move, white continues (Blockade rule)
                switchTurnToBlack();
            } else {
                switchTurnToWhite(); //
            }
        } else {
            if (whiteCanMove) {
                // White cannot move, black continues (Blockade rule)
                switchTurnToWhite();
            } else {
                switchTurnToBlack(); // Normal switch to Human
            }
        }
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
    }
}
