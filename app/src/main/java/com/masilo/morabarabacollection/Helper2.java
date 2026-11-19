package com.masilo.morabarabacollection;

import android.annotation.SuppressLint;

public class Helper2 {
    public double increment;
    private final Circle[] circleArray;
    private final Piece[] blackPieces;
    private final Piece[] whitePieces;
    private final Line[] lineArray;
    private final Circle whiteStater;
    private final Circle whiteContainer;
    private final Circle blackContainer;
    private final Circle blackStater;
    private final Circle blackTarget;
    private final Circle turnCircle1;
    private final Circle turnCircle;
    private final Circle whiteTarget;
    private float cellHeight = 0;
    private float cellWidth = 0;
    private final Piece turnPiece;
    private final Piece turnPiece1;

    public Helper2(Circle[] circleArray, Piece[] blackPieces,
                   Piece[] whitePieces, Line[] lineArray, Circle whiteStater,
                   Circle whiteContainer, Circle blackContainer, Circle blackStater, Circle blackTarget,
                   Circle turnCircle, Circle turnCircle1, Piece turnPiece, Piece turnPiece1, Circle whiteTarget) {

        this.lineArray = lineArray;
        this.blackPieces = blackPieces;
        this.whitePieces = whitePieces;
        this.circleArray = circleArray;
        this.whiteContainer = whiteContainer;
        this.blackContainer = blackContainer;
        this.blackStater = blackStater;
        this.blackTarget = blackTarget;
        this.whiteStater = whiteStater;
        this.whiteTarget = whiteTarget;
        this.turnCircle = turnCircle;
        this.turnCircle1 = turnCircle1;
        this.turnPiece = turnPiece;
        this.turnPiece1 = turnPiece1;

    }

