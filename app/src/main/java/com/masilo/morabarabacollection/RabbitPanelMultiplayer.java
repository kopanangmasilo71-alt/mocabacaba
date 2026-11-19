package com.masilo.morabarabacollection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
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
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * The panel that controls the rabbit game with 2 players (PvP) on the same device.
 */
public class RabbitPanelMultiplayer extends SurfaceView implements SurfaceHolder.Callback {
    String[] mapArray = new String[25];
    private Paint backgroundPaint;
    private boolean gameOnPause = false;
    private boolean rabbitTurn = false; // Starts with Dogs (White)
    private boolean dogTurn = true;
    
    Animator1 animator = null;
    public PanelUpdater panelUpdater;
    String[][] block = new String[18][3];
    private final Piece[] blackPieces = new Piece[1]; // Rabbit
    private Piece[] whitePieces; // Dogs - dynamic based on level
    public Circle[] circleArray = new Circle[25];
    public Line[] lineArray = new Line[40];
    String[][] segmentData = new String[42][2];
    public int index = -1;
    private int[][] segmentPatterns, patterns;
    private final Paint circleArrayPaint = new Paint();

    // Highlight Paints
    private Paint highlightPaint;
    private Paint validMovePaint;
    private float pulseAngle = 0f;

    public Piece turnPiece, turnPiece1;
    private String level;
    private GameThread gameThread;
    private Piece clickedPiece;
    private int userMovesCount = 0;
    private boolean isFirstLayout = true;

    public RabbitPanelMultiplayer(Context context) {
        super(context);
        init();
    }

