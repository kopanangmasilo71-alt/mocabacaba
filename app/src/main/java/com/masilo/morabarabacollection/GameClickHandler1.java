package com.masilo.morabarabacollection;

import android.graphics.PointF;

/**
 * Handles piece and circle clicks from both human players
 * Manages game flow including mill formation and piece removal
 */
public class GameClickHandler1 {
    public Circle clickedCircle = null;
    String level;
    public int mill_count = 0;
    public Circle whereTheCircleWas = null;
    public boolean whiteGoal = false, blackGoal = false;
    private final NormalPanel gamePanel;
    public final Animator1 animator;
    public GameInfoActivity gameInfoActivity;
    private final GoalManager goalManager;
    private final PanelUpdater panelUpdater;
    private final Circle[] circleArray;
    public Piece clickedPiece = null;
    private boolean isGameOnStart = true;

    public GameClickHandler1(NormalPanel gamePanel) {
        this.gamePanel = gamePanel;
        this.animator = gamePanel.animator;
        this.goalManager = gamePanel.goalManager;
        this.panelUpdater = gamePanel.panelUpdater;
        this.circleArray = gamePanel.circleArray;
        this.level = gamePanel.getLevel();
        gameInfoActivity = new GameInfoActivity();
        updateGameStartStatus();
    }
    /**
     * Check if we are currently in killing phase after mill formation
     */
    public boolean isKillingPhase() {
        return whiteGoal || blackGoal;
    }

    /**
     * Check if a piece is already killed (in target container)
     */
    private boolean clickedKilledPiece(Piece piece) {
        if ((piece.isInContainer(gamePanel.blackTarget) && piece.isBlack()) ||
                (piece.isInContainer(gamePanel.whiteTarget) && !piece.isBlack())) {
            gamePanel.segment("you clicked a killed piece");
            return true;
        }
        return false;
    }