    public void start(int w, int h) {
        boolean isPortrait = h > w;
        float effW = isPortrait ? h : w;
        float effH = isPortrait ? w : h;

        cellWidth = effW / 16.0f;
        cellHeight = effH / 14.0f;
        increment = cellHeight / 12.0f;
        double incrementHeight = cellHeight / 2.0f;

        // Set radius for all circles
        for (Circle circle : circleArray) {
            if (circle != null)
                circle.setRadius((float) (incrementHeight + increment));
        }
        
        // Outer Square logical coords
        float left = cellWidth * 3.0f;
        float right = cellWidth * 13.0f;
        float top = cellHeight * 2.0f;
        float bottom = cellHeight * 12.0f;
        float midX = (left + right) / 2.0f;
        float midY = (top + bottom) / 2.0f;

        setCirclePos(circleArray[0], left, top, isPortrait);
        setCirclePos(circleArray[1], midX, top, isPortrait);
        setCirclePos(circleArray[2], right, top, isPortrait);
        setCirclePos(circleArray[3], right, midY, isPortrait);
        setCirclePos(circleArray[4], right, bottom, isPortrait);
        setCirclePos(circleArray[5], midX, bottom, isPortrait);
        setCirclePos(circleArray[6], left, bottom, isPortrait);
        setCirclePos(circleArray[7], left, midY, isPortrait);

        // Middle Square
        float left1 = left + cellWidth * 2.0f;
        float right1 = right - cellWidth * 2.0f;
        float top1 = top + cellHeight * 2.0f;
        float bottom1 = bottom - cellHeight * 2.0f;

        setCirclePos(circleArray[8], left1, top1, isPortrait);
        setCirclePos(circleArray[9], midX, top1, isPortrait);
        setCirclePos(circleArray[10], right1, top1, isPortrait);
        setCirclePos(circleArray[11], right1, midY, isPortrait);
        setCirclePos(circleArray[12], right1, bottom1, isPortrait);
        setCirclePos(circleArray[13], midX, bottom1, isPortrait);
        setCirclePos(circleArray[14], left1, bottom1, isPortrait);
        setCirclePos(circleArray[15], left1, midY, isPortrait);

        // Inner Square
        float left2 = left1 + cellWidth * 1.5f;
        float right2 = right1 - cellWidth * 1.5f;
        float top2 = top1 + cellHeight * 1.5f;
        float bottom2 = bottom1 - cellHeight * 1.5f;
        
        setCirclePos(circleArray[16], left2, top2, isPortrait);
        setCirclePos(circleArray[17], midX, top2, isPortrait);
        setCirclePos(circleArray[18], right2, top2, isPortrait);
        setCirclePos(circleArray[19], right2, midY, isPortrait);
        setCirclePos(circleArray[20], right2, bottom2, isPortrait);
        setCirclePos(circleArray[21], midX, bottom2, isPortrait);
        setCirclePos(circleArray[22], left2, bottom2, isPortrait);
        setCirclePos(circleArray[23], left2, midY, isPortrait);

        initialisePieces(isPortrait);

        // Neighbors (Indice-based, stay same)
        circleArray[0].setNeighbors(new int[]{1, 7, 8});
        circleArray[1].setNeighbors(new int[]{0, 2, 9});
        circleArray[2].setNeighbors(new int[]{1, 3, 10});
        circleArray[3].setNeighbors(new int[]{2, 4, 11});
        circleArray[4].setNeighbors(new int[]{3, 5, 12});
        circleArray[5].setNeighbors(new int[]{4, 6, 13});
        circleArray[6].setNeighbors(new int[]{5, 7, 14});
        circleArray[7].setNeighbors(new int[]{6, 0, 15});
        circleArray[8].setNeighbors(new int[]{0, 9, 15, 16});
        circleArray[9].setNeighbors(new int[]{1, 8, 10, 17});
        circleArray[10].setNeighbors(new int[]{2, 9, 11, 18});
        circleArray[11].setNeighbors(new int[]{3, 10, 12, 19});
        circleArray[12].setNeighbors(new int[]{4, 11, 13, 20});
        circleArray[13].setNeighbors(new int[]{5, 12, 14, 21});
        circleArray[14].setNeighbors(new int[]{6, 13, 15, 22});
        circleArray[15].setNeighbors(new int[]{7, 14, 8, 23});
        circleArray[16].setNeighbors(new int[]{8, 17, 23});
        circleArray[17].setNeighbors(new int[]{9, 16, 18});
        circleArray[18].setNeighbors(new int[]{10, 17, 19});
        circleArray[19].setNeighbors(new int[]{11, 18, 20});
        circleArray[20].setNeighbors(new int[]{12, 19, 21});
        circleArray[21].setNeighbors(new int[]{13, 20, 22});
        circleArray[22].setNeighbors(new int[]{14, 21, 23});
        circleArray[23].setNeighbors(new int[]{15, 22, 16});

        // Lines (Automatic)
        lineArray[0].setMyCircles(new Circle[]{circleArray[0], circleArray[1]});
        lineArray[1].setMyCircles(new Circle[]{circleArray[1], circleArray[2]});
        lineArray[2].setMyCircles(new Circle[]{circleArray[2], circleArray[3]});
        lineArray[3].setMyCircles(new Circle[]{circleArray[3], circleArray[4]});
        lineArray[4].setMyCircles(new Circle[]{circleArray[4], circleArray[5]});
        lineArray[5].setMyCircles(new Circle[]{circleArray[5], circleArray[6]});
        lineArray[6].setMyCircles(new Circle[]{circleArray[6], circleArray[7]});
        lineArray[7].setMyCircles(new Circle[]{circleArray[7], circleArray[0]});
        lineArray[8].setMyCircles(new Circle[]{circleArray[8], circleArray[9]});
        lineArray[9].setMyCircles(new Circle[]{circleArray[9], circleArray[10]});
        lineArray[10].setMyCircles(new Circle[]{circleArray[10], circleArray[11]});
        lineArray[11].setMyCircles(new Circle[]{circleArray[11], circleArray[12]});
        lineArray[12].setMyCircles(new Circle[]{circleArray[12], circleArray[13]});
        lineArray[13].setMyCircles(new Circle[]{circleArray[13], circleArray[14]});
        lineArray[14].setMyCircles(new Circle[]{circleArray[14], circleArray[15]});
        lineArray[15].setMyCircles(new Circle[]{circleArray[15], circleArray[8]});
        lineArray[16].setMyCircles(new Circle[]{circleArray[16], circleArray[17]});
        lineArray[17].setMyCircles(new Circle[]{circleArray[17], circleArray[18]});
        lineArray[18].setMyCircles(new Circle[]{circleArray[18], circleArray[19]});
        lineArray[19].setMyCircles(new Circle[]{circleArray[19], circleArray[20]});
        lineArray[20].setMyCircles(new Circle[]{circleArray[20], circleArray[21]});
        lineArray[21].setMyCircles(new Circle[]{circleArray[21], circleArray[22]});
        lineArray[22].setMyCircles(new Circle[]{circleArray[22], circleArray[23]});
        lineArray[23].setMyCircles(new Circle[]{circleArray[23], circleArray[16]});
        lineArray[24].setMyCircles(new Circle[]{circleArray[0], circleArray[8]});
        lineArray[25].setMyCircles(new Circle[]{circleArray[1], circleArray[9]});
        lineArray[26].setMyCircles(new Circle[]{circleArray[2], circleArray[10]});
        lineArray[27].setMyCircles(new Circle[]{circleArray[3], circleArray[11]});
        lineArray[28].setMyCircles(new Circle[]{circleArray[4], circleArray[12]});
        lineArray[29].setMyCircles(new Circle[]{circleArray[5], circleArray[13]});
        lineArray[30].setMyCircles(new Circle[]{circleArray[6], circleArray[14]});
        lineArray[31].setMyCircles(new Circle[]{circleArray[7], circleArray[15]});
        lineArray[32].setMyCircles(new Circle[]{circleArray[8], circleArray[16]});
        lineArray[33].setMyCircles(new Circle[]{circleArray[9], circleArray[17]});
        lineArray[34].setMyCircles(new Circle[]{circleArray[10], circleArray[18]});
        lineArray[35].setMyCircles(new Circle[]{circleArray[11], circleArray[19]});
        lineArray[36].setMyCircles(new Circle[]{circleArray[12], circleArray[20]});
        lineArray[37].setMyCircles(new Circle[]{circleArray[13], circleArray[21]});
        lineArray[38].setMyCircles(new Circle[]{circleArray[14], circleArray[22]});
        lineArray[39].setMyCircles(new Circle[]{circleArray[15], circleArray[23]});
    }

