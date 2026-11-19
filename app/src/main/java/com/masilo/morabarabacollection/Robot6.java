package com.masilo.morabarabacollection;

import android.graphics.PointF;

import java.util.Arrays;

/**
 * Refactored AI algorithm with improved structure and reduced spaghetti code
 */
public class Robot6  {
    public final GamePanel1 panel;
    private final Game game;
    Piece clicked = null;
    public boolean global_previous_goal = false;
    private String[][] block;
    private Piece[] blackPieces;
    private Circle[] circleArray;
    private Circle blackContainer;
    private int[][] patterns;
    private RobotData6 dataBase;
    private String level;
    // State flags
    private boolean onStart = false;
    // Pattern constants for better readability
    private static final String BLACK = "B";
    GameClickHandler gameClickHandler;
    private static final String WHITE = "W";
    private static final String NONE = "N";

    public Robot6(GamePanel1 panel) {
        this.panel = panel;
        this.circleArray = panel.circleArray;
        this.game = new Game(this, circleArray);
    }

    public void updateRobot() {
        this.gameClickHandler=panel.gameClickHandler;
        this.blackContainer = panel.getBlackContainer();
        this.circleArray = panel.circleArray;
        this.blackPieces = panel.getBlackPieces();
        this.block = panel.block;
        this.patterns = panel.getPatterns();
        this.dataBase = new RobotData6(this);
        this.level = panel.getLevel();
    }


    public void start(boolean killing) {
        updateRobot();
        if (!killing) {
            global_previous_goal = false;
            handleNonKillingMove();
        } else {
            kill();
        }
    }

    private void handleNonKillingMove() {
        for (Piece blackPiece : blackPieces) {
            if (blackContainer.isClicked(blackPiece.getX(), blackPiece.getY())) {
                pieceClicked(blackPiece);
                onStart = true;
                break;
            } else{
                onStart = false;
            }
        }

        if (onStart) {
            checkMyDanger();
        } else {
            evaluateDanger();
        }
    }

    // Delegate methods to panel
    public void circleClicked(Circle circle) {
        if(panel.isMalulaStarted){
            if(clicked != null){
                panel.update(null,circle,clicked);
                panel.animator.moveAnimation(new PointF(circle.getX(),circle.getY()),clicked);
                return;
            }
        }
        gameClickHandler.circleClicked(circle,true);
    }
    public void pieceClicked(Piece piece) {
        if(panel.isMalulaStarted){
            clicked = piece;
            return;
        }
        gameClickHandler.clickPiece(piece);
    }

    public Circle[] getCircleArray() { return circleArray; }

    // Pattern matching and danger detection
    private void checkMyDanger() {
        if (handleDangerPattern(new String[]{BLACK, NONE, BLACK}, 1) ||
                handleDangerPattern(new String[]{NONE, BLACK, BLACK}, 0) ||
                handleDangerPattern(new String[]{BLACK, BLACK, NONE}, 2)) {
            return;
        }
        checkHumanDanger1();
    }

