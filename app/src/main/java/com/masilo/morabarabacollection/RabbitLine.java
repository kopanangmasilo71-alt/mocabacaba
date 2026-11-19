package com.masilo.morabarabacollection;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

/**
 * A premium, highly textured 3D-feeling path style line for Rabbit panels.
 * Matches perfectly with the high-fidelity aesthetic of updated piece objects.
 */
public class RabbitLine extends Line {
    private final Paint baseShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float cachedX1 = Float.NaN;
    private float cachedY1 = Float.NaN;
    private float cachedX2 = Float.NaN;
    private float cachedY2 = Float.NaN;

    public RabbitLine(float x1, float y1, float x2, float y2) {
        super(x1, y1, x2, y2);
        initPaints();
    }

    private void initPaints() {
        // Deep sunken trench/emboss ambient occlusion line shadow underneath the path
        baseShadowPaint.setStyle(Paint.Style.STROKE);
        baseShadowPaint.setStrokeWidth(22f);
        baseShadowPaint.setStrokeCap(Paint.Cap.ROUND);
        baseShadowPaint.setColor(0x4D000000); // 30% alpha black

        // Main rich trail path
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(16f);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);

        // Dashed soil texture trail lines for grain feel
        texturePaint.setStyle(Paint.Style.STROKE);
        texturePaint.setStrokeWidth(3f);
        texturePaint.setStrokeCap(Paint.Cap.ROUND);
        texturePaint.setColor(0x2A150A00); // Earthy dark brown grit texture
        texturePaint.setPathEffect(new DashPathEffect(new float[]{14, 24}, 0));

        // High-end tactical overlay path indicator
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(6f);
        highlightPaint.setStrokeCap(Paint.Cap.ROUND);
        highlightPaint.setColor(0xAA4CAF50); // Soft beautiful emerald green trail

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setColor(0x24000000);
    }

    private void ensurePathShader(float x1, float y1, float x2, float y2) {
        if (x1 == cachedX1 && y1 == cachedY1 && x2 == cachedX2 && y2 == cachedY2) {
            return;
        }
        cachedX1 = x1;
        cachedY1 = y1;
        cachedX2 = x2;
        cachedY2 = y2;
        pathPaint.setShader(new LinearGradient(x1, y1, x2, y2,
                new int[]{0xFF4E342E, 0xFF5D4037, 0xFF4E342E},
                null, Shader.TileMode.CLAMP));
    }

    @Override
    public void draw(Canvas canvas) {
        float x1 = getX1();
        float y1 = getY1();
        float x2 = getX2();
        float y2 = getY2();

        // Prevent rendering invalid lines that break canvas bounds
        if (Math.abs(x1 - x2) < 0.1f && Math.abs(y1 - y2) < 0.1f) return;

        ensurePathShader(x1, y1, x2, y2);

        // 1. Draw soft environment trench shadow (Offset down slightly for 3D realism)
        canvas.drawLine(x1, y1 + 3.5f, x2, y2 + 3.5f, baseShadowPaint);

        // 2. Draw main trail body
        canvas.drawLine(x1, y1, x2, y2, pathPaint);

        canvas.drawLine(x1, y1, x2, y2, borderPaint);

        // 4. Draw high grit dash texture
        canvas.drawLine(x1, y1, x2, y2, texturePaint);

        // 5. Draw overlay indicator if active
        if (isHighlighted()) {
            canvas.drawLine(x1, y1, x2, y2, highlightPaint);
        }
    }
    
    @Override
    public boolean isHighlighted() {
        return super.isHighlighted();
    }
}
