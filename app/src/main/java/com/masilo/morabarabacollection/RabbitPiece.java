package com.masilo.morabarabacollection;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * A highly professional, premium 3D-shaded Rabbit piece for rabbit-type games.
 * Completely optimized to eliminate flatness and deliver a high-fidelity look and feel.
 */
public class RabbitPiece extends Piece {

    private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint earPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint innerEarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint muzzlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyeIrisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyePupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint nosePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint whiskerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint componentShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path rabbitPath = new Path();
    private final Path leftEarPath = new Path();
    private final Path rightEarPath = new Path();
    private final Path leftInnerEarPath = new Path();
    private final Path rightInnerEarPath = new Path();
    private final Path tailPath = new Path();
    private final Path nosePath = new Path();

    private float lastRadius = -1;

    public RabbitPiece(PointF position, boolean isBlack) {
        super(position, isBlack);
        initPaints();
    }

    private void initPaints() {
        // Outline for professional comic/board-game finish
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(0xFF3E2723); // Soft dark espresso brown instead of harsh black
        outlinePaint.setStrokeCap(Paint.Cap.ROUND);
        outlinePaint.setStrokeJoin(Paint.Join.ROUND);

        whiskerPaint.setStyle(Paint.Style.STROKE);
        whiskerPaint.setColor(0x8A4E342E);
        whiskerPaint.setStrokeCap(Paint.Cap.ROUND);

        muzzlePaint.setStyle(Paint.Style.FILL);
        
        eyePaint.setStyle(Paint.Style.FILL);
        eyePaint.setColor(Color.WHITE);

        eyeIrisPaint.setStyle(Paint.Style.FILL);
        eyeIrisPaint.setColor(0xFF5D4037); // Rich brown iris

        eyePupilPaint.setStyle(Paint.Style.FILL);
        eyePupilPaint.setColor(0xFF1A0C08);

        nosePaint.setStyle(Paint.Style.FILL);
        nosePaint.setColor(0xFFFF8A80); // Beautiful pastel rose nose

        componentShadowPaint.setStyle(Paint.Style.FILL);
        componentShadowPaint.setColor(0x25000000); // Internal depth shadow
    }

