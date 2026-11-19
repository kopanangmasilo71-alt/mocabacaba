package com.masilo.morabarabacollection;

/**
 * update the game state
 */
public class PanelUpdater {

    public Circle[] circleArray;
    public Piece[] blackPieces;
    public Piece[] whitePieces;
    public String[][] block;
    private final int[][] patterns;
    public String[] mapArray;
    public String[][] segmentData;
    private final int[][] segmentPatterns;

    public PanelUpdater(Piece[] blackPieces, Piece[] whitePieces, Circle[] circleArray, String[][] block, int[][] patterns, String[] mapArray,String[][] segmentData,int[][] segmentPatterns) {
        this.blackPieces = blackPieces;
        this.whitePieces = whitePieces;
        this.circleArray = circleArray;
        this.patterns = patterns;
        this.block = block;
        this.segmentData = segmentData;
        this.segmentPatterns = segmentPatterns;
        this.mapArray = mapArray;

    }

    // Method to initialize pieces


    // Method to start the game and update piece positions
    public void start(Circle previous,Circle target, Piece moving) {
        // Update positions of black pieces
        if(previous != null) {
            if (previous.getPiece() != null) {
                previous.resetPiece();
            }
        }
        if (target != null) {
            target.setMyPiece(moving, moving.isBlack() ? "B" : "W");
            moving.setMyCircle(target);
        }
        updateBlock();
        updateSegments();
    }
    // Method to update the block and map arrays based on current board state
    public void updateBlock() {
        for (int i = 0; i < patterns.length; i++) {
            for (int j = 0; j < patterns[i].length; j++) {
                String pieceType = circleArray[patterns[i][j]].getPieceType();
                switch (pieceType) {
                    case "N":
                        block[i][j] = "N";
                        mapArray[patterns[i][j]] = "N";
                        break;
                    case "B":
                        block[i][j] = "B";
                        mapArray[patterns[i][j]] = "B";
                        break;
                    default:
                        block[i][j] = "W";
                        mapArray[patterns[i][j]] = "W";
                        break;
                }
            }
        }
    }
    public void updateSegments(){
        for (int i = 0; i < segmentPatterns.length; i++) {
            for (int j = 0; j < segmentPatterns[i].length; j++) {
                String pieceType = circleArray[segmentPatterns[i][j]].getPieceType();
                switch (pieceType) {
                    case "N":
                        segmentData[i][j] = "N";
                        break;
                    case "B":
                        segmentData[i][j] = "B";
                        break;
                    default:
                        segmentData[i][j] = "W";
                        break;
                }
            }
        }
    }
}
