package com.masilo.morabarabacollection;

import android.graphics.PointF;

public class GameClickHandler {

    public Circle clickedCircle=null;
    String level;
    public int mill_count = 0;
    public Circle whereTheCircleWas = null;
    public boolean whiteGoal = false, blackGoal = false;
    private final GamePanel1 gamePanel;
    public final Animator1 animator;
    public GameInfoActivity gameInfoActivity;
    private final GoalManager goalManager;
    private final PanelUpdater panelUpdater;
    private final Circle[] circleArray;
    public Piece clickedPiece = null;
    private boolean isGameOnStart = true;

    // 1. ADD THE LEARNER

    public GameClickHandler(GamePanel1 gamePanel) {
        this.gamePanel = gamePanel;
        this.animator = gamePanel.animator;
        this.goalManager = new GoalManager(gamePanel);
        this.panelUpdater = gamePanel.panelUpdater;
        this.circleArray = gamePanel.circleArray;
        this.level = gamePanel.getLevel();
        gameInfoActivity = new GameInfoActivity();


        updateGameStartStatus();
    }

    // ... [Existing clickedKilledPiece method] ...
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
            if(mill_count ==0){
                whiteGoal=false;
                endTurn(false);
            }
        } else{
            gamePanel.segment("you can not do that");
        }
    }

    // ... [Existing clickPieceWhite and clickPiece methods] ...
    public void clickPieceWhite(Piece piece) {
        if(!piece.isBlack()) {
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
        if(!gamePanel.isMalulaStarted) {
            if (gameInfoActivity.whitePlayerTurn) {
                gamePanel.segment("NOT YOUR TURN ROBOT");
                return;
            }
        }
        clickedPiece = piece;
        whereTheCircleWas = clickedPiece.getMyCircle();
        updateGameStartStatus();
    }

    // ... [Existing robotKill method] ...
    private void robotKill(boolean isDouble){
        resetMoveState();
        blackGoal = false;
        if(isDouble){
            if(!level.equals("LEVEL1")){
                gamePanel.getRobot1().start(true);
                gamePanel.getRobot1().start(true);
            } else {
                gamePanel.getRobot3().start(true);
                gamePanel.getRobot3().start(true);
            }
        }
        else {
            gamePanel.getRobot3().start(true);
        }
        mill_count = 0;
    }

    public void killPiece(Piece piece) {
        // 3. RECORD KILL ACTION
        // We record BEFORE the piece is physically moved to target, so we know where it was on the board


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

    // ... [Existing movePiece method] ...
    public void movePiece(Piece piece, Circle target) {
        gamePanel.update(piece.getMyCircle(), target, piece);
        animator.moveAnimation(new PointF(target.getX(), target.getY()), piece);
    }

    public void circleClicked(Circle circle, boolean isRobot) {
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
            // Explicit check: if game is NOT on start, verify container status again just in case

            if (!isGameOnStart && (clickedPiece.getMyCircle().getNeighbour() == null)) {
                // This piece thinks it's in a container (no neighbors), but game is in movement phase.
                // This is the exact error condition user is seeing.
                // We should FORCE update status and retry or fail gracefully.
                updateGameStartStatus();
                if (isGameOnStart) {
                    gamePanel.segment("you still have pieces in the container");
                    clickedPiece = null;
                    return;
                }
            }
            // Original check:
            if (isGameOnStart && !(clickedPiece.getMyCircle().getNeighbour() == null)) {
                // Piece is on board, but game is in start phase.
                // This shouldn't happen in standard play, but in Malula it means we are moving too early?
                // Or we should allow moving if pieces are on board?
                // Actually, if isGameOnStart is true, we are PLACING.
                // We shouldn't be selecting a piece on the board to move it.
                gamePanel.segment("you still have pieces in the container");
                clickedPiece = null;
                return;
            }

        // New Safety Check: Cannot move to an occupied circle
        if (circle.getPiece() != null) {
            if (!isRobot) gamePanel.segment("Spot occupied");
            return;
        }

        storeMoveHistory();
        if(!isRobot) gamePanel.robotController.set();

        if (isGameOnStart) {
            handleGameStartMove(circle, isRobot);
        } else {
            if(!isValidMove(clickedPiece,circle)){
                gamePanel.segment("invalid move");
                return;
            }
            handleGameProgressMove(circle);
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
        boolean millFormed=false;
        boolean doubleMill=false;
        executeMove(circle);
       if(!gamePanel.isMalulaStarted) {
           millFormed = checkMillFormation(circle);
           doubleMill = millFormed && checkDoubleMill(circle);
       }
        if (millFormed) {
            if (doubleMill) {
                mill_count =2;
                triggerMillAnimation(true);
            } else {
                mill_count =1;
                triggerMillAnimation(false);
            }
            if(isRobot){
                blackGoal = true;
                robotKill(doubleMill);

            }else{
                whiteGoal = true;
            }
        }
        if(isRobot) {
            endTurn(true);
        }
    }

    private void handleGameProgressMove(Circle circle) {
        gamePanel.robotController.setDanger();

        // 5. RECORD MOVEMENT ACTION
        // Determine who is moving based on the clicked piece color

        executeMove(circle);
        // ... [Rest of handleGameProgressMove logic] ...
        boolean millFormed = checkMillFormation(circle);
        boolean doubleMill = millFormed && checkDoubleMill(circle);
        boolean robot=false;
        if (millFormed) {
            if (doubleMill) {
                mill_count =2;
                triggerMillAnimation(true);
                if(gameInfoActivity.blackPlayerTurn){
                    robotKill(true);
                    robot=true;
                }else{
                    whiteGoal=true;
                    return;
                }
            } else {
                mill_count =1;
                triggerMillAnimation(false);
                if(clickedPiece.isBlack()){
                    robotKill(false);
                    robot=true;
                }else {
                    whiteGoal=true;
                    return;
                }
            }
        }
        if(robot) {
            endTurn(true);
        }
    }

    // ... [The rest of your methods: executeMove, findMillFormationCircles, triggerMillAnimation, etc. remain unchanged]
    public void executeMove(Circle destination) {
        if (clickedPiece == null) return;
        gamePanel.movesCount++;
        panelUpdater.start(whereTheCircleWas, destination, clickedPiece);
        animator.moveAnimation(new PointF(destination.getX(), destination.getY()), clickedPiece);
    }

    // ... [Existing helper methods omitted for brevity, ensure you keep them] ...

    private void triggerMillAnimation(boolean isDoubleMill) {
        Piece[] goalPieces = clickedPiece.isBlack() ? goalManager.goalPieces : goalManager.storePieces;

        if (isDoubleMill) {
            Piece[] goalPieces1 = clickedPiece.isBlack() ? goalManager.goalPieces1 : goalManager.storePieces1;
            if (goalPieces != null && goalPieces.length >= 3) {
                animator.goalAnimation(goalPieces[0], goalPieces[1], goalPieces[2]);
            }
            if (goalPieces1 != null && goalPieces1.length >= 3) {
                animator.goalAnimation(goalPieces1[0], goalPieces1[1], goalPieces1[2]);
            }
        } else {
            if (goalPieces != null && goalPieces.length >= 3) {
                animator.goalAnimation(goalPieces[0], goalPieces[1], goalPieces[2]);
            }
        }
    }
    private boolean checkMillFormation(Circle circle) {
        int goalIndex;
        if (clickedPiece.isBlack()) {
            goalIndex = goalManager.checkBlackGoal(circle.getIndex());
        } else {
            goalIndex = goalManager.checkWhiteGoal(circle.getIndex());
        }
        return goalIndex != -1;
    }
    public boolean checkDoubleMill(Circle circle) {
        if (circle == null) return false;
        if (clickedPiece.isBlack()) {
            int goalIndex = goalManager.checkBlackGoal(circle.getIndex());
            return goalManager.checkBlackGoal1(circle.getIndex(), goalIndex + 1);
        } else {
            int goalIndex = goalManager.checkWhiteGoal(circle.getIndex());
            return goalManager.checkWhiteGoal1(circle.getIndex(), goalIndex + 1);
        }
    }
    private void endTurn(boolean isRobot) {
        if (mill_count == 0) {
            updateGameStartStatus();
            gamePanel.maker.checkGameOver(isRobot);
        }
    }
    private void storeMoveHistory() {}
    /**
     * Reverses the last human move
     */
    public void reverseLastHumanMove() {
        if (clickedPiece == null || whereTheCircleWas == null) {
           // logError("Cannot reverse move - null piece or original circle");
            return;
        }

        Piece piece = clickedPiece;
        Circle currentCircle = piece.getMyCircle();

        if (currentCircle != null && !isContainerCircle(currentCircle)) {
            currentCircle.resetPiece();
        }

        piece.setMyCircle(whereTheCircleWas);
        whereTheCircleWas.setMyPiece(piece, piece.isBlack() ? "B" : "W");

        gamePanel.panelUpdater.start(null, whereTheCircleWas, piece);
        gamePanel.panelUpdater.updateBlock();
        animator.moveAnimation(new PointF(whereTheCircleWas.getX(), whereTheCircleWas.getY()), piece);
        panelUpdater.updateBlock();
    }
    public void forceMillMove1(Circle circle) {
        if (circle != null && clickedPiece != null) {
            executeMove(circle);
            boolean isDoubleMill = checkDoubleMill(circle);
            triggerMillAnimation(isDoubleMill);
        }
    }
    /**
     * Force move with specified pieces (robot use)
     */
    public void forceMillMove2(Circle empty, Circle moving) {
        if (empty != null && moving != null && moving.getPiece() != null) {
            clickedPiece = moving.getPiece();
            panelUpdater.start(moving, empty, clickedPiece);
            animator.moveAnimation(new PointF(empty.getX(), empty.getY()), clickedPiece);
            boolean isDoubleMill = checkDoubleMill(empty);
            triggerMillAnimation(isDoubleMill);
        }
    }
    private boolean isContainerCircle(Circle circle) {
        return circle == gamePanel.whiteContainer || circle == gamePanel.blackContainer ||
                circle == gamePanel.whiteTarget || circle == gamePanel.blackTarget;
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
    public boolean isGameOnStart() { return isGameOnStart; }
    public Piece getClickedPiece() { return clickedPiece; }
}
