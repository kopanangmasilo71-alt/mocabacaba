package com.masilo.morabarabacollection;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

import androidx.annotation.NonNull;

public class Line {

    private float x1, x2, y1, y2;
    private boolean isHighlighted = false;
    private float pulseAlpha = 0f;
    private float pulseDir = 1f;
    private float energyOffset = 0f;

    // Paints
    private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint energyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Shaders
    private LinearGradient energyShader;
    private final Matrix shaderMatrix = new Matrix();

    public Line(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        initPaints();
    }

    private void initPaints() {
        basePaint.setStyle(Paint.Style.STROKE);
        basePaint.setStrokeWidth(12f);
        basePaint.setStrokeCap(Paint.Cap.ROUND);
        basePaint.setColor(0xFF2D1B15); // Ebony

        shadowPaint.setStyle(Paint.Style.STROKE);
        shadowPaint.setStrokeWidth(14f);
        shadowPaint.setStrokeCap(Paint.Cap.ROUND);
        shadowPaint.setColor(0x44000000);

        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(3f);
        rimPaint.setStrokeCap(Paint.Cap.ROUND);
        rimPaint.setColor(0x22FFFFFF);

        energyPaint.setStyle(Paint.Style.STROKE);
        energyPaint.setStrokeWidth(5f);
        energyPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void updateShaders() {
        energyShader = new LinearGradient(x1, y1, x2, y2,
                new int[]{0x0000FFFF, 0xAA00FFFF, 0x0000FFFF},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP);
        energyPaint.setShader(energyShader);
    }

    public void update() {
        pulseAlpha += 0.03f * pulseDir;
        if (pulseAlpha >= 1f || pulseAlpha <= 0f) pulseDir *= -1;

        energyOffset += 0.015f;
        if (energyOffset > 1f) energyOffset -= 1f;
    }

    public void draw(Canvas canvas) {
        update();

        // 1. Shadow
        canvas.drawLine(x1 + 4, y1 + 4, x2 + 4, y2 + 4, shadowPaint);

        // 2. Base
        canvas.drawLine(x1, y1, x2, y2, basePaint);

        // 3. Rim Highlight (Subtle bevel)
        canvas.drawLine(x1 - 1, y1 - 1, x2 - 1, y2 - 1, rimPaint);

        // 4. Energy Flow
        if (isHighlighted) {
            if (energyShader == null) updateShaders();
            shaderMatrix.setTranslate((x2 - x1) * (energyOffset - 0.5f) * 2, (y2 - y1) * (energyOffset - 0.5f) * 2);
            energyShader.setLocalMatrix(shaderMatrix);
            energyPaint.setAlpha((int) (80 + 175 * pulseAlpha));
            canvas.drawLine(x1, y1, x2, y2, energyPaint);
        }
    }

    public void setHighlighted(boolean highlighted) {
        this.isHighlighted = highlighted;
        if (highlighted && energyShader == null) updateShaders();
    }

    public boolean isHighlighted() { return isHighlighted; }

    public void setDimensions(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.energyShader = null;
    }

    public float getX1() { return x1; }
    public float getY1() { return y1; }
    public float getX2() { return x2; }
    public float getY2() { return y2; }

    public void setMyCircles(@NonNull Circle[] circles) {
        if (circles.length >= 2 && circles[0] != null && circles[1] != null) {
            this.x1 = circles[0].getX();
            this.y1 = circles[0].getY();
            this.x2 = circles[1].getX();
            this.y2 = circles[1].getY();
            this.energyShader = null;
        }
    }
}
