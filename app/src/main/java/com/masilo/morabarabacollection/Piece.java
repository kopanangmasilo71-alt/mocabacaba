package com.masilo.morabarabacollection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class Piece {

    public enum IconType { STAR, DIAMOND, CIRCLE, SQUARE, CROSS }

    private int index;
    private Circle myCircle;
    private float outerRadius;
    private float animationScale = 1.0f;
    private final PointF position = new PointF();
    private boolean isBlack;
    private boolean isHighlighted = false;
    private boolean isTurn = false;
    private IconType iconType = IconType.STAR;

    // Animation state
    private float pulseAngle = 0f;
    private float floatOffset = 0f;
    private float idleRotation = 0f;
    private float animationRotation = 0f;
    private float idleScale = 1.0f;
    /** Vertical hop offset applied during chase move animations (negative = up). */
    private float moveHopOffset = 0f;
    private int visualAlpha = 255;

    // Colors
    private int baseColor;
    private int highlightColor;
    private int iconStartColor;
    private int iconEndColor;

    // Theme backup to allow temporary color changes (e.g. goal animation)
    private int themeBase;
    private int themeHighlight;
    private int themeIconStart;
    private int themeIconEnd;

    // Paints
    protected final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint glossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected final Paint turnGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Optimization
    private final Path iconPath = new Path();
    private final Matrix shaderMatrix = new Matrix();
    private Shader circleShader;
    private Shader shadowShader;

    public static final int[] BASE_COLORS = {
            0xFF005A5A, 0xFFFF6450, 0xFF6400FF, 0xFFFFC800,
            0xFF32C8C8, 0xFFFF3296, 0xFF1E90FF, 0xFF800080
    };
    public static final int[] ICON_START_COLORS = { 0xFF32FF96, 0xFFFFDC64, 0xFFFF64FF };
    public static final int[] ICON_END_COLORS = { 0xFF00C864, 0xFFFFA032, 0xFFC800FF };

    public Piece(PointF position, boolean isBlack) {
        if (position != null) {
            this.position.set(position);
        }
        this.isBlack = isBlack;
        setDefaultColors();
    }

    private void setDefaultColors() {
        int idx = isBlack ? 0 : 1;
        baseColor = BASE_COLORS[idx % BASE_COLORS.length];
        highlightColor = brighten(baseColor);
        iconStartColor = ICON_START_COLORS[idx % ICON_START_COLORS.length];
        iconEndColor = ICON_END_COLORS[idx % ICON_END_COLORS.length];
        saveTheme();
    }

    private void saveTheme() {
        themeBase = baseColor;
        themeHighlight = highlightColor;
        themeIconStart = iconStartColor;
        themeIconEnd = iconEndColor;
    }

    public void restoreThemeColors() {
        this.baseColor = themeBase;
        this.highlightColor = themeHighlight;
        this.iconStartColor = themeIconStart;
        this.iconEndColor = themeIconEnd;
        updatePaints();
    }

    public void setColors(int base, int highlight, int iconStart, int iconEnd) {
        this.baseColor = base;
        this.highlightColor = highlight;
        this.iconStartColor = iconStart;
        this.iconEndColor = iconEnd;
        saveTheme();
        updatePaints();
    }

    public void setIconType(IconType type) {
        this.iconType = type;
    }

    public void updatePaints() {
        // Minimum radius to avoid Skia native crashes during bounds computation
        float r = Math.max(outerRadius, 1.0f);

        Shader newShadowShader = new RadialGradient(0, 0, r * 1.3f,
                new int[]{0xAA000000, 0x00000000},
                new float[]{0.6f, 1.0f},
                Shader.TileMode.CLAMP);
        shadowPaint.setShader(newShadowShader);
        this.shadowShader = newShadowShader;

        Shader newCircleShader = new RadialGradient(
                -r * 0.35f, -r * 0.35f,
                r * 1.6f,
                new int[]{highlightColor, baseColor, darken(baseColor), darken(darken(baseColor))},
                new float[]{0.0f, 0.4f, 0.8f, 1.0f},
                Shader.TileMode.CLAMP
        );
        circlePaint.setShader(newCircleShader);
        this.circleShader = newCircleShader;

        Shader glossShader = new RadialGradient(
                -r * 0.4f, -r * 0.4f,
                r * 0.7f,
                new int[]{0xDDFFFFFF, 0x00FFFFFF},
                new float[]{0.0f, 1.0f},
                Shader.TileMode.CLAMP
        );
        glossPaint.setShader(glossShader);

        Shader iconShader = new RadialGradient(0, 0, r * 0.65f,
                new int[]{iconStartColor, iconEndColor, darken(iconEndColor)},
                new float[]{0.0f, 0.7f, 1.0f},
                Shader.TileMode.CLAMP);
        iconPaint.setShader(iconShader);

        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(8f);
        highlightPaint.setColor(highlightColor);
        highlightPaint.setShadowLayer(20, 0, 0, highlightColor);

        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(r * 0.1f);
        rimPaint.setColor(0x44FFFFFF);

        turnGlowPaint.setStyle(Paint.Style.FILL);
        turnGlowPaint.setShadowLayer(15, 0, 0, 0xFFFFFFFF);
    }

    public void update() {
        pulseAngle += 0.15f;
        if (isHighlighted) {
            floatOffset = (float) Math.sin(pulseAngle * 0.5f) * 12f;
            idleRotation += 4.0f;
            idleScale = 1.15f + (float) Math.sin(pulseAngle * 0.8f) * 0.1f;
        } else if (isTurn) {
            floatOffset = (float) Math.sin(pulseAngle * 0.4f) * 5f;
            idleScale = 1.05f + (float) Math.sin(pulseAngle * 0.4f) * 0.05f;
            idleRotation = 0;
        } else {
            floatOffset = 0;
            idleRotation = 0;
            idleScale = 1.0f;
        }
    }

    public void draw(Canvas canvas) {
        if (outerRadius <= 0) return;

        update();

        float cx = position.x;
        float cy = position.y;

        // Combine animation effects with idle effects
        float totalScale = animationScale * idleScale;
        float totalRotation = animationRotation + idleRotation;
        float r = outerRadius * totalScale;

        canvas.save();
        canvas.translate(cx, cy + floatOffset);
        canvas.scale(totalScale, totalScale);
        canvas.rotate(totalRotation);

        // Dynamic Shadow
        float shadowScale = 1.0f - (floatOffset / 40f);
        float shadowAlpha = 1.0f - (floatOffset / 60f);
        shadowPaint.setAlpha((int)(255 * shadowAlpha));

        shaderMatrix.setTranslate(0, r * 0.2f - floatOffset);
        shaderMatrix.preScale(shadowScale, shadowScale * 0.5f);
        
        Shader currentShadow = this.shadowShader;
        if (currentShadow != null) {
            currentShadow.setLocalMatrix(shaderMatrix);
            canvas.drawCircle(0, r * 0.2f - floatOffset, r * 1.3f, shadowPaint);
        }

        shaderMatrix.reset();
        Shader currentCircle = this.circleShader;
        if (currentCircle != null) {
            currentCircle.setLocalMatrix(shaderMatrix);
            canvas.drawCircle(0, 0, r, circlePaint);
        }

        canvas.drawCircle(0, 0, r, glossPaint);
        canvas.drawCircle(0, 0, r * 0.95f, rimPaint);

        drawIcon(canvas, r * 0.65f);

        if (isHighlighted) {
            highlightPaint.setAlpha((int) (180 + Math.sin(pulseAngle * 2) * 75));
            canvas.drawCircle(0, 0, r + 8, highlightPaint);
        }

        canvas.restore();
    }

    private void drawIcon(Canvas canvas, float radius) {
        iconPath.rewind();
        switch (iconType) {
            case STAR:
                drawStarPath(radius);
                break;
            case DIAMOND:
                iconPath.moveTo(0, -radius);
                iconPath.lineTo(radius, 0);
                iconPath.lineTo(0, radius);
                iconPath.lineTo(-radius, 0);
                iconPath.close();
                break;
            case SQUARE:
                iconPath.addRect(-radius * 0.7f, -radius * 0.7f, radius * 0.7f, radius * 0.7f, Path.Direction.CW);
                break;
            case CROSS:
                float w = radius * 0.3f;
                iconPath.moveTo(-w, -radius);
                iconPath.lineTo(w, -radius);
                iconPath.lineTo(w, -w);
                iconPath.lineTo(radius, -w);
                iconPath.lineTo(radius, w);
                iconPath.lineTo(w, w);
                iconPath.lineTo(w, radius);
                iconPath.lineTo(-w, radius);
                iconPath.lineTo(-w, w);
                iconPath.lineTo(-radius, w);
                iconPath.lineTo(-radius, -w);
                iconPath.lineTo(-w, -w);
                iconPath.close();
                break;
            case CIRCLE:
                iconPath.addCircle(0, 0, radius * 0.8f, Path.Direction.CW);
                break;
        }
        canvas.drawPath(iconPath, iconPaint);
    }

    private void drawStarPath(float radius) {
        int numPoints = 5;
        float innerRadius = radius * 0.4f;
        double angleOffset = -Math.PI / 2.0;
        double angleIncrement = 2.0 * Math.PI / numPoints;

        for (int i = 0; i < numPoints; i++) {
            double angle = angleOffset + i * angleIncrement;
            float x = (float) (radius * Math.cos(angle));
            float y = (float) (radius * Math.sin(angle));
            if (i == 0) iconPath.moveTo(x, y);
            else iconPath.lineTo(x, y);

            double innerAngle = angle + angleIncrement / 2.0;
            float ix = (float) (innerRadius * Math.cos(innerAngle));
            float iy = (float) (innerRadius * Math.sin(innerAngle));
            iconPath.lineTo(ix, iy);
        }
        iconPath.close();
    }

    protected int darken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.75f;
        return Color.HSVToColor(hsv);
    }

    protected int brighten(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = Math.min(1.0f, hsv[2] * 1.4f);
        hsv[1] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public void setOuterRadius(float r) {
        if (this.outerRadius != r) {
            this.outerRadius = r;
            updatePaints();
        }
    }

    public float getOuterRadius() { return outerRadius; }
    public boolean isInContainer(Circle container) { return myCircle == container; }
    public void setPosition(PointF p) { this.position.set(p); }
    public void setX(float x) { this.position.x = x; }
    public void setY(float y) { this.position.y = y; }
    public float getX() { return position.x; }
    public float getY() { return position.y; }
    public void setXY(float x, float y) { this.position.set(x, y); }
    public void setScale(float scale) { this.animationScale = scale; }
    public float getScale() { return animationScale; }
    public float getTotalScale() { return animationScale * idleScale; }
    public float getTotalRotation() { return animationRotation + idleRotation; }
    public float getFloatOffset() { return floatOffset + moveHopOffset; }
    public float getPulseAngle() { return pulseAngle; }

    public void setMoveHopOffset(float offset) { this.moveHopOffset = offset; }
    public float getMoveHopOffset() { return moveHopOffset; }

    public void setVisualAlpha(int alpha) {
        this.visualAlpha = Math.max(0, Math.min(255, alpha));
    }
    public int getVisualAlpha() { return visualAlpha; }

    public void setHighlighted(boolean h) { this.isHighlighted = h; }
    public boolean isHighlighted() { return isHighlighted; }

    public void setTurn(boolean isTurn) { this.isTurn = isTurn; }
    public boolean isTurn() { return isTurn; }

    public boolean isBlack() { return isBlack; }
    public String getPieceType() { return isBlack ? "B" : "W"; }
    public void setMyCircle(Circle c) { this.myCircle = c; }
    public Circle getMyCircle() { return myCircle; }
    public void setIndex(int i) { this.index = i; }
    public int getIndex() { return index; }
    public void setType(String t) {
        this.isBlack = "B".equals(t);
        setDefaultColors();
        updatePaints();
    }
    public int getBaseColor() { return baseColor; }
    public int getHighlightColor() { return highlightColor; }

    public void setTemporaryColors(int color) {
        this.baseColor = color;
        this.highlightColor = brighten(color);
        this.iconStartColor = Color.WHITE;
        this.iconEndColor = color;
        updatePaints();
    }

    public void setRotation(float rotation) { this.animationRotation = rotation; }
    public float getRotation() { return animationRotation; }

    public boolean isClicked(float touchX, float touchY) {
        float dx = touchX - position.x;
        float dy = touchY - position.y;
        return (dx * dx + dy * dy) <= (outerRadius * outerRadius * 1.6f);
    }
}
