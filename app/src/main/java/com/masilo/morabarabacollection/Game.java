package com.masilo.morabarabacollection;


import java.util.Arrays;

/**
 * the class contains hardcoded patterns for the game
 */
public class Game {

    private final Circle[] circleArray;
    // Replace with your actual patterns
    private final int[] builtPatterns = {12,4,20,18,6,9,15,23,1,10,14,19,2,5,22,3,24};

    private final int[][] beater = new int[][]{
            {0, 1, 2, 4},
            {0, 9, 10, 22},
            {22, 20, 23, 24},
            {2, 14, 15, 24},
            {3, 9, 10, 19},
            {3, 1, 4, 5},
            {5, 14, 15, 21},
            {19, 20, 23, 21},
            {9, 10, 19, 11},
            {9, 3, 10, 11},
            {1, 3, 4, 7},
            {1, 4, 5, 7},
            {13, 5, 14, 15},
            {13, 21, 14, 15},
            {17, 20, 21, 23},
            {17, 19, 20, 23},
            {6, 10, 11, 16},
            {6, 4, 7, 8},
            {0, 13, 14, 18},
            {16, 17, 20, 18},
            {6, 11, 12, 16},
            {6, 12, 7, 8},
            {8, 12, 13, 18},
            {16, 12, 17, 18},
            {11, 12, 17, 13},
            {7, 11, 12, 17},
            {7, 12, 13, 17},
            {11, 7, 12, 13}
    };



    private final int[] mover = new int[]{1, 9, 23, 15, 10, 4, 14, 20, 10, 10, 4, 4, 14, 14, 20, 20, 11, 7, 13, 17, 11, 7, 13, 17, 12, 12, 12, 12};
    public final int[][] handWrittenData2 = new int[][] {
            {1, 2, 9, 22, 3, 0},
            {0, 9, 23, 24, 19, 22},
            {22, 23, 2, 15, 21, 24},
            {0, 1, 15, 24, 5, 2},
            {3, 4, 14, 21, 2, 5},
            {4, 5, 10, 19, 0, 3},
            {3, 10, 20, 21, 22, 19},
            {5, 14, 19, 20, 24, 21},
            {7, 12, 20, 23, 18, 17},
            {7, 12, 20, 23, 16, 17},
            {12, 13, 9, 10, 16, 11},
            {12, 13, 9, 10, 6, 11},
            {12, 17, 1, 4, 8, 7},
            {12, 17, 1, 4, 6, 7},
            {12, 11, 14, 15, 8, 13},
            {12, 11, 14, 15, 18, 13}
    };
    public final int[][] handWrittenData1 = new int[][] {
            {4, 17, 7, 12},
            {10, 13, 11, 12},
            {12, 7, 17, 12},
            {14, 11, 13, 12},
            {12, 15, 13, 14},
            {12, 23, 17, 20},
            {12, 9, 11, 10},
            {12, 1, 4, 7},
    };
    public final int[][] handWrittenData3 = new int[][] {
            {1, 4, 13, 8, 7},
            {1, 4, 11, 6, 7},
            {1, 4, 17, 12, 7},
            {9, 10, 7, 6, 11},
            {9, 10, 17, 16, 11},
            {9, 10, 13, 12, 11},
            {23, 20, 11, 16, 17},
            {23, 20, 11, 16, 17},
            {23, 20, 7, 12, 17}
    };
    public final int[][] dataPatterns1 = new int[][]{
            {4, 5, 19, 3, 10, 3},
            {10, 3, 19, 21, 20, 19},
            {14, 5, 21, 19, 20, 21},
            {4, 5, 13, 21, 14, 5},
            {1, 0, 2, 24, 15, 2},
            {15, 2, 22, 24, 23, 24},
            {9, 0, 24, 22, 23, 22},
            {1, 2, 22, 0, 9, 0}
    };
    public final int[][] dataPatterns = new int[][]{
            {4, 1, 7, 17, 12, 7},
            {10, 9, 11, 13, 12, 11},
            {20, 7, 17, 23, 12, 17},
            {14, 11, 13, 15, 12, 13},
            {15,13,11,14,12,13},
            {1,4,7,17,12,7},
            {9,10,11,13,12,11},
            {23,20,17,7,12,17},

            {1, 0, 2, 24, 15, 2},
            {9, 0, 24, 22, 23, 22},
            {1, 2, 22, 0, 9, 0},
            {15, 2, 22, 24, 23, 24},
            {2, 0, 1, 9, 22, 0},
            {0, 9, 22, 23, 24, 22},
            {4, 5, 19, 3, 10, 3},
            {4, 5, 13, 21, 14, 5},
            {10, 3, 19, 21, 20, 19},
            {14, 5, 21, 19, 20, 21},
            {5, 3, 4, 10, 19, 3},
            {3, 10, 20, 19, 21, 19},
            {6, 11, 16, 17, 18, 16},
            {0, 1, 2, 7, 4, 1},
            {2, 0, 1, 7, 4, 1},
            {0, 9, 22, 11, 10, 9},
            {22, 0, 9, 11, 10, 9},
            {3, 9, 10, 19, 11, 10},
            {19, 9, 10, 3, 11, 10},
            {3, 1, 4, 5, 7, 4},
            {5, 1, 3, 4, 7, 4},
            {5, 14, 15, 21, 13, 14},
            {21, 5, 14, 15, 13, 14},
            {21, 19, 20, 23, 17, 20},
            {19, 20, 21, 23, 17, 20},
            {2, 13, 15, 24, 14, 15},
            {24, 13, 2, 15, 14, 15},
            {24, 17, 22, 23, 20, 23},
            {20, 17, 23, 24, 22, 23},
            {3, 4, 5, 7, 1, 4},
            {1, 3, 4, 7, 5, 4},
            {5, 5, 13, 14, 21, 14},
            {21, 5, 13, 14, 15, 14},
            {21, 17, 19, 20, 23, 20},
            {19, 17, 20, 21, 23, 20},
            {9, 3, 10, 11, 19, 10},
            {3, 10, 11, 19, 9, 10},
            {10, 6, 11, 9, 16, 11},
            {10, 11, 16, 9, 6, 11},
            {4, 1, 7, 8, 16, 7},
            {4, 1, 6, 7, 8, 7},
            {8, 13, 15, 18, 14, 13},
            {18, 8, 13, 15, 14, 13},

            {18, 16, 17, 23, 20, 17},
            {16, 17, 18, 23, 20, 17}};