    private boolean handleDangerPattern(String[] pattern, int targetIndex) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                circleClicked(circleArray[patterns[i][targetIndex]]);
                return true;
            }
        }
        return false;
    }

    // Human danger evaluation
    private void checkHumanDanger1() {
        Integer dangerIndex = findPrimaryDangerIndex();
        boolean moved = false;

        if (dangerIndex != null) {
            moved = handleImmediateDanger(dangerIndex);
        }

        if (moved) {
            return;
        }

        if (dangerIndex != null) {
            checkHumanDanger(dangerIndex);
        } else {
            createFutureGoal();
        }
    }

    private Integer findPrimaryDangerIndex() {
        for (int i = 0; i < block.length; i++) {
            if (isDangerPattern(block[i])) {
                return i;
            }
        }
        return null;
    }

    private boolean isDangerPattern(String[] pattern) {
        return Arrays.equals(pattern, new String[]{WHITE, NONE, WHITE}) ||
                Arrays.equals(pattern, new String[]{NONE, WHITE, WHITE}) ||
                Arrays.equals(pattern, new String[]{WHITE, WHITE, NONE});
    }

    private boolean handleImmediateDanger(int dangerIndex) {
        String[] pattern = block[dangerIndex];
        int targetIndex = getTargetIndexForPattern(pattern);

        if (targetIndex != -1) {
            Circle targetCircle = circleArray[patterns[dangerIndex][targetIndex]];
            if (isTargeted(dangerIndex, targetCircle)) {
                checkHumanDanger(dangerIndex);
                return true;
            }
        }
        return false;
    }

    private int getTargetIndexForPattern(String[] pattern) {
        if (Arrays.equals(pattern, new String[]{WHITE, NONE, WHITE})) return 1;
        if (Arrays.equals(pattern, new String[]{NONE, WHITE, WHITE})) return 0;
        if (Arrays.equals(pattern, new String[]{WHITE, WHITE, NONE})) return 2;
        return -1;
    }

    private void checkHumanDanger(int dangerIndex) {
        String[] pattern = block[dangerIndex];
        int targetIndex = getTargetIndexForPattern(pattern);

        if (targetIndex != -1) {
            circleClicked(circleArray[patterns[dangerIndex][targetIndex]]);
        }
    }

    // Strategic goal creation
    private boolean canMakeGoal(int targeted, Circle circle) {
        for (int i = 0; i < block.length; i++) {
            if (i != targeted && isCircleInPattern(i, circle)) {
                if (handleGoalCreation(i, circle)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCircleInPattern(int patternIndex, Circle circle) {
        return circle == circleArray[patterns[patternIndex][0]] ||
                circle == circleArray[patterns[patternIndex][1]] ||
                circle == circleArray[patterns[patternIndex][2]];
    }

    private boolean handleGoalCreation(int patternIndex, Circle circle) {
        String[] currentBlock = block[patternIndex];

        if (Arrays.equals(currentBlock, new String[]{BLACK, BLACK, NONE}) ||
                Arrays.equals(currentBlock, new String[]{WHITE, BLACK, NONE}) ||
                Arrays.equals(currentBlock, new String[]{NONE, BLACK, BLACK}) ||
                Arrays.equals(currentBlock, new String[]{NONE, BLACK, NONE}) ||
                Arrays.equals(currentBlock, new String[]{NONE, BLACK, WHITE})) {

            return executeGoalMove(patternIndex, circle, 1);
        }

        if (Arrays.equals(currentBlock, new String[]{WHITE, NONE, BLACK})) {
            return executeGoalMove(patternIndex, circle, 2);
        }

        if (Arrays.equals(currentBlock, new String[]{BLACK, NONE, NONE})) {
            return executeSegmentMove(patternIndex, circle, 0);
        }

        if (Arrays.equals(currentBlock, new String[]{NONE, NONE, BLACK})) {
            return executeSegmentMove(patternIndex, circle, 2);
        }

        if (Arrays.equals(currentBlock, new String[]{BLACK, NONE, BLACK})) {
            return handleDoubleBlackPattern(patternIndex, circle);
        }

        if (Arrays.equals(currentBlock, new String[]{BLACK, NONE, WHITE})) {
            return executeGoalMove(patternIndex, circle, 0);
        }

        return false;
    }

    private boolean executeGoalMove(int patternIndex, Circle circle, int pieceIndex) {
        Piece piece = circleArray[patterns[patternIndex][pieceIndex]].getPiece();
        if (piece != null) {
            pieceClicked(piece);
            circleClicked(circle);
            return true;
        }
        return false;
    }

    private boolean executeSegmentMove(int patternIndex, Circle circle, int pieceIndex) {
        Piece piece = circleArray[patterns[patternIndex][pieceIndex]].getPiece();
        if (piece != null && checkSegment(piece, circle)) {
            pieceClicked(piece);
            circleClicked(circle);
            return true;
        }
        return false;
    }

    private boolean handleDoubleBlackPattern(int patternIndex, Circle circle) {
        return executeSegmentMove(patternIndex, circle, 0) ||
                executeSegmentMove(patternIndex, circle, 2);
    }

    public boolean checkSegment(Piece piece, Circle circle) {
        float givenX1 = piece.getX();
        float givenY1 = piece.getY();
        float givenX2 = circle.getX();
        float givenY2 = circle.getY();

        double TOLERANCE = 0.01;
        for (Line line : panel.lineArray) {
            if (isLineMatch(line, givenX1, givenY1, givenX2, givenY2, TOLERANCE)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLineMatch(Line line, float x1, float y1, float x2, float y2, double tolerance) {
        return (Math.abs(line.getX1() - x1) < tolerance && Math.abs(line.getY1() - y1) < tolerance &&
                Math.abs(line.getX2() - x2) < tolerance && Math.abs(line.getY2() - y2) < tolerance) ||
                (Math.abs(line.getX1() - x2) < tolerance && Math.abs(line.getY1() - y2) < tolerance &&
                        Math.abs(line.getX2() - x1) < tolerance && Math.abs(line.getY2() - y1) < tolerance);
    }

    // Pattern evaluation methods
    private boolean evaluate3(String[] pattern, int pieceIndex) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                if (canMakeGoal(i, circleArray[patterns[i][pieceIndex]])) {
                    circleClicked(circleArray[patterns[i][pieceIndex]]);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkForBBN(String[] pattern, int pieceIndex, int x, int z) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                Circle a = circleArray[patterns[i][x]];
                Circle b = circleArray[patterns[i][z]];
                Circle target = circleArray[patterns[i][pieceIndex]];
                Circle c = getNeighborForBBN(target, a, b);

                if (c != null) {
                    pieceClicked(c.getPiece());
                    circleClicked(target);
                    return false;
                }
            }
        }
        return true;
    }

    private Circle getNeighborForBBN(Circle circle, Circle exclude1, Circle exclude2) {
        for (int neighborIndex : circle.getNeighbour()) {
            Circle neighbor = circleArray[neighborIndex];
            if (BLACK.equals(neighbor.getPieceType()) && neighbor != exclude1 && neighbor != exclude2) {
                return neighbor;
            }
        }
        return null;
    }

    // Neighborhood analysis utilities
    private boolean hasNeighborWithType(Circle circle, String pieceType, int minCount) {
        int count = 0;
        for (int neighborIndex : circle.getNeighbour()) {
            if (pieceType.equals(circleArray[neighborIndex].getPieceType())) {
                count++;
                if (count >= minCount) return true;
            }
        }
        return false;
    }

    private boolean hasNeighborWithType(Circle circle, String pieceType) {
        return hasNeighborWithType(circle, pieceType, 1);
    }

    private boolean hasNeighborsWith2W(Circle circle, Circle exclude1, Circle exclude2) {
        for (int neighborIndex : circle.getNeighbour()) {
            Circle neighbor = circleArray[neighborIndex];
            if (WHITE.equals(neighbor.getPieceType()) && neighbor != exclude1 && neighbor != exclude2) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNeighborWithN(Circle circle) {
        for (int neighborIndex : circle.getNeighbour()) {
            if (NONE.equals(circleArray[neighborIndex].getPieceType())) {
                return true;
            }
        }
        return false;
    }

    // Convenience methods for common neighbor checks
    private boolean hasNeighborWithB(Circle circle) { return hasNeighborWithType(circle, BLACK); }
    private boolean hasNeighborWithW(Circle circle) { return hasNeighborWithType(circle, WHITE); }

    private boolean hasNeighborWith4B(Circle circle) { return hasNeighborWithType(circle, BLACK, 4); }
    private boolean hasNeighborWith3W(Circle circle) { return hasNeighborWithType(circle, WHITE, 3); }
    private boolean hasNeighborWith3B(Circle circle) { return hasNeighborWithType(circle, BLACK, 3); }
    private boolean hasNeighborsWith2W(Circle circle) {
        int count = 0;
        for (int neighborIndex : circle.getNeighbour()) {
            if (WHITE.equals(circleArray[neighborIndex].getPieceType())) {
                count++;
            }
        }
        return count == 2;
    }

    private void useNeighborWithB(Circle circle) {
        for (int neighborIndex : circle.getNeighbour()) {
            Circle neighbor = circleArray[neighborIndex];
            if (neighbor.getPiece() != null && BLACK.equals(neighbor.getPieceType())) {
                pieceClicked(neighbor.getPiece());
                circleClicked(circle);
                return;
            }
        }
    }

    // Danger assessment
    private boolean isTargeted(int targeted, Circle circle) {
        int known = circle.getIndex();
        for (int i = 0; i < block.length; i++) {
            if (i != targeted) {
                if (isCircleThreatened(i, known, block[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCircleThreatened(int patternIndex, int circleIndex, String[] pattern) {
        if (Arrays.equals(pattern, new String[]{WHITE, NONE, NONE})) {
            return circleIndex == patterns[patternIndex][1] || circleIndex == patterns[patternIndex][2];
        }
        if (Arrays.equals(pattern, new String[]{NONE, WHITE, NONE})) {
            return circleIndex == patterns[patternIndex][0] || circleIndex == patterns[patternIndex][2];
        }
        if (Arrays.equals(pattern, new String[]{NONE, NONE, WHITE})) {
            return circleIndex == patterns[patternIndex][1] || circleIndex == patterns[patternIndex][0];
        }
        return false;
    }

    // Strategic move creation
    public void createFutureGoal() {
        MoveStrategy[] strategies = {
                () -> dataBase.usedData(),
                game::checkerB,
                game::checkerW,
                game::useQuickPatterns,
                () -> game.useDataPatterns(game.dataPatterns1),
                () -> game.useDataPatterns(game.dataPatterns),
                game::useDataPatterns,
                () -> level.equals("LEVEL1") && game.gaming(),
                () -> createSimpleMove(new String[]{NONE, NONE, NONE}, 1),
                () -> game.useDataPatterns(game.dataPatterns2),
                () -> createSimpleMove(new String[]{BLACK, NONE, NONE}, 1),
                () -> createSimpleMove(new String[]{NONE, NONE, BLACK}, 1),
                () -> createSimpleMove(new String[]{WHITE, NONE, NONE}, 1),
                () -> createSimpleMove(new String[]{NONE, NONE, WHITE}, 1),
                () -> createSimpleMove(new String[]{NONE, BLACK, NONE}, 0),
                () -> createSimpleMove(new String[]{BLACK, NONE, WHITE}, 1),
                () -> createSimpleMove(new String[]{WHITE, NONE, BLACK}, 1),
                () -> createSimpleMove(new String[]{NONE, WHITE, BLACK}, 0),
                () -> createSimpleMove(new String[]{NONE, BLACK, WHITE}, 0),
                () -> createSimpleMove(new String[]{WHITE, BLACK, NONE}, 2),
                () -> createSimpleMove(new String[]{BLACK, WHITE, NONE}, 2)
        };

        for (MoveStrategy strategy : strategies) {
            if (strategy.execute()) return;
        }
        panel.maker.switchTurnToWhite();
        panel.segment("I am stuck");
    }

    private boolean createSimpleMove(String[] pattern, int position) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                circleClicked(circleArray[patterns[i][position]]);
                return true;
            }
        }
        return false;
    }

    // Killing logic
    private void kill() {
        if (tryCenterKill()) return;

        String[][] killPatterns = {
                {WHITE, WHITE, NONE}, {NONE, WHITE, WHITE}, {WHITE, NONE, WHITE},
                {WHITE, BLACK, BLACK}, {BLACK, BLACK, WHITE}, {BLACK, WHITE, BLACK},
                {NONE, WHITE, BLACK}, {BLACK, WHITE, NONE}, {NONE, WHITE, NONE},
                {WHITE, BLACK, NONE}, {WHITE, NONE, BLACK}, {BLACK, NONE, WHITE},
                {NONE, BLACK, WHITE}, {NONE, NONE, WHITE}, {WHITE, NONE, NONE}
        };

        int[] targetIndices = {1, 1, 0, 0, 2, 1, 1, 1, 1, 0, 0, 2, 2, 2, 0};

        for (int i = 0; i < killPatterns.length; i++) {
            if (handleKillPattern(killPatterns[i], targetIndices[i])) {
                return;
            }
        }
    }

    private boolean tryCenterKill() {
        int[] centerIndices = {4, 14, 10, 20, 12};
        for (int index : centerIndices) {
            Circle circle = circleArray[index];
            if (WHITE.equals(circle.getPieceType()) && hasNeighborWith4B(circle)) {
                panel.segment("trying to kill piece at :"+circle.getIndex());
                gameClickHandler.killPiece(circle.getPiece());
                return true;
            }
        }
        return false;
    }
    private boolean handleKillPattern(String[] pattern, int pieceIndex) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                Circle targetCircle = circleArray[patterns[i][pieceIndex]];
                if (hasNeighborWith3W(targetCircle)) {
                    return handleWhitePieceRemoval(patterns[i]);
                }
                gameClickHandler.killPiece(targetCircle.getPiece());
                return true;
            }
        }
        return false;
    }

    private boolean handleWhitePieceRemoval(int[] patternIndices) {
        for (int index : new int[]{0, 2}) {
            Circle circle = circleArray[patternIndices[index]];
            if (circle.getPiece() != null && WHITE.equals(circle.getPieceType())) {
                panel.segment("trying to move piece at :"+circle.getIndex());
                gameClickHandler.killPiece(circle.getPiece());
                return true;
            }
        }
        return false;
    }

    // Danger evaluation
    private void evaluateDanger() {
        if(game.handWrittenData2())return;
        if (evaluate3(new String[]{BLACK, NONE, BLACK}, 1) &&
                evaluate3(new String[]{NONE, BLACK, BLACK}, 0) &&
                evaluate3(new String[]{BLACK, BLACK, NONE}, 2) &&

                checkForBBN(new String[]{NONE, BLACK, BLACK}, 0, 1, 2) &&
                checkForBBN(new String[]{BLACK, BLACK, NONE}, 2, 0, 1) &&

                evaluate2(new String[]{WHITE, NONE, WHITE}, 1, 0, 2) &&
                evaluate2(new String[]{NONE, WHITE, WHITE}, 0, 1, 2) &&
                evaluate2(new String[]{WHITE, WHITE, NONE}, 2, 0, 1)) {
            evaluateMove();
        }
    }

    private boolean evaluate2(String[] pattern, int pieceIndex, int x, int y) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                Circle a = circleArray[patterns[i][x]];
                Circle b = circleArray[patterns[i][y]];
                Circle target = circleArray[patterns[i][pieceIndex]];

                if (hasNeighborsWith2W(target, a, b) && hasNeighborWithB(target)) {
                    performClickNow(target);
                    return false;
                }
            }
        }
        return true;
    }

    private void performClickNow(Circle circle) {
        for (int neighborIndex : circle.getNeighbour()) {
            Circle neighbor = circleArray[neighborIndex];
            if (neighbor.getPiece() != null && BLACK.equals(neighbor.getPieceType())) {
                pieceClicked(neighbor.getPiece());
                circleClicked(circle);
                return;
            }
        }
    }

    private boolean evaluate(String[] pattern, int pieceIndex) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                Circle targetCircle = circleArray[patterns[i][pieceIndex]];
                for (int neighborIndex : targetCircle.getNeighbour()) {
                    Circle neighbor = circleArray[neighborIndex];
                    if (neighbor.getPiece() != null && BLACK.equals(neighbor.getPieceType())) {
                        if (!hasNeighborsWith2W(neighbor) && !hasNeighborWith3W(neighbor)) {
                            pieceClicked(neighbor.getPiece());
                            circleClicked(targetCircle);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // Move evaluation
    private void evaluateMove() {
        if (makeGoal(new String[]{BLACK, BLACK, BLACK})) {
            return;
        }

        if (createMove1(new String[]{BLACK, NONE, NONE}, 1, 2, 0) ||
                createMove1(new String[]{NONE, NONE, BLACK}, 0, 1, 2) ||
                createMove1(new String[]{NONE, BLACK, NONE}, 0, 2, 1)) {
            return;
        }

        if (createMove2(new String[]{NONE, NONE, NONE})) {
            return;
        }

        if (makeGoal1(new String[]{BLACK, BLACK, BLACK})) {
            return;
        }

        evaluateMove1();
    }

    private boolean createMove1(String[] pattern, int pos, int pos1, int known) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                if (hasNeighborWith3B(circleArray[patterns[i][pos]]) &&
                        hasNeighborWith3B(circleArray[patterns[i][pos1]])) {

                    for (int neighborIndex : circleArray[patterns[i][pos]].getNeighbour()) {
                        Circle neighbor = circleArray[neighborIndex];
                        if (BLACK.equals(neighbor.getPieceType()) &&
                                neighbor != circleArray[patterns[i][known]]) {
                            pieceClicked(neighbor.getPiece());
                            circleClicked(circleArray[patterns[i][pos]]);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean createMove2(String[] pattern) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                Circle circle0 = circleArray[patterns[i][0]];
                Circle circle1 = circleArray[patterns[i][1]];
                Circle circle2 = circleArray[patterns[i][2]];

                if (hasNeighborWithB(circle0) && hasNeighborWithB(circle1) && hasNeighborWithB(circle2)) {
                    for (int neighborIndex : circle1.getNeighbour()) {
                        Circle neighbor = circleArray[neighborIndex];
                        if (BLACK.equals(neighbor.getPieceType())) {
                            pieceClicked(neighbor.getPiece());
                            circleClicked(circle1);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean makeGoal(String[] pattern) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                for (int j = 0; j < 3; j++) {
                    Circle circle = circleArray[patterns[i][j]];
                    if (!hasNeighborWithW(circle) && hasNeighborWithN(circle)) {
                        Circle target = findEmptyNeighbor(circle);
                        if (target != null) {
                            pieceClicked(circle.getPiece());
                            circleClicked(target);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean makeGoal1(String[] pattern) {
        for (int i = 0; i < block.length; i++) {
            if (Arrays.equals(block[i], pattern)) {
                for (int j = 0; j < 3; j++) {
                    Circle circle = circleArray[patterns[i][j]];
                    if (hasNeighborWithN(circle)) {
                        Circle target = findEmptyNeighbor(circle);
                        if (target != null) {
                            pieceClicked(circle.getPiece());
                            circleClicked(target);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Circle findEmptyNeighbor(Circle circle) {
        for (int neighborIndex : circle.getNeighbour()) {
            Circle neighbor = circleArray[neighborIndex];
            if (NONE.equals(neighbor.getPieceType())) {
                return neighbor;
            }
        }
        return null;
    }

    public void evaluateMove1() {
        if (game.beaterMove() || game.moving()) {
            return;
        }
        if (evaluate(new String[]{BLACK, NONE, NONE}, 1) ||
                evaluate(new String[]{BLACK, NONE, NONE}, 2) ||
                evaluate(new String[]{NONE, NONE, BLACK}, 0) ||
                evaluate(new String[]{NONE, NONE, BLACK}, 1) ||
                evaluate(new String[]{NONE, BLACK, NONE}, 0) ||
                evaluate(new String[]{NONE, BLACK, NONE}, 2) ||

                evaluate(new String[]{WHITE, NONE, NONE}, 1) ||
                evaluate(new String[]{WHITE, NONE, NONE}, 2) ||
                evaluate(new String[]{NONE, NONE, WHITE}, 0) ||
                evaluate(new String[]{NONE, NONE, WHITE}, 1) ||
                evaluate(new String[]{NONE, WHITE, NONE}, 0) ||
                evaluate(new String[]{NONE, WHITE, NONE}, 2) ||

                evaluate(new String[]{BLACK, WHITE, NONE}, 2) ||
                evaluate(new String[]{WHITE, BLACK, NONE}, 2) ||
                evaluate(new String[]{BLACK, NONE, WHITE}, 1) ||
                evaluate(new String[]{WHITE, NONE, BLACK}, 1) ||
                evaluate(new String[]{NONE, BLACK, WHITE}, 0) ||
                evaluate(new String[]{NONE, WHITE, BLACK}, 0)) {
            return;
        }
        randomMove();
    }

    private void randomMove() {

        if (game.useDataPatterns3() || game.useDataPatterns4() || game.handWrittenData3() || game.handWrittenData1()) {
            return;
        }
        if (createMove()) {
            panel.segment("your turn");
        } else {
            for (Circle circle : circleArray) {
                if (NONE.equals(circle.getPieceType()) && hasNeighborWithB(circle)) {
                    useNeighborWithB(circle);
                    return;
                }
            }
            panel.segment("i can not move");
        }
    }

    private boolean createMove() {
        String[][] segmentData = panel.segmentData;
        int[][] segmentPatterns = panel.getSegmentPatterns();

        for (int c = 0; c < segmentData.length; c++) {
            if ("N".equals(segmentData[c][0]) && "B".equals(segmentData[c][1]) &&
                    isDanger(circleArray[segmentPatterns[c][1]]) && !hasNeighborWith3W(circleArray[segmentPatterns[c][1]])) {
                pieceClicked(circleArray[segmentPatterns[c][1]].getPiece());
                circleClicked(circleArray[segmentPatterns[c][0]]);
                return true;
            }
            if ("N".equals(segmentData[c][1]) && "B".equals(segmentData[c][0]) &&
                    isDanger(circleArray[segmentPatterns[c][0]]) && !hasNeighborWith3W(circleArray[segmentPatterns[c][0]])) {
                pieceClicked(circleArray[segmentPatterns[c][0]].getPiece());
                circleClicked(circleArray[segmentPatterns[c][1]]);
                return true;
            }
        }
        return false;
    }

    private boolean isDanger(Circle circle) {
        int known = circle.getIndex();
        for (int i = 0; i < block.length; i++) {
            if ((Arrays.equals(block[i], new String[]{BLACK, WHITE, WHITE}) && patterns[i][0] == known && hasNeighborsWith2W(circle)) ||
                    (Arrays.equals(block[i], new String[]{WHITE, WHITE, BLACK}) && patterns[i][2] == known && hasNeighborsWith2W(circle))) {
                return false;
            }
        }
        return true;
    }

    // Functional interface for strategy pattern
    private interface MoveStrategy {
        boolean execute();
    }
}