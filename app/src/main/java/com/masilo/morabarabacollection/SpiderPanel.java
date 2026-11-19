package com.masilo.morabarabacollection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
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
import java.util.Objects;
import java.util.Random;

/**
 * SpiderPanel: Colorful and beautiful background with nebula effects and glowing web.
 */
public class SpiderPanel extends SurfaceView implements SurfaceHolder.Callback {
    // ============================================================================================
    // DRAWING CONSTANTS
    // ============================================================================================
    private static final float GRID_W = 16.0f;
    private static final float GRID_H = 14.0f;
    private static final float[][] NODE_MAP = {
            {3.5f, 2f}, {8f, 2f}, {13f, 2f},
            {3.5f, 7f}, {8f, 7f}, {13f, 7f},
            {3.5f, 12f}, {8f, 12f}, {13f, 12f}
    };
    private static final int[][] LINE_MAP = {
            {0, 1}, {1, 2}, {3, 4}, {4, 5}, {6, 7}, {7, 8},
            {0, 3}, {3, 6}, {1, 4}, {4, 7}, {2, 5}, {5, 8},
            {0, 4}, {4, 8}, {2, 4}, {4, 6}
    };

    public enum Player { BLACK, WHITE }
    public enum GamePhase { SELECTING, MOVING, CAPTURING_OPPONENT, GAME_OVER }
    private Player currentPlayer = Player.WHITE;
    private GamePhase currentPhase = GamePhase.SELECTING;
    private boolean isPaused = false;
    private Piece selectedPiece = null;
    public int blackPieceKilledCount = 0;
    public int whitePieceKilledCount = 0;
    private static final int PIECE_COUNT = 4;
    private Piece[] blackPieces;
    private Piece[] whitePieces;
    private Circle[] circleArray;
    private Line[] lineArray;
    private Circle whiteContainer, blackContainer, whiteTarget, blackTarget;
    private final int[][] patterns = new int[][]{
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6},
            {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}
    };
    public int movesCount = 0;
    private boolean isFirstLayout = true;

    private Paint selectedPiecePaint;
    private Paint capturablePiecePaint;
    private Paint backgroundPaint;
    private Paint vignettePaint;
    private Paint webPaint;
    private Path webPath = new Path();
    private Paint particlePaint;

    private GameThread gameThread;
    private Animator1 animator;
    private SpiderPanelListener gamePanelListener;
    private float cellWidth, cellHeight;
    private TextView gameMessages;
    private SpiderRobot robot;

    private List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    public SpiderPanel(Context context) { super(context); init(); }
    public SpiderPanel(Context context, AttributeSet attrs) { super(context, attrs); init(); }
    public SpiderPanel(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); init(); }

    private void init() {
        getHolder().addCallback(this);
        setFocusable(true);

        animator = new Animator1(this);
        int speedPercent = GameData.getInstance().getAnimationSpeed();
        long baseDuration = 1000;
        animator.setDuration((speedPercent > 0) ? baseDuration * 100 / speedPercent : baseDuration);

        // --- Background & Visual Setup ---
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        vignettePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        
        webPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        webPaint.setStyle(Paint.Style.STROKE);
        webPaint.setStrokeWidth(2f);
        webPaint.setColor(Color.WHITE);
        webPaint.setAlpha(30);

        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        selectedPiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectedPiecePaint.setColor(Color.YELLOW);
        selectedPiecePaint.setStyle(Paint.Style.STROKE);
        selectedPiecePaint.setStrokeWidth(10f);

        capturablePiecePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capturablePiecePaint.setColor(Color.RED);
        capturablePiecePaint.setStyle(Paint.Style.STROKE);
        capturablePiecePaint.setStrokeWidth(10f);

        // --- Game Objects Init ---
        circleArray = new Circle[9];
        for (int i = 0; i < circleArray.length; i++) circleArray[i] = new Circle(0, 0, 0);

        lineArray = new Line[LINE_MAP.length];
        for (int i = 0; i < lineArray.length; i++) lineArray[i] = new Line(0, 0, 0, 0);

        blackPieces = new Piece[PIECE_COUNT];
        whitePieces = new Piece[PIECE_COUNT];

        for (int i = 0; i < PIECE_COUNT; i++) {
            blackPieces[i] = new Piece(new PointF(0, 0), true);
            whitePieces[i] = new Piece(new PointF(0, 0), false);
        }

        blackContainer = new Circle(0, 0, 1);
        whiteContainer = new Circle(0, 0, 1);
        blackTarget = new Circle(0, 0, 1);
        whiteTarget = new Circle(0, 0, 1);

        robot = new SpiderRobot(this);
        gameThread = new GameThread(getHolder(), this,"sp");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        boolean isPortrait = h > w;
        float effW = isPortrait ? h : w;
        float effH = isPortrait ? w : h;
        // Colorful Nebula Gradient
        Shader bgShader = new RadialGradient(
                w / 2f, h / 2f,
                Math.max(w, h) * 1.1f,
                new int[]{0xFF240046, 0xFF10002B, 0xFF050010, 0xFF000000},
                new float[]{0.0f, 0.5f, 0.8f, 1.0f},
                Shader.TileMode.CLAMP
        );
        backgroundPaint.setShader(bgShader);
        // Vignette
        Shader vignetteShader = new RadialGradient(
                w / 2f, h / 2f,
                Math.max(w, h) * 0.9f,
                new int[]{Color.TRANSPARENT, 0xAA000000},
                new float[]{0.6f, 1.0f},
                Shader.TileMode.CLAMP
        );
        vignettePaint.setShader(vignetteShader);
        // Generate Spider Web Path
        webPath.reset();
        float cx = w / 2f;
        float cy = h / 2f;
        float maxR = Math.min(w, h) * 0.6f;
        int strands = 12;
        for (int i = 0; i < strands; i++) {
            double angle = i * 2 * Math.PI / strands;
            webPath.moveTo(cx, cy);
            webPath.lineTo(cx + (float)Math.cos(angle) * maxR, cy + (float)Math.sin(angle) * maxR);
        }
        for (float r = maxR * 0.15f; r <= maxR; r += maxR * 0.15f) {
            for (int i = 0; i <= strands; i++) {
                double angle = i * 2 * Math.PI / strands;
                float x = cx + (float)Math.cos(angle) * r;
                float y = cy + (float)Math.sin(angle) * r;
                if (i == 0) webPath.moveTo(x, y);
                else webPath.lineTo(x, y);
            }
        }

        // Init Particles
        particles.clear();
        int[] colors = {0x66FF00FF, 0x6600FFFF, 0x667B2CBF, 0x44FFFFFF};
        for (int i = 0; i < 40; i++) {
            particles.add(new Particle(
                    random.nextFloat() * w, random.nextFloat() * h,
                    random.nextFloat() * 3f + 1f,
                    colors[random.nextInt(colors.length)],
                    (random.nextFloat() - 0.5f) * 0.5f,
                    (random.nextFloat() - 0.5f) * 0.5f
            ));
        }

        cellWidth = effW / GRID_W;
        cellHeight = effH / GRID_H;
        float nodeRadius = cellHeight / 0.99f;

        for (int i = 0; i < circleArray.length; i++) {
            float lx = cellWidth * NODE_MAP[i][0];
            float ly = cellHeight * NODE_MAP[i][1];
            if (isPortrait) circleArray[i].setDimensions(ly, lx);
            else circleArray[i].setDimensions(lx, ly);
            circleArray[i].setRadius(nodeRadius);
        }

        for (int i = 0; i < LINE_MAP.length; i++) {
            Circle c1 = circleArray[LINE_MAP[i][0]];
            Circle c2 = circleArray[LINE_MAP[i][1]];
            lineArray[i].setDimensions(c1.getX(), c1.getY(), c2.getX(), c2.getY());
        }

        float targetLX = cellWidth * 14.8f;
        float targetLY_W = cellHeight * 10;
        float targetLY_B = cellHeight * 12.5f;

        if (isPortrait) {
            whiteTarget.setDimensions(targetLY_W, targetLX);
            blackTarget.setDimensions(targetLY_B, targetLX);
        } else {
            whiteTarget.setDimensions(targetLX, targetLY_W);
            blackTarget.setDimensions(targetLX, targetLY_B);
        }
        whiteTarget.setRadius(nodeRadius * 1.1f);
        blackTarget.setRadius(nodeRadius * 1.1f);

        if (isFirstLayout) {
            initialisePieces(isPortrait);
            isFirstLayout = false;
        } else {
            syncPiecePositions(isPortrait);
        }
        requestRender();
    }

    private void syncPiecePositions(boolean isPortrait) {
        float pRadius = cellHeight * 0.8f;
        float startLX = cellWidth * 2.0f;
        float gap = cellWidth * 2.5f;
        float lyW = cellHeight * 0.7f;
        float lyB = cellHeight * 13.3f;

        for (int i = 0; i < PIECE_COUNT; i++) {
            Piece pW = whitePieces[i];
            if (pW.getMyCircle() == whiteContainer) {
                if (isPortrait) pW.setXY(lyW, startLX + (i * gap));
                else pW.setXY(startLX + (i * gap), lyW);
            } else if (pW.getMyCircle() != null) {
                pW.setXY(pW.getMyCircle().getX(), pW.getMyCircle().getY());
            }
            pW.setOuterRadius(pRadius);

            Piece pB = blackPieces[i];
            if (pB.getMyCircle() == blackContainer) {
                if (isPortrait) pB.setXY(lyB, startLX + (i * gap));
                else pB.setXY(startLX + (i * gap), lyB);
            } else if (pB.getMyCircle() != null) {
                pB.setXY(pB.getMyCircle().getX(), pB.getMyCircle().getY());
            }
            pB.setOuterRadius(pRadius);
        }
    }

    public void render(Canvas canvas) {
        if (canvas == null) return;

        // 1. Draw Base Background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        // 2. Draw Nebula Glows
        drawNebulaGlow(canvas, getWidth() * 0.2f, getHeight() * 0.3f, getWidth() * 0.5f, 0x22FF00FF);
        drawNebulaGlow(canvas, getWidth() * 0.8f, getHeight() * 0.7f, getWidth() * 0.6f, 0x2200FFFF);

        // 3. Draw Spider Web Motif
        canvas.drawPath(webPath, webPaint);

        // 4. Update and Draw Particles
        for (Particle p : particles) {
            p.update(getWidth(), getHeight());
            particlePaint.setColor(p.color);
            int alpha = (int) (100 + Math.sin(p.alpha) * 80);
            particlePaint.setAlpha(alpha);
            canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
        }

        // 5. Vignette
        canvas.drawRect(0, 0, getWidth(), getHeight(), vignettePaint);

        // 6. Draw Board
        for (Line line : lineArray) {
            line.draw(canvas);
            line.setHighlighted(true);
        }

        for (Circle circle : circleArray) {
            if (currentPhase == GamePhase.MOVING && selectedPiece != null && isValidMove(selectedPiece, circle)) {
                circle.setIsTarget(true, selectedPiece.getHighlightColor());
            } else {
                circle.setIsTarget(false);
            }
            circle.draw(canvas);
        }

        blackTarget.draw(canvas);
        whiteTarget.draw(canvas);

        for (Piece p : blackPieces) if (p != null) p.draw(canvas);
        for (Piece p : whitePieces) if (p != null) p.draw(canvas);

        if (selectedPiece != null) {
            canvas.drawCircle(selectedPiece.getX(), selectedPiece.getY(), selectedPiece.getOuterRadius() + 5f, selectedPiecePaint);
        }

        if (currentPhase == GamePhase.CAPTURING_OPPONENT) {
            Piece[] opponentPieces = (currentPlayer == Player.BLACK) ? whitePieces : blackPieces;
            for (Piece p : opponentPieces) {
                if (p != null && !isInGraveyard(p)) {
                    canvas.drawCircle(p.getX(), p.getY(), p.getOuterRadius() + 5f, capturablePiecePaint);
                }
            }
        }
        
        requestRender();
    }

    private void drawNebulaGlow(Canvas canvas, float x, float y, float radius, int color) {
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader glowShader = new RadialGradient(x, y, radius,
                new int[]{color, 0x00000000}, null, Shader.TileMode.CLAMP);
        glowPaint.setShader(glowShader);
        canvas.drawCircle(x, y, radius, glowPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        performClick();
        if (isPaused) { displayMessage("Game Paused"); return false; }
        if (currentPhase == GamePhase.GAME_OVER) return false;
        if (currentPlayer == Player.BLACK) return false;

        float x = event.getX();
        float y = event.getY();
        SoundManager.getInstance(getContext()).playClick();

        Piece clickedPiece = findPieceAt(x, y);
        if (clickedPiece != null) {
            handlePieceClick(clickedPiece);
            requestRender();
            return true;
        }

        Circle clickedCircle = findCircleAt(x, y);
        if (clickedCircle != null) {
            handleCircleClick(clickedCircle);
            requestRender();
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void handlePieceClick(Piece piece) {
        boolean isPieceBlack = piece.isBlack();
        boolean isMyPiece = (currentPlayer == Player.BLACK && isPieceBlack) || (currentPlayer == Player.WHITE && !isPieceBlack);

        if (currentPhase == GamePhase.CAPTURING_OPPONENT) {
            if (!isMyPiece) {
                if (isInGraveyard(piece)) { displayMessage("This piece is already dead."); return; }
                killPiece(piece);
            } else { displayMessage("Select an OPPONENT'S piece to remove!"); }
            return;
        }

        if (isMyPiece) {
            if (isInGraveyard(piece)) return;
            selectedPiece = piece;
            currentPhase = GamePhase.MOVING;
        } else {
            displayMessage("It is " + currentPlayer + "'s turn.");
        }
    }

    private void handleCircleClick(Circle targetCircle) {
        if (currentPhase != GamePhase.MOVING || selectedPiece == null) return;
        if (isValidMove(selectedPiece, targetCircle)) {
            executeMove(selectedPiece, targetCircle);
        } else {
            displayMessage("Invalid Move.");
        }
    }

    public boolean isValidMove(Piece piece, Circle target) {
        if (target.getPiece() != null) return false;
        Circle currentCircle = piece.getMyCircle();
        boolean isFromContainer = (currentCircle == null || currentCircle == blackContainer || currentCircle == whiteContainer);
        if (isFromContainer) return true;
        return isConnected(piece.getX(), piece.getY(), target.getX(), target.getY());
    }

    private void executeMove(Piece piece, Circle target) {
        movesCount++;
        Circle oldCircle = piece.getMyCircle();
        if (oldCircle != null) oldCircle.resetPiece();

        String typeTag = (currentPlayer == Player.BLACK) ? "B" : "W";
        target.setMyPiece(piece, typeTag);
        piece.setMyCircle(target);

        animator.moveAnimation(new PointF(target.getX(), target.getY()), piece);

        if (checkForMill(target)) {
            currentPhase = GamePhase.CAPTURING_OPPONENT;
            displayMessage(currentPlayer + " made a goal");
            if (currentPlayer == Player.BLACK) robot.triggerMove();
        } else {
            switchTurn();
        }
        selectedPiece = null;
    }

    public void robotExecuteMove(Piece piece, Circle target) {
        executeMove(piece, target);
        requestRender();
    }

    public void killPiece(Piece victim) {
        Circle graveyard = (victim.isBlack()) ? whiteTarget : blackTarget;
        if (victim.isBlack()) whitePieceKilledCount++;
        else blackPieceKilledCount++;

        if (victim.getMyCircle() != null) victim.getMyCircle().resetPiece();
        victim.setMyCircle(graveyard);

        animator.moveAnimation(new PointF(graveyard.getX(), graveyard.getY()), victim);
        
        if (gamePanelListener != null) gamePanelListener.onUpdate(currentPlayer == Player.BLACK);

        if (whitePieceKilledCount >= PIECE_COUNT) {
            handleGameOver(Player.WHITE);
        } else if (blackPieceKilledCount >= PIECE_COUNT) {
            handleGameOver(Player.BLACK);
        } else {
            switchTurn();
        }
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == Player.WHITE) ? Player.BLACK : Player.WHITE;
        currentPhase = GamePhase.SELECTING;
        if (gamePanelListener != null) gamePanelListener.onUpdate(currentPlayer == Player.BLACK);
        if (currentPlayer == Player.BLACK) {
            robot.triggerMove();
        }
    }

    private boolean checkForMill(Circle targetCircle) {
        int targetIndex = -1;
        for (int i = 0; i < circleArray.length; i++) {
            if (circleArray[i] == targetCircle) { targetIndex = i; break; }
        }
        if (targetIndex == -1) return false;

        String playerTag = (currentPlayer == Player.BLACK) ? "B" : "W";
        for (int[] pattern : patterns) {
            boolean involvesCurrent = (pattern[0] == targetIndex || pattern[1] == targetIndex || pattern[2] == targetIndex);
            if (involvesCurrent) {
                if (isMatch(pattern[0], playerTag) && isMatch(pattern[1], playerTag) && isMatch(pattern[2], playerTag)) {
                    animator.goalAnimation(circleArray[pattern[0]].getPiece(), 
                                           circleArray[pattern[1]].getPiece(), 
                                           circleArray[pattern[2]].getPiece());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMatch(int index, String tag) {
        return tag.equals(circleArray[index].getPieceType());
    }

    private boolean isConnected(float x1, float y1, float x2, float y2) {
        float tolerance = 10.0f;
        for (Line line : lineArray) {
            boolean match1 = dist(line.getX1(), line.getY1(), x1, y1) < tolerance && dist(line.getX2(), line.getY2(), x2, y2) < tolerance;
            boolean match2 = dist(line.getX1(), line.getY1(), x2, y2) < tolerance && dist(line.getX2(), line.getY2(), x1, y1) < tolerance;
            if (match1 || match2) return true;
        }
        return false;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        return (float) Math.hypot(x2 - x1, y2 - y1);
    }

    private Piece findPieceAt(float x, float y) {
        for (Piece p : whitePieces) if (p.isClicked(x, y)) return p;
        for (Piece p : blackPieces) if (p.isClicked(x, y)) return p;
        return null;
    }

    private Circle findCircleAt(float x, float y) {
        for (Circle c : circleArray) if (c.isClicked(x, y)) return c;
        return null;
    }

    public boolean isInGraveyard(Piece p) {
        return p.getMyCircle() == whiteTarget || p.getMyCircle() == blackTarget;
    }

    private void initialisePieces(boolean isPortrait) {
        float startLX = cellWidth * 2.0f;
        float gap = cellWidth * 2.5f;
        float pRadius = cellHeight * 0.8f;

        // Custom piece colors for SpiderPanel (Purple/Teal theme)
        int blackBase = 0xFF4B0082; // Indigo
        int blackHigh = 0xFF9370DB; // Medium Purple
        int blackIconS = 0xFFE6E6FA;
        int blackIconE = 0xFF483D8B;

        int whiteBase = 0xFF00CED1; // Dark Turquoise
        int whiteHigh = 0xFF40E0D0; // Turquoise
        int whiteIconS = 0xFFE0FFFF;
        int whiteIconE = 0xFF008B8B;

        for (int i = 0; i < PIECE_COUNT; i++) {
            float lx = startLX + (i * gap);
            float lyW = cellHeight * 0.7f;
            float lyB = cellHeight * 13.3f;

            if (isPortrait) {
                whitePieces[i].setXY(lyW, lx);
                blackPieces[i].setXY(lyB, lx);
            } else {
                whitePieces[i].setXY(lx, lyW);
                blackPieces[i].setXY(lx, lyB);
            }
            
            whitePieces[i].setOuterRadius(pRadius);
            whitePieces[i].setMyCircle(whiteContainer);
            whitePieces[i].setIconType(Piece.IconType.CIRCLE);
            whitePieces[i].setColors(whiteBase, whiteHigh, whiteIconS, whiteIconE);

            blackPieces[i].setOuterRadius(pRadius);
            blackPieces[i].setMyCircle(blackContainer);
            blackPieces[i].setIconType(Piece.IconType.CIRCLE);
            blackPieces[i].setColors(blackBase, blackHigh, blackIconS, blackIconE);
        }
    }

    public void setGamePanelListener(SpiderPanelListener listener) { this.gamePanelListener = listener; }
    public void setGameMessages(TextView gameMessages) { this.gameMessages = gameMessages; }
    public String getWhitePieceKilled() { return String.valueOf(whitePieceKilledCount); }
    public String getBlackPieceKilled() { return String.valueOf(blackPieceKilledCount); }
    
    public void displayMessage(String message) {
        if (gameMessages != null) {
            new Handler(Looper.getMainLooper()).post(() -> gameMessages.setText(message));
        } else {
             new Handler(getContext().getMainLooper()).post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    private void handleGameOver(Player winner) {
        currentPhase = GamePhase.GAME_OVER;
        GameData.getInstance().incrementSpiderGames();
        GameData.getInstance().save(getContext());
        int score = (winner == Player.BLACK) ? whitePieceKilledCount : blackPieceKilledCount;
        EndGameDialog endGameDialog = new EndGameDialog(getContext(), winner.toString(), score, movesCount, true);
        Objects.requireNonNull(endGameDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(0));
        endGameDialog.show();
    }

    public void restart() {
        blackPieceKilledCount = 0; whitePieceKilledCount = 0;
        currentPlayer = Player.WHITE; currentPhase = GamePhase.SELECTING;
        isPaused = false;
        for(Circle c : circleArray) c.resetPiece();
        initialisePieces(getWidth() < getHeight());
        if (gamePanelListener != null) gamePanelListener.onUpdate(false);
        requestRender();
    }

    public void pause() {
        isPaused = true;
        if (gameThread != null) {
            gameThread.setRunning(false);
            try { gameThread.join(); } catch (InterruptedException ignored) { }
        }
    }

    public void resume() {
        isPaused = false;
        if (gameThread == null || gameThread.getState() == Thread.State.TERMINATED) {
            gameThread = new GameThread(getHolder(), this,"sp");
        }
        if (!gameThread.isAlive()) {
            gameThread.setRunning(true);
            gameThread.start();
        }
        requestRender();
    }

    @Override public void surfaceCreated(@NonNull SurfaceHolder holder) { resume(); }
    @Override public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) { requestRender(); }
    @Override public void surfaceDestroyed(@NonNull SurfaceHolder holder) { pause(); }
    public void requestRender() { if (gameThread != null) gameThread.setDirty(); }

    public GamePhase getCurrentPhase() { return currentPhase; }
    public Piece[] getBlackPieces() { return blackPieces; }
    public Piece[] getWhitePieces() { return whitePieces; }
    public Circle[] getCircleArray() { return circleArray; }
    public int[][] getPatterns() { return patterns; }
    public Circle getBlackContainer() { return blackContainer; }

    public interface SpiderPanelListener { void onUpdate(boolean isBlackTurn); }

    private static class Particle {
        float x, y, radius;
        int color;
        float vx, vy;
        float alpha;
        float alphaSpeed;

        Particle(float x, float y, float radius, int color, float vx, float vy) {
            this.x = x; this.y = y; this.radius = radius;
            this.color = color; this.vx = vx; this.vy = vy;
            this.alpha = (float) Math.random() * 6.28f;
            this.alphaSpeed = 0.02f + (float) Math.random() * 0.03f;
        }

        void update(int w, int h) {
            x += vx; y += vy;
            if (x < 0) x = w; if (x > w) x = 0;
            if (y < 0) y = h; if (y > h) y = 0;
            alpha += alphaSpeed;
        }
    }
}