    public final int[][] dataPatterns2 = new int[][]{
            {13, 8, 18, 16, 17, 18},
            {7, 6, 8, 16, 11, 6},
            {11, 6, 16, 18, 17, 16},
            {7, 6, 8, 18, 13, 8},
            {11,12,13,14,15,13},
            {11,12,13,15,14,13}};

    public final int[][] dataPatterns3 = new int[][]{
            {0,1,2,15,24},
            {3,4,5,14,21},
            {6,7,8,13,18},
            {22,9,0,1,2},
            {19,10,3,4,5},
            {16,11,6,7,8},
            {24,23,22,9,0},
            {21,20,9,10,3},
            {18,17,16,11,6},
            {2,15,24,23,22},
            {5,14,21,20,19},
            {8,13,18,17,16},
            {24,15,2,1,0},
            {21,14,5,4,3},
            {18,13,8,7,6},
            {2,1,0,9,22},
            {5,4,3,10,19},
            {8,7,6,11,16},
            {0,9,22,23,24},
            {3,10,19,20,21},
            {6,11,16,17,18},
            {22,23,24,15,2},
            {19,20,21,14,5},
            {16,17,18,13,8}};

    public final int[][] dataPatterns4 = new int[][]{
            {12,14,24,15,13},
            {12,14,2,15,13},
            {12,20,24,23,17},
            {12,20,22,23,17},
            {12,4,2,1,7},
            {12,4,0,1,7},
            {12,10,0,9,11},
            {12,10,22,9,11},
            {12,3,5,4,7},
            {7,0,2,1,4},
            {17,6,8,7,12},
            {20,11,13,12,17},
            {23,16,18,17,20},
            {11,0,22,9,10},
            {12,3,19,10,11},
            {13,6,16,11,12},
            {14,7,17,12,13},
            {15,8,18,13,14},
            {13,2,24,15,14},
            {12,5,21,14,13},
            {11,8,18,13,12},
            {10,7,17,12,11},
            {9,6,16,11,10},
            {17,22,24,23,20},
            {12,19,21,20,17},
            {7,16,18,17,12},
            {4,11,13,12,7},
            {1,6,8,7,4},
            {4,13,15,14,5},
            {20,13,15,14,21},
            {14,1,7,4,5},
            {10,1,7,4,3},
            {4,9,11,10,3},
            {20,9,11,10,19},
            {10,17,23,20,19},
            {14,17,23,20,21}};


