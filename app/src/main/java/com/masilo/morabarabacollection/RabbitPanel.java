package com.masilo.morabarabacollection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class RabbitPanel extends SurfaceView implements SurfaceHolder.Callback {
    String[] mapArray = new String[25];
    private Paint backgroundPaint;
    private boolean gameOnPause = false, rabbitTurn = false, humanTurn = true;
    private RabbitChaseRobot robot;
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

    private Bitmap backgroundBitmap;
    private final Random random = new Random();

    // Cinematic Atmospheric Layer state
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

    public Piece turnPiece, turnPiece1;
    private String level;
    private GameThread gameThread;
    private Piece clickedPiece;
    private TextView gameMessages;
    private int userMovesCount = 0;
    private boolean isFirstLayout = true;

    public RabbitPanel(Context context) {
        super(context);
        init();
    }

    public RabbitPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RabbitPanel(Context context, AttributeSet attrs, int defStyle) {
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

    public RabbitChaseRobot getRobot() {
        return robot;
    }

    public void setBlack() {
        level = GameData.getInstance().getRabbit_level();
        initPieces();
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
            displayMessage("Game paused");
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            performClick();
            float x = event.getX();
            float y = event.getY();

            boolean handled = false;
            for (Piece piece : whitePieces) {
                if (piece != null && piece.isClicked(x, y)) {
                    vibrate(30);
                    SoundManager.getInstance(getContext()).playClick();
                    pieceClicked(piece);
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                for (Piece piece : blackPieces) {
                    if (piece != null && piece.isClicked(x, y)) {
                        vibrate(30);
                        SoundManager.getInstance(getContext()).playClick();
                        pieceClicked(piece);
                        handled = true;
                        break;
                    }
                }
            }

            if (!handled) {
                for (Circle circle : circleArray) {
                    if (circle != null && circle.isClicked(x, y)) {
                        vibrate(20);
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

    private void circleClicked(Circle circle) {
        if (gameOnPause) {
            segment("Game is paused");
            return;
        }
        if (rabbitTurn) {
            segment("It's not your turn");
            return;
        }
        if (humanTurn) {
            if (clickedPiece == null) return;

            if (circle.getPiece() != null) {
                segment("This spot is already occupied!");
                return;
            }

            if (shouldCheckInvalidMoves() && !isValidMove(clickedPiece, circle)) {
                segment("Invalid move! ");
                return;
            }

            movePiece(clickedPiece, circle);
            clickedPiece = null;

            userMovesCount++;
            if (getContext() instanceof Rabbit) {
                ((Rabbit) getContext()).incrementUserMoves();
            }

            humanTurn = false;
            rabbitTurn = true;

            if (isRabbitTrapped()) {
                gameOver("YOU");
                return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!gameOnPause) {
                    robot.start(true);
                    rabbitTurn = false;
                    humanTurn = true;

                    if (isRabbitTrapped()) {
                        gameOver("YOU");
                    }
                    requestRender();
                }
            }, 750);
        }
    }

    public void movePiece(Piece piece, Circle target) {
        update(piece.getMyCircle(), target, piece);
        if (animator != null) {
            animator.moveAnimation(new PointF(target.getX(), target.getY()), piece);
        }
        SoundManager.getInstance(getContext()).playChaseStep(piece instanceof RabbitPiece);
        requestRender();
    }

    private void pieceClicked(Piece piece) {
        if (piece.isBlack()) {
            segment("You can only move dogs, not the rabbit!");
            return;
        }
        if (gameOnPause) {
            segment("Game is paused");
            return;
        }
        if (rabbitTurn) {
            segment("It's not your turn");
            return;
        }
        if (humanTurn) {
            clickedPiece = piece;
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void init() {
        robot = new RabbitChaseRobot(this);
        
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#3E2723"));

        // Visuals

        getHolder().addCallback(this);
        gameThread = new GameThread(getHolder(), this,"rp");
        setFocusable(true);
        level = GameData.getInstance().getRabbit_level();
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
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new GameThread(getHolder(), this,"rp");
            gameThread.setRunning(true);
            gameThread.start();
        }
        requestRender();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (width <= 0 || height <= 0) return;
        generateBackgroundBitmap(width, height);
        recomputeGeometry();
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 100);
        new Handler(Looper.getMainLooper()).postDelayed(this::requestRender, 300);
    }

    private void recomputeGeometry() {
        MorabarabaHelper.initBoard(getWidth(), getHeight(), circleArray, lineArray);
        if (isFirstLayout) {
            MorabarabaHelper.initRabbitGamePieces(getWidth(), getHeight(), level, circleArray, blackPieces, whitePieces);
            isFirstLayout = false;
        } else {
            syncPiecePositions();
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

    private void generateBackgroundBitmap(int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (backgroundBitmap != null) backgroundBitmap.recycle();
        backgroundBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas bgCanvas = new Canvas(backgroundBitmap);

        Shader shader = getShader(width, height);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(shader);
        bgCanvas.drawRect(0, 0, width, height, p);

        // Ground micro-texture overlay (gives a professional textured board-game look)
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        
        Random decorRng = RabbitRenderHelper.seededForLevel(level, width, height);
        RabbitRenderHelper.paintGroundDecor(bgCanvas, width, height, decorRng, p);
        RabbitRenderHelper.drawVignette(bgCanvas, width, height, p);
    }

    @NonNull
    private Shader getShader(int width, int height) {
        int bgColor = 0xFF2E7D32; // Forest Green
        int secondaryColor = 0xFF1B5E20;

        switch (level) {
            case "LEVEL2": bgColor = 0xFF37474F; secondaryColor = 0xFF263238; break;
            case "LEVEL3": bgColor = 0xFFBF360C; secondaryColor = 0xFF3E2723; break; // Deep Autumn (Was Blue)
            case "LEVEL4": bgColor = 0xFF263238; secondaryColor = 0xFF102027; break;
            case "LEVEL5": bgColor = 0xFF212121; secondaryColor = 0xFF000000; break;
            case "LEVEL6": bgColor = 0xFF000000; secondaryColor = 0xFF212121; break;
        }

        return new RadialGradient(width / 2f, height / 2f, Math.max(width, height) * 0.85f,
                new int[]{bgColor, secondaryColor}, new float[]{0.2f, 1.0f}, Shader.TileMode.CLAMP);
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
            DogPiece dog = new DogPiece(new PointF(0, 0), false);
            dog.setDogVariant(i);
            whitePieces[i] = dog;
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
        switch (level) {
            case "LEVEL1": return 5;
            case "LEVEL2":
            case "LEVEL3": return 4;
            case "LEVEL6": return 2;
            default: return 3;
        }
    }

    private boolean shouldCheckInvalidMoves() {
        return level.equals("LEVEL3") || level.equals("LEVEL5");
    }

    private boolean isValidMove(Piece piece, Circle target) {
        Circle current = piece.getMyCircle();
        if (current == null) return false;
        for (int neighborIndex : current.getNeighbour()) {
            if (circleArray[neighborIndex] == target) return true;
        }
        return false;
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
            gameThread = new GameThread(getHolder(), this,"rp");
            gameThread.setRunning(true);
            gameThread.start();
        }
        requestRender();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        pause();
    }

    public void render(Canvas canvas) {
        if (canvas == null) return;
        if (backgroundBitmap != null && !backgroundBitmap.isRecycled()) canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        else canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        RabbitRenderHelper.drawBoardPlayMat(canvas, circleArray, boardMatPaint, boardMatShadowPaint);

        // Dynamically highlight valid lines relative to the currently active clicked piece
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

        boolean dogsTurn = !rabbitTurn && humanTurn;
        for (Piece p : blackPieces) {
            if (p == null) continue;
            p.setTurn(rabbitTurn);
            p.setVisualAlpha(RabbitRenderHelper.inactivePieceAlpha(rabbitTurn, p.isHighlighted()));
            p.draw(canvas);
        }
        for (Piece p : whitePieces) {
            if (p == null) continue;
            p.setTurn(dogsTurn);
            p.setVisualAlpha(RabbitRenderHelper.inactivePieceAlpha(dogsTurn, p.isHighlighted()));
            p.draw(canvas);
        }
        
        // Draw highly professional atmospheric cinematic overlay pass
        drawAmbientAtmosphere(canvas);

        // Turn indicator at the top
        drawTurnIndicator(canvas);
    }

    private void drawAmbientAtmosphere(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        // Initialize dust particle coordinates if not ready
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

        // 1. Render Cinematic Sun Shafts / God Rays stretching from top-left diagonally
        rayPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 3; i++) {
            float pulseRay = (float) Math.sin(ambientTime * 0.4f + i * 1.2f);
            int alphaRay = (int) (16 + pulseRay * 8);
            if (alphaRay < 0) alphaRay = 0;

            rayPaint.setColor(0xFFFFF9C4); // Soft honey gold ray spectrum
            rayPaint.setAlpha(alphaRay);

            float startX1 = (width * 0.1f) + (i * width * 0.25f) + (pulseRay * 20f);
            float startX2 = startX1 + (width * 0.18f);
            
            Path rayPath = new Path();
            rayPath.moveTo(startX1, -50f);
            rayPath.lineTo(startX2, -50f);
            rayPath.lineTo(startX2 + (width * 0.4f), height + 50f);
            rayPath.lineTo(startX1 + (width * 0.3f), height + 50f);
            rayPath.close();

            canvas.drawPath(rayPath, rayPaint);
        }

        // 2. Render floating magical forest pollen dust particles
        dustPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < dustX.length; i++) {
            // Drift dust motes diagonally down-right mimicking ray direction
            dustX[i] += dustSpeed[i] * 0.7f;
            dustY[i] += dustSpeed[i];

            // Wrap around viewport boundaries organically
            if (dustX[i] > width) dustX[i] = -20f;
            if (dustY[i] > height) {
                dustY[i] = -20f;
                dustX[i] = random.nextFloat() * width;
            }

            // High-fidelity individual breathing glow alpha
            float pulseMote = (float) Math.sin(ambientTime * 2.0f + i);
            int alphaMote = (int) (130 + pulseMote * 60);
            if (alphaMote < 0) alphaMote = 0;
            if (alphaMote > 255) alphaMote = 255;

            dustPaint.setColor(0xFFFFFDE7); // Cream glowing pollen speck
            dustPaint.setAlpha(alphaMote);
            
            // Draw premium soft blurred bokeh glare mote
            canvas.drawCircle(dustX[i], dustY[i], dustSize[i], dustPaint);
        }
    }

    private void drawTurnIndicator(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(36f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        
        String text = rabbitTurn ? "RABBIT'S TURN (ROBOT)" : "DOGS' TURN (YOU)";
        
        float centerX = getWidth() / 2f;
        float topY = 25f;
        float bottomY = 95f;
        float halfWidth = 280f;

        // Draw professional premium glass pill shadow
        paint.setColor(0x40000000);
        canvas.drawRoundRect(centerX - halfWidth + 4, topY + 4, centerX + halfWidth + 4, bottomY + 4, 35, 35, paint);

        // Draw premium status background container with a deep dark glass panel look
        paint.setColor(0xDD1A0C08); // Espresso matte dark glass
        canvas.drawRoundRect(centerX - halfWidth, topY, centerX + halfWidth, bottomY, 35, 35, paint);

        // Draw luxury gold accent ring border frame
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3.5f);
        paint.setShader(new LinearGradient(centerX - halfWidth, topY, centerX + halfWidth, bottomY,
                new int[]{0xFFFFD700, 0xFFFFA000, 0xFFFFD700}, null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(centerX - halfWidth, topY, centerX + halfWidth, bottomY, 35, 35, paint);

        // Reset style to paint textual components with a sleek light sheen shadow
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(null);
        paint.setShadowLayer(6f, 0f, 2f, Color.BLACK);

        // Set rich text color mapping based on active player's turn profile
        paint.setColor(rabbitTurn ? 0xFFFFD54F : 0xFFFFCC80);
        canvas.drawText(text, centerX, 70f, paint);
        paint.clearShadowLayer();
    }

    public void setGameMessages(TextView gameMessages) { this.gameMessages = gameMessages; }
    
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
        generateBackgroundBitmap(w, h);
        recomputeGeometry();
    }

    public void gameOver(String winnerName) {
        if ("YOU".equals(winnerName)) updateLevel();
        Context context = getContext();
        if (context instanceof Rabbit) ((Rabbit) context).onGameOver(winnerName);
        else {
            showGameOverDialog(winnerName, userMovesCount);
        }
    }

    public void showGameOverDialog(String winnerName, int moves) {
        Context context = getContext();
        EndGameDialog endGameDialog = new EndGameDialog(context, winnerName, 10, moves, true, "RABBIT");
        endGameDialog.setCancelable(false);
        Objects.requireNonNull(endGameDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
        endGameDialog.show();
    }

    private void updateLevel() {
        GameData gameData = GameData.getInstance();
        String currentLevel = gameData.getRabbit_level();
        String nextLevel;
        switch (currentLevel) {
            case "LEVEL1": nextLevel = "LEVEL2"; break;
            case "LEVEL2": nextLevel = "LEVEL3"; break;
            case "LEVEL3": nextLevel = "LEVEL4"; break;
            case "LEVEL4": nextLevel = "LEVEL5"; break;
            default: nextLevel = "LEVEL6"; break;
        }
        if (!currentLevel.equals(nextLevel)) {
            gameData.setRabbit_level(nextLevel);
            gameData.save(this.getContext());
        }
    }
    public void gamePaused() { gameOnPause = true; requestRender(); }
    public void gameResumed() { gameOnPause = false; requestRender(); }
    public void gameSurrender() { gameOver("ROBOT"); }
    public void segment(String message) { displayMessage(message); }

    public void update(Circle from, Circle to, Piece piece) {
        if (panelUpdater != null) panelUpdater.start(from, to, piece);
        requestRender();
    }

    public boolean isRabbitTrapped() { return robot != null && robot.isRabbitTrapped(); }

    public void requestRender() { if (gameThread != null) gameThread.setDirty(); }
}
