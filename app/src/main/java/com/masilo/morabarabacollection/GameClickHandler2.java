package com.masilo.morabarabacollection;

import android.graphics.PointF;

/**
 * GameClickHandler2 - Adapted for GamePanel2 (24-point board)
 */
public class GameClickHandler2 {

    public Circle clickedCircle = null;
    String level;
    public int mill_count = 0;
    public Circle whereTheCircleWas = null;
    public boolean whiteGoal = false, blackGoal = false;
    private final GamePanel2 gamePanel;
    public final Animator1 animator;
    public GameInfoActivity gameInfoActivity;
    // GoalManager removed/unused
    private final PanelUpdater panelUpdater;
    private final Circle[] circleArray;
    public Piece clickedPiece = null;
    private boolean isGameOnStart = true;

    public GameClickHandler2(GamePanel2 gamePanel) {
        this.gamePanel = gamePanel;
        this.animator = gamePanel.animator;
        // GoalManager reference removed as it's not compatible
        
        this.panelUpdater = gamePanel.panelUpdater;
        this.circleArray = gamePanel.circleArray;
        this.level = gamePanel.getLevel();
        gameInfoActivity = new GameInfoActivity();

        updateGameStartStatus();
    }

    private boolean clickedKilledPiece(Piece piece){
        if((piece.isInContainer(gamePanel.blackTarget) && piece.isBlack()) || (piece.isInContainer(gamePanel.whiteTarget) && !piece.isBlack())){
            gamePanel.segment("you clicked a killed piece");
            return true;
        }
        return false;
    }

    public void clickPieceBlack(Piece piece) {
        if(whiteGoal){
            mill_count--;
            killPiece(piece);
            gamePanel.segment("number of killed ...."+mill_count);
            if(mill_count ==0){
                whiteGoal=false;
                gamePanel.segment("used one goal");
                endTurn(false);
            }
        } else{
            gamePanel.segment("you can not do that");
        }
    }

    public void clickPieceWhite(Piece piece) {
        if(!piece.isBlack()) {
            if (whiteGoal) {
                gamePanel.segment("Kill an opponent piece first!");
                return;
            }
            if (gameInfoActivity.blackPlayerTurn) {
                gamePanel.segment("NOT YOUR TURN");
                return;
            }
            if (clickedKilledPiece(piece)) {
                return;
            }
            clickedPiece = piece;
            whereTheCircleWas = clickedPiece.getMyCircle();
            updateGameStartStatus();
        }
    }

    public void clickPiece(Piece piece) {
        clickedPiece = piece;
        whereTheCircleWas = clickedPiece.getMyCircle();
        updateGameStartStatus();
    }

    public void killPiece(Piece piece) {
        resetMoveState();
        if (!piece.isBlack()) {
            gamePanel.whitePieceKilledCount++;
            movePiece(piece, gamePanel.whiteTarget);
        }
        else {
            gamePanel.blackPieceKilledCount++;
            movePiece(piece, gamePanel.blackTarget);
        }
        gamePanel.blackScore.setText(gamePanel.getWhitePieceKilled());
        gamePanel.whiteScore.setText(gamePanel.getBlackPieceKilled());
    }

    public void movePiece(Piece piece, Circle target) {
        gamePanel.update(piece.getMyCircle(), target, piece);
        animator.moveAnimation(new PointF(target.getX(), target.getY()), piece);
    }

    public void circleClicked(Circle circle, boolean isRobot) {
        if (!isRobot && whiteGoal) {
            gamePanel.segment("Kill an opponent piece first!");
            return;
        }

            if (clickedPiece == null || gameInfoActivity.isValidTurn(isRobot)) {
                gamePanel.segment("No piece selected or not your turn!");
                clickedPiece = null;
                return;
            }

            if (!isRobot && clickedPiece.isBlack()) {
                gamePanel.segment("select your piece");
                clickedPiece = null;
                return;
            }

            if (!isGameOnStart && (clickedPiece.getMyCircle().getNeighbour() == null)) {
                updateGameStartStatus();
                if (isGameOnStart) {
                    gamePanel.segment("you still have pieces in the container");
                    clickedPiece = null;
                    return;
                }
            }
            
        if (circle.getPiece() != null) {
            if (!isRobot) gamePanel.segment("Spot occupied");
            return;
        }

        if (isGameOnStart) {
            handleGameStartMove(circle, isRobot);
        } else {
            if(!isValidMove(clickedPiece,circle)){
                gamePanel.segment("invalid move");
                return;
            }
            handleGameProgressMove(circle, isRobot);
        }



        if(whiteGoal){
            gamePanel.maker.switchTurnToWhite();
            return;
        }
        if(!isRobot) {
            clickedCircle = circle;
        }
        endTurn(isRobot);
    }