    private final int[][] checker = new int[][]{

            {4, 14, 19, 3, 21, 20, 10, 5},
            {10, 20, 5, 3, 4, 14, 21, 19},
            {3, 14, 20, 4, 5, 10, 19, 21},
            {4, 10, 21, 5, 14, 19, 20, 3},
            {1, 9, 24, 2, 22, 15, 23, 0},
            {15, 23, 0, 1, 2, 9, 22, 24},
            {9, 23, 2, 0, 1, 15, 24, 22},
            {1, 15, 22, 0, 24, 9, 23, 2},
            {7, 11, 18, 8, 16, 13, 17, 6}

    };
    private final int[][] checker2 = new int[][]{
            {0, 3, 2, 1},
            {0, 3, 22, 9},
            {2, 5, 0, 1},
            {22, 19, 0, 9},
            {22, 19, 24, 23},
            {24, 21, 22, 23},
            {24, 21, 2, 15},
            {2, 5, 24, 15},
            {5, 2, 3, 4},
            {3, 0, 5, 4},
            {3, 19, 0, 10},
            {19, 22, 3, 10},
            {19, 22, 21, 20},
            {21, 24, 19, 20},
            {21, 24, 5, 14},
            {5, 2, 21, 14},
            {13,18,15,14},
            {13,8,15,14},
            {7,1,8,4},
            {7,1,6,4},
            {11,16,9,10},
            {11,6,9,10},
            {17,16,23,20},
            {17,18,23,20}
    };
    private Robot5 robot5 = null;
    private Robot4 robot4 = null;
    private Robot6 robot6 = null;
    private Robot3 robot3 = null;
    private Robot2 robot2= null;

