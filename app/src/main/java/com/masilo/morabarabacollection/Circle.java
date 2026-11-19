package com.masilo.morabarabacollection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

public class Circle {
    private int[] myNeighbour;
    private int index = 0;
    private Piece myPiece;
    private String pieceType = "N";
    private float x;
    private float y;
    private float radius;
    private float baseRadius;

    // AAA State & Animation
    private boolean isTarget = false;
    private int highlightColor = Color.GREEN;
    private boolean isHovered = false;
    private float hoverScale = 1.0f;

    // Paints & Shaders
    private final Paint socketPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF arcRect = new RectF();

    public Circle(float x, float y, double radius) {
        this.x = x;
        this.y = y;
        this.baseRadius = (float) radius;
        this.radius = baseRadius;
        initPaints();
    }

    private void initPaints() {
        socketPaint.setStyle(Paint.Style.FILL);
        innerShadowPaint.setStyle(Paint.Style.FILL);

        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(3f);

        glowPaint.setStyle(Paint.Style.FILL);

        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeWidth(5f);
        targetPaint.setColor(0xAA00FF00);
    }

    private void updateShaders() {
        if (radius <= 0) return;

        // Deep socket look - Polished with more depth
        Shader socketShader = new RadialGradient(0, 0, radius,
                new int[]{0xFF121212, 0xFF2D1B15, 0xFF3E2723, 0xFF1B100D},
                new float[]{0f, 0.6f, 0.9f, 1.0f},
                Shader.TileMode.CLAMP);
        socketPaint.setShader(socketShader);

        // Inner shadow for depth - slightly more pronounced
        Shader shadowShader = new RadialGradient(0, -radius * 0.3f, radius * 1.2f,
                new int[]{0x00000000, 0xBB000000},
                new float[]{0.5f, 1.0f},
                Shader.TileMode.CLAMP);
        innerShadowPaint.setShader(shadowShader);

        updateHighlightShader();
    }

    private void updateHighlightShader() {
        if (radius <= 0) return;
        
        int color = highlightColor;
        int transparentColor = color & 0x00FFFFFF; // Keep RGB, set Alpha to 0
        
        Shader glowShader = new RadialGradient(0, 0, radius * 1.4f,
                new int[]{ (0xAA << 24) | (color & 0xFFFFFF), transparentColor },
                null, Shader.TileMode.CLAMP);
        glowPaint.setShader(glowShader);
        
        targetPaint.setColor(color);
    }

    public void update() {
        // Smooth transitions for hover
        if (isHovered) {
            hoverScale = Math.min(1.15f, hoverScale + 0.04f);
        } else {
            hoverScale = Math.max(1.0f, hoverScale - 0.04f);
        }
    }

    public void draw(Canvas canvas) {
        update();

        canvas.save();
        canvas.translate(x, y);
        float currentR = radius * hoverScale;

        // 1. Draw static target glow if this is a valid move
        if (isTarget) {
            glowPaint.setAlpha(160);
            canvas.drawCircle(0, 0, currentR * 1.4f, glowPaint);
        }

        // 2. Draw socket body
        canvas.drawCircle(0, 0, currentR, socketPaint);

        // 3. Draw inner depth shadow
        canvas.drawCircle(0, 0, currentR, innerShadowPaint);

        // 4. Draw rim highlights - Added more polish
        arcRect.set(-currentR, -currentR, currentR, currentR);
        rimPaint.setStrokeWidth(currentR * 0.1f);
        
        rimPaint.setColor(0x55FFFFFF);
        canvas.drawArc(arcRect, 315, 180, false, rimPaint);
        rimPaint.setColor(0x77000000);
        canvas.drawArc(arcRect, 135, 180, false, rimPaint);

        // 5. If it's a target, draw a static ring
        if (isTarget) {
            float ringR = currentR * 0.95f;
            targetPaint.setAlpha(255);
            targetPaint.setStrokeWidth(currentR * 0.15f);
            canvas.drawCircle(0, 0, ringR, targetPaint);
        }

        canvas.restore();
    }

    public boolean isClicked(float touchX, float touchY) {
        float dx = touchX - x;
        float dy = touchY - y;
        boolean clicked = (dx * dx + dy * dy) <= (radius * radius * 1.8f); // Slightly larger click area
        if (clicked) {
            isHovered = true;
            // Temporary hover effect
            new android.os.Handler().postDelayed(() -> isHovered = false, 150);
        }
        return clicked;
    }

    // Standard Getters/Setters
    public void setIndex(int a) { this.index = a; }
    public int getIndex() { return index; }
    public void setNeighbors(int[] a) { this.myNeighbour = a; }
    public int[] getNeighbour() { return myNeighbour; }
    public void setMyPiece(Piece a, String type) { this.myPiece = a; this.pieceType = type; }
    public String getPieceType() { return pieceType; }
    public Piece getPiece() { return myPiece; }
    public void resetPiece() { this.myPiece = null; this.pieceType = "N"; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getRadius() { return radius; }
    public void setRadius(double radius) {
        this.radius = (float) radius;
        this.baseRadius = (float) radius;
        updateShaders();
    }
    public void setDimensions(float x, float y) { this.x = x; this.y = y; }
    public void setPieceType(String type) { this.pieceType = type; }
    
    public void setIsTarget(boolean isTarget) { 
        this.isTarget = isTarget; 
    }
    
    public void setIsTarget(boolean isTarget, int color) {
        if (this.isTarget != isTarget || this.highlightColor != color) {
            this.isTarget = isTarget;
            this.highlightColor = color;
            updateHighlightShader();
        }
    }

    public boolean isTarget() { return isTarget; }
}
