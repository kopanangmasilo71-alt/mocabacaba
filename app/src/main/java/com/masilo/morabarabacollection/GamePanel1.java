package com.masilo.morabarabacollection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GamePanel1 extends SurfaceView implements SurfaceHolder.Callback, GoalManager.GoalDataProvider {
    public boolean blackIsWinner = false, whiteIsWinner = false;
    String[] mapArray = new String[25];
    boolean started = true;
    int levelNumber = 1;
    private boolean gameOnPause = false;
    public TextView blackScore, whiteScore;
    private Robot6 robot6;
    private Robot5 robot5;
    private Robot4 robot4;
    private Robot3 robot3;
    private Robot2 robot2;
    private Robot1 robot1;
    public Animator1 animator = null;
    public GoalManager goalManager;
    public GameClickHandler gameClickHandler;
    public RobotController robotController;
    public Maker maker;
    private Button exit, pause, resume;
    public PanelUpdater panelUpdater;
    String[][] block = new String[18][3];
    private final Piece[] blackPieces = new Piece[12];
    private final Piece[] whitePieces = new Piece[12];
    public Circle[] circleArray = new Circle[25];
    public Line[] lineArray = new Line[40];
    String[][] segmentData = new String[42][2];
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

    // --- VISUAL UPGRADES ---
    private Bitmap backgroundBitmap;
    private Paint paint;
    private Random random;

    // Particles
    private List<Particle> particles;
    private static final int PARTICLE_COUNT = 50;
    private final int[] particleColors = { 0x664FC3F7, 0x66FFD54F, 0x44FFFFFF };

    // Ripples
    private final List<Ripple> ripples = new ArrayList<>();
    private Paint ripplePaint;

    public GamePanel1(Context context) { super(context); init(); }
    public GamePanel1(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public GamePanel1(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }
    public String getWhitePieceKilled() { return String.valueOf(whitePieceKilledCount); }
    public String getBlackPieceKilled() { return String.valueOf(blackPieceKilledCount); }
    public int[][] getSegmentPatterns() { return segmentPatterns; }
    @Override
    public int[][] getPatterns() { return patterns; }
    @Override
    public Circle getBlackContainer() { return blackContainer; }
    @Override
    public Piece[] getBlackPieces() { return blackPieces; }
    @Override
    public Piece[] getWhitePieces() { return whitePieces; }
    public String getLevel() { return level; }
    
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

    public Robot6 getRobot6() { return robot6; }
    public Robot5 getRobot5() { return robot5; }

    public Robot2 getRobot2() { return robot2; }
    public Robot1 getRobot1() { return robot1; }

    public void setBlack() {
        level = GameData.getInstance().getLevel();
        try {
            levelNumber = Integer.parseInt(level.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ignored) {
        }
        if (levelNumber % 2 != 0) {
            // Odd levels (1, 3, 5...): Human (White) starts
            gameClickHandler.gameInfoActivity.blackPlayerTurn = false;
            gameClickHandler.gameInfoActivity.whitePlayerTurn = true;
        } else {
            // Even levels (2, 4, 6...): Robot (Black) starts
            gameClickHandler.gameInfoActivity.blackPlayerTurn = true;
            gameClickHandler.gameInfoActivity.whitePlayerTurn = false;
            
            postDelayed(() -> {
                if (started) {
                    started = false;
                    maker.switchTurnToBlack();
                }
            }, 500);
        }
        // Force background refresh on level change
        if (getWidth() > 0 && getHeight() > 0) {
            generateBackgroundBitmap(getWidth(), getHeight());
        }
        requestRender();
    }
    public void setButtons1(Button exit, Button pause, Button resume){
        this.exit = exit; this.pause = pause; this.resume = resume;
    }

    private void init() {
        maker = new Maker(this);
        getHolder().addCallback(this);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        random = new Random();
        particles = new ArrayList<>();

        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setColor(Color.WHITE);
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setStrokeWidth(3);

        gameThread = new GameThread(getHolder(), this,"gp1");
        setFocusable(true);
        robot1 = new Robot1(this); robot2 = new Robot2(this); robot3 = new Robot3(this);
        robot4 = new Robot4(this); robot5 = new Robot5(this); robot6 = new Robot6(this);

        goalManager = new GoalManager(GamePanel1.this);
        level = GameData.getInstance().getLevel();

        initGameItems();
        initPatterns();
        initPieces();

        panelUpdater = new PanelUpdater(blackPieces, whitePieces, circleArray, block, patterns, mapArray, segmentData, segmentPatterns);
        animator = new Animator1(this);
        gameClickHandler = new GameClickHandler(this);
        robotController = new RobotController(this, gameClickHandler);
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
        containerPaint.setColor(0xFF00E5FF);
        containerPaint.setStyle(Paint.Style.STROKE);
        containerPaint.setStrokeWidth(5);
        targetPaint.setColor(Color.rgb(255, 152, 0));
    }

    private void initPatterns() {
        for (String[] strings : block) Arrays.fill(strings, "N");
        for (String[] segmentDatum : segmentData) Arrays.fill(segmentDatum, "N");
        segmentPatterns = BoardDefinition.getSegmentPatterns();
        patterns = BoardDefinition.getPatterns();
        Arrays.fill(mapArray, "N");
    }

    private void initPieces() {
        for (int i1 = 0; i1 < lineArray.length; i1++) {
            lineArray[i1] = new Line(0, 0, 0, 0);
        }
        
        for (int i = 0; i < blackPieces.length; i++) {
            blackPieces[i] = new Piece(new PointF(0, 0), true);
            blackPieces[i].setIndex(i);
            blackPieces[i].setMyCircle(blackContainer);
        }
        for (int i = 0; i < whitePieces.length; i++) {
            whitePieces[i] = new Piece(new PointF(0, 0), false);
            whitePieces[i].setIndex(i);
            whitePieces[i].setMyCircle(whiteContainer);
        }

        for (int i = 0; i < circleArray.length; i++) {
            circleArray[i] = new Circle(0, 0, 0);
            circleArray[i].setIndex(i);
        }
    }

    public void anim() {
        int speedPercent = GameData.getInstance().getAnimationSpeed();
        long baseDuration = 1000;
        long adjustedDuration = baseDuration * 100 / Math.max(1, speedPercent);
        if (animator != null) animator.setDuration(adjustedDuration);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(started) {
            started = false;
            if (levelNumber % 2 != 0) {
                gameClickHandler.gameInfoActivity.blackPlayerTurn = false;
                gameClickHandler.gameInfoActivity.whitePlayerTurn = true;
            } else {
                gameClickHandler.gameInfoActivity.blackPlayerTurn = true;
                gameClickHandler.gameInfoActivity.whitePlayerTurn = false;
                maker.switchTurnToBlack();
            }
        }
        if (gameOnPause) {
            showToast(getContext().getString(R.string.game_paused));
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            ripples.add(new Ripple(x, y));
            performClick();

            boolean handled = false;
            for (Piece piece : whitePieces) {
                if (piece.isClicked(x, y)) {
                    vibrate(30);
                    SoundManager.getInstance(getContext()).playClick();
                    gameClickHandler.clickPieceWhite(piece);
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                for (Piece piece : blackPieces) {
                    if (piece.isClicked(x, y)) {
                        vibrate(30);
                        SoundManager.getInstance(getContext()).playClick();
                        gameClickHandler.clickPieceBlack(piece);
                        handled = true;
                        break;
                    }
                }
            }
            if (!handled) {
                for (Circle circle : circleArray) {
                    if (circle.isClicked(x, y)) {
                        vibrate(20);
                        SoundManager.getInstance(getContext()).playClick();
                        gameClickHandler.circleClicked(circle, false);
                        break;
                    }
                }
            }
            requestRender();
            return true;
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

    @Override
    public boolean performClick() { super.performClick(); return true; }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (!gameThread.isAlive()) {
            gameThread = new GameThread(getHolder(), this,"gp1");
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

        double a = whitePieces[0].getOuterRadius() * 2f;
        if (pause != null) {
            updateButtonSize(pause, (int)a);
            updateButtonSize(resume, (int)a);
            updateButtonSize(exit, (int)a);
        }

        requestRender();
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 100);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 300);
    }

    private void syncPiecePositions() {
        for (Piece p : blackPieces) if (p != null && p.getMyCircle() != null) p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
        for (Piece p : whitePieces) if (p != null && p.getMyCircle() != null) p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
    }

    private void updateButtonSize(Button btn, int size) {
        if (btn == null || btn.getLayoutParams() == null) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btn.getLayoutParams();
        params.width = size; params.height = size;
        btn.setLayoutParams(params);
    }

    private void generateBackgroundBitmap(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (backgroundBitmap != null) backgroundBitmap.recycle();
        backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas bgCanvas = new Canvas(backgroundBitmap);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        int mainColor = 0xFF3E2723;
        
        if (isMalulaStarted) {
            // Unique Malula Background: Solid Dark Gray
            bgCanvas.drawColor(0xFF212121);
            
            // Constellations (Dim silver lines) - Nebula circles and star circles removed
            p.setColor(0x33BDBDBD);
            p.setStrokeWidth(1.5f);
            for(int i=0; i<12; i++) {
                float x1 = random.nextFloat() * width;
                float y1 = random.nextFloat() * height;
                float x2 = x1 + (random.nextFloat() - 0.5f) * 100;
                float y2 = y1 + (random.nextFloat() - 0.5f) * 100;
                bgCanvas.drawLine(x1, y1, x2, y2, p);
            }
        } else {
            String currentLevel = (level != null) ? level : "LEVEL1";

            switch (currentLevel) {
                case "LEVEL1": // Earthy Brown
                    bgCanvas.drawColor(mainColor);
                    break;
                    
                case "LEVEL2": // Slate/Carbon
                    bgCanvas.drawColor(0xFF263238);
                    // Grid lines
                    p.setColor(0x0AFFFFFF);
                    p.setStrokeWidth(2f);
                    for (int i = 0; i < width; i += 40) bgCanvas.drawLine(i, 0, i, height, p);
                    for (int i = 0; i < height; i += 40) bgCanvas.drawLine(0, i, width, i, p);
                    break;
                    
                case "LEVEL3": // Deep Forest Green
                    bgCanvas.drawColor(0xFF1B5E20);
                    // Subtle leaves/vines texture
                    p.setColor(0x11000000);
                    for(int i=0; i<300; i++) {
                        bgCanvas.drawCircle(random.nextFloat()*width, random.nextFloat()*height, random.nextFloat()*5, p);
                    }
                    break;
                    
                case "LEVEL4": // Deep Teal / Mystic
                    bgCanvas.drawColor(0xFF003133);
                    // Bio-luminescent particles
                    for(int i=0; i<120; i++) {
                        p.setColor(random.nextBoolean() ? 0x4480CBC4 : 0x224DB6AC);
                        bgCanvas.drawCircle(random.nextFloat()*width, random.nextFloat()*height, random.nextFloat()*3, p);
                    }
                    break;
                    
                case "LEVEL5": // Royal Purple / Mystic
                    bgCanvas.drawColor(0xFF311B92);
                    // Subtle energy lines
                    p.setColor(0x15FFFFFF);
                    p.setStrokeWidth(1f);
                    for (int i = 0; i < width; i += 100) bgCanvas.drawLine(i, 0, width - i, height, p);
                    break;
                    
                case "LEVEL6": // Volcanic / Obsidian
                    bgCanvas.drawColor(0xFF000000);
                    // Glowing embers
                    for(int i=0; i<40; i++) {
                        p.setColor(random.nextBoolean() ? 0x22FF5722 : 0x11FF9800);
                        bgCanvas.drawCircle(random.nextFloat()*width, random.nextFloat()*height, random.nextFloat()*30 + 10, p);
                    }
                    break;
            }
        }
    }

    private void initParticles(int width, int height) {
        if (width <= 0 || height <= 0) return;
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(createParticle(width, height));
        }
    }

    private Particle createParticle(int w, int h) {
        return new Particle(
                random.nextFloat() * w,
                random.nextFloat() * h,
                random.nextFloat() * 2f + 1f,
                particleColors[random.nextInt(particleColors.length)],
                (random.nextFloat() - 0.5f) * 0.3f,
                (random.nextFloat() - 0.5f) * 0.3f
        );
    }

    public void render(Canvas canvas) {
        if (canvas != null) {
            if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) canvas.drawBitmap(backgroundBitmap, 0, 0, null);
            else canvas.drawColor(0xFF263238);

            updateParticles();
            drawParticles(canvas);
            drawRipples(canvas);

            turnPiece.draw(canvas);
            turnPiece1.draw(canvas);

            for (Line line : lineArray) line.draw(canvas);

            for (Circle circle : circleArray) {
                if(circle != null) {
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
    }

    private void drawTurnIndicator(Canvas canvas) {
        if (gameClickHandler == null || gameClickHandler.gameInfoActivity == null) return;
        
        Paint tPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tPaint.setTextSize(25f);
        tPaint.setTextAlign(Paint.Align.CENTER);
        tPaint.setFakeBoldText(true);
        
        boolean isWhiteTurn = gameClickHandler.gameInfoActivity.whitePlayerTurn;
        String text = isWhiteTurn ? "YOUR TURN (WHITE)" : "ROBOT'S TURN (BLACK)";
        int color = isWhiteTurn ? 0xFFFFFFFF : 0xFFB0BEC5;
        
        // Glassmorphism background
        tPaint.setColor(0xAA000000);
        float textW = tPaint.measureText(text);
        RectF rect = new RectF(getWidth()/2f - textW/2 - 40, 20, getWidth()/2f + textW/2 + 40, 90);
        canvas.drawRoundRect(rect, 35, 35, tPaint);
        
        tPaint.setColor(color);
        canvas.drawText(text, getWidth() / 2f, 70, tPaint);
        
        // Active turn glow under the piece type
        tPaint.setAlpha(120);
        if (isWhiteTurn) canvas.drawCircle(whiteTarget.getX(), whiteTarget.getY(), whiteTarget.getRadius() * 1.3f, tPaint);
        else canvas.drawCircle(blackTarget.getX(), blackTarget.getY(), blackTarget.getRadius() * 1.3f, tPaint);
    }

    private void updateParticles() {
        for (Particle p : particles) {
            p.x += p.vx; p.y += p.vy;
            if (p.x < 0) p.x = getWidth(); if (p.x > getWidth()) p.x = 0;
            if (p.y < 0) p.y = getHeight(); if (p.y > getHeight()) p.y = 0;
            p.alphaOffset += 0.04f;
        }
    }

    private void drawParticles(Canvas canvas) {
        for (Particle p : particles) {
            paint.setColor(p.color);
            int alpha = (int) (120 + Math.sin(p.alphaOffset) * 60);
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

    public void pause() {
        boolean retry = true;
        if (gameThread != null) {
            gameThread.setRunning(false);
            while (retry) {
                try { gameThread.join(); retry = false; }
                catch (InterruptedException ignored) {}
            }
        }
    }
    public void stop() { pause(); if (gameThread != null) gameThread = null; }
    public void resume() {
        if (gameThread == null || gameThread.getState() == Thread.State.TERMINATED) {
            gameThread = new GameThread(getHolder(), this,"gp1");
        }
        gameThread.setRunning(true);
        if (gameThread.getState() == Thread.State.NEW) gameThread.start();
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

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    public void gameOver(String winnerName) {
        GameData data = GameData.getInstance();
        data.incrementMalulaGames();
        if (winnerName.equals(getContext().getString(R.string.winner_you))) {
            switch (level) {
                case "LEVEL1": data.setLevel("LEVEL2"); break;
                case "LEVEL2": data.setLevel("LEVEL3"); break;
                case "LEVEL3": data.setLevel("LEVEL4"); break;
                case "LEVEL4": data.setLevel("LEVEL5"); break;
                case "LEVEL5": data.setLevel("LEVEL6"); break;
            }
        }
        data.save(getContext());

        String type = isMalulaStarted ? null : "NORMAL";
        EndGameDialog endGameDialog = new EndGameDialog(getContext(), winnerName, (blackIsWinner ? whitePieceKilledCount : blackPieceKilledCount), movesCount, true, type);
        endGameDialog.setCancelable(false);
        Objects.requireNonNull(endGameDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
        endGameDialog.show();
    }

    public void gamePaused() { gameOnPause = true; requestRender(); }
    public void gameResumed() { gameOnPause = false; requestRender(); }
    public void segment(String message) { showToast(message); }
    public void update(Circle aa, Circle b, Piece a) {
        panelUpdater.start(aa, b, a);
        updateScoreUI();
        requestRender();
    }

    public void requestRender() { if (gameThread != null) gameThread.setDirty(); }
    public int getWhiteContainerCount() {
        int count = 0; for (Piece p : whitePieces) if (p != null && p.isInContainer(whiteContainer)) count++; return count;
    }
    public int getBlackContainerCount() {
        int count = 0; for (Piece p : blackPieces) if (p != null && p.isInContainer(blackContainer)) count++; return count;
    }

    @Override public Circle[] getCircleArray() { return circleArray; }
    @Override public String[][] getSegmentData() { return segmentData; }
    @Override public Circle getWhiteContainer() { return whiteContainer; }

    public Robot4 getRobot4() {
        return robot4;
    }

    public Robot3 getRobot3() {
        return robot3;
    }

    private static class Particle {
        float x, y, size;
        int color;
        float vx, vy;
        float alphaOffset = 0;
        Particle(float x, float y, float size, int color, float vx, float vy) {
            this.x = x; this.y = y; this.size = size; this.color = color; this.vx = vx; this.vy = vy;
        }
    }

    private static class Ripple {
        float x, y;
        float radius = 0;
        int alpha = 255;
        Ripple(float x, float y) { this.x = x; this.y = y; }
        void update() { radius += 6; alpha -= 12; if (alpha < 0) alpha = 0; }
    }
}