    public Game(Robot2 robot, Circle[] circleArray) {
        this.robot2 = robot;
        this.circleArray = circleArray;

    }
    public Game(Robot5 robot, Circle[] circleArray) {
        this.robot5 = robot;
        this.circleArray = circleArray;
    }
    public Game(Robot4 robot, Circle[] circleArray) {
        this.robot4 = robot;
        this.circleArray = circleArray;
    }
    public Game(Robot3 robot, Circle[] circleArray) {
        this.robot3 = robot;
        this.circleArray = circleArray;
    }
    public Game(Robot6 robot, Circle[] circleArray) {
        this.robot6 = robot;
        this.circleArray = circleArray;
    }
    private void clickRobot(Piece a){
        if(robot2 != null){
            robot2.pieceClicked(a);
        }else if(robot3 != null){
            robot3.pieceClicked(a);
        }else if(robot4 != null){
            robot4.pieceClicked(a);
        }else if(robot5 != null){
            robot5.pieceClicked(a);
        }else if(robot6 != null){
            robot6.pieceClicked(a);
        }
    }
    public boolean useQuickPatterns() {
        int[][] checkers = new int[][]{
                {1,9,15,23,0,2,22,24,0},
                {4,10,14,20,3,5,19,21,21},
                {7,11,13,17,6,8,16,18,18}
        };
        String[] a = new String[]{"B", "B", "B", "B", "N", "N", "N", "N"};
        String[] b = new String[]{"W", "W", "W", "W", "N", "N", "N", "N"};
        for (int[] dataPattern : checkers) {
            String[] c = new String[]{circleArray[dataPattern[0]].getPieceType(),
                    circleArray[dataPattern[1]].getPieceType(),
                    circleArray[dataPattern[2]].getPieceType(),
                    circleArray[dataPattern[3]].getPieceType(),
                    circleArray[dataPattern[4]].getPieceType(),
                    circleArray[dataPattern[5]].getPieceType(),
                    circleArray[dataPattern[6]].getPieceType(),
                    circleArray[dataPattern[7]].getPieceType()
            };
            if (Arrays.equals(a, c) || Arrays.equals(b, c) ) {
                circleClicked(circleArray[dataPattern[8]]);
                return true;
            }
        }
        return false;
    }
    public boolean useDataPatterns() {
        String[] a = new String[]{"W", "N", "N", "N", "W"};
        for (int[] dataPattern : dataPatterns) {
            String[] c = new String[]{circleArray[dataPattern[0]].getPieceType(),
                    circleArray[dataPattern[1]].getPieceType(),
                    circleArray[dataPattern[2]].getPieceType(),
                    circleArray[dataPattern[3]].getPieceType(),
                    circleArray[dataPattern[4]].getPieceType()};
            if (Arrays.equals(a, c)) {
                circleClicked(circleArray[dataPattern[5]]);
                return true;
            }
        }
        return false;
    }
    public boolean useDataPatterns(int[][] dataPatterns) {
        String[] a = new String[]{"B", "N", "N", "N", "B"};
        for (int[] dataPattern : dataPatterns) {
            String[] c = new String[]{circleArray[dataPattern[0]].getPieceType(),
                    circleArray[dataPattern[1]].getPieceType(),
                    circleArray[dataPattern[2]].getPieceType(),
                    circleArray[dataPattern[3]].getPieceType(),
                    circleArray[dataPattern[4]].getPieceType()};
            if (Arrays.equals(a, c)) {
                circleClicked(circleArray[dataPattern[5]]);
                return true;
            }
        }
        return false;
    }
    public boolean useDataPatterns3() {
        String[] a = new String[]{"B", "B", "N", "N", "B"};
        for (int[] dataPattern : dataPatterns3) {
            String[] c = new String[]{circleArray[dataPattern[0]].getPieceType(),
                    circleArray[dataPattern[1]].getPieceType(),
                    circleArray[dataPattern[2]].getPieceType(),
                    circleArray[dataPattern[3]].getPieceType(),
                    circleArray[dataPattern[4]].getPieceType()};
            if (Arrays.equals(a, c)) {
                clickRobot(circleArray[dataPattern[4]].getPiece());
                circleClicked(circleArray[dataPattern[3]]);
                return true;
            }
        }
        return false;
    }
    public boolean handWrittenData1() {
        String[] a = new String[]{"B", "B", "B", "N"};
        for (int[] dataPattern : handWrittenData1) {
            String[] c = new String[]{circleArray[dataPattern[0]].getPieceType(),
                    circleArray[dataPattern[1]].getPieceType(),
                    circleArray[dataPattern[2]].getPieceType(),
                    circleArray[dataPattern[3]].getPieceType()};
            if (Arrays.equals(a, c)) {
                clickRobot(circleArray[dataPattern[2]].getPiece());
                circleClicked(circleArray[dataPattern[3]]);
                return true;
            }
        }
        return false;
    }
    public boolean handWrittenData2() {
        String[] a = new String[]{"B", "B", "B", "B","B","N"};
        for (int[] dataPattern : handWrittenData2) {
            String[] c = new String[]{circleArray[dataPattern[0]].getPieceType(),
                    circleArray[dataPattern[1]].getPieceType(),
                    circleArray[dataPattern[2]].getPieceType(),
                    circleArray[dataPattern[3]].getPieceType(),
                    circleArray[dataPattern[4]].getPieceType(),
                    circleArray[dataPattern[5]].getPieceType()};
            if (Arrays.equals(a, c)) {
                clickRobot(circleArray[dataPattern[4]].getPiece());
                circleClicked(circleArray[dataPattern[5]]);
                return true;
            }
        }
        return false;
    }
    public boolean handWrittenData3() {
        String[] a = new String[]{"B", "B", "B", "N","N"};
        for (int[] dataPattern : handWrittenData3) {
            String[] c = new String[]{circleArray[dataPattern[0]].getPieceType(),
                    circleArray[dataPattern[1]].getPieceType(),
                    circleArray[dataPattern[2]].getPieceType(),
                    circleArray[dataPattern[3]].getPieceType(),
                    circleArray[dataPattern[4]].getPieceType()};
            if (Arrays.equals(a, c)) {
                clickRobot(circleArray[dataPattern[2]].getPiece());
                circleClicked(circleArray[dataPattern[3]]);
                return true;
            }
        }
        return false;
    }
    public boolean useDataPatterns4() {
        String[] a = new String[]{"B", "B", "B", "N", "N"};
        for (int[] dataPattern : dataPatterns4) {
            String[] c = new String[]{circleArray[dataPattern[0]].getPieceType(),
                    circleArray[dataPattern[1]].getPieceType(),
                    circleArray[dataPattern[2]].getPieceType(),
                    circleArray[dataPattern[3]].getPieceType(),
                    circleArray[dataPattern[4]].getPieceType()};
            if (Arrays.equals(a, c)) {
                clickRobot(circleArray[dataPattern[0]].getPiece());
                circleClicked(circleArray[dataPattern[4]]);
                return true;
            }
        }
        return false;
    }
    public boolean beaterMove() {
        String[] c = new String[]{"B", "B", "B", "N"};
        String[] d = new String[]{"N", "B", "B", "B"};
        for (int i = 0; i < beater.length; i++) {
            String[] a = new String[]{circleArray[beater[i][0]].getPieceType(),
                    circleArray[beater[i][1]].getPieceType(),
                    circleArray[beater[i][2]].getPieceType(),
                    circleArray[beater[i][3]].getPieceType()};
            if (Arrays.equals(a, c)) {
                clickRobot(circleArray[mover[i]].getPiece());
                circleClicked(circleArray[beater[i][3]]);
                return true;
            }
            if (Arrays.equals(a, d)) {
                clickRobot(circleArray[mover[i]].getPiece());
                circleClicked(circleArray[beater[i][0]]);
                return true;
            }
        }
        return false;
    }