    private void setCirclePos(Circle c, float lx, float ly, boolean isPortrait) {
        if (isPortrait) c.setDimensions(ly, lx);
        else c.setDimensions(lx, ly);
    }

    @SuppressLint("SuspiciousIndentation")
    public void initialisePieces(boolean isPortrait) {
        double incrementWidth = cellHeight / 2.0f;
        increment = cellHeight / 12.0f;

        float wsX = cellWidth, wsY = cellHeight * 9.0f;
        float wcX = wsX + (float)incrementWidth, wcY = wsY;
        
        float wtX = cellWidth * 15 + (float)incrementWidth, wtY = cellHeight * 10;
        float btX = cellWidth * 15 + (float)incrementWidth, btY = cellHeight * 12;
        
        float bsX = cellWidth, bsY = cellHeight * 11.5f;
        float bcX = bsX + (float)incrementWidth, bcY = bsY;
        
        float tcX = cellWidth * 2.50f, tcY = cellHeight - (float)incrementWidth;
        float tc1X = cellWidth * 14.0f + (float)incrementWidth, tc1Y = cellHeight - (float)incrementWidth;

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
        whiteTarget.setRadius((float) (incrementWidth + increment * 4));
        blackTarget.setRadius((float) (incrementWidth + increment * 4));
        blackContainer.setRadius(cellWidth);
        turnCircle.setRadius((float) (incrementWidth * 2.0f));
        turnCircle1.setRadius((float) incrementWidth);

        turnPiece.setXY(turnCircle.getX(), turnCircle.getY());
        turnPiece.setOuterRadius((float) incrementWidth);

        turnPiece1.setXY(turnCircle1.getX(), turnCircle1.getY());
        turnPiece1.setOuterRadius(turnCircle1.getRadius());

        float curBsX = bsX;
        for (Piece blackPiece : blackPieces) {
            if (isPortrait) blackPiece.setXY(bsY, curBsX);
            else blackPiece.setXY(curBsX, bsY);
            curBsX += increment;
            blackPiece.setOuterRadius((float) incrementWidth * 1.7f);
        }
        
        float curWsX = wsX;
        for (Piece whitePiece : whitePieces) {
            if (isPortrait) whitePiece.setXY(wsY, curWsX);
            else whitePiece.setXY(curWsX, wsY);
            whitePiece.setOuterRadius((float) incrementWidth * 1.7f);
            curWsX += increment;
        }
    }
}
