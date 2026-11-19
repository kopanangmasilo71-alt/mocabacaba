package com.masilo.morabarabacollection;

import android.graphics.PointF;

/**
 * A unified helper class for initializing the 25-node Morabaraba board and its pieces.
 * Used by GamePanel1, NormalPanel, RabbitPanel, and RabbitPanelMultiplayer.
 */
public class MorabarabaHelper {

    /**
     * Initializes the standard 25-node Morabaraba board layout (circles, neighbors, and lines).
     */
    public static void initBoard(int w, int h, Circle[] circleArray, Line[] lineArray) {
        boolean isPortrait = h > w;
        float effW = isPortrait ? h : w;
        float effH = isPortrait ? w : h;

        float cellWidth = effW / 16.0f;
        float cellHeight = effH / 14.0f;
        float increment = cellHeight / 12.0f;
        float incrementHeight = cellHeight / 2.0f;
        float incrementWidth = cellWidth / 2.0f;

        for (Circle circle : circleArray) {
            if (circle != null) circle.setRadius(incrementHeight + increment);
        }

        // Standard 25-node positions (landscape coordinates)
        PointF[] pos = new PointF[25];
        for (int i = 0; i < 25; i++) pos[i] = new PointF();

        pos[0].set(cellWidth * 4.0f, cellHeight * 2.0f - incrementHeight);
        pos[1].set(cellWidth * 9.0f, cellHeight * 2.0f - incrementHeight);
        pos[2].set(cellWidth * 14.0f, cellHeight * 2.0f - incrementHeight);
        pos[3].set(cellWidth * 6.0f, cellHeight * 4.0f - incrementHeight - increment * 2);
        pos[4].set(cellWidth * 9.0f, cellHeight * 4.0f - incrementHeight - increment * 2);
        pos[5].set(cellWidth * 12.0f, cellHeight * 4.0f - incrementHeight - increment * 2);
        pos[6].set(cellWidth * 7.0f + incrementWidth, cellHeight * 5.0f);
        pos[7].set(cellWidth * 9.0f, cellHeight * 5.0f);
        pos[8].set(cellWidth * 10.0f + incrementWidth, cellHeight * 5.0f);
        pos[9].set(cellWidth * 4.0f, cellHeight * 7.0f);
        pos[10].set(cellWidth * 6.0f, cellHeight * 7.0f);
        pos[11].set(cellWidth * 7.0f + incrementWidth, cellHeight * 7.0f);
        pos[12].set(cellWidth * 9.0f, cellHeight * 7.0f);
        pos[13].set(cellWidth * 10.0f + incrementWidth, cellHeight * 7.0f);
        pos[14].set(cellWidth * 12.0f, cellHeight * 7.0f);
        pos[15].set(cellWidth * 14.0f, cellHeight * 7.0f);
        pos[16].set(cellWidth * 7.0f + incrementWidth, cellHeight * 8.0f + (incrementHeight * 2));
        pos[17].set(cellWidth * 9.0f, cellHeight * 8.0f + (incrementHeight * 2));
        pos[18].set(cellWidth * 10.0f + incrementWidth, cellHeight * 8.0f + (incrementHeight * 2));
        pos[19].set(cellWidth * 6.0f, cellHeight * 10.0f + incrementHeight + increment * 2);
        pos[20].set(cellWidth * 9.0f, cellHeight * 10.0f + incrementHeight + increment * 2);
        pos[21].set(cellWidth * 12.0f, cellHeight * 10.0f + incrementHeight + increment * 2);
        pos[22].set(cellWidth * 4.0f, cellHeight * 12.0f + incrementHeight);
        pos[23].set(cellWidth * 9.0f, cellHeight * 12.0f + incrementHeight);
        pos[24].set(cellWidth * 14.0f, cellHeight * 12.0f + incrementHeight);

        for (int i = 0; i < 25; i++) {
            float x = pos[i].x - incrementWidth;
            float y = pos[i].y;
            if (isPortrait) {
                circleArray[i].setDimensions(y, x);
            } else {
                circleArray[i].setDimensions(x, y);
            }
        }

        // Neighbors setup (remains indices-based, so same)
        circleArray[0].setNeighbors(new int[]{1, 3, 9});
        circleArray[1].setNeighbors(new int[]{4, 0, 2});
        circleArray[2].setNeighbors(new int[]{1, 5, 15});
        circleArray[3].setNeighbors(new int[]{0, 4, 10});
        circleArray[4].setNeighbors(new int[]{1, 3, 5, 7});
        circleArray[5].setNeighbors(new int[]{2, 4, 14});
        circleArray[6].setNeighbors(new int[]{7, 11});
        circleArray[7].setNeighbors(new int[]{4, 6, 8, 12});
        circleArray[8].setNeighbors(new int[]{7, 13});
        circleArray[9].setNeighbors(new int[]{0, 10, 22});
        circleArray[10].setNeighbors(new int[]{3, 9, 11, 19});
        circleArray[11].setNeighbors(new int[]{6, 10, 12, 16});
        circleArray[12].setNeighbors(new int[]{7, 11, 13, 17});
        circleArray[13].setNeighbors(new int[]{8, 12, 14, 18});
        circleArray[14].setNeighbors(new int[]{5, 13, 15, 21});
        circleArray[15].setNeighbors(new int[]{2, 14, 24});
        circleArray[16].setNeighbors(new int[]{11, 17});
        circleArray[17].setNeighbors(new int[]{12, 16, 18, 20});
        circleArray[18].setNeighbors(new int[]{13, 17});
        circleArray[19].setNeighbors(new int[]{10, 20, 22});
        circleArray[20].setNeighbors(new int[]{17, 19, 21, 23});
        circleArray[21].setNeighbors(new int[]{14, 20, 24});
        circleArray[22].setNeighbors(new int[]{19, 23, 9});
        circleArray[23].setNeighbors(new int[]{20, 22, 24});
        circleArray[24].setNeighbors(new int[]{15, 21, 23});

        // Lines setup (refers to circle objects, so coordinates update automatically)
        if (lineArray != null && lineArray.length >= 40) {
            int[][] connections = {
                {0, 1}, {1, 2}, {2, 15}, {15, 24}, {24, 23}, {23, 22}, {22, 9}, {0, 9},
                {0, 3}, {4, 1}, {2, 5}, {15, 14}, {21, 24}, {20, 23}, {22, 19}, {10, 9},
                {3, 4}, {4, 5}, {5, 14}, {14, 21}, {21, 20}, {19, 20}, {19, 10}, {10, 3},
                {4, 7}, {13, 14}, {20, 17}, {10, 11}, {6, 7}, {7, 8}, {8, 13}, {13, 18},
                {18, 17}, {17, 16}, {16, 11}, {11, 6}, {7, 12}, {13, 12}, {12, 17}, {11, 12}
            };
            for (int i = 0; i < connections.length; i++) {
                if (lineArray[i] != null) {
                    lineArray[i].setMyCircles(new Circle[]{circleArray[connections[i][0]], circleArray[connections[i][1]]});
                }
            }
        }
    }