    private void updateShapes(float r) {
        if (r == lastRadius) return;
        lastRadius = r;

        outlinePaint.setStrokeWidth(r * 0.07f);
        whiskerPaint.setStrokeWidth(r * 0.04f);

        // Head (Sophisticated pear shape rather than flat oval)
        rabbitPath.reset();
        rabbitPath.moveTo(0, -r * 0.85f);
        rabbitPath.cubicTo(r * 0.75f, -r * 0.85f, r * 1.05f, r * 0.1f, r * 0.9f, r * 0.6f);
        rabbitPath.cubicTo(r * 0.75f, r * 1.0f, -r * 0.75f, r * 1.0f, -r * 0.9f, r * 0.6f);
        rabbitPath.cubicTo(-r * 1.05f, r * 0.1f, -r * 0.75f, -r * 0.85f, 0, -r * 0.85f);
        rabbitPath.close();

        // Left Long Ear
        leftEarPath.reset();
        leftEarPath.moveTo(-r * 0.35f, -r * 0.65f);
        leftEarPath.cubicTo(-r * 0.8f, -r * 1.6f, -r * 0.9f, -r * 2.5f, -r * 0.4f, -r * 2.6f);
        leftEarPath.cubicTo(-r * 0.05f, -r * 2.5f, -r * 0.05f, -r * 1.4f, -r * 0.05f, -r * 0.65f);
        leftEarPath.close();

        // Left Inner Ear
        leftInnerEarPath.reset();
        leftInnerEarPath.moveTo(-r * 0.32f, -r * 0.75f);
        leftInnerEarPath.cubicTo(-r * 0.65f, -r * 1.5f, -r * 0.75f, -r * 2.35f, -r * 0.4f, -r * 2.45f);
        leftInnerEarPath.cubicTo(-r * 0.15f, -r * 2.35f, -r * 0.15f, -r * 1.4f, -r * 0.12f, -r * 0.75f);
        leftInnerEarPath.close();

        // Right Long Ear
        rightEarPath.reset();
        rightEarPath.moveTo(r * 0.35f, -r * 0.65f);
        rightEarPath.cubicTo(r * 0.8f, -r * 1.6f, r * 0.9f, -r * 2.5f, r * 0.4f, -r * 2.6f);
        rightEarPath.cubicTo(r * 0.05f, -r * 2.5f, r * 0.05f, -r * 1.4f, r * 0.05f, -r * 0.65f);
        rightEarPath.close();

        // Right Inner Ear
        rightInnerEarPath.reset();
        rightInnerEarPath.moveTo(r * 0.32f, -r * 0.75f);
        rightInnerEarPath.cubicTo(r * 0.65f, -r * 1.5f, r * 0.75f, -r * 2.35f, r * 0.4f, -r * 2.45f);
        rightInnerEarPath.cubicTo(r * 0.15f, -r * 2.35f, r * 0.15f, -r * 1.4f, r * 0.12f, -r * 0.75f);
        rightInnerEarPath.close();

        // Fluffy Cotton Tail
        tailPath.reset();
        tailPath.addCircle(r * 0.75f, r * 0.75f, r * 0.32f, Path.Direction.CW);

        // Soft Cute Nose (curved triangle)
        nosePath.reset();
        nosePath.moveTo(0, r * 0.18f);
        nosePath.cubicTo(-r * 0.12f, r * 0.18f, -r * 0.14f, r * 0.32f, 0, r * 0.34f);
        nosePath.cubicTo(r * 0.14f, r * 0.32f, r * 0.12f, r * 0.18f, 0, r * 0.18f);
        nosePath.close();

        // Premium Palette Mapping (White/Cream Rabbit vs Silver Fox)
        int baseColor, lightColor, darkColor;
        if (isBlack()) {
            baseColor  = 0xFFFDFBF7; // Luxurious Ivory Cream
            lightColor = 0xFFFFFFFF; // Pure White Highlight
            darkColor  = 0xFFE3DDD3; // Soft Warm Grey Shadow
        } else {
            baseColor  = 0xFFE2E2E6; // Elegant Platinum Silver
            lightColor = 0xFFFFFFFF; // Crisp White Highlight
            darkColor  = 0xFFBFC0C7; // Metallic Shadow Grey
        }

        // Multi-stop radial gradients for advanced spherical volume
        bodyPaint.setShader(new RadialGradient(-r * 0.2f, -r * 0.3f, r * 1.6f,
                new int[]{lightColor, baseColor, darkColor, darken(darkColor)},
                new float[]{0.0f, 0.4f, 0.85f, 1.0f}, Shader.TileMode.CLAMP));

        earPaint.setShader(new RadialGradient(0, -r * 1.5f, r * 1.8f,
                new int[]{lightColor, baseColor, darkColor},
                new float[]{0.0f, 0.6f, 1.0f}, Shader.TileMode.CLAMP));

        // Soft pastel pink gradient for interior ears
        innerEarPaint.setShader(new RadialGradient(0, -r * 1.5f, r * 1.2f,
                new int[]{0xFFFFEbee, 0xFFF8BBD0, 0xFFF06292},
                new float[]{0.0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP));

        // Muzzle Gradient
        muzzlePaint.setShader(new RadialGradient(0, r * 0.3f, r * 0.5f,
                new int[]{lightColor, baseColor, darken(darkColor)},
                new float[]{0.0f, 0.7f, 1.0f}, Shader.TileMode.CLAMP));

        // Global Drop Shadow underneath the entire piece
        shadowPaint.setShader(new RadialGradient(0, r * 0.3f, r * 1.5f,
                new int[]{0x99000000, 0x00000000},
                new float[]{0.55f, 1.0f}, Shader.TileMode.CLAMP));
        shadowPaint.setMaskFilter(new BlurMaskFilter(16, BlurMaskFilter.Blur.NORMAL));
    }

    @Override
    public void draw(Canvas canvas) {
        float r = getOuterRadius();
        if (r <= 0) return;

        updateShapes(r);
        update(); // Update animations from core Piece
        float pulse = getPulseAngle();

        float cx = getX();
        float cy = getY();
        float s = getTotalScale();
        float rot = getTotalRotation();
        float fOff = getFloatOffset();

        // Refined organic idle animations
        float breathe = (float) Math.sin(pulse * 0.4f) * 0.03f + 1.0f;
        float noseTwitch = (float) Math.sin(pulse * 5.0f) * 0.08f + 1.0f;
        float earTwitch = isHighlighted() || isTurn() ? (float) Math.sin(pulse * 3.5f) * 3.5f : 0;

        s *= breathe;

        canvas.save();
        canvas.translate(cx, cy + fOff);
        canvas.rotate(rot);
        canvas.scale(s, s);

        int alpha = getVisualAlpha();
        if (alpha < 255) {
            canvas.saveLayerAlpha((int) (-r * 1.6f), (int) (-r * 3.2f), (int) (r * 1.6f), (int) (r * 1.4f), alpha);
        }

        // Professional Board Turn Indicator (Soft pulse ring)
        if (isTurn() && !isHighlighted()) {
            turnGlowPaint.setColor(0xFFFFA000); // Warm rich amber
            turnGlowPaint.setAlpha((int) (70 + Math.sin(pulse * 1.5f) * 35));
            canvas.drawCircle(0, 0, r * 1.22f, turnGlowPaint);
        }

        // Draw Dynamic Drop Shadow
        float shadowScale = 1.0f - (fOff / 60f);
        canvas.save();
        canvas.scale(shadowScale, 0.38f * shadowScale);
        canvas.drawCircle(0, r * 2.2f, r * 1.4f, shadowPaint);
        canvas.restore();

        // Draw Tail (with ambient outline)
        canvas.drawPath(tailPath, bodyPaint);
        canvas.drawPath(tailPath, outlinePaint);

        // Draw Left Ear (Shadow + Ear + Inner Ear + Outline)
        canvas.save();
        canvas.rotate(earTwitch, -r * 0.2f, -r * 0.6f);
        // Cast shadow on head
        canvas.save();
        canvas.translate(-r * 0.05f, r * 0.08f);
        canvas.drawPath(leftEarPath, componentShadowPaint);
        canvas.restore();
        canvas.drawPath(leftEarPath, earPaint);
        canvas.drawPath(leftInnerEarPath, innerEarPaint);
        canvas.drawPath(leftEarPath, outlinePaint);
        canvas.restore();

        // Draw Right Ear
        canvas.save();
        canvas.rotate(-earTwitch, r * 0.2f, -r * 0.6f);
        // Cast shadow on head
        canvas.save();
        canvas.translate(r * 0.05f, r * 0.08f);
        canvas.drawPath(rightEarPath, componentShadowPaint);
        canvas.restore();
        canvas.drawPath(rightEarPath, earPaint);
        canvas.drawPath(rightInnerEarPath, innerEarPaint);
        canvas.drawPath(rightEarPath, outlinePaint);
        canvas.restore();

        // Draw Head Body
        canvas.drawPath(rabbitPath, bodyPaint);
        canvas.drawPath(rabbitPath, outlinePaint);

        // Draw Plump 3D Cheeks/Muzzle
        canvas.save();
        canvas.drawCircle(-r * 0.16f, r * 0.36f, r * 0.24f, muzzlePaint);
        canvas.drawCircle(r * 0.16f, r * 0.36f, r * 0.24f, muzzlePaint);
        canvas.drawCircle(-r * 0.16f, r * 0.36f, r * 0.24f, outlinePaint);
        canvas.drawCircle(r * 0.16f, r * 0.36f, r * 0.24f, outlinePaint);
        canvas.restore();

        // Draw Professional Expressive Eyes
        float eyeY = -r * 0.12f;
        float eyeXL = -r * 0.38f;
        float eyeXR = r * 0.38f;
        float eyeRadX = r * 0.18f;
        float eyeRadY = r * 0.24f;

        // Left Sclera (White base)
        canvas.drawCircle(eyeXL, eyeY, eyeRadY, eyePaint);
        canvas.drawCircle(eyeXL, eyeY, eyeRadY, outlinePaint);
        // Left Iris
        canvas.drawCircle(eyeXL + r * 0.03f, eyeY + r * 0.03f, eyeRadX, eyeIrisPaint);
        // Left Pupil
        canvas.drawCircle(eyeXL + r * 0.04f, eyeY + r * 0.04f, eyeRadX * 0.6f, eyePupilPaint);
        // Specular Highlights (Life dots)
        canvas.drawCircle(eyeXL - r * 0.04f, eyeY - r * 0.06f, r * 0.06f, eyePaint);
        canvas.drawCircle(eyeXL + r * 0.06f, eyeY + r * 0.08f, r * 0.025f, eyePaint);

        // Right Sclera
        canvas.drawCircle(eyeXR, eyeY, eyeRadY, eyePaint);
        canvas.drawCircle(eyeXR, eyeY, eyeRadY, outlinePaint);
        // Right Iris
        canvas.drawCircle(eyeXR - r * 0.03f, eyeY + r * 0.03f, eyeRadX, eyeIrisPaint);
        // Right Pupil
        canvas.drawCircle(eyeXR - r * 0.04f, eyeY + r * 0.04f, eyeRadX * 0.6f, eyePupilPaint);
        // Specular Highlights
        canvas.drawCircle(eyeXR - r * 0.08f, eyeY - r * 0.06f, r * 0.06f, eyePaint);
        canvas.drawCircle(eyeXR + r * 0.02f, eyeY + r * 0.08f, r * 0.025f, eyePaint);

        // Whiskers (Crisp and organic placement)
        canvas.drawLine(-r * 0.35f, r * 0.38f, -r * 1.15f, r * 0.25f, whiskerPaint);
        canvas.drawLine(-r * 0.35f, r * 0.46f, -r * 1.1f, r * 0.52f, whiskerPaint);
        canvas.drawLine(-r * 0.35f, r * 0.54f, -r * 1.0f, r * 0.78f, whiskerPaint);

        canvas.drawLine(r * 0.35f, r * 0.38f, r * 1.15f, r * 0.25f, whiskerPaint);
        canvas.drawLine(r * 0.35f, r * 0.46f, r * 1.1f, r * 0.52f, whiskerPaint);
        canvas.drawLine(r * 0.35f, r * 0.54f, r * 1.0f, r * 0.78f, whiskerPaint);

        // Twitching Nose
        canvas.save();
        canvas.translate(0, r * 0.28f);
        canvas.scale(noseTwitch, noseTwitch);
        canvas.translate(0, -r * 0.28f);
        canvas.drawPath(nosePath, nosePaint);
        canvas.drawPath(nosePath, outlinePaint);
        // Small nose shine
        eyePaint.setColor(Color.WHITE);
        canvas.drawCircle(r * 0.03f, r * 0.22f, r * 0.025f, eyePaint);
        canvas.restore();

        // Luxury Glass Gloss Overlay
        glossPaint.setAlpha(140);
        canvas.drawPath(rabbitPath, glossPaint);

        // Selection / Highlighting Ring Glow
        if (isHighlighted()) {
            highlightPaint.setAlpha((int) (190 + Math.sin(pulse * 1.8f) * 65));
            highlightPaint.setStrokeWidth(r * 0.09f);
            highlightPaint.setColor(Color.WHITE);
            highlightPaint.setShadowLayer(25, 0, 0, Color.WHITE);
            canvas.drawPath(rabbitPath, highlightPaint);
        }

        if (alpha < 255) {
            canvas.restore();
        }
        canvas.restore();
    }

    @Override
    public boolean isClicked(float touchX, float touchY) {
        float dx = touchX - getX();
        float dy = touchY - getY();
        float r = getOuterRadius();
        return (dx * dx + dy * dy) <= (r * r * 3.8f); // High generosity interaction box
    }
}
