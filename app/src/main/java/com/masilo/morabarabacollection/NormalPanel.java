package com.masilo.morabarabacollection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The class that controls the game loop for the player and the robot
 */
public class NormalPanel extends SurfaceView implements SurfaceHolder.Callback, GoalManager.GoalDataProvider {

    public boolean blackIsWinner = false, whiteIsWinner = false;
    String[] mapArray = new String[25];
    private boolean gameOnPause = false;
    public TextView blackScore, whiteScore, gameMessages;
    Animator1 animator = null;
    public GoalManager goalManager;
    public GameClickHandler1 gameClickHandler;
    public Maker1 maker;
    public PanelUpdater panelUpdater;

    String[][] block = new String[18][3];
    private final Piece[] blackPieces = new Piece[12];
    private final Piece[] whitePieces = new Piece[12];
    public Circle[] circleArray = new Circle[25];
    public Line[] lineArray = new Line[40];
    String[][] segmentData = new String[42][2];

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
    public int movesCount = 0;
    private boolean isFirstLayout = true;

    private GameThread gameThread;
    private Bitmap backgroundBitmap;

    private Paint paint;
    private final Random random = new Random();

    // Particles
    private final List<Particle> particles = new ArrayList<>();
    private static final int PARTICLE_COUNT = 40;
    private final int[] particleColors = { 0x44FFAB91, 0x44A5D6A7, 0x22FFFFFF };

    // Ripples
    private final List<Ripple> ripples = new ArrayList<>();
    private Paint ripplePaint;

    public NormalPanel(Context context) {
        super(context);
        init();
    }

    public NormalPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NormalPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    // --- Public getters/setters ---

    public String getWhitePieceKilled() {
        return String.valueOf(whitePieceKilledCount);
    }

    public String getBlackPieceKilled() {
        return String.valueOf(blackPieceKilledCount);
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
    public void setViews(TextView whiteScore, TextView blackScore, TextView gameMessages) {
        this.whiteScore = whiteScore;
        this.blackScore = blackScore;
        this.gameMessages = gameMessages;
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


    public void anim() {
        int speedPercent = GameData.getInstance().getAnimationSpeed();
        long adjustedDuration = 1000L * 100 / Math.max(1, speedPercent);
        if (animator != null) animator.setDuration(adjustedDuration);
    }

    // --- Touch handling ---

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOnPause) {
            displayMessage("game paused");
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            ripples.add(new Ripple(x, y));
            performClick();

            if (gameClickHandler.isKillingPhase()) return handleKillingPhaseTouch(x, y);
            return handleNormalPhaseTouch(x, y);
        }

        return super.onTouchEvent(event);
    }