    public boolean gaming() {
        if (usable(builtPatterns)) {
            useIt(builtPatterns);
            return true;
        }
        return false;
    }

    private boolean usable(int[] a) {
        for (int j : a) {
            if (circleArray[j].getPieceType().equals("N")) {
                return true;
            }
        }
        return false;
    }

    private void useIt(int[] a) {
        for (int j : a) {
            if (circleArray[j].getPieceType().equals("N")) {
                circleClicked(circleArray[j]);
                break;
            }
        }
    }

    private void circleClicked(Circle a) {
        // Implement your click action here
        if(robot2 != null){
            robot2.circleClicked(a);
        }else if(robot3 != null){
            robot3.circleClicked(a);
        }else if(robot4 != null){
            robot4.circleClicked(a);
        }else if(robot5 != null){
            robot5.circleClicked(a);
        }else if(robot6 != null){
            robot6.circleClicked(a);
        }
    }

    public boolean checkerB() {
        String[] c = new String[]{"B", "B", "B", "N", "N", "N", "N", "N",};
        for (int[] ints : checker) {
            String[] a = new String[]{circleArray[ints[0]].getPieceType(),
                    circleArray[ints[1]].getPieceType(),
                    circleArray[ints[2]].getPieceType(),
                    circleArray[ints[3]].getPieceType(),
                    circleArray[ints[4]].getPieceType(),
                    circleArray[ints[5]].getPieceType(),
                    circleArray[ints[6]].getPieceType(),
                    circleArray[ints[7]].getPieceType()
            };
            if (Arrays.equals(a, c)) {
                circleClicked(circleArray[ints[7]]);
                return true;
            }
        }
        return false;
    }

    public boolean checkerW() {
        String[] c = new String[]{"W", "W", "W", "N", "N", "N", "N", "N",};
        for (int[] ints : checker) {
            String[] a = new String[]{circleArray[ints[0]].getPieceType(),
                    circleArray[ints[1]].getPieceType(),
                    circleArray[ints[2]].getPieceType(),
                    circleArray[ints[3]].getPieceType(),
                    circleArray[ints[4]].getPieceType(),
                    circleArray[ints[5]].getPieceType(),
                    circleArray[ints[6]].getPieceType(),
                    circleArray[ints[7]].getPieceType()
            };
            if (Arrays.equals(a, c)) {
                circleClicked(circleArray[ints[7]]);
                return true;
            }
        }
        return false;
    }

    public boolean moving() {
        String[] c = new String[]{"B", "B", "B", "N"};
        for (int[] ints : checker2) {
            String[] a = new String[]{circleArray[ints[0]].getPieceType(),
                    circleArray[ints[1]].getPieceType(),
                    circleArray[ints[2]].getPieceType(),
                    circleArray[ints[3]].getPieceType()
            };
            if (Arrays.equals(a, c)) {
                clickRobot(circleArray[ints[0]].getPiece());
                circleClicked(circleArray[ints[3]]);
                return true;
            }
        }
        return false;
    }

}