    /**
     * Initializes pieces and auxiliary circles (containers, staters, targets) for Normal/Malula games.
     */
    public static void initNormalGamePieces(int w, int h, Piece[] blackPieces, Piece[] whitePieces,
                                         Circle whiteStater, Circle whiteContainer,
                                         Circle blackContainer, Circle blackStater,
                                         Circle blackTarget, Circle whiteTarget,
                                         Circle turnCircle, Circle turnCircle1,
                                         Piece turnPiece, Piece turnPiece1) {
        boolean isPortrait = h > w;
        float effW = isPortrait ? h : w;
        float effH = isPortrait ? w : h;

        float cellWidth = effW / 16.0f;
        float cellHeight = effH / 14.0f;
        float increment = cellHeight / 12.0f;
        float incrementWidth = cellHeight / 2.0f;

        // Logical positions (landscape)
        float wsX = cellWidth, wsY = cellHeight * 9.0f;
        float wcX = wsX + incrementWidth, wcY = wsY;
        
        float wtX = cellWidth * 15 + incrementWidth, wtY = cellHeight * 10;
        float btX = cellWidth * 15 + incrementWidth, btY = cellHeight * 12;
        
        float bsX = cellWidth, bsY = cellHeight * 11.5f;
        float bcX = bsX + incrementWidth, bcY = bsY;
        
        float tcX = cellWidth * 2.50f, tcY = cellHeight - incrementWidth;
        float tc1X = cellWidth * 14.0f + incrementWidth, tc1Y = cellHeight - incrementWidth;

        if (isPortrait) {
            whiteStater.setDimensions(wsY, wsX);
            whiteContainer.setDimensions(wcY, wcX);
            whiteTarget.setDimensions(wtY, wtX);
            blackTarget.setDimensions(btY, btX);
            blackStater.setDimensions(bsY, bsX);
            blackContainer.setDimensions(bcY, bcX);
            turnCircle.setDimensions(tcY, tcX);
            turnCircle1.setDimensions(tc1Y, tc1X);
        } else {
            whiteStater.setDimensions(wsX, wsY);
            whiteContainer.setDimensions(wcX, wcY);
            whiteTarget.setDimensions(wtX, wtY);
            blackTarget.setDimensions(btX, btY);
            blackStater.setDimensions(bsX, bsY);
            blackContainer.setDimensions(bcX, bcY);
            turnCircle.setDimensions(tcX, tcY);
            turnCircle1.setDimensions(tc1X, tc1Y);
        }

        whiteContainer.setRadius(cellWidth);
        whiteTarget.setRadius(incrementWidth + increment * 4);
        blackTarget.setRadius(incrementWidth + increment * 4);
        blackContainer.setRadius(cellWidth);
        turnCircle.setRadius(incrementWidth * 2.0f);
        turnCircle1.setRadius(incrementWidth);

        turnPiece.setOuterRadius(incrementWidth);
        turnPiece.setXY(turnCircle.getX(), turnCircle.getY());

        turnPiece1.setOuterRadius(turnCircle1.getRadius());
        turnPiece1.setXY(turnCircle1.getX(), turnCircle1.getY());

        float curBsX = bsX, curBsY = bsY;
        for (Piece blackPiece : blackPieces) {
            if (isPortrait) blackPiece.setXY(curBsY, curBsX);
            else blackPiece.setXY(curBsX, curBsY);
            curBsX += increment;
            blackPiece.setOuterRadius(incrementWidth * 1.7f);
        }
        
        float curWsX = wsX, curWsY = wsY;
        for (Piece whitePiece : whitePieces) {
            if (isPortrait) whitePiece.setXY(curWsY, curWsX);
            else whitePiece.setXY(curWsX, curWsY);
            whitePiece.setOuterRadius(incrementWidth * 1.7f);
            curWsX += increment;
        }
    }

