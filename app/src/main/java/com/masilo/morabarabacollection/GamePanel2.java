package com.masilo.morabarabacollection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Arrays;
import java.util.Objects;

/**
 * GamePanel2 - Variant with 3 concentric squares and connected corners/middles.
 * Robots are disabled for this view as they are coupled to GamePanel1.
 */
public class GamePanel2 extends SurfaceView implements SurfaceHolder.Callback {
    public boolean blackIsWinner = false, whiteIsWinner = false;
    String[] mapArray = new String[24];
    private boolean gameOnPause = false;
    public TextView blackScore;
    public TextView whiteScore;
    private Paint backgroundPaint;

    private RobotForGamePanel2 robot1;

    public Animator1 animator = null;

    public GameClickHandler2 gameClickHandler;


    public Maker2 maker;
    private Button exit, surrender, pause, resume;
    public PanelUpdater panelUpdater;

    String[][] block = new String[20][3];
    private final Piece[] blackPieces = new Piece[12];
    private final Piece[] whitePieces = new Piece[12];
    public Circle[] circleArray = new Circle[24];
    public Line[] lineArray = new Line[40];
    String[][] segmentData = new String[40][2];
    public int movesCount = 0;
    public Paint containerPaint = new Paint();
    private final Paint targetPaint = new Paint();
    public Circle blackTarget, whiteTarget;
    private Circle whiteStater, blackStater;
    public Circle whiteContainer, blackContainer, turnCircle, turnCircle1;
    public int blackPieceKilledCount = 0, whitePieceKilledCount = 0;
    public int index = -1;
    private int[][] segmentPatterns, patterns;
    private final Paint circleArrayPaint = new Paint();
    public Piece turnPiece, turnPiece1;
    private String level;
    public boolean isMalulaStarted = false;
    private boolean isFirstLayout = true;

    private GameThread gameThread;
    private Bitmap backgroundBitmap;

    // Visual Paints
    private Paint highlightPaint;
    private Paint grainPaint;



    public GamePanel2(Context context) {
        super(context);
        init();
    }

    public GamePanel2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GamePanel2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public String getWhitePieceKilled() {
        return String.valueOf(whitePieceKilledCount);
    }

    public String getBlackPieceKilled() {
        return String.valueOf(blackPieceKilledCount);
    }

    public int[][] getSegmentPatterns() {
        return segmentPatterns;
    }

    public int[][] getPatterns() {
        return patterns;
    }

    public Circle getBlackContainer() {
        return blackContainer;
    }

    public Piece[] getBlackPieces() {
        return blackPieces;
    }

    public Piece[] getWhitePieces() {
        return whitePieces;
    }

    public String getLevel() {
        return level;
    }

    public void setButtons(TextView whiteScore, TextView blackScore) {
        this.whiteScore = whiteScore;
        this.blackScore = blackScore;
        updateScoreUI();
    }

