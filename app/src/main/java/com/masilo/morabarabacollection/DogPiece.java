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
 * A highly professional, premium 3D-shaded Dog piece for rabbit-type games.
 * Optimized to deliver extreme character depth, 3D volume, and rich textures.
 */
public class DogPiece extends Piece {

    private final Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint earPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint snoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint detailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint whiskerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint collarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint furTexturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyeIrisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint eyePupilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint componentShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path dogHeadPath = new Path();
    private final Path leftEarPath = new Path();
    private final Path rightEarPath = new Path();
    private final Path snoutPath = new Path();
    private final Path tonguePath = new Path();
    private final Path nosePath = new Path();
    private final Path collarPath = new Path();
    private final Path furPath = new Path();

    private float lastRadius = -1;
    private int dogVariant;

    private static final int[] COLLAR_VARIANTS = {
            0xFF1565C0, 0xFFC62828, 0xFF2E7D32, 0xFF6A1B9A, 0xFFEF6C00, 0xFF00838F
    };

    public DogPiece(PointF position, boolean isBlack) {
        super(position, isBlack);
        initPaints();
    }

    public void setDogVariant(int variant) {
        this.dogVariant = Math.max(0, variant);
        lastRadius = -1;
    }

    private void initPaints() {
        // Sophisticated board game outlines
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(0xFF2D1500); // Very deep roasted coffee brown
        outlinePaint.setStrokeCap(Paint.Cap.ROUND);
        outlinePaint.setStrokeJoin(Paint.Join.ROUND);

        detailPaint.setStyle(Paint.Style.FILL);
        detailPaint.setColor(Color.WHITE);
        
        shadowPaint.setStyle(Paint.Style.FILL);
        
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setColor(Color.CYAN);

        whiskerPaint.setStyle(Paint.Style.STROKE);
        whiskerPaint.setColor(0xAA2D1500);

        collarPaint.setStyle(Paint.Style.STROKE);
        collarPaint.setStrokeCap(Paint.Cap.ROUND);

        furTexturePaint.setStyle(Paint.Style.STROKE);
        furTexturePaint.setColor(0x28000000);

        eyeIrisPaint.setStyle(Paint.Style.FILL);
        eyeIrisPaint.setColor(0xFFC67A25); // Alert amber/brown eyes for hounds

        eyePupilPaint.setStyle(Paint.Style.FILL);
        eyePupilPaint.setColor(0xFF151515);

        tagPaint.setStyle(Paint.Style.FILL);

        componentShadowPaint.setStyle(Paint.Style.FILL);
        componentShadowPaint.setColor(0x28000000); // 3D overlay shadow
    }

