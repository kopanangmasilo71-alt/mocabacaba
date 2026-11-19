package com.masilo.morabarabacollection;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * A highly polished 3D natural burrow-hole node style circle specifically for Rabbit panels.
 * Features advanced structural environment occlusion shadows and realistic grass clusters.
 */
public class RabbitCircle extends Circle {
    private final Paint baseShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint burrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grassPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private float pulse = 0f;
    private int customHighlightColor = Color.YELLOW;
    private float shaderRadius = -1f;

    public RabbitCircle(float x, float y, double radius) {
        super(x, y, radius);
        initPaints();
    }

    public void initPaints() {
        baseShadowPaint.setStyle(Paint.Style.FILL);
        burrowPaint.setStyle(Paint.Style.FILL);
        grassPaint.setStyle(Paint.Style.FILL);
        
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setStrokeWidth(4f);

        rimStrokePaint.setStyle(Paint.Style.STROKE);
        rimStrokePaint.setStrokeWidth(2.5f);
        rimStrokePaint.setColor(0xFF2D1500); // Earthy dark brown rim ring outline
        
        highlightPaint.setStyle(Paint.Style.STROKE);
    }

    private void updateShaders(float r) {
        if (r == shaderRadius) return;
        shaderRadius = r;
        // Deep sunken soil burrow hole gradient with extreme dark core cavity look
        burrowPaint.setShader(new RadialGradient(0, 0, r,
                new int[]{0xFF0A0503, 0xFF23120B, 0xFF4E342E, 0xFF6D4C41},
                new float[]{0.0f, 0.45f, 0.85f, 1.0f}, Shader.TileMode.CLAMP));

        // Soft elegant grassy aura overlay blending outwards
        grassPaint.setShader(new RadialGradient(0, 0, r * 1.6f,
                new int[]{0xC51B5E20, 0x552E7D32, 0x1181C784, 0x00000000},
                new float[]{0.0f, 0.5f, 0.8f, 1.0f}, Shader.TileMode.CLAMP));

        // Global structural ambient shadow around the node base
        baseShadowPaint.setShader(new RadialGradient(0, r * 0.15f, r * 1.8f,
                new int[]{0x75000000, 0x00000000},
                new float[]{0.4f, 1.0f}, Shader.TileMode.CLAMP));
    }

    @Override
    public void draw(Canvas canvas) {
        float r = getRadius();
        if (r <= 0) return;
        
        float x = getX();
        float y = getY();
        
        updateShaders(r);
        pulse += 0.08f;

        canvas.save();
        canvas.translate(x, y);

        // 1. Structural environment drop shadow under the node base
        canvas.drawCircle(0, 0, r * 1.8f, baseShadowPaint);

        // 2. Grassy perimeter cluster blend
        canvas.drawCircle(0, 0, r * 1.6f, grassPaint);

        // 3. Rich deep soil burrow hole
        canvas.drawCircle(0, 0, r, burrowPaint);

        // 4. Draw strong structural node outline frame matching pieces finish
        canvas.drawCircle(0, 0, r, rimStrokePaint);

        // 5. Advanced 3D lighting arcs (Specular top crest and deep bottom shadow bevel)
        rect.set(-r + 1f, -r + 1f, r - 1f, r - 1f);
        rimPaint.setColor(0x40FFFFFF); // Soft white sunlight catch
        canvas.drawArc(rect, 210, 140, false, rimPaint);
        rimPaint.setColor(0x50000000); // Dense shade curve
        canvas.drawArc(rect, 30, 140, false, rimPaint);

        // 6. Dynamic target destination highlights pulsing gracefully
        if (isTarget()) {
            float scalePulse = (float) Math.sin(pulse) * 0.08f + 1.1f;
            highlightPaint.setColor(customHighlightColor);
            highlightPaint.setStrokeWidth(r * 0.18f);
            highlightPaint.setAlpha((int) (160 + 80 * Math.sin(pulse)));
            highlightPaint.setMaskFilter(new BlurMaskFilter(12, BlurMaskFilter.Blur.NORMAL));
            canvas.drawCircle(0, 0, r * scalePulse, highlightPaint);
            highlightPaint.setMaskFilter(null);
        }

        canvas.restore();
    }

    @Override
    public void setIsTarget(boolean isTarget, int color) {
        super.setIsTarget(isTarget, color);
        this.customHighlightColor = color;
    }
}
