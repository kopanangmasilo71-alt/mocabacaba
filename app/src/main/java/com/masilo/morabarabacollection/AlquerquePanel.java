package com.masilo.morabarabacollection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AlquerquePanel extends SurfaceView implements SurfaceHolder.Callback {

    private AlquerqueRobot robot;
    private static final int BASE_NODES = 13;
    private static final int STACKS = 1;
    public final Circle[] circleArray = new Circle[BASE_NODES * STACKS];
    private static final int[][] BASE_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 5}, {5, 8}, {8, 7}, {7, 6}, {6, 3}, {3, 0},
            {1, 4}, {4, 7}, {3, 4}, {4, 5},
            {0, 9}, {9, 4}, {1, 9}, {9, 3},
            {1, 10}, {10, 5}, {2, 10}, {10, 4},
            {3, 11}, {11, 7}, {6, 11}, {11, 4},
            {4, 12}, {12, 8}, {5, 12}, {12, 7}
    };

    private static final int[][] MILLS = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Horizontal
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8}  // Vertical
    };
    public Line[] lineArray;
    private boolean gameOnPause = false;
    public boolean isPlayer1Turn = true;
    public boolean isPlacementPhase = true;
    public boolean isRemovePiecePhase = false;
    public Animator1 animator = null;
    public PanelUpdater1 panelUpdater;
    public final Piece[] player1Pieces = new Piece[6];
    public final Piece[] player2Pieces = new Piece[6];
    private int player1PiecesToPlace = 6;
    private int player2PiecesToPlace = 6;
    private Circle player1Container, player2Container;
    private Paint linePaint, shadowLinePaint, socketBodyPaint, socketShadowPaint, socketHighlightPaint, validMovePaint, highlightPaint, backgroundPaint, grainPaint, containerPaint;
    private int[][] segmentPatterns;
    private int boardWidth;
    private int boardHeight;
    private GameThread gameThread;
    public Piece clickedPiece;
    public int movesCount = 0;
    private boolean isFirstLayout = true;
    
    private TextView goalW, goalB, gameMessages;

    // Theme support

    public AlquerquePanel(Context context) { super(context); init(); }
    public AlquerquePanel(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public AlquerquePanel(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }
    
    public void setViews(TextView w, TextView b, TextView gameMessages) {
        this.goalW = w;
        this.goalB = b;
        this.gameMessages = gameMessages;
        updateScoreUI();
    }

    private void init() {

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#C5A059"));
        linePaint.setStrokeWidth(10);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);

        grainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grainPaint.setColor(Color.parseColor("#1A000000"));
        grainPaint.setStrokeWidth(1f);

        shadowLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowLinePaint.setColor(Color.parseColor("#66000000"));
        shadowLinePaint.setStrokeWidth(10);
        shadowLinePaint.setStyle(Paint.Style.STROKE);
        shadowLinePaint.setStrokeCap(Paint.Cap.ROUND);

        socketBodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        socketBodyPaint.setColor(Color.parseColor("#2D1B15"));
        socketBodyPaint.setStyle(Paint.Style.FILL);

        socketShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        socketShadowPaint.setColor(Color.parseColor("#AA000000"));
        socketShadowPaint.setStyle(Paint.Style.STROKE);
        socketShadowPaint.setStrokeWidth(3);


        socketHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        socketHighlightPaint.setColor(Color.parseColor("#33FFFFFF"));
        socketHighlightPaint.setStyle(Paint.Style.STROKE);
        socketHighlightPaint.setStrokeWidth(3);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.CYAN);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(6);
        highlightPaint.setShadowLayer(12.0f, 0.0f, 0.0f, Color.CYAN);

        validMovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        validMovePaint.setColor(Color.parseColor("#9900FF00"));
        validMovePaint.setStyle(Paint.Style.FILL);
        validMovePaint.setShadowLayer(8.0f, 0.0f, 0.0f, Color.GREEN);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#4A4E69"));


        containerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        containerPaint.setColor(Color.parseColor("#805D4037")); // Semi-transparent brown
        containerPaint.setStyle(Paint.Style.FILL);

        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this,"ap");
        setFocusable(true);

        initPatterns();
        initPieces();
        robot = new AlquerqueRobot(this);

        panelUpdater = new PanelUpdater1(player1Pieces, player2Pieces, circleArray);
        animator = new Animator1(this);
        anim();
    }
    public void anim() {
        long baseDuration = 1000;
        if (animator != null) animator.setDuration(baseDuration);
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread(getHolder(), this,"ap");
            gameThread.setRunning(true);
            gameThread.start();
        }
        requestRender();
    }
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (width <= 0 || height <= 0) return;
        int bgColor = 0xFF4A4E69;
        int secondaryColor = 0xFF22223B;

        Shader shader = new RadialGradient(
                width / 2f, height / 2f,
                Math.max(width, height) * 0.85f,
                new int[]{bgColor, secondaryColor},
                new float[]{0.2f, 1.0f},
                Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(shader);

        this.boardWidth = width;
        this.boardHeight = height;
        recomputeGeometry();
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 100);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 300);
    }
    private void recomputeGeometry() {
        initializeBoardPositions();
        initializeLineConnections();
        if (isFirstLayout) {
            initializePiecePositionsOnBoard();
            isFirstLayout = false;
        } else {
            syncPiecePositions();
        }
        requestRender();
    }

    private void syncPiecePositions() {
        for (int i = 0; i < player1Pieces.length; i++) {
            Piece p = player1Pieces[i];
            if (p == null) continue;
            if (p.getMyCircle() != null) {
                p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
                p.setOuterRadius(p.getMyCircle().getRadius() * 1.4f);
            } else {
                // Still in container
                float angle = (float) (i * 2 * Math.PI / player1Pieces.length);
                float lx = player1Container.getX() + (float) (Math.cos(angle) * player1Container.getRadius() * 0.6);
                float ly = player1Container.getY() + (float) (Math.sin(angle) * player1Container.getRadius() * 0.6);
                p.setXY(lx, ly);
                p.setOuterRadius(player1Container.getRadius() / 4);
            }
        }
        for (int i = 0; i < player2Pieces.length; i++) {
            Piece p = player2Pieces[i];
            if (p == null) continue;
            if (p.getMyCircle() != null) {
                p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
                p.setOuterRadius(p.getMyCircle().getRadius() * 1.4f);
            } else {
                // Still in container
                float angle = (float) (i * 2 * Math.PI / player2Pieces.length);
                float lx = player2Container.getX() + (float) (Math.cos(angle) * player2Container.getRadius() * 0.6);
                float ly = player2Container.getY() + (float) (Math.sin(angle) * player2Container.getRadius() * 0.6);
                p.setXY(lx, ly);
                p.setOuterRadius(player2Container.getRadius() / 4);
            }
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) { pause(); }
    private void initPatterns() {
        segmentPatterns = BASE_CONNECTIONS;
        lineArray = new Line[segmentPatterns.length];
    }
    private void initializeBoardPositions() {
        boolean isPortrait = boardHeight > boardWidth;
        float effW = isPortrait ? boardHeight : boardWidth;
        float effH = isPortrait ? boardWidth : boardHeight;

        float cx = effW / 2f;
        float cy = effH / 2f;
        float boardSize = Math.min(effW, effH) * 0.85f;
        float step = boardSize / 2f;
        float halfStep = step / 2f;
        float radius = boardSize / 20f;

        PointF[] pos = new PointF[BASE_NODES];
        for (int i = 0; i < BASE_NODES; i++) pos[i] = new PointF();

        pos[0].set(cx - step, cy - step);
        pos[1].set(cx, cy - step);
        pos[2].set(cx + step, cy - step);
        pos[3].set(cx - step, cy);
        pos[4].set(cx, cy);
        pos[5].set(cx + step, cy);
        pos[6].set(cx - step, cy + step);
        pos[7].set(cx, cy + step);
        pos[8].set(cx + step, cy + step);
        pos[9].set(cx - halfStep, cy - halfStep);
        pos[10].set(cx + halfStep, cy - halfStep);
        pos[11].set(cx - halfStep, cy + halfStep);
        pos[12].set(cx + halfStep, cy + halfStep);

        for (int i = 0; i < BASE_NODES; i++) {
            if (circleArray[i] == null) circleArray[i] = new Circle(0,0,0);
            if (isPortrait) circleArray[i].setDimensions(pos[i].y, pos[i].x);
            else circleArray[i].setDimensions(pos[i].x, pos[i].y);
            circleArray[i].setRadius(radius);
            circleArray[i].setIndex(i);
        }

        float p1cX = effW * 0.1f, p1cY = effH / 2f;
        float p2cX = effW * 0.9f, p2cY = effH / 2f;
        float cR = effW * 0.08f;

        if (player1Container == null) player1Container = new Circle(0,0,0);
        if (player2Container == null) player2Container = new Circle(0,0,0);

        if (isPortrait) {
            player1Container.setDimensions(p1cY, p1cX);
            player2Container.setDimensions(p2cY, p2cX);
        } else {
            player1Container.setDimensions(p1cX, p1cY);
            player2Container.setDimensions(p2cX, p2cY);
        }
        player1Container.setRadius(cR);
        player2Container.setRadius(cR);

        for (int i = 0; i < circleArray.length; i++) {
            if (circleArray[i] == null) continue;
            List<Integer> neighborIndices = new ArrayList<>();
            for (int[] seg : segmentPatterns) {
                if (seg[0] == i) neighborIndices.add(seg[1]);
                else if (seg[1] == i) neighborIndices.add(seg[0]);
            }
            circleArray[i].setNeighbors(neighborIndices.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    private void initPieces() {
        int p1Base=0xFF006400, p1High=0xFF32CD32, p1Start=0xFF00FF00, p1End=0xFF008000;
        int p2Base=0xFF8B0000, p2High=0xFFFF6347, p2Start=0xFFFF4500, p2End=0xFFB22222;

        for (int i = 0; i < circleArray.length; i++) {
            if(circleArray[i] == null) circleArray[i] = new Circle(0, 0, 0);
        }
        for (int i = 0; i < player1Pieces.length; i++) {
            player1Pieces[i] = new Piece(new PointF(0, 0), true);
            player1Pieces[i].setType("B");
            player1Pieces[i].setColors(p1Base, p1High, p1Start, p1End);
            player1Pieces[i].setIconType(Piece.IconType.CIRCLE);
        }
        for (int i = 0; i < player2Pieces.length; i++) {
            player2Pieces[i] = new Piece(new PointF(0, 0), false);
            player2Pieces[i].setType("W");
            player2Pieces[i].setColors(p2Base, p2High, p2Start, p2End);
            player2Pieces[i].setIconType(Piece.IconType.CIRCLE);
        }
    }
    private void initializeLineConnections() {
        for (int i = 0; i < segmentPatterns.length; i++) {
            int[] seg = segmentPatterns[i];
            if (seg[0] < circleArray.length && seg[1] < circleArray.length) {
                Circle c1 = circleArray[seg[0]];
                Circle c2 = circleArray[seg[1]];
                if (c1 != null && c2 != null) {
                    if (lineArray[i] == null) lineArray[i] = new Line(0,0,0,0);
                    lineArray[i].setDimensions(c1.getX(), c1.getY(), c2.getX(), c2.getY());
                }
            }
        }
    }
    public void initializePiecePositionsOnBoard() {
        for (Circle c : circleArray) if (c != null) c.resetPiece();

        for (int i = 0; i < player1Pieces.length; i++) {
            float angle = (float) (i * 2 * Math.PI / player1Pieces.length);
            float lx = player1Container.getX() + (float) (Math.cos(angle) * player1Container.getRadius() * 0.6);
            float ly = player1Container.getY() + (float) (Math.sin(angle) * player1Container.getRadius() * 0.6);
            player1Pieces[i].setXY(lx, ly);
            player1Pieces[i].setOuterRadius(player1Container.getRadius() / 4);
        }

        for (int i = 0; i < player2Pieces.length; i++) {
            float angle = (float) (i * 2 * Math.PI / player2Pieces.length);
            float lx = player2Container.getX() + (float) (Math.cos(angle) * player2Container.getRadius() * 0.6);
            float ly = player2Container.getY() + (float) (Math.sin(angle) * player2Container.getRadius() * 0.6);
            player2Pieces[i].setXY(lx, ly);
            player2Pieces[i].setOuterRadius(player2Container.getRadius() / 4);
        }

        panelUpdater = new PanelUpdater1(player1Pieces, player2Pieces, circleArray);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOnPause) return false;

        float sx = event.getX();
        float sy = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isPlayer1Turn) {
                    for (Piece p : player1Pieces) {
                        if (p.isClicked(sx, sy)) {
                            clickedPiece = p;
                            requestRender();
                            return true;
                        }
                    }
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                return true;

            case MotionEvent.ACTION_UP:
                if (!isPlayer1Turn) return false;
                handleTap(sx, sy);
                performClick();
                return true;
        }
        return super.onTouchEvent(event);
    }
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
    private void handleTap(float sx, float sy) {
        for (Circle c : circleArray) {
            if (c != null && c.isClicked(sx, sy)) {
                SoundManager.getInstance(getContext()).playClick();
                circleClicked(c);
                return;
            }
        }
        clickedPiece = null;
        requestRender();
    }
    public void circleClicked(Circle circle) {
        if (isRemovePiecePhase) {
            handleRemovePiece(circle);
            return;
        }

        if (isPlacementPhase) {
            handlePlacement(circle);
        } else {
            handleMovement(circle);
        }
    }
    private void handlePlacement(Circle circle) {
        if (circle.getPiece() != null) {
            displayMessage("Position already occupied");
            return;
        }

        Piece pieceToPlace;
        if (isPlayer1Turn) {
            if (clickedPiece == null) {
                displayMessage("Select a piece from your container first");
                return;
            }
            pieceToPlace = clickedPiece;
        } else { // Robot's turn
            if (player2PiecesToPlace <= 0) return;
            pieceToPlace = player2Pieces[player2Pieces.length - player2PiecesToPlace];
        }

        if (pieceToPlace != null) {
            placePiece(pieceToPlace, circle);
            if (checkMill(circle)) {
                isRemovePiecePhase = true;
                displayMessage("Mill formed! Remove an opponent's piece.");
            } else {
                endTurn();
            }
        }
        clickedPiece = null;
        if (player1PiecesToPlace == 0 && player2PiecesToPlace == 0) {
            isPlacementPhase = false;
            displayMessage("Movement phase begins!");
        }
        requestRender();
    }
    private void handleMovement(Circle circle) {
        if (clickedPiece == null) {
            if (circle.getPiece() != null && circle.getPiece().isBlack() == isPlayer1Turn) {
                clickedPiece = circle.getPiece();
            }
        } else {
            if (circle.getPiece() == null && isNeighbor(clickedPiece.getMyCircle(), circle)) {
                movePiece(clickedPiece, circle);
                if (checkMill(circle)) {
                    isRemovePiecePhase = true;
                    displayMessage("Mill formed! Remove an opponent's piece.");
                } else {
                    endTurn();
                }
            } else {
                displayMessage("Invalid move");
            }
            clickedPiece = null;
        }
        requestRender();
    }

    private void handleRemovePiece(Circle circle) {
        Piece pieceToRemove = circle.getPiece();
        if (pieceToRemove == null || pieceToRemove.isBlack() == isPlayer1Turn) {
            displayMessage("You must remove an opponent's piece");
            return;
        }

        if (isPieceInMill(pieceToRemove)) {
            displayMessage("Cannot remove a piece that is part of a mill");
            return;
        }
        circle.resetPiece();
        isRemovePiecePhase = false;
        updateScoreUI();
        endTurn();
        displayMessage("Piece removed. Next player's turn.");
        requestRender();
    }
    private void endTurn() {
        isPlayer1Turn = !isPlayer1Turn;
        if (!isPlayer1Turn) { // It's robot's turn
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isRemovePiecePhase) {
                    robot.removePlayerPiece();
                } else {
                    robot.makeMove();
                }
            }, 1000);
        }
        checkWinConditions();
    }
    private void checkWinConditions() {
        if (!isPlacementPhase) {
            int player1PieceCount = 0;
            for(Piece p : player1Pieces) {
                if(p.getMyCircle() != null) player1PieceCount++;
            }

            int player2PieceCount = 0;
            for(Piece p : player2Pieces) {
                if(p.getMyCircle() != null) player2PieceCount++;
            }

            if (player1PieceCount < 3) {
                gameOver("Player 2 Wins!");
            } else if (player2PieceCount < 3) {
                gameOver("Player 1 Wins!");
            }
        }
    }
    private void placePiece(Piece piece, Circle target) {
        movesCount++;
        if (piece.isBlack()) {
            if (player1PiecesToPlace > 0) player1PiecesToPlace--;
        } else {
            if (player2PiecesToPlace > 0) player2PiecesToPlace--;
        }
        target.setMyPiece(piece, piece.getPieceType());
        piece.setMyCircle(target);
        animator.moveAnimation(new PointF(target.getX(), target.getY()), piece);
        piece.setOuterRadius(target.getRadius() * 1.4f);
    }
    private void movePiece(Piece piece, Circle target) {
        Circle current = piece.getMyCircle();
        if(current != null) {
            current.resetPiece();
        }
        placePiece(piece, target);
    }
    public boolean checkMill(Circle circle) {
        Piece piece = circle.getPiece();
        if (piece == null) return false;

        for (int[] mill : MILLS) {
            boolean isMill = true;
            for (int index : mill) {
                if (circleArray[index].getPiece() == null || circleArray[index].getPiece().isBlack() != piece.isBlack()) {
                    isMill = false;
                    break;
                }
            }
            if (isMill) return true;
        }
        return false;
    }
    public boolean isPieceInMill(Piece piece) {
        if (piece == null || piece.getMyCircle() == null) return false;

        int pieceIndex = piece.getMyCircle().getIndex();
        String pieceType = piece.getPieceType();

        for (int[] mill : MILLS) {
            boolean pieceInThisMill = false;
            for(int index : mill) {
                if (index == pieceIndex) {
                    pieceInThisMill = true;
                    break;
                }
            }

            if(pieceInThisMill) {
                boolean isCompleteMill = true;
                for(int index : mill) {
                    if(circleArray[index].getPiece() == null || !circleArray[index].getPiece().getPieceType().equals(pieceType)) {
                        isCompleteMill = false;
                        break;
                    }
                }
                if(isCompleteMill) return true;
            }
        }
        return false;
    }
    
    public void displayMessage(String message) {
        if (gameMessages != null) {
            new Handler(Looper.getMainLooper()).post(() -> gameMessages.setText(message));
        } else {
             new Handler(getContext().getMainLooper()).post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    public void requestRender() {
        if (gameThread != null) gameThread.setDirty();
    }
    public void render(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        if (player1Container != null) {
            canvas.drawCircle(player1Container.getX(), player1Container.getY(), player1Container.getRadius(), containerPaint);
        }
        if (player2Container != null) {
            canvas.drawCircle(player2Container.getX(), player2Container.getY(), player2Container.getRadius(), containerPaint);
        }

        for (int y = 0; y < getHeight(); y += 8) {
            float offset = (float) Math.sin(y * 0.05f) * 5f;
            canvas.drawLine(-20 + offset, y, getWidth() + 20 + offset, y, grainPaint);
        }

        for (Line line : lineArray) {
            if (line != null) {
                canvas.drawLine(line.getX1() + 4, line.getY1() + 4, line.getX2() + 4, line.getY2() + 4, shadowLinePaint);
                canvas.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2(), linePaint);
            }
        }

        for (Circle circle : circleArray) {
            if (circle != null) {
                float r = circle.getRadius();
                float x = circle.getX();
                float y = circle.getY();
                canvas.drawCircle(x, y, r, socketBodyPaint);
                canvas.drawArc(x - r, y - r, x + r, y + r, 135, 180, false, socketShadowPaint);
                canvas.drawArc(x - r, y - r, x + r, y + r, 315, 180, false, socketHighlightPaint);

                if (clickedPiece != null && circle.getPiece() == null && (isPlacementPhase || isNeighbor(clickedPiece.getMyCircle(), circle))) {
                    canvas.drawCircle(x, y, r * 0.6f, validMovePaint);
                }

                if (clickedPiece != null && (clickedPiece.getMyCircle() == circle || (isPlacementPhase && clickedPiece.getMyCircle() == null))) {
                    canvas.drawCircle(x, y, r + 8, highlightPaint);
                }
            }
        }

        for (Piece p : player1Pieces) if (p != null) p.draw(canvas);
        for (Piece p : player2Pieces) if (p != null) p.draw(canvas);
    }
    public boolean isNeighbor(Circle current, Circle target) {
        if (current == null || current.getNeighbour() == null) return false;
        for (int idx : current.getNeighbour()) {
            if (idx >= 0 && idx < circleArray.length && circleArray[idx] == target) return true;
        }
        return false;
    }
    public void pause() { if (gameThread != null) gameThread.setRunning(false); }
    public void stop() {
        boolean retry = true;
        if (gameThread != null) {
            gameThread.setRunning(false);
            while(retry) {
                try { gameThread.join(); retry = false; }
                catch (InterruptedException ignored) {  }
            }
        }
        gameThread = null;
    }
    public void resume() {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread(getHolder(), this,"ap");
            gameThread.setRunning(true);
            gameThread.start();
        }
    }
    public void update(Circle from, Circle to, Piece piece) {
        if (panelUpdater != null) panelUpdater.start(from, to, piece);
        requestRender();
    }
    public void gameOver(String winnerName) {
        Context context = getContext();
        if (context instanceof AlquerqueActivity) {
            ((AlquerqueActivity) context).onGameOver(winnerName, movesCount);
        }
    }
    public void gamePaused() { gameOnPause = true; requestRender(); }
    public void gameResumed() { gameOnPause = false; requestRender(); }
    public void gameSurrender() { gameOver("ROBOT"); }
    
    private void updateScoreUI() {
        if (goalW != null && goalB != null) {
            int p1Count = 0;
            for (Piece p : player1Pieces) if (p.getMyCircle() != null) p1Count++;
            int p2Count = 0;
            for (Piece p : player2Pieces) if (p.getMyCircle() != null) p2Count++;
            
            final int scoreW = p1Count;
            final int scoreB = p2Count;
            
            new Handler(Looper.getMainLooper()).post(() -> {
                goalW.setText(String.valueOf(scoreW));
                goalB.setText(String.valueOf(scoreB));
            });
        }
    }

}