    public void updateScoreUI() {
        if (whiteScore != null && blackScore != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                whiteScore.setText(String.valueOf(whitePieceKilledCount));
                blackScore.setText(String.valueOf(blackPieceKilledCount));
            });
        }
    }

    // Provide the new robot
    public RobotForGamePanel2 getRobot1() { return robot1; }
    public void setBlack() {
        level = GameData.getInstance().getLevel();
        gameClickHandler.gameInfoActivity.blackPlayerTurn = false;
        gameClickHandler.gameInfoActivity.whitePlayerTurn = true;
        requestRender();
    }

    public void anim() {
        int speedPercent = GameData.getInstance().getAnimationSpeed();
        long baseDuration = 1000;
        long adjustedDuration = baseDuration * 100 / speedPercent;
        animator.setDuration(adjustedDuration);
    }

    private void init() {
        maker = new Maker2(this);

        getHolder().addCallback(this);



        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#4A4E69"));

        grainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grainPaint.setColor(Color.parseColor("#1A000000")); // Very subtle
        grainPaint.setStrokeWidth(1f);

        // Highlight Paint for selected piece
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.CYAN);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(6);
        highlightPaint.setShadowLayer(12.0f, 0.0f, 0.0f, Color.CYAN);

        gameThread = new GameThread(getHolder(), this,"gp2");
        setFocusable(true);

        level = GameData.getInstance().getLevel();
        initGameItems();
        initPatterns();
        initPieces();

        panelUpdater = new PanelUpdater(blackPieces, whitePieces, circleArray, block, patterns, mapArray, segmentData, segmentPatterns);
        animator = new Animator1(this);

        // Use the new GameClickHandler2
        gameClickHandler = new GameClickHandler2(this);

        // Initialize the new robot
        robot1 = new RobotForGamePanel2(this);

        anim();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOnPause) {
            showToast("game paused");
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            boolean handled = false;
            // Check piece clicks
            for (Piece piece : whitePieces) {
                if (piece.isClicked(x, y)) {
                    SoundManager.getInstance(getContext()).playClick();
                    gameClickHandler.clickPieceWhite(piece);
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                for (Piece piece : blackPieces) {
                    if (piece.isClicked(x, y)) {
                        SoundManager.getInstance(getContext()).playClick();
                        gameClickHandler.clickPieceBlack(piece);
                        handled = true;
                        break;
                    }
                }
            }
            // Check circle clicks
            if (!handled) {
                for (Circle circle : circleArray) {
                    if (circle != null && circle.isClicked(x, y)) {
                        SoundManager.getInstance(getContext()).playClick();
                        gameClickHandler.circleClicked(circle, false);
                        handled = true;
                        break;
                    }
                }
            }
            if (handled) {
                requestRender();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void initGameItems() {
        blackContainer = new Circle(0, 0, 1);
        whiteContainer = new Circle(0, 0, 1);
        whiteStater = new Circle(0, 0, 1);
        blackStater = new Circle(0, 0, 1);
        blackTarget = new Circle(0, 0, 1);
        whiteTarget = new Circle(0, 0, 1);
        turnPiece1 = new Piece(new PointF(0, 0), true);
        turnPiece = new Piece(new PointF(0, 0), true);
        turnCircle = new Circle(0, 0, 1);
        turnCircle1 = new Circle(0, 0, 1);
        circleArrayPaint.setColor(Color.BLACK);

        containerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        containerPaint.setColor(Color.parseColor("#805D4037")); // Semi-transparent brown matching AlquerquePanel
        containerPaint.setStyle(Paint.Style.FILL);

        targetPaint.setColor(Color.rgb(255, 152, 0));
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (!gameThread.isAlive()) {
            gameThread = new GameThread(getHolder(), this,"gp2");
            gameThread.setRunning(true);
            gameThread.start();
        } else {
            gameThread.setRunning(true);
        }
        requestRender();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (width <= 0 || height <= 0) return;
        // Professional Radial Gradient (Matching AlquerquePanel)
        Shader shader = new RadialGradient(
                width / 2f, height / 2f,
                Math.max(width, height) * 0.85f,
                new int[]{0xFF4A4E69, 0xFF22223B}, // Matching AlquerquePanel colors
                new float[]{0.2f, 1.0f},
                Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(shader);
        requestRender();
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 100);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 300);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        Helper2 helper = new Helper2(circleArray, blackPieces,
                whitePieces, lineArray, whiteStater,
                whiteContainer, blackContainer, blackStater, blackTarget,
                turnCircle, turnCircle1, turnPiece, turnPiece1, whiteTarget);

        // ALWAYS recalculate board geometry on rotation
        helper.start(w, h);

        if (isFirstLayout) {
            isFirstLayout = false;
        } else {
            syncPiecePositions();
        }

       requestRender();
    }

    private void syncPiecePositions() {
        for (Piece p : blackPieces) if (p != null && p.getMyCircle() != null) p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
        for (Piece p : whitePieces) if (p != null && p.getMyCircle() != null) p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
    }

    private void initPatterns() {
        for (String[] row : block) Arrays.fill(row, "N");
        for (String[] row : segmentData) Arrays.fill(row, "N");

        // Segments for drawing lines
        segmentPatterns = new int[][]{
                // Outer ring (0-7)
                {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 7}, {7, 0},
                // Middle ring (8-15)
                {8, 9}, {9, 10}, {10, 11}, {11, 12}, {12, 13}, {13, 14}, {14, 15}, {15, 8},
                // Inner ring (16-23)
                {16, 17}, {17, 18}, {18, 19}, {19, 20}, {20, 21}, {21, 22}, {22, 23}, {23, 16},
                // Radial connections
                {0, 8}, {1, 9}, {2, 10}, {3, 11}, {4, 12}, {5, 13}, {6, 14}, {7, 15},
                {8, 16}, {9, 17}, {10, 18}, {11, 19}, {12, 20}, {13, 21}, {14, 22}, {15, 23}
        };

        // Winning patterns (mills)
        patterns = new int[][]{
                // Horizontal/Vertical on squares
                {0, 1, 2}, {2, 3, 4}, {4, 5, 6}, {6, 7, 0},
                {8, 9, 10}, {10, 11, 12}, {12, 13, 14}, {14, 15, 8},
                {16, 17, 18}, {18, 19, 20}, {20, 21, 22}, {22, 23, 16},
                // Radial lines
                {0, 8, 16}, {1, 9, 17}, {2, 10, 18}, {3, 11, 19},
                {4, 12, 20}, {5, 13, 21}, {6, 14, 22}, {7, 15, 23}
        };

        // Set each element to "N"
        Arrays.fill(mapArray, "N");
    }

    private void initPieces() {
        int bound = lineArray.length;
        int i1 = 0;
        while (i1 < bound) {
            lineArray[i1] = new Line(0, 0, 0, 0);
            i1++;
        }
        
        // Custom Piece styles for GamePanel2
        int blackBase = 0xFF311B92;   // Deep Purple
        int blackHigh = 0xFF673AB7;   // Purple
        int blackIconS = 0xFFB39DDB;  // Light Purple
        int blackIconE = 0xFF512DA8;  // Darker Purple

        int whiteBase = 0xFFE65100;   // Deep Orange
        int whiteHigh = 0xFFFF9800;   // Orange
        int whiteIconS = 0xFFFFE0B2;  // Light Orange
        int whiteIconE = 0xFFF57C00;  // Darker Orange

        for (int i = 0; i < blackPieces.length; i++) {
            blackPieces[i] = new Piece(new PointF(0, 0), true);
            blackPieces[i].setIndex(i);
            blackPieces[i].setType("B");
            blackPieces[i].setMyCircle(blackContainer);
            blackPieces[i].setIconType(Piece.IconType.CIRCLE);
            blackPieces[i].setColors(blackBase, blackHigh, blackIconS, blackIconE);
        }
        for (int i = 0; i < whitePieces.length; i++) {
            whitePieces[i] = new Piece(new PointF(0, 0), false);
            whitePieces[i].setIndex(i);
            whitePieces[i].setMyCircle(whiteContainer);
            whitePieces[i].setType("W");
            whitePieces[i].setIconType(Piece.IconType.CIRCLE);
            whitePieces[i].setColors(whiteBase, whiteHigh, whiteIconS, whiteIconE);
        }
        
        turnPiece.setIconType(Piece.IconType.CROSS);
        turnPiece1.setIconType(Piece.IconType.CROSS);

        for (int i = 0; i < circleArray.length; i++) {
            circleArray[i] = new Circle(0, 0, 0);
            circleArray[i].setIndex(i);
        }
    }

    public void pause() {
        boolean retry = true;
        if (gameThread != null) {
            gameThread.setRunning(false);
            while (retry) {
                try {
                    gameThread.join();
                    retry = false;
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void stop() {
        pause();
        if (gameThread != null) {
            gameThread = null;
        }
    }

    public void resume() {
        if (gameThread == null || gameThread.getState() == Thread.State.TERMINATED) {
            gameThread = new GameThread(getHolder(), this,"gp2");
        }
        gameThread.setRunning(true);
        if (gameThread.getState() == Thread.State.NEW) {
            gameThread.start();
        }
        requestRender();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) {
            backgroundBitmap.recycle();
            backgroundBitmap = null;
        }
        pause();
    }

    public void render(Canvas canvas) {
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

            // Wood Grain (Adjusted to match AlquerquePanel spacing)
            for (int y = 0; y < getHeight(); y += 8) {
                float offset = (float) Math.sin(y * 0.05f) * 5f;
                canvas.drawLine(-20 + offset, y, getWidth() + 20 + offset, y, grainPaint);
            }

            turnPiece.draw(canvas);
            turnPiece1.draw(canvas);

            // Draw Lines
            for (Line line : lineArray) {
                if(line != null) {
                    line.draw(canvas);
               }
            }

            // Handle Circle Highlighting & Draw
            for (Circle circle : circleArray) {
                if(circle != null) {
                    // Update circle highlight state using piece's highlight color
                    if (gameClickHandler != null && gameClickHandler.clickedPiece != null) {
                        Piece clicked = gameClickHandler.clickedPiece;
                        circle.setIsTarget(circle.getPiece() == null && gameClickHandler.isValidMove(clicked, circle), clicked.getHighlightColor());
                    } else {
                        circle.setIsTarget(false);
                    }
                    
                    circle.draw(canvas);
                }
            }

            // Highlight selected piece manually (legacy) or via piece.setHighlighted
            if (gameClickHandler != null && gameClickHandler.clickedPiece != null) {
                Piece clicked = gameClickHandler.clickedPiece;
                if (clicked.getMyCircle() != null) {
                    highlightPaint.setColor(clicked.getHighlightColor());
                    canvas.drawCircle(clicked.getMyCircle().getX(), clicked.getMyCircle().getY(), clicked.getMyCircle().getRadius() + 8, highlightPaint);
                }
            }

            canvas.drawCircle(blackTarget.getX(), blackTarget.getY(), blackTarget.getRadius(), containerPaint);
            canvas.drawCircle(whiteTarget.getX(), whiteTarget.getY(), whiteTarget.getRadius(), containerPaint);
            drawPieces(canvas, blackPieces);
            drawPieces(canvas, whitePieces);
        }
    }

    private void drawPieces(Canvas canvas, Piece[] pieces) {
        for (Piece piece : pieces) if (piece != null) {
            // Apply highlight state if it's the currently clicked piece
            piece.setHighlighted(gameClickHandler != null && gameClickHandler.clickedPiece == piece);
            piece.draw(canvas);
        }
    }

    private void showToast(String message) {
        Handler handler = new Handler(getContext().getMainLooper());
        handler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }
    public void setButtons1(Button exit,Button surrender,Button pause ,Button resume){
        this.exit = exit;
        this.pause = pause;
        this.resume = resume;
        this.surrender = surrender;
    }

    public void gameOver(String winnerName) {
        GameData data = GameData.getInstance();
        if (winnerName.equals("YOU")) {
            switch (level) {
                case "LEVEL1": data.setLevel("LEVEL2"); break;
                case "LEVEL2": data.setLevel("LEVEL3"); break;
                case "LEVEL3": data.setLevel("LEVEL4"); break;
                case "LEVEL4": data.setLevel("LEVEL5"); break;
                case "LEVEL5": data.setLevel("LEVEL6"); break;
            }
            data.save(getContext());
        }
        EndGameDialog endGameDialog = new EndGameDialog(getContext(), winnerName, (blackIsWinner ? whitePieceKilledCount : blackPieceKilledCount), movesCount, true, "NORMAL");
        endGameDialog.setCancelable(false);
        Objects.requireNonNull(endGameDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
        endGameDialog.show();
    }

    public void gamePaused() {
        gameOnPause = true;
        requestRender();
    }

    public void gameResumed() {
        gameOnPause = false;
        requestRender();
    }

    public void gameSurrender() {
        if (gameClickHandler.gameInfoActivity.blackPlayerTurn) {
            whiteIsWinner = true;
        } else {
            blackIsWinner = true;
        }

        gameOver(gameClickHandler.gameInfoActivity.blackPlayerTurn ? "YOU" : "ROBOT");
    }

    public void segment(String message) {
        showToast(message);
    }

    public void update(Circle aa, Circle b, Piece a) {
        panelUpdater.start(aa, b, a);
        updateScoreUI();
        requestRender();
    }



    public void requestRender() {
        if (gameThread != null) {
            gameThread.setDirty();
        }
    }


    public int getWhiteContainerCount() {
        int count = 0;
        for (Piece p : whitePieces) {
            if (p != null && p.isInContainer(whiteContainer)) count++;
        }
        return count;
    }

    public int getBlackContainerCount() {
        int count = 0;
        for (Piece p : blackPieces) {
            if (p != null && p.isInContainer(blackContainer)) count++;
        }
        return count;
    }


}
