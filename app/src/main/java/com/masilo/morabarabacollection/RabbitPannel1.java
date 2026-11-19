package com.masilo.morabarabacollection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RabbitPannel1 extends SurfaceView implements SurfaceHolder.Callback {

    // ---- BOARD STRUCTURE ----
    private static final int BASE_NODES = 25;
    private static final int STACKS = 2;
    public final Circle[] circleArray = new Circle[BASE_NODES * STACKS];

    private static final int[][] BASE_MORABARABA_CONNECTIONS = {
            // Outer
            {0, 1}, {1, 2}, {2, 15}, {15, 24}, {24, 23}, {23, 22}, {22, 9}, {9, 0},
            // Middle
            {3, 4}, {4, 5}, {5, 14}, {14, 21}, {21, 20}, {20, 19}, {19, 10}, {10, 3},
            // Inner
            {6, 7}, {7, 8}, {8, 13}, {13, 18}, {18, 17}, {17, 16}, {16, 11}, {11, 6},
            // Cross
            {0, 3}, {1, 4}, {4, 7}, {2, 5},
            {9, 10}, {10, 11}, {22, 19}, {23, 20}, {20, 17},
            {24, 21}, {15, 14}, {14, 13},
            // Center
            {7, 12}, {11, 12}, {17, 12}, {13, 12}
    };

    public Line[] lineArray;

    // ---- Game Internals ----
    private boolean gameOnPause = false, humanTurn = true;
    private RabbitChaseRobot1 robot;
    public Animator1 animator = null;
    public PanelUpdater1 panelUpdater;
    private final Piece[] blackPieces = new Piece[2];
    private Piece[] whitePieces;
    private int currentRabbitIndex = 0;
    public int movesCount = 0;
    private boolean isFirstLayout = true;

    // ---- Paints ----
    private Paint highlightPaint, backgroundPaint, grainPaint, particlePaint;
    private float pulseAngle = 0f;

    private String level;
    private int[][] segmentPatterns;
    private int boardWidth;
    private int boardHeight;
    private Bitmap backgroundBitmap;

    private GameThread gameThread;
    public Piece clickedPiece;

    // ---- Particles ----
    private final List<Particle> particles = new ArrayList<>();
    private static final int PARTICLE_COUNT = 45;
    private final int[] particleColors = { 0x664FC3F7, 0x66FFD54F, 0x44FFFFFF };
    private final Random random = new Random();
    private final Paint boardMatPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint boardMatShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RabbitPannel1(Context context) { super(context); init(); }
    public RabbitPannel1(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public RabbitPannel1(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }

    private void init() {
        grainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        grainPaint.setColor(Color.parseColor("#1A000000"));
        grainPaint.setStrokeWidth(1f);

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.CYAN);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(6);
        highlightPaint.setShadowLayer(12.0f, 0.0f, 0.0f, Color.CYAN);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#3E2723"));

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this,"rp1");
        setFocusable(true);

        level = GameData.getInstance().getRabbit_1_level();

        initPatterns();
        initPieces();

        robot = new RabbitChaseRobot1(this);
        panelUpdater = new PanelUpdater1(blackPieces, whitePieces, circleArray);
        animator = new Animator1(this);
        anim();
    }

    public void anim() {
        int speedPercent = GameData.getInstance().getAnimationSpeed();
        long baseDuration = 1000;
        long adjustedDuration = baseDuration * 10 / Math.max(1, speedPercent);
        if (animator != null) animator.setDuration(adjustedDuration);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread(getHolder(), this,"rp1");
            gameThread.setRunning(true);
            gameThread.start();
        }
        requestRender();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
        generateBackgroundBitmap(width, height);
        initParticles(width, height);
        recomputeGeometry();
        if (isFirstLayout) {
            startRabbitTurn();
            isFirstLayout = false;
        }
        
        // Force redraw after short delay to ensure dimensions are fully applied and avoid black screen
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 100);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 300);
    }

    private void generateBackgroundBitmap(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (backgroundBitmap != null) backgroundBitmap.recycle();
        backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas bgCanvas = new Canvas(backgroundBitmap);

        int bgColor = 0xFF2E7D32;
        int secondaryColor = 0xFF1B5E20;

        if (level != null) {
            switch (level) {
                case "LEVEL2": bgColor = 0xFF37474F; secondaryColor = 0xFF263238; break;
                case "LEVEL3": bgColor = 0xFFBF360C; secondaryColor = 0xFF3E2723; break;
                case "LEVEL4": bgColor = 0xFF263238; secondaryColor = 0xFF102027; break;
                case "LEVEL5": bgColor = 0xFF212121; secondaryColor = 0xFF000000; break;
                case "LEVEL6": bgColor = 0xFF000000; secondaryColor = 0xFF212121; break;
            }
        }

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(new android.graphics.RadialGradient(width / 2f, height / 2f, Math.max(width, height) * 0.85f,
                new int[]{bgColor, secondaryColor}, new float[]{0.2f, 1.0f}, android.graphics.Shader.TileMode.CLAMP));
        bgCanvas.drawRect(0, 0, width, height, p);

        p.setShader(null);
        p.setStyle(Paint.Style.FILL);

        Random decorRng = RabbitRenderHelper.seededForLevel(level, width, height);
        RabbitRenderHelper.paintGroundDecor(bgCanvas, width, height, decorRng, p);
        RabbitRenderHelper.drawVignette(bgCanvas, width, height, p);
    }

    private void initParticles(int width, int height) {
        if (width <= 0 || height <= 0) return;
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle(
                    random.nextFloat() * width,
                    random.nextFloat() * height,
                    random.nextFloat() * 2f + 1f,
                    particleColors[random.nextInt(particleColors.length)],
                    (random.nextFloat() - 0.5f) * 0.3f,
                    (random.nextFloat() - 0.5f) * 0.3f
            ));
        }
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
            particlePaint.setColor(p.color);
            int alpha = (int) (120 + Math.sin(p.alphaOffset) * 60);
            particlePaint.setAlpha(alpha);
            canvas.drawCircle(p.x, p.y, p.size, particlePaint);
        }
    }

    private void recomputeGeometry() {
        initializeBoardPositions();
        initializeLineConnections();
        if (isFirstLayout) {
            initializePiecePositionsOnBoard();
        } else {
            syncPiecePositions();
        }
        requestRender();
    }

    private void syncPiecePositions() {
        for (Piece p : blackPieces) {
            if (p != null && p.getMyCircle() != null) {
                p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
                p.setOuterRadius(p.getMyCircle().getRadius() * 2.2f);
            }
        }
        for (Piece p : whitePieces) {
            if (p != null && p.getMyCircle() != null) {
                p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
                p.setOuterRadius(p.getMyCircle().getRadius() * 1.8f);
            }
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

    private void initPatterns() {
        List<int[]> allSegments = new ArrayList<>();
        // Add segments for each stack
        for (int s = 0; s < STACKS; s++) {
            int offset = s * BASE_NODES;
            for (int[] pair : BASE_MORABARABA_CONNECTIONS) {
                allSegments.add(new int[]{pair[0] + offset, pair[1] + offset});
            }
        }
        // Inter-stack connections (e.g. connecting Stack 0 to Stack 1)
        if (STACKS == 2) {
            int cBase = 0;
            int nBase = BASE_NODES;
            allSegments.add(new int[]{cBase + 2, nBase});
            allSegments.add(new int[]{cBase + 15, nBase + 9});
            allSegments.add(new int[]{cBase + 24, nBase + 22});
        }

        segmentPatterns = allSegments.toArray(new int[0][]);
        lineArray = new Line[segmentPatterns.length];
    }

    private void initializeBoardPositions() {
        boolean isPortrait = boardHeight > boardWidth;
        float effWidth = isPortrait ? boardHeight : boardWidth;
        float effHeight = isPortrait ? boardWidth : boardHeight;

        if (effWidth <= 0 || effHeight <= 0) return;

        float widthMargin = 0.92f;
        float heightMargin = 0.85f;
        float maxBoardSizeByWidth = (effWidth * widthMargin) / 2.278f;
        float maxBoardSizeByHeight = (effHeight * heightMargin) / 1.128f;
        
        float boardSize = Math.min(maxBoardSizeByHeight, maxBoardSizeByWidth);
        float stride = boardSize * 1.15f;
        float startL = (effWidth - stride) / 2f;
        float centerL = effHeight / 2f;

        float radius = boardSize / 25f;
        float halfSize = boardSize / 2f;
        float mediumOffset = halfSize * 0.66f;
        float smallOffset = halfSize * 0.33f;

        PointF[] basePos = new PointF[BASE_NODES];
        basePos[0] = new PointF(-halfSize, -halfSize);
        basePos[1] = new PointF(0, -halfSize);
        basePos[2] = new PointF(halfSize, -halfSize);
        basePos[9] = new PointF(-halfSize, 0);
        basePos[15] = new PointF(halfSize, 0);
        basePos[22] = new PointF(-halfSize, halfSize);
        basePos[23] = new PointF(0, halfSize);
        basePos[24] = new PointF(halfSize, halfSize);

        basePos[3] = new PointF(-mediumOffset, -mediumOffset);
        basePos[4] = new PointF(0, -mediumOffset);
        basePos[5] = new PointF(mediumOffset, -mediumOffset);
        basePos[10] = new PointF(-mediumOffset, 0);
        basePos[14] = new PointF(mediumOffset, 0);
        basePos[19] = new PointF(-mediumOffset, mediumOffset);
        basePos[20] = new PointF(0, mediumOffset);
        basePos[21] = new PointF(mediumOffset, mediumOffset);

        basePos[6] = new PointF(-smallOffset, -smallOffset);
        basePos[7] = new PointF(0, -smallOffset);
        basePos[8] = new PointF(smallOffset, -smallOffset);
        basePos[11] = new PointF(-smallOffset, 0);
        basePos[13] = new PointF(smallOffset, 0);
        basePos[16] = new PointF(-smallOffset, smallOffset);
        basePos[17] = new PointF(0, smallOffset);
        basePos[18] = new PointF(smallOffset, smallOffset);
        basePos[12] = new PointF(0, 0);

        for (int s = 0; s < STACKS; s++) {
            for (int i = 0; i < BASE_NODES; i++) {
                int idx = s * BASE_NODES + i;
                if (basePos[i] != null) {
                    float lx = startL + s * stride + basePos[i].x;
                    float ly = centerL + basePos[i].y;
                    
                    if (circleArray[idx] == null) circleArray[idx] = new RabbitCircle(0, 0, 0);
                    if (isPortrait) circleArray[idx].setDimensions(ly, lx);
                    else circleArray[idx].setDimensions(lx, ly);
                    circleArray[idx].setRadius(radius);
                }
            }
        }

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
        int numDogs = getNumberOfDogsForLevel(level);
        whitePieces = new DogPiece[numDogs];
        int rBase, rHigh, rStart, rEnd;
        int dBase, dHigh, dStart, dEnd;

        if (level != null) {
            switch (level) {
                case "LEVEL1":
                    rBase = 0xFF006400; rHigh = 0xFF32CD32; rStart = 0xFF00FF00; rEnd = 0xFF008000;
                    dBase = 0xFF8B0000; dHigh = 0xFFFF6347; dStart = 0xFFFF4500; dEnd = 0xFFB22222;
                    break;
                case "LEVEL2":
                    rBase = 0xFF00008B; rHigh = 0xFF1E90FF; rStart = 0xFF00BFFF; rEnd = 0xFF0000CD;
                    dBase = 0xFFFF8C00; dHigh = 0xFFFFD700; dStart = 0xFFFFA500; dEnd = 0xFFFF4500;
                    break;
                case "LEVEL3":
                    rBase = 0xFF4B0082; rHigh = 0xFF9370DB; rStart = 0xFF8A2BE2; rEnd = 0xFF800080;
                    dBase = 0xFFFFD700; dHigh = 0xFFFFFFE0; dStart = 0xFFFFFF00; dEnd = 0xFFBDB76B;
                    break;
                case "LEVEL4":
                    rBase = 0xFF008B8B; rHigh = 0xFF00FFFF; rStart = 0xFFE0FFFF; rEnd = 0xFF20B2AA;
                    dBase = 0xFF8B008B; dHigh = 0xFFFF00FF; dStart = 0xFFFF69B4; dEnd = 0xFFC71585;
                    break;
                case "LEVEL5":
                    rBase = 0xFF008080; rHigh = 0xFF40E0D0; rStart = 0xFFAFEEEE; rEnd = 0xFF008B8B;
                    dBase = 0xFFFF1493; dHigh = 0xFFFF69B4; dStart = 0xFFFFC0CB; dEnd = 0xFFC71585;
                    break;
                case "LEVEL6":
                    rBase = 0xFFB8860B; rHigh = 0xFFFFD700; rStart = 0xFFFFFACD; rEnd = 0xFFDAA520;
                    dBase = 0xFF696969; dHigh = 0xFFC0C0C0; dStart = 0xFFD3D3D3; dEnd = 0xFFA9A9A9;
                    break;
                default:
                    rBase = 0xFF000000; rHigh = 0xFF555555; rStart = 0xFFAAAAAA; rEnd = 0xFF333333;
                    dBase = 0xFFFFFFFF; dHigh = 0xFFEEEEEE; dStart = 0xFFFFFFFF; dEnd = 0xFFDDDDDD;
                    break;
            }
        } else { rBase=0xFF000000; rHigh=0xFF555555; rStart=0xFFAAAAAA; rEnd=0xFF333333; dBase=0xFFFFFFFF; dHigh=0xFFEEEEEE; dStart=0xFFFFFFFF; dEnd=0xFFDDDDDD; }

        for (int i = 0; i < circleArray.length; i++) { if(circleArray[i] == null) circleArray[i] = new RabbitCircle(0, 0, 0); circleArray[i].setIndex(i); }
        for (int i = 0; i < blackPieces.length; i++) { blackPieces[i] = new RabbitPiece(new PointF(0, 0), true); blackPieces[i].setType("B"); blackPieces[i].setColors(rBase, rHigh, rStart, rEnd); }
        for (int i = 0; i < whitePieces.length; i++) {
            DogPiece dog = new DogPiece(new PointF(0, 0), false);
            dog.setDogVariant(i);
            whitePieces[i] = dog;
            whitePieces[i].setType("W");
            whitePieces[i].setColors(dBase, dHigh, dStart, dEnd);
        }
        for (int i = 0; i < lineArray.length; i++) { lineArray[i] = new RabbitLine(0, 0, 0, 0); }
    }

    private void initializeLineConnections() {
        for (int i = 0; i < segmentPatterns.length; i++) {
            int[] seg = segmentPatterns[i];
            if (seg[0] < circleArray.length && seg[1] < circleArray.length) {
                Circle c1 = circleArray[seg[0]]; Circle c2 = circleArray[seg[1]];
                if (c1 != null && c2 != null) {
                    if (lineArray[i] == null) lineArray[i] = new RabbitLine(0,0,0,0);
                    lineArray[i].setDimensions(c1.getX(), c1.getY(), c2.getX(), c2.getY());
                }
            }
        }
    }

    public void initializePiecePositionsOnBoard() {
        for (Circle c : circleArray) if (c != null) c.resetPiece();
        int[] rabbitIndices = {12, BASE_NODES + 12};
        for (int i = 0; i < blackPieces.length; i++) {
            if (rabbitIndices[i] < circleArray.length) {
                Circle rabbitCircle = circleArray[rabbitIndices[i]];
                blackPieces[i].setMyCircle(rabbitCircle); blackPieces[i].setPosition(new PointF(rabbitCircle.getX(), rabbitCircle.getY()));
                blackPieces[i].setOuterRadius(rabbitCircle.getRadius() * 2.2f); rabbitCircle.setMyPiece(blackPieces[i], "B");
            }
        }
        int numDogs = getNumberOfDogsForLevel(level);
        int[] dogPositions;
        if (numDogs >= 3) dogPositions = new int[]{ BASE_NODES - 3, BASE_NODES - 1, BASE_NODES + 1, BASE_NODES + 22, BASE_NODES + 24, BASE_NODES * 2 - 1 };
        else if (numDogs == 2) dogPositions = new int[]{ BASE_NODES - 2, BASE_NODES + 1 };
        else dogPositions = new int[]{ BASE_NODES, BASE_NODES + 2, BASE_NODES + 12 };
        
        for(int i = 0; i < numDogs && i < dogPositions.length; i++) {
            int dogIdx = dogPositions[i];
            if (dogIdx < circleArray.length && circleArray[dogIdx].getPiece() == null) {
                Circle dogCircle = circleArray[dogIdx];
                whitePieces[i].setMyCircle(dogCircle); whitePieces[i].setPosition(new PointF(dogCircle.getX(), dogCircle.getY()));
                whitePieces[i].setOuterRadius(dogCircle.getRadius() * 1.8f); dogCircle.setMyPiece(whitePieces[i], "W");
            }
        }
        panelUpdater = new PanelUpdater1(blackPieces, whitePieces, circleArray);
    }

    private int getNumberOfDogsForLevel(String lvl) {
        if (lvl == null) return 3;
        switch (lvl) {
            case "LEVEL1": return 5;
            case "LEVEL2":
            case "LEVEL3": return 4;
            case "LEVEL6": return 2;
            default: return 3;
        }
    }

    private boolean shouldCheckInvalidMoves() { return level != null && (level.equals("LEVEL3") || level.equals("LEVEL5")); }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOnPause) return false;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            handleTap(event.getX(), event.getY());
        }
        return true;
    }

    private void handleTap(float x, float y) {
        for (Circle c : circleArray) {
            if (c != null && c.isClicked(x, y)) {
                SoundManager.getInstance(getContext()).playClick();
                circleClicked(c);
                return;
            }
        }
        clickedPiece = null;
        requestRender();
    }

    private void circleClicked(Circle circle) {
        if (humanTurn) {
            if (clickedPiece != null) {
                if (shouldCheckInvalidMoves() && !isNeighbor(clickedPiece.getMyCircle(), circle)) {
                    showToast("Invalid move!"); return;
                }
                movePiece(clickedPiece, circle);
            } else if (circle.getPiece() != null && !circle.getPiece().isBlack()) {
                clickedPiece = circle.getPiece(); requestRender();
            } else { clickedPiece = null; requestRender(); }
        } else {
            if (circle.getPiece() != null && circle.getPiece().isBlack()) { clickedPiece = circle.getPiece(); requestRender(); }
            else if (clickedPiece != null) movePiece(clickedPiece, circle);
        }
    }

    private void movePiece(Piece piece, Circle target) {
        if (piece == null || target == null) return;
        if (target.getPiece() != null) {
            showToast("Cannot move to occupied circle"); if (!piece.isBlack()) clickedPiece = null;
            requestRender(); return;
        }
        Circle current = piece.getMyCircle(); if (current == null) return;
        if (shouldCheckInvalidMoves() && !isNeighbor(current, target)) {
             showToast("Can only move to adjacent circles"); if (!piece.isBlack()) clickedPiece = null;
             requestRender(); return;
        }
        movesCount++;
        update(current, target, piece);
        if (animator != null) animator.moveAnimation(new PointF(target.getX(), target.getY()), piece);
        SoundManager.getInstance(getContext()).playChaseStep(piece instanceof RabbitPiece);
        clickedPiece = null;
        if (piece.isBlack()) humanTurn = true;
        else { humanTurn = false; startRabbitTurn(); }
    }

    private void startRabbitTurn() {
        if (circleArray == null || circleArray.length < 1 || circleArray[0] == null || circleArray[0].getX() == 0) return;
        if (robot.isRabbitTrapped(blackPieces[0]) && robot.isRabbitTrapped(blackPieces[1])) { gameOver("DOGS"); return; }
        new Handler().postDelayed(() -> {
            if (!gameOnPause) {
                robot.makeRabbitMove(blackPieces[currentRabbitIndex]);
                currentRabbitIndex = (currentRabbitIndex + 1) % blackPieces.length;
                humanTurn = true; requestRender();
            }
        }, 1000);
    }

    private void showToast(String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    public void requestRender() { if (gameThread != null) gameThread.setDirty(); }

    private float ambientTime = 0f;
    private final Paint rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] dustX = new float[20];
    private final float[] dustY = new float[20];
    private final float[] dustSpeed = new float[20];
    private final float[] dustSize = new float[20];
    private final Paint dustPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean dustInitialized = false;

    public void render(Canvas canvas) {
        if (canvas == null) return;
        if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        else canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // Dynamically highlight valid connecting lines relative to the currently active clicked piece
        for (Line line : lineArray) {
            if (line != null) {
                boolean highlightLine = false;
                if (clickedPiece != null && clickedPiece.getMyCircle() != null) {
                    Circle current = clickedPiece.getMyCircle();
                    boolean touchesCurrent = (Math.abs(line.getX1() - current.getX()) < 3f && Math.abs(line.getY1() - current.getY()) < 3f) ||
                                             (Math.abs(line.getX2() - current.getX()) < 3f && Math.abs(line.getY2() - current.getY()) < 3f);
                    if (touchesCurrent) {
                        for (Circle c : circleArray) {
                            if (c != null && c != current) {
                                boolean touchesC = (Math.abs(line.getX1() - c.getX()) < 3f && Math.abs(line.getY1() - c.getY()) < 3f) ||
                                                   (Math.abs(line.getX2() - c.getX()) < 3f && Math.abs(line.getY2() - c.getY()) < 3f);
                                if (touchesC) {
                                    boolean isValidTarget = c.getPiece() == null && (!shouldCheckInvalidMoves() || isNeighbor(current, c));
                                    if (isValidTarget) {
                                        highlightLine = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                line.setHighlighted(highlightLine);
                line.draw(canvas);
            }
        }
        
        for (Circle circle : circleArray) {
            if (circle != null) {
                if (clickedPiece != null) {
                    boolean isValidTarget = circle.getPiece() == null && (!shouldCheckInvalidMoves() || isNeighbor(clickedPiece.getMyCircle(), circle));
                    circle.setIsTarget(isValidTarget, clickedPiece.getHighlightColor());
                } else {
                    circle.setIsTarget(false);
                }
                circle.draw(canvas);
            }
        }

        for (Piece p : blackPieces) if (p != null) p.draw(canvas);
        for (Piece p : whitePieces) if (p != null) p.draw(canvas);
        
        // Draw cinematic environmental atmosphere layer
        drawAmbientAtmosphere(canvas);

        // Turn indicator header
        drawTurnIndicator(canvas);
    }

    private void drawAmbientAtmosphere(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        if (!dustInitialized) {
            for (int i = 0; i < dustX.length; i++) {
                dustX[i] = random.nextFloat() * width;
                dustY[i] = random.nextFloat() * height;
                dustSpeed[i] = random.nextFloat() * 0.4f + 0.15f;
                dustSize[i] = random.nextFloat() * 4.5f + 2.0f;
            }
            dustInitialized = true;
        }

        ambientTime += 0.015f;

        // God Rays
        rayPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 3; i++) {
            float pulseRay = (float) Math.sin(ambientTime * 0.4f + i * 1.2f);
            int alphaRay = (int) (16 + pulseRay * 8);
            if (alphaRay < 0) alphaRay = 0;

            rayPaint.setColor(0xFFFFF9C4);
            rayPaint.setAlpha(alphaRay);

            float startX1 = (width * 0.1f) + (i * width * 0.25f) + (pulseRay * 20f);
            float startX2 = startX1 + (width * 0.18f);

            android.graphics.Path rayPath = new android.graphics.Path();
            rayPath.moveTo(startX1, -50f);
            rayPath.lineTo(startX2, -50f);
            rayPath.lineTo(startX2 + (width * 0.4f), height + 50f);
            rayPath.lineTo(startX1 + (width * 0.3f), height + 50f);
            rayPath.close();

            canvas.drawPath(rayPath, rayPaint);
        }

        // Pollen Dust Particles
        dustPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < dustX.length; i++) {
            dustX[i] += dustSpeed[i] * 0.7f;
            dustY[i] += dustSpeed[i];

            if (dustX[i] > width) dustX[i] = -20f;
            if (dustY[i] > height) {
                dustY[i] = -20f;
                dustX[i] = random.nextFloat() * width;
            }

            float pulseMote = (float) Math.sin(ambientTime * 2.0f + i);
            int alphaMote = (int) (130 + pulseMote * 60);
            if (alphaMote < 0) alphaMote = 0;
            if (alphaMote > 255) alphaMote = 255;

            dustPaint.setColor(0xFFFFFDE7);
            dustPaint.setAlpha(alphaMote);

            canvas.drawCircle(dustX[i], dustY[i], dustSize[i], dustPaint);
        }
    }

    private void drawTurnIndicator(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(36f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);

        String text = humanTurn ? "DOGS' TURN (YOU)" : "RABBITS' TURN (ROBOT)";

        float centerX = getWidth() / 2f;
        float topY = 25f;
        float bottomY = 95f;
        float halfWidth = 310f;

        // Shadow
        paint.setColor(0x40000000);
        canvas.drawRoundRect(centerX - halfWidth + 4, topY + 4, centerX + halfWidth + 4, bottomY + 4, 35, 35, paint);

        // Espresso Dark Glass Pill Container
        paint.setColor(0xDD1A0C08);
        canvas.drawRoundRect(centerX - halfWidth, topY, centerX + halfWidth, bottomY, 35, 35, paint);

        // Gold Ring Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.5f);
        paint.setShader(new android.graphics.LinearGradient(centerX - halfWidth, topY, centerX + halfWidth, bottomY,
                new int[]{0xFFFFD700, 0xFFFFA000, 0xFFFFD700}, null, android.graphics.Shader.TileMode.CLAMP));
        canvas.drawRoundRect(centerX - halfWidth, topY, centerX + halfWidth, bottomY, 35, 35, paint);

        // Status Text
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        paint.setShadowLayer(6f, 0f, 2f, Color.BLACK);
        paint.setColor(humanTurn ? 0xFFFFCC80 : 0xFFFFD54F);
        canvas.drawText(text, centerX, 70f, paint);
        paint.clearShadowLayer();
    }

    private boolean isNeighbor(Circle current, Circle target) {
        if (current == null || current.getNeighbour() == null) return false;
        for (int idx : current.getNeighbour()) if (idx >= 0 && idx < circleArray.length && circleArray[idx] == target) return true;
        return false;
    }

    public void pause() { if (gameThread != null) gameThread.setRunning(false); }
    public void stop() {
        boolean retry = true; if (gameThread != null) { gameThread.setRunning(false);
            while(retry) { try { gameThread.join(); retry = false; } catch (InterruptedException ignored) {  } }
        }
        gameThread = null;
    }

    public void resume() {
        if (gameThread == null || !gameThread.isAlive()) { gameThread = new GameThread(getHolder(), this,"rp1"); gameThread.setRunning(true); gameThread.start(); }
    }

    public void update(Circle from, Circle to, Piece piece) { if (panelUpdater != null) panelUpdater.start(from, to, piece); requestRender(); }

    public void gameOver(String winnerName) {
        if ("DOGS".equals(winnerName)) { updateLevel(); }
        Context context = getContext();
        EndGameDialog endGameDialog = new EndGameDialog(context, winnerName, 10, movesCount, true, "RABBIT1");
        endGameDialog.setCancelable(false);
        Objects.requireNonNull(endGameDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
        endGameDialog.show();
    }

    private void updateLevel() {
        GameData gameData = GameData.getInstance(); String currentLevel = gameData.getRabbit_1_level(), nextLevel;
        switch (currentLevel) {
            case "LEVEL1": nextLevel = "LEVEL2"; break;
            case "LEVEL2": nextLevel = "LEVEL3"; break;
            case "LEVEL3": nextLevel = "LEVEL4"; break;
            case "LEVEL4": nextLevel = "LEVEL5"; break;
            default: nextLevel = "LEVEL6"; break;
        }
        if (!currentLevel.equals(nextLevel)) { gameData.setRabbit_1_level(nextLevel); gameData.save(this.getContext()); }
    }
    public void gamePaused() { gameOnPause = true; requestRender(); }
    public void gameResumed() { gameOnPause = false; requestRender(); }
    public void gameSurrender() { gameOver("RABBIT"); }
    public void segment(String message) { showToast(message); }
    public RabbitChaseRobot1 getRobot() { return robot; }

    private static class Particle {
        float x, y, size;
        int color;
        float vx, vy;
        float alphaOffset = 0;
        Particle(float x, float y, float size, int color, float vx, float vy) {
            this.x = x; this.y = y; this.size = size; this.color = color; this.vx = vx; this.vy = vy;
        }
    }
}
