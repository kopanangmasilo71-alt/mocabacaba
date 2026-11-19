package com.masilo.morabarabacollection;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

import java.util.Random;

/**
 * Shared visual helpers for Rabbit chase boards (mat, stable ground decor).
 */
final class RabbitRenderHelper {

    private RabbitRenderHelper() {}

    static Random seededForLevel(String level, int width, int height) {
        long seed = 0x4D4F5241; // "MORA"
        if (level != null) {
            for (int i = 0; i < level.length(); i++) {
                seed = seed * 31 + level.charAt(i);
            }
        }
        seed ^= (long) width << 16;
        seed ^= height;
        return new Random(seed);
    }

    static void paintGroundDecor(Canvas canvas, int width, int height, Random rng, Paint p) {
        p.setShader(null);
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 260; i++) {
            float x = rng.nextFloat() * width;
            float y = rng.nextFloat() * height;
            int typeChance = rng.nextInt(3);

            if (typeChance == 0) {
                p.setColor(0x18000000 | (0x2E7D32 & 0xFFFFFF));
                float h = rng.nextFloat() * 18 + 6;
                float skew = rng.nextFloat() * 4 - 2;
                canvas.save();
                canvas.translate(x, y);
                canvas.drawLine(0, 0, skew, -h, p);
                canvas.drawLine(0, 0, skew + 3, -h + 2, p);
                canvas.restore();
            } else if (typeChance == 1) {
                p.setColor(0x1A000000 | (0x1B5E20 & 0xFFFFFF));
                float r = rng.nextFloat() * 25 + 12;
                canvas.drawCircle(x, y, r, p);
            } else {
                p.setColor(0x12000000 | (0x7D6B5D & 0xFFFFFF));
                float rw = rng.nextFloat() * 6 + 3;
                float rh = rng.nextFloat() * 4 + 2;
                canvas.drawOval(x - rw, y - rh, x + rw, y + rh, p);
            }
        }
    }

    static void drawVignette(Canvas canvas, int width, int height, Paint p) {
        RadialGradient vignette = new RadialGradient(width / 2f, height / 2f, Math.max(width, height) * 0.95f,
                new int[]{0x00000000, 0x1E000000, 0x7A000000}, new float[]{0.0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP);
        p.setShader(vignette);
        p.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, width, height, p);
        p.setShader(null);
    }

    /**
     * Physical play mat under junctions — carved board feel on the field.
     */
    static void drawBoardPlayMat(Canvas canvas, Circle[] circles, Paint matPaint, Paint shadowPaint) {
        if (circles == null || circles.length == 0) return;

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        int count = 0;
        for (Circle c : circles) {
            if (c == null || c.getRadius() <= 0) continue;
            minX = Math.min(minX, c.getX());
            maxX = Math.max(maxX, c.getX());
            minY = Math.min(minY, c.getY());
            maxY = Math.max(maxY, c.getY());
            count++;
        }
        if (count == 0) return;

        float pad = 56f;
        RectF mat = new RectF(minX - pad, minY - pad - 24f, maxX + pad, maxY + pad + 16f);
        float cx = mat.centerX();
        float cy = mat.centerY();

        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setColor(0x55000000);
        canvas.drawRoundRect(mat.left + 6, mat.top + 10, mat.right + 6, mat.bottom + 10, 42, 42, shadowPaint);

        matPaint.setShader(new LinearGradient(cx, mat.top, cx, mat.bottom,
                new int[]{0xFF6D4C41, 0xFF5D4037, 0xFF4E342E},
                new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
        matPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(mat, 38, 38, matPaint);

        matPaint.setShader(null);
        matPaint.setStyle(Paint.Style.STROKE);
        matPaint.setStrokeWidth(4f);
        matPaint.setColor(0x66FFFFFF);
        canvas.drawRoundRect(mat.left + 2, mat.top + 2, mat.right - 2, mat.bottom - 2, 36, 36, matPaint);

        matPaint.setColor(0x44000000);
        matPaint.setStrokeWidth(6f);
        canvas.drawRoundRect(mat.left + 4, mat.top + 4, mat.right - 4, mat.bottom - 4, 34, 34, matPaint);
        matPaint.setShader(null);
    }

    /** Alpha for pieces on the inactive side during turn handoff. */
    static int inactivePieceAlpha(boolean sideActive, boolean highlighted) {
        if (highlighted) return 255;
        return sideActive ? 255 : 168;
    }
}