    private void handleGameStartMove(Circle circle, boolean isRobot) {
        executeMove(circle);

        boolean millFormed = checkMillFormation(circle);
        if (millFormed) {
             triggerMillAnimation(circle);
             mill_count = 1;
             if(isRobot){
                blackGoal = true;
                robotKill();
            }else{
                whiteGoal = true;
            }
        }
    }

    private void handleGameProgressMove(Circle circle, boolean isRobot) {
        executeMove(circle);
        
        boolean millFormed = checkMillFormation(circle);
        
        if (millFormed) {
            triggerMillAnimation(circle);
            mill_count = 1;
            if(isRobot){
                blackGoal = true;
                robotKill();
            }else {
                whiteGoal=true;
            }
        }
    }

    private void triggerMillAnimation(Circle circle) {
        if (circle == null || circle.getPiece() == null) return;
        String moveType = circle.getPieceType();
        int[][] patterns = gamePanel.getPatterns();
        int idx = circle.getIndex();

        for (int[] pattern : patterns) {
            if (pattern[0] == idx || pattern[1] == idx || pattern[2] == idx) {
                Circle c1 = gamePanel.circleArray[pattern[0]];
                Circle c2 = gamePanel.circleArray[pattern[1]];
                Circle c3 = gamePanel.circleArray[pattern[2]];

                if (moveType.equals(c1.getPieceType()) &&
                        moveType.equals(c2.getPieceType()) &&
                        moveType.equals(c3.getPieceType())) {
                    animator.goalAnimation(c1.getPiece(), c2.getPiece(), c3.getPiece());
                }
            }
        }
    }
    
    private void robotKill() {
        if (gamePanel.getRobot1() != null) {
            gamePanel.getRobot1().start(true); // Robot kills a piece
        }
        mill_count = 0;
        blackGoal = false;
    }

    public void executeMove(Circle destination) {
        if (clickedPiece == null) return;
        gamePanel.movesCount++;
        panelUpdater.start(whereTheCircleWas, destination, clickedPiece);
        animator.moveAnimation(new PointF(destination.getX(), destination.getY()), clickedPiece);
    }

    private boolean checkMillFormation(Circle circle) {
        if (circle == null || circle.getPiece() == null) return false;
        String moveType = circle.getPieceType();
        if ("N".equals(moveType)) return false;

        int[][] patterns = gamePanel.getPatterns();
        int idx = circle.getIndex();

        for (int[] pattern : patterns) {
            if (pattern[0] == idx || pattern[1] == idx || pattern[2] == idx) {
                String type1 = gamePanel.circleArray[pattern[0]].getPieceType();
                String type2 = gamePanel.circleArray[pattern[1]].getPieceType();
                String type3 = gamePanel.circleArray[pattern[2]].getPieceType();

                if (moveType.equals(type1) && moveType.equals(type2) && moveType.equals(type3)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void endTurn(boolean isRobot) {
        if (mill_count == 0) {
            updateGameStartStatus();
            gamePanel.maker.checkGameOver();
        }
    }

    public boolean isValidMove(Piece piece, Circle target) {
        if (isGameOnStart) return true;

        Circle currentCircle = piece.getMyCircle();
        if (currentCircle == null) return false;

        int[] neighbors = currentCircle.getNeighbour();
        if (neighbors == null) return false;

        for (int neighborIndex : neighbors) {
            if (neighborIndex >= 0 && neighborIndex < circleArray.length &&
                    circleArray[neighborIndex] == target) {
                return true;
            }
        }
        return false;
    }

    public void updateGameStartStatus() {
        isGameOnStart = (gamePanel.getWhiteContainerCount() > 0 || gamePanel.getBlackContainerCount() > 0);
    }
    
    public void resetMoveState() { whereTheCircleWas = null; }
    public Piece getClickedPiece() { return clickedPiece; }
}
