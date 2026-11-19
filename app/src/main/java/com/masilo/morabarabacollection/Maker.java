package com.masilo.morabarabacollection;

import android.os.Handler;

public class Maker {
    private final GamePanel1 myPanel;
    private int count = 0;
    public Maker(GamePanel1 myPanel) {
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
                // Black can move, white continues (Blockade rule)
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
        if(myPanel.isMalulaStarted){
            switchTurnToWhite();
            count++;
            if(count == 12){
                myPanel.gameClickHandler.whiteGoal = false;
                callRobot();
            }
            return;
        }
        callRobot2();
    }
    private  void callRobot() {
        for(int i =0; i<12; i++){
            final int index = i;
            new Handler().postDelayed(() -> {
                myPanel.gameClickHandler.gameInfoActivity.whitePlayerTurn = false;
                myPanel.gameClickHandler.gameInfoActivity.blackPlayerTurn = true;
                myPanel.getRobot3().start(false);
                if(index == 11){
                    myPanel.isMalulaStarted = false;
                    switchTurnToWhite();
                }
            }, 1000L * (i+1));
        }
    }
    private void callRobot2(){
        myPanel.gameClickHandler.gameInfoActivity.whitePlayerTurn = false;
        myPanel.gameClickHandler.gameInfoActivity.blackPlayerTurn = true;
        myPanel.panelUpdater.updateBlock();
        new Handler().postDelayed(() -> myPanel.robotController.startRobotControl(),1000);
    }
}