    /**
     * Initializes rabbit and dog pieces for Rabbit games based on level.
     */
    public static void initRabbitGamePieces(int w, int h, String level, Circle[] circleArray, Piece[] blackPieces, Piece[] whitePieces) {
        boolean isPortrait = h > w;
        float effH = isPortrait ? w : h;
        float cellHeight = effH / 14.0f;
        for (Circle circle : circleArray) if (circle != null) circle.resetPiece();

        int rBase, rHigh, rStart, rEnd, dBase, dHigh, dStart, dEnd;
        switch (level) {
            case "LEVEL1":
                rBase=0xFF006400; rHigh=0xFF32CD32; rStart=0xFF00FF00; rEnd=0xFF008000;
                dBase=0xFF8B0000; dHigh=0xFFFF6347; dStart=0xFFFF4500; dEnd=0xFFB22222;
                break;
            case "LEVEL2":
                rBase=0xFF00008B; rHigh=0xFF1E90FF; rStart=0xFF00BFFF; rEnd=0xFF0000CD;
                dBase=0xFFFF8C00; dHigh=0xFFFFD700; dStart=0xFFFFA500; dEnd=0xFFFF4500;
                break;
            case "LEVEL3":
                rBase=0xFF4B0082; rHigh=0xFF9370DB; rStart=0xFF8A2BE2; rEnd=0xFF800080;
                dBase=0xFFFFD700; dHigh=0xFFFFFFE0; dStart=0xFFFFFF00; dEnd=0xFFBDB76B;
                break;
            case "LEVEL4":
                rBase=0xFF008B8B; rHigh=0xFF00FFFF; rStart=0xFFE0FFFF; rEnd=0xFF20B2AA;
                dBase=0xFF8B008B; dHigh=0xFFFF00FF; dStart=0xFFFF69B4; dEnd=0xFFC71585;
                break;
            case "LEVEL5":
                rBase=0xFF008080; rHigh=0xFF40E0D0; rStart=0xFFAFEEEE; rEnd=0xFF008B8B;
                dBase=0xFFFF1493; dHigh=0xFFFF69B4; dStart=0xFFFFC0CB; dEnd=0xFFC71585;
                break;
            case "LEVEL6":
                rBase=0xFFB8860B; rHigh=0xFFFFD700; rStart=0xFFFFFACD; rEnd=0xFFDAA520;
                dBase=0xFF696969; dHigh=0xFFC0C0C0; dStart=0xFFD3D3D3; dEnd=0xFFA9A9A9;
                break;
            default:
                rBase=0xFF000000; rHigh=0xFF555555; rStart=0xFFAAAAAA; rEnd=0xFF333333;
                dBase=0xFFFFFFFF; dHigh=0xFFEEEEEE; dStart=0xFFFFFFFF; dEnd=0xFFDDDDDD;
                break;
        }

        // Rabbit at center
        if (blackPieces.length > 0) {
            blackPieces[0] = new RabbitPiece(new PointF(circleArray[12].getX(), circleArray[12].getY()), true);
            blackPieces[0].setOuterRadius(cellHeight - (cellHeight / 6));
            blackPieces[0].setMyCircle(circleArray[12]);
            blackPieces[0].setColors(rBase, rHigh, rStart, rEnd);
            circleArray[12].setMyPiece(blackPieces[0], "B");
        }

        // Dogs placement based on level
        int[] dogIndices;
        switch (level) {
            case "LEVEL1":
                dogIndices = new int[]{0, 4, 20, 22, 24};
                break;
            case "LEVEL2":
            case "LEVEL3":
                dogIndices = new int[]{0, 4, 20, 24};
                break;
            case "LEVEL4":
            case "LEVEL5":
                dogIndices = new int[]{0, 4, 24};
                break;
            case "LEVEL6":
                dogIndices = new int[]{0, 24};
                break;
            default:
                dogIndices = new int[]{0, 4, 24};
                break;
        }

        for (int i = 0; i < dogIndices.length && i < whitePieces.length; i++) {
            int circleIdx = dogIndices[i];
            whitePieces[i] = new DogPiece(new PointF(circleArray[circleIdx].getX(), circleArray[circleIdx].getY()), false);
            whitePieces[i].setOuterRadius(cellHeight - (cellHeight / 4));
            whitePieces[i].setMyCircle(circleArray[circleIdx]);
            whitePieces[i].setColors(dBase, dHigh, dStart, dEnd);
            circleArray[circleIdx].setMyPiece(whitePieces[i], "W");
        }
    }
}