    /**
     * Handle white piece click (for white player)
     */
    public void clickPieceWhite(Piece piece) {
        if (isKillingPhase()) {
            gamePanel.segment("First complete the killing phase");
            return;
        }

        if (piece.isBlack()) {
            gamePanel.segment("Select your white piece");
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
        gamePanel.segment("White piece selected");
    }

    /**
     * Generic piece click handler
     */
    public void clickPiece(Piece piece, boolean isBlackPlayer) {
        if (isKillingPhase()) {
            gamePanel.segment("First complete the killing phase");
            return;
        }

        // Validate turn
        if (isBlackPlayer && gameInfoActivity.whitePlayerTurn) {
            gamePanel.segment("NOT YOUR TURN");
            return;
        }
        if (!isBlackPlayer && gameInfoActivity.blackPlayerTurn) {
            gamePanel.segment("NOT YOUR TURN");
            return;
        }

        // Validate piece ownership
        if (isBlackPlayer && !piece.isBlack()) {
            gamePanel.segment("Select your black piece");
            return;
        }
        if (!isBlackPlayer && piece.isBlack()) {
            gamePanel.segment("Select your white piece");
            return;
        }

        if (clickedKilledPiece(piece)) {
            return;
        }

        clickedPiece = piece;
        whereTheCircleWas = clickedPiece.getMyCircle();
        updateGameStartStatus();
        gamePanel.segment((piece.isBlack() ? "Black" : "White") + " piece selected");
    }

    /**
     * Handle circle click for movement
     */
    public void circleClicked(Circle circle, boolean isBlackPlayer) {
        // If in killing phase, don't allow movement
        if (isKillingPhase()) {
            gamePanel.segment("Complete killing phase first");
            clickedPiece = null;
            return;
        }

        // Validate selection and turn
        if (clickedPiece == null) {
            gamePanel.segment("No piece selected!");
            return;
        }

        if (!isValidTurn(isBlackPlayer)) {
            gamePanel.segment("Not your turn!");
            clickedPiece = null;
            return;
        }

        // Validate piece ownership
        if (isBlackPlayer && !clickedPiece.isBlack()) {
            gamePanel.segment("Select your black piece");
            clickedPiece = null;
            return;
        }
        if (!isBlackPlayer && clickedPiece.isBlack()) {
            gamePanel.segment("Select your white piece");
            clickedPiece = null;
            return;
        }

        // Check if still in placement phase but trying to move placed piece
        if (isGameOnStart && !clickedPiece.isInContainer(gamePanel.blackContainer) &&
                !clickedPiece.isInContainer(gamePanel.whiteContainer)) {
            gamePanel.segment("you still have pieces in the container");
            clickedPiece = null;
            return;
        }

        storeMoveHistory();

        if (isGameOnStart) {
            handleGameStartMove(circle, isBlackPlayer);
        } else {
            if (!isValidMove(clickedPiece, circle)) {
                gamePanel.segment("invalid move");
                clickedPiece = null;
                return;
            }
            handleGameProgressMove(circle, isBlackPlayer);
        }

        // If not in killing phase after move, end turn
        if (!isKillingPhase()) {
            endTurn(isBlackPlayer);
        }
    }

    /**
     * Handle moves during the initial placement phase
     */
    private void handleGameStartMove(Circle circle, boolean isBlackPlayer) {
        executeMove(circle);
        boolean millFormed = checkMillFormation(circle);
        boolean doubleMill = millFormed && checkDoubleMill(circle);

        if (millFormed) {
            if (doubleMill) {
                mill_count = 2;
                triggerMillAnimation(true);
                gamePanel.segment("Double mill formed! Remove 2 opponent pieces");
            } else {
                mill_count = 1;
                triggerMillAnimation(false);
                gamePanel.segment("Mill formed! Remove 1 opponent piece");
            }

            // Enter killing phase
            if (isBlackPlayer) {
                blackGoal = true;
            } else {
                whiteGoal = true;
            }
        }
    }

    /**
     * Handle moves during the movement phase
     */
    private void handleGameProgressMove(Circle circle, boolean isBlackPlayer) {
        executeMove(circle);
        boolean millFormed = checkMillFormation(circle);
        boolean doubleMill = millFormed && checkDoubleMill(circle);

        if (millFormed) {
            if (doubleMill) {
                mill_count = 2;
                triggerMillAnimation(true);
                gamePanel.segment("Double mill formed! Remove 2 opponent pieces");
            } else {
                mill_count = 1;
                triggerMillAnimation(false);
                gamePanel.segment("Mill formed! Remove 1 opponent piece");
            }

            // Enter killing phase
            if (isBlackPlayer) {
                blackGoal = true;
            } else {
                whiteGoal = true;
            }
        }
    }

    /**
     * Execute the actual piece movement
     */
    public void executeMove(Circle destination) {
        if (clickedPiece == null) return;
        gamePanel.movesCount++;
        panelUpdater.start(whereTheCircleWas, destination, clickedPiece);
        animator.moveAnimation(new PointF(destination.getX(), destination.getY()), clickedPiece);

    }

    /**
     * Kill an opponent's piece during killing phase
     */
    public void killPiece(Piece piece) {
        // Validate killing phase
        if (!isKillingPhase()) {
            gamePanel.segment("Not in killing phase");
            return;
        }

        // Validate piece ownership
        if (whiteGoal && !piece.isBlack()) {
            gamePanel.segment("Cannot kill your own white piece");
            return;
        }
        if (blackGoal && piece.isBlack()) {
            gamePanel.segment("Cannot kill your own black piece");
            return;
        }
        // Check if piece is already killed
        if (piece.isInContainer(gamePanel.whiteTarget) || piece.isInContainer(gamePanel.blackTarget)) {
            gamePanel.segment("Piece already killed");
            return;
        }

        // Execute the kill
        if (!piece.isBlack()) {
            gamePanel.whitePieceKilledCount++;
            movePiece(piece, gamePanel.whiteTarget);
           gamePanel.whiteScore.setText(gamePanel.getWhitePieceKilled());
            gamePanel.segment("White piece removed! " + mill_count + " remaining to remove");
        } else {
            gamePanel.blackPieceKilledCount++;
            movePiece(piece, gamePanel.blackTarget);
          gamePanel.blackScore.setText(gamePanel.getBlackPieceKilled());
            gamePanel.segment("Black piece removed! " + mill_count + " remaining to remove");
        }
    }

    /**
     * Move piece to target (for killing)
     */
    public void movePiece(Piece piece, Circle target) {
        gamePanel.update(piece.getMyCircle(), target, piece);
        animator.moveAnimation(new PointF(target.getX(), target.getY()), piece);
    }

    /**
     * End the killing phase and resume normal play
     */
    public void endKillingPhase(boolean isBlackPlayer) {
        whiteGoal = false;
        blackGoal = false;
        mill_count = 0;
        resetMoveState();
        gamePanel.segment("Killing phase completed");
        endTurn(isBlackPlayer);
    }


    /**
     * Trigger mill formation animation
     */
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

    /**
     * Check if a mill was formed at the given circle
     */
    private boolean checkMillFormation(Circle circle) {
        int goalIndex;
        if (clickedPiece.isBlack()) {
            goalIndex = goalManager.checkBlackGoal(circle.getIndex());
        } else {
            goalIndex = goalManager.checkWhiteGoal(circle.getIndex());
        }
        return goalIndex != -1;
    }

    /**
     * Check for double mill formation
     */
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

    /**
     * End the current player's turn
     */
    private void endTurn(boolean isBlackPlayer) {
        updateGameStartStatus();
        gamePanel.maker.checkGameOver(isBlackPlayer);
        resetMoveState();
    }

    /**
     * Store move history for potential reversal
     */
    private void storeMoveHistory() {
        // Move history implementation can be added here
    }

    /**
     * Check if it's a valid turn for the player
     */
    public boolean isValidTurn(boolean isBlackPlayer) {
        return (isBlackPlayer && gameInfoActivity.blackPlayerTurn) ||
                (!isBlackPlayer && gameInfoActivity.whitePlayerTurn);
    }

    /**
     * Validate if the move is legal
     */
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

    /**
     * Update game phase (placement vs movement)
     */
    public void updateGameStartStatus() {
        isGameOnStart = (gamePanel.getWhiteContainerCount() > 0 || gamePanel.getBlackContainerCount() > 0);
    }

    /**
     * Reset move state after turn completion
     */
    public void resetMoveState() {
        clickedPiece = null;
        whereTheCircleWas = null;
        clickedCircle = null;
    }

}
