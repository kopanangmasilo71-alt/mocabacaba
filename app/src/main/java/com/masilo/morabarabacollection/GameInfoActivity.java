package com.masilo.morabarabacollection;

public class GameInfoActivity {

     public boolean blackPlayerTurn = false;
     public boolean  whitePlayerTurn = true;


    public GameInfoActivity(){

    }
    public boolean isValidTurn(boolean isRobot) {
        if (isRobot) {
            return !blackPlayerTurn || whitePlayerTurn;
        } else {
            return !whitePlayerTurn || blackPlayerTurn;
        }
    }
}