    private void vibrate(long duration) {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(duration);
            }
        }
    }

    private boolean handleKillingPhaseTouch(float x, float y) {
        Piece[] targetPieces;
        boolean isWhiteTurn;
        if (gameClickHandler.gameInfoActivity.whitePlayerTurn && gameClickHandler.whiteGoal) {
            targetPieces = blackPieces;
            isWhiteTurn = true;
        } else if (gameClickHandler.gameInfoActivity.blackPlayerTurn && gameClickHandler.blackGoal) {
            targetPieces = whitePieces;
            isWhiteTurn = false;
        } else {
            displayMessage("Select an opponent's piece to remove");
            return false;
        }

        for (Piece piece : targetPieces) {
            Circle target = isWhiteTurn ? blackTarget : whiteTarget;
            if (piece.isClicked(x, y) && !piece.isInContainer(target)) {
                vibrate(50);
                gameClickHandler.killPiece(piece);
                gameClickHandler.mill_count--;
                if (gameClickHandler.mill_count == 0) {
                    if (isWhiteTurn) gameClickHandler.whiteGoal = false;
                    else gameClickHandler.blackGoal = false;
                    gameClickHandler.endKillingPhase(!isWhiteTurn);
                }
                updateScoreUI();
                requestRender();
                return true;
            }
        }

        displayMessage("Select an opponent's piece to remove");
        return false;
    }

    private boolean handleNormalPhaseTouch(float x, float y) {
        Piece[] pieces = gameClickHandler.gameInfoActivity.whitePlayerTurn ? whitePieces : blackPieces;
        Circle target = gameClickHandler.gameInfoActivity.whitePlayerTurn ? whiteTarget : blackTarget;

        for (Piece piece : pieces) {
            if (piece.isClicked(x, y) && !piece.isInContainer(target)) {
                vibrate(30);
                if (piece.isBlack()) {
                    SoundManager.getInstance(getContext()).playClick();
                    gameClickHandler.clickPiece(piece, true);
                } else {
                    SoundManager.getInstance(getContext()).playClick();
                    gameClickHandler.clickPieceWhite(piece);
                }
                requestRender();
                return true;
            }
        }

        for (Circle circle : circleArray) {
            if (circle.isClicked(x, y) && gameClickHandler.clickedPiece != null) {
                boolean valid = (gameClickHandler.gameInfoActivity.whitePlayerTurn && !gameClickHandler.clickedPiece.isBlack()) ||
                        (gameClickHandler.gameInfoActivity.blackPlayerTurn && gameClickHandler.clickedPiece.isBlack());
                if (valid) {
                    vibrate(20);
                    SoundManager.getInstance(getContext()).playClick();
                    gameClickHandler.circleClicked(circle, gameClickHandler.gameInfoActivity.blackPlayerTurn);
                } else displayMessage("Not your piece");
                requestRender();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    // --- Initialization ---

    private void init() {
        maker = new Maker1(this);
        getHolder().addCallback(this);

        setFocusable(true);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setColor(Color.WHITE);
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setStrokeWidth(3);

        gameThread = new GameThread(getHolder(), this, "np");

        goalManager = new GoalManager(this);
        level = GameData.getInstance().getLevel();

        initGameItems();
        initPatterns();
        initPieces();

        panelUpdater = new PanelUpdater(blackPieces, whitePieces, circleArray, block, patterns, mapArray, segmentData, segmentPatterns);
        animator = new Animator1(this);
        gameClickHandler = new GameClickHandler1(this);
        anim();
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
        containerPaint.setColor(0xFFFFAB91);
        containerPaint.setStyle(Paint.Style.STROKE);
        containerPaint.setStrokeWidth(5);
        targetPaint.setColor(Color.rgb(255, 152, 0));
    }

    private void initPatterns() {
        for (String[] strings : block) Arrays.fill(strings, "N");
        for (String[] strings : segmentData) Arrays.fill(strings, "N");

        segmentPatterns = BoardDefinition.getSegmentPatterns();
        patterns = BoardDefinition.getPatterns();

        Arrays.fill(mapArray, "N");
    }

    private void initPieces() {
        for (int i = 0; i < lineArray.length; i++) lineArray[i] = new Line(0, 0, 0, 0);

        // Professional Mahogany Theme
        int blackBase = 0xFF1B5E20;   // Green 900
        int blackHigh = 0xFF4CAF50;   // Green 500
        int blackIconS = 0xFFA5D6A7;  // Green 200
        int blackIconE = 0xFF1B5E20;  // Green 900

        int whiteBase = 0xFFBF360C;   // Deep Orange 900
        int whiteHigh = 0xFFFF5722;   // Deep Orange 500
        int whiteIconS = 0xFFFFAB91;  // Deep Orange 200
        int whiteIconE = 0xFFBF360C;  // Deep Orange 900

        for (int i = 0; i < blackPieces.length; i++) {
            blackPieces[i] = new Piece(new PointF(0, 0), true);
            blackPieces[i].setIndex(i);
            blackPieces[i].setType("B");
            blackPieces[i].setMyCircle(blackContainer);
            blackPieces[i].setIconType(Piece.IconType.STAR);
            blackPieces[i].setColors(blackBase, blackHigh, blackIconS, blackIconE);
        }

        for (int i = 0; i < whitePieces.length; i++) {
            whitePieces[i] = new Piece(new PointF(0, 0), false);
            whitePieces[i].setIndex(i);
            whitePieces[i].setType("W");
            whitePieces[i].setMyCircle(whiteContainer);
            whitePieces[i].setIconType(Piece.IconType.STAR);
            whitePieces[i].setColors(whiteBase, whiteHigh, whiteIconS, whiteIconE);
        }

        turnPiece.setIconType(Piece.IconType.CIRCLE);
        turnPiece1.setIconType(Piece.IconType.CIRCLE);

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
                try { gameThread.join(); retry = false; } catch (InterruptedException ignored) {}
            }
        }
    }

    public void resume() {
        if (gameThread == null || gameThread.getState() == Thread.State.TERMINATED) {
            gameThread = new GameThread(getHolder(), this, "np");
        }
        gameThread.setRunning(true);
        if (gameThread.getState() == Thread.State.NEW) gameThread.start();
        requestRender();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        resume();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        generateBackgroundBitmap(width, height);
        initParticles(width, height);
        
        MorabarabaHelper.initBoard(width, height, circleArray, lineArray);
        MorabarabaHelper.initNormalGamePieces(width, height, blackPieces, whitePieces,
                whiteStater, whiteContainer, blackContainer, blackStater,
                blackTarget, whiteTarget, turnCircle, turnCircle1, turnPiece, turnPiece1);

        if (!isFirstLayout) {
            syncPiecePositions();
        } else {
            isFirstLayout = false;
        }
        requestRender();
    }

    private void syncPiecePositions() {
        for (Piece p : blackPieces) if (p != null && p.getMyCircle() != null) p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
        for (Piece p : whitePieces) if (p != null && p.getMyCircle() != null) p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
    }

    private void generateBackgroundBitmap(int width, int height) {
        if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) {
            backgroundBitmap.recycle();
        }
        backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas bgCanvas = new Canvas(backgroundBitmap);

        // Rich Mahogany Wood Finish
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader boardShader = new RadialGradient(width / 2f, height / 2f, Math.max(width, height) * 0.9f,
                new int[]{0xFF5D4037, 0xFF3E2723, 0xFF1B100D},
                new float[]{0f, 0.6f, 1.0f}, Shader.TileMode.CLAMP);
        p.setShader(boardShader);
        bgCanvas.drawRect(0, 0, width, height, p);

        // Wood grain lines
        p.setShader(null);
        p.setStrokeWidth(2f);
        for (int y = 0; y < height; y += 8) {
            float offset = (float) Math.sin(y * 0.04f) * 6f;
            p.setColor(0x1A000000); // Very subtle dark grain
            bgCanvas.drawLine(-30 + offset, y, width + 30 + offset, y, p);
            p.setColor(0x08FFFFFF); // Even subtler highlight grain
            bgCanvas.drawLine(-30 + offset, y + 2, width + 30 + offset, y + 2, p);
        }
        
        // Vignette
        Shader vignette = new RadialGradient(width/2f, height/2f, Math.max(width, height) * 0.9f,
                new int[]{0x00000000, 0x88000000}, null, Shader.TileMode.CLAMP);
        p.setShader(vignette);
        bgCanvas.drawRect(0, 0, width, height, p);
    }

    private void initParticles(int width, int height) {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle(
                    random.nextFloat() * width,
                    random.nextFloat() * height,
                    random.nextFloat() * 2f + 1f,
                    particleColors[random.nextInt(particleColors.length)],
                    (random.nextFloat() - 0.5f) * 0.2f,
                    (random.nextFloat() - 0.5f) * 0.2f
            ));
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (backgroundBitmap != null) {
            backgroundBitmap.recycle();
            backgroundBitmap = null;
        }
        pause();
    }

    public void render(Canvas canvas) {
        if (canvas == null) return;

        if (backgroundBitmap != null) canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        else canvas.drawColor(0xFF3E2723);

        updateParticles();
        drawParticles(canvas);
        drawRipples(canvas);

        turnPiece.draw(canvas);
        turnPiece1.draw(canvas);

        for (Line line : lineArray) if (line != null) line.draw(canvas);

        for (Circle circle : circleArray) {
            if (circle != null) {
                if (gameClickHandler != null && gameClickHandler.clickedPiece != null) {
                    Piece clicked = gameClickHandler.clickedPiece;
                    circle.setIsTarget(circle.getPiece() == null && gameClickHandler.isValidMove(clicked, circle), clicked.getHighlightColor());
                } else {
                    circle.setIsTarget(false);
                }
                circle.draw(canvas);
            }
        }

        canvas.drawCircle(blackTarget.getX(), blackTarget.getY(), blackTarget.getRadius(), containerPaint);
        canvas.drawCircle(whiteTarget.getX(), whiteTarget.getY(), whiteTarget.getRadius(), containerPaint);

        drawPieces(canvas, blackPieces);
        drawPieces(canvas, whitePieces);
        
        drawTurnIndicator(canvas);
    }

    private void drawTurnIndicator(Canvas canvas) {
        if (gameClickHandler == null || gameClickHandler.gameInfoActivity == null) return;
        
        Paint tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tPaint.setTextSize(25f);
        tPaint.setTextAlign(Paint.Align.CENTER);
        tPaint.setFakeBoldText(true);
        
        boolean isWhiteTurn = gameClickHandler.gameInfoActivity.whitePlayerTurn;
        String text = isWhiteTurn ? "PLAYER TURN (WHITE)" : "OPPONENT TURN (BLACK)";
        int color = isWhiteTurn ? 0xFFFFAB91 : 0xFFA5D6A7;
        
        tPaint.setColor(0xBB000000);
        float textW = tPaint.measureText(text);
        RectF rect = new RectF(getWidth()/2f - textW/2 - 40, 20, getWidth()/2f + textW/2 + 40, 90);
        canvas.drawRoundRect(rect, 35, 35, tPaint);
        
        tPaint.setColor(color);
        canvas.drawText(text, getWidth() / 2f, 70, tPaint);
    }

    private void updateParticles() {
        for (Particle p : particles) {
            p.x += p.vx; p.y += p.vy;
            if (p.x < 0) p.x = getWidth(); if (p.x > getWidth()) p.x = 0;
            if (p.y < 0) p.y = getHeight(); if (p.y > getHeight()) p.y = 0;
            p.alphaOffset += 0.03f;
        }
    }

    private void drawParticles(Canvas canvas) {
        for (Particle p : particles) {
            paint.setColor(p.color);
            int alpha = (int) (100 + Math.sin(p.alphaOffset) * 50);
            paint.setAlpha(alpha);
            canvas.drawCircle(p.x, p.y, p.size, paint);
        }
    }

    private void drawRipples(Canvas canvas) {
        Iterator<Ripple> iterator = ripples.iterator();
        while (iterator.hasNext()) {
            Ripple r = iterator.next();
            r.update();
            ripplePaint.setAlpha(r.alpha);
            canvas.drawCircle(r.x, r.y, r.radius, ripplePaint);
            if (r.alpha <= 0) iterator.remove();
        }
    }

    private void drawPieces(Canvas canvas, Piece[] pieces) {
        for (Piece piece : pieces) if (piece != null) {
            piece.setHighlighted(gameClickHandler != null && gameClickHandler.clickedPiece == piece);
            piece.draw(canvas);
        }
    }

    public void displayMessage(String message) {
        if (gameMessages != null) {
            new Handler(Looper.getMainLooper()).post(() -> gameMessages.setText(message));
        } else {
             new Handler(getContext().getMainLooper()).post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        MorabarabaHelper.initBoard(w, h, circleArray, lineArray);
        MorabarabaHelper.initNormalGamePieces(w, h, blackPieces, whitePieces,
                whiteStater, whiteContainer, blackContainer, blackStater,
                blackTarget, whiteTarget, turnCircle, turnCircle1, turnPiece, turnPiece1);
        if (!isFirstLayout) syncPiecePositions();
        requestRender();
    }

    public void gameOver(String winnerName) {
        GameData data = GameData.getInstance();
        data.incrementNormalGames();
        data.save(getContext());

        if (blackPieceKilledCount == 10) {
            switch (level) {
                case "LEVEL1": data.setLevel("LEVEL2"); break;
                case "LEVEL2": data.setLevel("LEVEL3"); break;
                case "LEVEL3": data.setLevel("LEVEL4"); break;
            }
        }

        EndGameDialog dialog = new EndGameDialog(getContext(), winnerName,
                (blackIsWinner ? whitePieceKilledCount : blackPieceKilledCount), movesCount, true);
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
        dialog.show();
    }

    public void gamePaused() { gameOnPause = true; }
    public void gameResumed() { gameOnPause = false; }

    public void segment(String message) { displayMessage(message); }

    public void update(Circle aa, Circle b, Piece a) { 
        panelUpdater.start(aa, b, a); 
        updateScoreUI();
        requestRender();
    }

    public int getWhiteContainerCount() { return getContainerCount(whitePieces, whiteContainer); }
    public int getBlackContainerCount() { return getContainerCount(blackPieces, blackContainer); }
    private int getContainerCount(Piece[] pieces, Circle container) {
        int count = 0;
        for (Piece p : pieces) if (p != null && p.isInContainer(container)) count++;
        return count;
    }

    public void saveState(Bundle outState) {
        outState.putInt("blackPieceKilledCount", blackPieceKilledCount);
        outState.putInt("whitePieceKilledCount", whitePieceKilledCount);
        if (gameClickHandler != null && gameClickHandler.gameInfoActivity != null) {
            outState.putBoolean("blackPlayerTurn", gameClickHandler.gameInfoActivity.blackPlayerTurn);
            outState.putBoolean("whitePlayerTurn", gameClickHandler.gameInfoActivity.whitePlayerTurn);
            outState.putBoolean("whiteGoal", gameClickHandler.whiteGoal);
            outState.putBoolean("blackGoal", gameClickHandler.blackGoal);
            outState.putInt("mill_count", gameClickHandler.mill_count);
        }
        outState.putBoolean("gameOnPause", gameOnPause);
        outState.putString("level", level);
        outState.putBoolean("blackIsWinner", blackIsWinner);
        outState.putBoolean("whiteIsWinner", whiteIsWinner);

        int[] blackPieceCircleIndices = new int[blackPieces.length];
        for (int i = 0; i < blackPieces.length; i++) {
            Circle c = blackPieces[i].getMyCircle();
            if (c == null) blackPieceCircleIndices[i] = -10;
            else if (c == blackContainer) blackPieceCircleIndices[i] = -1;
            else if (c == blackTarget) blackPieceCircleIndices[i] = -3;
            else blackPieceCircleIndices[i] = c.getIndex();
        }
        outState.putIntArray("blackPieceCircleIndices", blackPieceCircleIndices);

        int[] whitePieceCircleIndices = new int[whitePieces.length];
        for (int i = 0; i < whitePieces.length; i++) {
            Circle c = whitePieces[i].getMyCircle();
            if (c == null) whitePieceCircleIndices[i] = -10;
            else if (c == whiteContainer) whitePieceCircleIndices[i] = -2;
            else if (c == whiteTarget) whitePieceCircleIndices[i] = -4;
            else whitePieceCircleIndices[i] = c.getIndex();
        }
        outState.putIntArray("whitePieceCircleIndices", whitePieceCircleIndices);
    }

    public void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        blackPieceKilledCount = savedInstanceState.getInt("blackPieceKilledCount");
        whitePieceKilledCount = savedInstanceState.getInt("whitePieceKilledCount");
        if (gameClickHandler != null && gameClickHandler.gameInfoActivity != null) {
            gameClickHandler.gameInfoActivity.blackPlayerTurn = savedInstanceState.getBoolean("blackPlayerTurn");
            gameClickHandler.gameInfoActivity.whitePlayerTurn = savedInstanceState.getBoolean("whitePlayerTurn");
            gameClickHandler.whiteGoal = savedInstanceState.getBoolean("whiteGoal");
            gameClickHandler.blackGoal = savedInstanceState.getBoolean("blackGoal");
            gameClickHandler.mill_count = savedInstanceState.getInt("mill_count");
        }
        gameOnPause = savedInstanceState.getBoolean("gameOnPause");
        level = savedInstanceState.getString("level");
        blackIsWinner = savedInstanceState.getBoolean("blackIsWinner");
        whiteIsWinner = savedInstanceState.getBoolean("whiteIsWinner");

        for (Circle c : circleArray) if (c != null) c.resetPiece();

        int[] bIndices = savedInstanceState.getIntArray("blackPieceCircleIndices");
        if (bIndices != null) {
            for (int i = 0; i < bIndices.length; i++) {
                if (i < blackPieces.length) {
                    Piece p = blackPieces[i];
                    Circle c = getCircleForIndex(bIndices[i]);
                    p.setMyCircle(c);
                    if (c != null && bIndices[i] >= 0) c.setMyPiece(p, "B");
                }
            }
        }

        int[] wIndices = savedInstanceState.getIntArray("whitePieceCircleIndices");
        if (wIndices != null) {
            for (int i = 0; i < wIndices.length; i++) {
                if (i < whitePieces.length) {
                    Piece p = whitePieces[i];
                    Circle c = getCircleForIndex(wIndices[i]);
                    p.setMyCircle(c);
                    if (c != null && wIndices[i] >= 0) c.setMyPiece(p, "W");
                }
            }
        }
        updateAllPiecePositions();
        updateScoreUI();
        requestRender();
    }

    private Circle getCircleForIndex(int index) {
        if (index >= 0 && index < circleArray.length) return circleArray[index];
        switch (index) {
            case -1: return blackContainer;
            case -2: return whiteContainer;
            case -3: return blackTarget;
            case -4: return whiteTarget;
            default: return null;
        }
    }

    private void updateAllPiecePositions() {
        post(() -> {
            for(Piece p : blackPieces) if(p != null && p.getMyCircle() != null) p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
            for(Piece p : whitePieces) if(p != null && p.getMyCircle() != null) p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
            requestRender();
        });
    }

    public void requestRender() { if (gameThread != null) gameThread.setDirty(); }
    @Override public Circle[] getCircleArray() { return circleArray; }
    @Override public String[][] getSegmentData() { return segmentData; }
    @Override public Circle getWhiteContainer() { return whiteContainer; }

    private static class Particle {
        float x, y, size, vx, vy, alphaOffset = 0;
        int color;
        Particle(float x, float y, float s, int c, float vx, float vy) { this.x=x; this.y=y; this.size=s; this.color=c; this.vx=vx; this.vy=vy; }
    }
    private static class Ripple {
        float x, y, radius = 0;
        int alpha = 255;
        Ripple(float x, float y) { this.x = x; this.y = y; }
        void update() { radius += 6; alpha -= 12; if (alpha < 0) alpha = 0; }
    }
}