    public RabbitPanelMultiplayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RabbitPanelMultiplayer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public int[][] getPatterns() {
        return patterns;
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

    public void setBlack() {
        level = GameData.getInstance().getRabbit_level(); // Use rabbit_level
        initPieces(); // Reinitialize pieces with correct level
        requestRender();
    }

    public void anim() {
        int speedPercent = GameData.getInstance().getAnimationSpeed();
        long baseDuration = 1000;
        long adjustedDuration = baseDuration * 10 / Math.max(1, speedPercent);
        if (animator != null) {
            animator.setDuration(adjustedDuration);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOnPause) {
            showToast("Game paused");
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
            float x = event.getX();
            float y = event.getY();

            boolean handled = false;
            // Check piece clicks
            // If Dog turn, check white pieces
            if (dogTurn) {
                for (Piece piece : whitePieces) {
                    if (piece != null && piece.isClicked(x, y)) {
                        SoundManager.getInstance(getContext()).playClick();
                        pieceClicked(piece);
                        handled = true;
                        break;
                    }
                }
            }
            // If Rabbit turn, check black pieces
            if (!handled && rabbitTurn) {
                for (Piece piece : blackPieces) {
                    if (piece != null && piece.isClicked(x, y)) {
                        SoundManager.getInstance(getContext()).playClick();
                        pieceClicked(piece);
                        handled = true;
                        break;
                    }
                }
            }
            
            // Allow clicking the opponent's pieces just to say "Not your turn"?
            if (!handled) {
                for (Piece piece : whitePieces) {
                    if (piece != null && piece.isClicked(x, y)) {
                        SoundManager.getInstance(getContext()).playClick();
                        pieceClicked(piece);
                        handled = true;
                        break;
                    }
                }
                 if (!handled) {
                    for (Piece piece : blackPieces) {
                        if (piece != null && piece.isClicked(x, y)) {
                            SoundManager.getInstance(getContext()).playClick();
                            pieceClicked(piece);
                            handled = true;
                            break;
                        }
                    }
                }
            }

            // Check circle clicks
            if (!handled) {
                for (Circle circle : circleArray) {
                    if (circle != null && circle.isClicked(x, y)) {
                        SoundManager.getInstance(getContext()).playClick();
                        circleClicked(circle);
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

    private void circleClicked(Circle circle) {
        if (gameOnPause) {
            segment("Game is paused");
            return;
        }
        
        if (clickedPiece == null) return;
        
        if (dogTurn) {
             if (clickedPiece.isBlack()) {
                 segment("It's Dogs' (White) turn!");
                 return;
             }
             
            // Check for invalid moves if required by level
            if (shouldCheckInvalidMoves() && !isValidMove(clickedPiece, circle)) {
                segment("Invalid move!");
                return;
            }

            movePiece(clickedPiece, circle);
            if (clickedPiece != null) clickedPiece.setHighlighted(false);
            clickedPiece = null;
            userMovesCount++;

            // Increment user moves if applicable (Context dependent)
            if (getContext() instanceof Rabbit) {
                ((Rabbit) getContext()).incrementUserMoves();
            }

            // Switch turns
            dogTurn = false;
            rabbitTurn = true;
            
            segment("Rabbit's Turn");

            // Check if rabbit is trapped after Dog move
            if (isRabbitTrapped()) {
                gameOver("DOGS"); // Dogs win
            }
            
        } else if (rabbitTurn) {
            if (!clickedPiece.isBlack()) {
                segment("It's Rabbit's (Black) turn!");
                return;
            }
            
            // Rabbit always needs valid moves
            if (!isValidMove(clickedPiece, circle)) {
                segment("Invalid move!");
                return;
            }

            movePiece(clickedPiece, circle);
            if (clickedPiece != null) clickedPiece.setHighlighted(false);
            clickedPiece = null;
            userMovesCount++;

            // Switch turns
            rabbitTurn = false;
            dogTurn = true;
            

            
             // Check if rabbit is trapped after its own move
            if (isRabbitTrapped()) {
                gameOver("DOGS");
            }
        }
    }

    public void movePiece(Piece piece, Circle target) {
        update(piece.getMyCircle(), target, piece);
        if (animator != null) {
            animator.moveAnimation(new PointF(target.getX(), target.getY()), piece);
        }
        requestRender();
    }

    private void pieceClicked(Piece piece) {
        if (gameOnPause) {
            segment("Game is paused");
            return;
        }
        
        if (dogTurn) {
            if (piece.isBlack()) {
                segment("It's Dogs' (White) turn!");
                return;
            }
            if (clickedPiece != null) clickedPiece.setHighlighted(false);
            clickedPiece = piece;
            clickedPiece.setHighlighted(true);

        } else if (rabbitTurn) {
            if (!piece.isBlack()) {
                 segment("It's Rabbit's (Black) turn!");
                 return;
            }
            if (clickedPiece != null) clickedPiece.setHighlighted(false);
            clickedPiece = piece;
            clickedPiece.setHighlighted(true);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void init() {

        // Background Paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#3E2723"));

        // Initialize highlight paints
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.parseColor("#FFD700")); // Golden
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(8);
        highlightPaint.setShadowLayer(20.0f, 0.0f, 0.0f, Color.parseColor("#FFD700"));

        validMovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        validMovePaint.setColor(Color.parseColor("#AA00E5FF")); // Neon Blue for valid moves
        validMovePaint.setStyle(Paint.Style.FILL);
        validMovePaint.setShadowLayer(15.0f, 0.0f, 0.0f, Color.parseColor("#00E5FF"));
        validMovePaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL));

        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this,"rpm");
        setFocusable(true);
        level = GameData.getInstance().getRabbit_level(); // Use rabbit_level
        initGameItems();
        initPatterns();
        initPieces();
        panelUpdater = new PanelUpdater(blackPieces, whitePieces, circleArray, block, patterns, mapArray, segmentData, segmentPatterns);
        animator = new Animator1(this);
        anim();
    }

    private void initGameItems() {
        turnPiece1 = new Piece( new PointF(0, 0), true);
        turnPiece = new Piece( new PointF(0, 0), true);
        circleArrayPaint.setColor(Color.BLACK);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread(getHolder(), this,"rpm");
            gameThread.setRunning(true);
            gameThread.start();
        }
        requestRender();
    }

    private void syncPiecePositions() {
        float effH = Math.min(getWidth(), getHeight());
        float cellHeight = effH / 14.0f;
        for (Piece p : blackPieces) {
            if (p != null && p.getMyCircle() != null) {
                p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
                p.setOuterRadius(cellHeight - (cellHeight / 6));
            }
        }
        for (Piece p : whitePieces) {
            if (p != null && p.getMyCircle() != null) {
                p.setXY(p.getMyCircle().getX(), p.getMyCircle().getY());
                p.setOuterRadius(cellHeight - (cellHeight / 4));
            }
        }
    }

    public void gameOver(String winnerName) {
        Context context = getContext();
        if (context instanceof Rabbit) {
            ((Rabbit) context).onGameOver(winnerName);
        } else {
            EndGameDialog endGameDialog = new EndGameDialog(context, winnerName, 10, userMovesCount, true);
            endGameDialog.setCancelable(false);
            Objects.requireNonNull(endGameDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
            endGameDialog.show();
        }
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
        gameOver("RABBIT");
    }

    public void segment(String message) {
        showToast(message);
    }

    public void update(Circle from, Circle to, Piece piece) {
        if (panelUpdater != null) {
            panelUpdater.start(from, to, piece);
        }
        requestRender();
    }

    public boolean isRabbitTrapped() {
        if (blackPieces[0] == null) return true;
        Circle rabbitCircle = blackPieces[0].getMyCircle();
        if (rabbitCircle == null) return true;
        List<Circle> moves = getValidMoves(rabbitCircle);
        return moves.isEmpty();
    }

    private List<Circle> getValidMoves(Circle c) {
        List<Circle> moves = new ArrayList<>();
        if (c == null || c.getNeighbour() == null) return moves;
        for (int n : c.getNeighbour()) {
            if (n >= 0 && n < circleArray.length && circleArray[n] != null) {
                String type = circleArray[n].getPieceType();
                if (type == null || type.equals("N") || type.isEmpty()) {
                    moves.add(circleArray[n]);
                }
            }
        }
        return moves;
    }

    public void requestRender() {
        if (gameThread != null) {
            gameThread.setDirty();
        }
    }

    private void initPatterns() {
        for (String[] strings : block) Arrays.fill(strings, "N");
        for (String[] segmentDatum : segmentData) Arrays.fill(segmentDatum, "N");
        segmentPatterns = BoardDefinition.getSegmentPatterns();
        patterns = BoardDefinition.getPatterns();
        Arrays.fill(mapArray, "N");
    }

    private void initPieces() {
        int numDogs = getNumberOfDogsForLevel(level);
        whitePieces = new DogPiece[numDogs];

        int rabbitBase = 0xFFFBC02D, rabbitHigh = 0xFFFFF176, rabbitStart = 0xFFFFFFFF, rabbitIconE = 0xFFFBC02D;
        int dogBase = 0xFF4E342E, dogHigh = 0xFFA1887F, dogStart = 0xFFD7CCC8, dogIconE = 0xFF4E342E;

        blackPieces[0] = new RabbitPiece(new PointF(0, 0), true);
        blackPieces[0].setColors(rabbitBase, rabbitHigh, rabbitStart, rabbitIconE);

        for (int i = 0; i < whitePieces.length; i++) {
            whitePieces[i] = new DogPiece(new PointF(0, 0), false);
            whitePieces[i].setColors(dogBase, dogHigh, dogStart, dogIconE);
        }

        for (int i1 = 0; i1 < lineArray.length; i1++) {
            lineArray[i1] = new RabbitLine(0, 0, 0, 0);
        }

        for (int i = 0; i < circleArray.length; i++) {
            circleArray[i] = new RabbitCircle(0, 0, 0);
            circleArray[i].setIndex(i);
        }
    }

    private int getNumberOfDogsForLevel(String level) {
        if (level == null) return 3;
        switch (level) {
            case "LEVEL1": return 5;
            case "LEVEL2":
            case "LEVEL3": return 4;
            case "LEVEL6": return 2;
            default: return 3;
        }
    }

    private boolean shouldCheckInvalidMoves() {
        if (level == null) return false;
        return level.equals("LEVEL3") || level.equals("LEVEL5");
    }

    private boolean isValidMove(Piece piece, Circle target) {
        Circle current = piece.getMyCircle();
        if (current == null || current.getNeighbour() == null) return false;
        for (int neighborIndex : current.getNeighbour()) {
            if (neighborIndex >= 0 && neighborIndex < circleArray.length && circleArray[neighborIndex] == target) return true;
        }
        return false;
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    private Bitmap backgroundBitmap;
    private final java.util.Random random = new java.util.Random();

    private float ambientTime = 0f;
    private final Paint rayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] dustX = new float[20];
    private final float[] dustY = new float[20];
    private final float[] dustSpeed = new float[20];
    private final float[] dustSize = new float[20];
    private final Paint dustPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean dustInitialized = false;
    private final Paint boardMatPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint boardMatShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (width <= 0 || height <= 0) return;
        generateBackgroundBitmap(width, height);
        MorabarabaHelper.initBoard(width, height, circleArray, lineArray);
        if (isFirstLayout) {
            MorabarabaHelper.initRabbitGamePieces(width, height, level, circleArray, blackPieces, whitePieces);
            isFirstLayout = false;
        } else {
            syncPiecePositions();
        }
        
        requestRender();
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

        // Micro textures
        for (int i = 0; i < 260; i++) {
            float x = random.nextFloat() * width;
            float y = random.nextFloat() * height;
            int typeChance = random.nextInt(3);

            if (typeChance == 0) {
                p.setColor(0x18000000 | (0x2E7D32 & 0xFFFFFF));
                float h = random.nextFloat() * 18 + 6;
                float skew = random.nextFloat() * 4 - 2;
                bgCanvas.save();
                bgCanvas.translate(x, y);
                bgCanvas.drawLine(0, 0, skew, -h, p);
                bgCanvas.drawLine(0, 0, skew + 3, -h + 2, p);
                bgCanvas.restore();
            } else if (typeChance == 1) {
                p.setColor(0x1A000000 | (0x1B5E20 & 0xFFFFFF));
                float r = random.nextFloat() * 25 + 12;
                bgCanvas.drawCircle(x, y, r, p);
            } else {
                p.setColor(0x12000000 | (0x7D6B5D & 0xFFFFFF));
                float rw = random.nextFloat() * 6 + 3;
                float rh = random.nextFloat() * 4 + 2;
                bgCanvas.drawOval(x - rw, y - rh, x + rw, y + rh, p);
            }
        }

        android.graphics.RadialGradient vignette = new android.graphics.RadialGradient(width / 2f, height / 2f, Math.max(width, height) * 0.95f,
                new int[]{0x00000000, 0x1E000000, 0x7A000000}, new float[]{0.0f, 0.5f, 1.0f}, android.graphics.Shader.TileMode.CLAMP);
        p.setShader(vignette);
        bgCanvas.drawRect(0, 0, width, height, p);
    }

    public void render(Canvas canvas) {
        if (canvas == null) return;

        if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        else canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // Dynamically highlight valid lines relative to current clicked piece
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
                                    boolean isValidTarget = c.getPiece() == null && (!shouldCheckInvalidMoves() || isValidMove(clickedPiece, c));
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
                    boolean isValidTarget = circle.getPiece() == null && (!shouldCheckInvalidMoves() || isValidMove(clickedPiece, circle));
                    circle.setIsTarget(isValidTarget, clickedPiece.getHighlightColor());
                } else {
                    circle.setIsTarget(false);
                }
                circle.draw(canvas);
            }
        }

        // Render black pieces (Rabbit)
        for (Piece p : blackPieces) {
            if (p != null) {
                p.setTurn(rabbitTurn);
                p.draw(canvas);
            }
        }
        
        // Render white pieces (Dogs)
        for (Piece p : whitePieces) {
            if (p != null) {
                p.setTurn(dogTurn);
                p.draw(canvas);
            }
        }

        // Draw ambient atmospheric elements
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

        // Dust pollen
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

        String text = dogTurn ? "P1: DOGS (YOU)" : "P2: RABBIT (P2)";

        float centerX = getWidth() / 2f;
        float topY = 25f;
        float bottomY = 95f;
        float halfWidth = 300f;

        // Shadow
        paint.setColor(0x40000000);
        canvas.drawRoundRect(centerX - halfWidth + 4, topY + 4, centerX + halfWidth + 4, bottomY + 4, 35, 35, paint);

        // Dark espresso glass pill container
        paint.setColor(0xDD1A0C08);
        canvas.drawRoundRect(centerX - halfWidth, topY, centerX + halfWidth, bottomY, 35, 35, paint);

        // Gold Ring Border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.5f);
        paint.setShader(new android.graphics.LinearGradient(centerX - halfWidth, topY, centerX + halfWidth, bottomY,
                new int[]{0xFFFFD700, 0xFFFFA000, 0xFFFFD700}, null, android.graphics.Shader.TileMode.CLAMP));
        canvas.drawRoundRect(centerX - halfWidth, topY, centerX + halfWidth, bottomY, 35, 35, paint);

        // Text
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        paint.setShadowLayer(6f, 0f, 2f, Color.BLACK);
        paint.setColor(dogTurn ? 0xFFFFCC80 : 0xFFFFD54F);
        canvas.drawText(text, centerX, 70f, paint);
        paint.clearShadowLayer();
    }

    public void pause() {
        if (gameThread != null) gameThread.setRunning(false);
    }

    public void stop() {
        pause();
        if (gameThread != null) {
            try { gameThread.join(); } catch (InterruptedException ignored) {  }
            gameThread = null;
        }
    }

    public void resume() {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread(getHolder(), this,"rpm");
            gameThread.setRunning(true);
            gameThread.start();
        }
        requestRender();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        pause();
    }
}