    private void updateShapes(float r) {
        if (r == lastRadius) return;
        lastRadius = r;

        outlinePaint.setStrokeWidth(r * 0.07f);
        whiskerPaint.setStrokeWidth(r * 0.035f);
        furTexturePaint.setStrokeWidth(r * 0.03f);
        collarPaint.setStrokeWidth(r * 0.16f);

        // Dog Head (Wide Oval with subtle structure)
        dogHeadPath.reset();
        dogHeadPath.addOval(-r * 0.98f, -r * 0.76f, r * 0.98f, r * 0.76f, Path.Direction.CW);

        // Snout (Beautiful 3D chin pad)
        snoutPath.reset();
        snoutPath.addOval(-r * 0.46f, r * 0.02f, r * 0.46f, r * 0.62f, Path.Direction.CW);

        // Floppy Left Ear
        leftEarPath.reset();
        leftEarPath.moveTo(-r * 0.72f, -r * 0.42f);
        leftEarPath.quadTo(-r * 1.38f, -r * 0.62f, -r * 1.28f, r * 0.48f);
        leftEarPath.quadTo(-r * 0.98f, r * 0.74f, -r * 0.62f, r * 0.28f);
        leftEarPath.close();

        // Floppy Right Ear
        rightEarPath.reset();
        rightEarPath.moveTo(r * 0.72f, -r * 0.42f);
        rightEarPath.quadTo(r * 1.38f, -r * 0.62f, r * 1.28f, r * 0.48f);
        rightEarPath.quadTo(r * 0.98f, r * 0.74f, r * 0.62f, r * 0.28f);
        rightEarPath.close();

        // Fur fluff textures
        furPath.reset();
        for (int i = 0; i < 5; i++) {
            float angle = (float) (Math.PI * (0.25 + i * 0.12));
            float fx = (float) (r * 0.82 * Math.cos(angle));
            float fy = (float) (r * 0.62 * Math.sin(angle));
            furPath.moveTo(fx, fy);
            furPath.lineTo(fx * 1.08f, fy * 1.08f);
        }

        // Playful tongue
        tonguePath.reset();
        tonguePath.addOval(-r * 0.16f, r * 0.46f, r * 0.16f, r * 0.76f, Path.Direction.CW);

        // Professional nose triangle
        nosePath.reset();
        nosePath.moveTo(0, r * 0.14f);
        nosePath.cubicTo(-r * 0.16f, r * 0.14f, -r * 0.20f, r * 0.32f, 0, r * 0.38f);
        nosePath.cubicTo(r * 0.20f, r * 0.32f, r * 0.16f, r * 0.14f, 0, r * 0.14f);
        nosePath.close();

        // Dog Collar curve
        collarPath.reset();
        collarPath.addArc(-r * 0.72f, r * 0.48f, r * 0.72f, r * 0.88f, 22, 136);

        // Premium Palette Definition (Rich Chestnut Hound vs Midnight Doberman)
        int baseColor, lightColor, darkColor, snoutColor;
        if (isBlack()) {
            baseColor  = 0xFF3A3A3A; // Midnight Charcoal
            lightColor = 0xFF5C5C5C; // Silver Sheen Highlight
            darkColor  = 0xFF1D1D1D; // Abyss Black Shadow
            snoutColor = 0xFF262626; // Matte Jet Black Snout
        } else {
            baseColor  = 0xFFD27D2D; // Autumn Chestnut / Tan Hound
            lightColor = 0xFFF5B05E; // Warm Sunlight Highlight
            darkColor  = 0xFF8A4A16; // Deep Chocolate Shadow
            snoutColor = 0xFFFAE19C; // Sandy Beige / Cream Snout Highlight
        }

        // Shading maps
        bodyPaint.setShader(new RadialGradient(-r * 0.2f, -r * 0.3f, r * 1.5f, 
                new int[]{lightColor, baseColor, darkColor, darken(darkColor)}, 
                new float[]{0.0f, 0.45f, 0.85f, 1.0f}, Shader.TileMode.CLAMP));
        
        earPaint.setShader(new RadialGradient(0, 0, r * 1.4f, 
                new int[]{baseColor, darkColor, darken(darkColor)}, 
                new float[]{0.3f, 0.8f, 1.0f}, Shader.TileMode.CLAMP));

        snoutPaint.setShader(new RadialGradient(0, r * 0.2f, r * 0.65f, 
                new int[]{Color.WHITE, snoutColor, darken(snoutColor)}, 
                new float[]{0.0f, 0.6f, 1.0f}, Shader.TileMode.CLAMP));

        // Polished golden tag shader
        tagPaint.setShader(new RadialGradient(0, r * 0.85f, r * 0.15f,
                new int[]{0xFFFFF59D, 0xFFFFD700, 0xFFF57F17},
                new float[]{0.0f, 0.5f, 1.0f}, Shader.TileMode.CLAMP));

        shadowPaint.setShader(new RadialGradient(0, r * 0.3f, r * 1.5f,
                new int[]{0x99000000, 0x00000000},
                new float[]{0.55f, 1.0f}, Shader.TileMode.CLAMP));
        shadowPaint.setMaskFilter(new BlurMaskFilter(16, BlurMaskFilter.Blur.NORMAL));

        if (isBlack()) {
            collarPaint.setColor(0xFFC62828);
        } else {
            collarPaint.setColor(COLLAR_VARIANTS[dogVariant % COLLAR_VARIANTS.length]);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        float r = getOuterRadius();
        if (r <= 0) return;

        updateShapes(r);
        update(); // Animated ticks
        float pulse = getPulseAngle();

        float cx = getX();
        float cy = getY();
        float s = getTotalScale();
        float rot = getTotalRotation();
        float fOff = getFloatOffset();

        // High responsiveness animation ticks
        float wag = isHighlighted() || isTurn() ? (float) Math.sin(pulse * 3.6f) * 4.5f : 0;
        float tonguePant = (float) Math.sin(pulse * 2.8f) * 0.12f + 1.0f;
        float earFlop = (float) Math.sin(pulse * 0.7f) * 2.5f;

        canvas.save();
        canvas.translate(cx, cy + fOff);
        canvas.rotate(rot + wag);
        canvas.scale(s, s);

        int alpha = getVisualAlpha();
        if (alpha < 255) {
            canvas.saveLayerAlpha((int) (-r * 1.6f), (int) (-r * 2.2f), (int) (r * 1.6f), (int) (r * 1.4f), alpha);
        }

        // Turn Ring Highlight Glow
        if (isTurn() && !isHighlighted()) {
            turnGlowPaint.setColor(isBlack() ? 0xFFFF5252 : 0xFF448AFF);
            turnGlowPaint.setAlpha((int) (80 + Math.sin(pulse * 1.4f) * 40));
            canvas.drawCircle(0, 0, r * 1.25f, turnGlowPaint);
        }

        // Draw Shadow
        float shadowScale = 1.0f - (fOff / 60f);
        canvas.save();
        canvas.scale(shadowScale, 0.42f * shadowScale);
        canvas.drawCircle(0, r * 1.9f, r * 1.4f, shadowPaint);
        canvas.restore();

        // Draw Ears (Casting internal shadow on head first)
        canvas.save();
        canvas.rotate(earFlop, -r * 0.72f, -r * 0.42f);
        canvas.save();
        canvas.translate(-r * 0.04f, r * 0.06f);
        canvas.drawPath(leftEarPath, componentShadowPaint);
        canvas.restore();
        canvas.drawPath(leftEarPath, earPaint);
        canvas.drawPath(leftEarPath, outlinePaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(-earFlop, r * 0.72f, -r * 0.42f);
        canvas.save();
        canvas.translate(r * 0.04f, r * 0.06f);
        canvas.drawPath(rightEarPath, componentShadowPaint);
        canvas.restore();
        canvas.drawPath(rightEarPath, earPaint);
        canvas.drawPath(rightEarPath, outlinePaint);
        canvas.restore();

        // Draw Head Oval
        canvas.drawPath(dogHeadPath, bodyPaint);
        canvas.drawPath(dogHeadPath, outlinePaint);

        // Fur textures
        canvas.drawPath(furPath, furTexturePaint);

        // Draw Premium Stitched Collar
        canvas.drawPath(collarPath, collarPaint);
        outlinePaint.setStrokeWidth(r * 0.03f);
        canvas.drawPath(collarPath, outlinePaint); // Collar border detail
        outlinePaint.setStrokeWidth(r * 0.07f);
        
        // Golden Tag
        canvas.drawCircle(0, r * 0.85f, r * 0.13f, tagPaint);
        canvas.drawCircle(0, r * 0.85f, r * 0.13f, outlinePaint);
        detailPaint.setColor(0x77000000);
        canvas.drawCircle(0, r * 0.85f, r * 0.04f, detailPaint);

        // Draw Snout (with cast shadow)
        canvas.save();
        canvas.translate(0, r * 0.04f);
        canvas.drawPath(snoutPath, componentShadowPaint);
        canvas.restore();
        canvas.drawPath(snoutPath, snoutPaint);
        canvas.drawPath(snoutPath, outlinePaint);
        
        // Draw Panting Tongue
        canvas.save();
        canvas.translate(0, r * 0.46f);
        canvas.scale(1.0f, tonguePant);
        canvas.translate(0, -r * 0.46f);
        detailPaint.setColor(0xFFFF5252); // Energetic Coral Pink
        canvas.drawPath(tonguePath, detailPaint);
        canvas.drawPath(tonguePath, outlinePaint);
        canvas.restore();

        // Draw Intense Professional Hunter Eyes
        float eyeY = -r * 0.15f;
        float eyeX = r * 0.32f;
        float eyeSize = r * 0.16f;

        // Left Eye
        detailPaint.setColor(Color.WHITE);
        canvas.drawCircle(-eyeX, eyeY, eyeSize, detailPaint);
        canvas.drawCircle(-eyeX, eyeY, eyeSize, outlinePaint);
        canvas.drawCircle(-eyeX, eyeY, eyeSize * 0.7f, eyeIrisPaint);
        canvas.drawCircle(-eyeX, eyeY, eyeSize * 0.4f, eyePupilPaint);
        // spec dots
        detailPaint.setColor(Color.WHITE);
        canvas.drawCircle(-eyeX - r * 0.03f, eyeY - r * 0.03f, r * 0.045f, detailPaint);
        canvas.drawCircle(-eyeX + r * 0.04f, eyeY + r * 0.04f, r * 0.02f, detailPaint);

        // Right Eye
        detailPaint.setColor(Color.WHITE);
        canvas.drawCircle(eyeX, eyeY, eyeSize, detailPaint);
        canvas.drawCircle(eyeX, eyeY, eyeSize, outlinePaint);
        canvas.drawCircle(eyeX, eyeY, eyeSize * 0.7f, eyeIrisPaint);
        canvas.drawCircle(eyeX, eyeY, eyeSize * 0.4f, eyePupilPaint);
        // spec dots
        canvas.drawCircle(eyeX - r * 0.03f, eyeY - r * 0.03f, r * 0.045f, detailPaint);
        canvas.drawCircle(eyeX + r * 0.04f, eyeY + r * 0.04f, r * 0.02f, detailPaint);

        // Whiskers
        canvas.drawLine(-r * 0.3f, r * 0.28f, -r * 0.76f, r * 0.16f, whiskerPaint);
        canvas.drawLine(-r * 0.3f, r * 0.38f, -r * 0.74f, r * 0.46f, whiskerPaint);
        canvas.drawLine(r * 0.3f, r * 0.28f, r * 0.76f, r * 0.16f, whiskerPaint);
        canvas.drawLine(r * 0.3f, r * 0.38f, r * 0.74f, r * 0.46f, whiskerPaint);

        // Dog Nose (Rich polished black leather)
        detailPaint.setColor(0xFF151515); 
        canvas.drawPath(nosePath, detailPaint);
        canvas.drawPath(nosePath, outlinePaint);
        // Highlight reflection shine on nose
        detailPaint.setColor(0x66FFFFFF);
        canvas.drawCircle(r * 0.03f, r * 0.20f, r * 0.04f, detailPaint);

        // Premium High Gloss glass overlay
        glossPaint.setAlpha(130);
        canvas.drawPath(dogHeadPath, glossPaint);

        // Interaction Highlight Overlay Frame
        if (isHighlighted()) {
            highlightPaint.setAlpha((int) (190 + Math.sin(pulse * 1.8f) * 65));
            highlightPaint.setStrokeWidth(r * 0.09f);
            highlightPaint.setShadowLayer(25, 0, 0, Color.CYAN);
            canvas.drawPath(dogHeadPath, highlightPaint);
            canvas.drawPath(leftEarPath, highlightPaint);
            canvas.drawPath(rightEarPath, highlightPaint);
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
        return (dx * dx + dy * dy) <= (r * r * 3.6f);
    }
}
