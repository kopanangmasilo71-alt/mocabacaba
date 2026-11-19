package com.masilo.morabarabacollection;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.OvershootInterpolator;

public class Animator1 {

    @FunctionalInterface
    private interface RenderCallback {
        void render();
    }
    private final RenderCallback activeRenderer;
    private long duration;

    // --- CONSTRUCTORS ---
    public Animator1() {
        this.activeRenderer = () -> {}; // No-op
        updateSpeed();
    }
    public Animator1(GamePanel1 panel) {
        this.activeRenderer = panel::requestRender;
        updateSpeed();
    }
    public Animator1(GamePanel2 panel) {
        this.activeRenderer = panel::requestRender;
        updateSpeed();
    }
    public Animator1(SpiderPanel panel) {
        this.activeRenderer = panel::requestRender;
        updateSpeed();
    }
    public Animator1(NormalPanel panel) {
        this.activeRenderer = panel::requestRender;
        updateSpeed();
    }
    public Animator1(RabbitPanel panel) {
        this.activeRenderer = panel::requestRender;
        updateSpeed();
    }
    public Animator1(RabbitPannel1 panel) {
        this.activeRenderer = panel::requestRender;
        updateSpeed();
    }
    public Animator1(RabbitPanelMultiplayer panel) {
        this.activeRenderer = panel::requestRender;
        updateSpeed();
    }
    public Animator1(AlquerquePanel panel) {
        this.activeRenderer = panel::requestRender;
        updateSpeed();
    }
    /**
     * Centralized speed calculation.
     */
    private void updateSpeed() {
        int speedPercent = GameData.getInstance().getAnimationSpeed();
        long baseDuration = 1000;
        // Ensure we don't divide by zero if speedPercent is 0
        long safePercent = Math.max(1, speedPercent);
        this.duration = baseDuration * 100 / safePercent;
    }
    public void setDuration(long adjustedDuration) {
        this.duration = adjustedDuration;
    }
    /**
     * Optimized render call. No if-else checks needed anymore.
     */
    private void requestRender() {
        if (activeRenderer != null) {
            activeRenderer.render();
        }
    }
    /**
     * Executes a grand, creative "Victory Celebration" animation for three pieces.
     * Slower and more colorful to make it stand out.
     */
    public void goalAnimation(final Piece a, final Piece b, final Piece c) {
        if (a == null || b == null || c == null) return;

        // Reset pieces to base state first
        new Handler(Looper.getMainLooper()).post(() -> {
            a.setScale(1.0f); b.setScale(1.0f); c.setScale(1.0f);
            
            // Staggered individual pops
            animateSingleGoalPiece(a, 0);
            animateSingleGoalPiece(b, 300);
            animateSingleGoalPiece(c, 600);

            // Grand finale: all pieces jump and spin together after the last individual pop
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                finalVictoryJump(a, b, c);
            }, 1200);
        });

        // Cleanup: Reset highlight after the full sequence (approx 4.5 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            resetPieceState(a);
            resetPieceState(b);
            resetPieceState(c);
            requestRender();
        }, 5000);
    }

    /**
     * All three pieces jump and spin in unison for a final celebration.
     */
    private void finalVictoryJump(Piece a, Piece b, Piece c) {
        ValueAnimator jumpAnimator = ValueAnimator.ofFloat(0f, 1f);
        jumpAnimator.setDuration(1500);
        jumpAnimator.setInterpolator(new OvershootInterpolator(1.2f));
        jumpAnimator.addUpdateListener(animation -> {
            float f = animation.getAnimatedFraction();

            float jumpScale = 1.0f + 0.4f * (float) Math.sin(f * Math.PI);
            float rotation = f * 720f;

            for (Piece p : new Piece[]{a, b, c}) {
                p.setScale(jumpScale);
                p.setRotation(rotation);
            }
            requestRender();
        });
        jumpAnimator.start();
    }

    /**
     * Helper to animate a single piece for the goal with a "Grand Pop" and body color shimmer.
     */
    private void animateSingleGoalPiece(final Piece piece, int startDelay) {
        if (piece == null) return;

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            piece.setHighlighted(true);
            final int originalBase = piece.getBaseColor();
            
            // 1. Scale "Grand Pop" effect
            ValueAnimator popAnimator = ValueAnimator.ofFloat(1.0f, 1.6f, 1.0f);
            popAnimator.setDuration(1000);
            popAnimator.setInterpolator(new AnticipateOvershootInterpolator(1.5f));
            popAnimator.addUpdateListener(animation -> {
                piece.setScale((float) animation.getAnimatedValue());
                requestRender();
            });

            // 2. Body Color Shimmer - Cycles through celebratory colors
            ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), 
                    originalBase, Color.YELLOW, Color.CYAN, Color.MAGENTA, Color.WHITE, originalBase);
            colorAnimator.setDuration(1000);
            colorAnimator.addUpdateListener(animation -> {
                piece.setTemporaryColors((int) animation.getAnimatedValue());
                requestRender();
            });
            
            popAnimator.start();
            colorAnimator.start();
        }, startDelay);
    }

    private void resetPieceState(Piece p) {
        if (p != null) {
            p.setScale(1.0f);
            p.setHighlighted(false);
            p.restoreThemeColors(); // Restores original colors correctly
        }
    }
    /**
     * Creates a smooth, expressive movement animation.
     * Includes:
     * 1. Parabolic lift (scaling)
     * 2. Spin rotation
     * 3. Overshoot landing with squash/stretch
     */
    public void moveAnimation(PointF targetPosition, Piece piece) {
        if (piece == null || targetPosition == null) return;
        if (piece instanceof RabbitPiece || piece instanceof DogPiece) {
            chaseMoveAnimation(targetPosition, piece);
            return;
        }
        boardMoveAnimation(targetPosition, piece);
    }

    private void boardMoveAnimation(PointF targetPosition, Piece piece) {
        final float startX = piece.getX();
        final float startY = piece.getY();
        final float deltaX = targetPosition.x - startX;
        final float deltaY = targetPosition.y - startY;

        ValueAnimator moveAnimator = ValueAnimator.ofFloat(0f, 1f);
        moveAnimator.setDuration(duration);
        moveAnimator.setInterpolator(new OvershootInterpolator(0.8f));

        moveAnimator.addUpdateListener(animation -> {
            float t = (float) animation.getAnimatedValue();
            float fraction = animation.getAnimatedFraction();

            float newX = startX + (deltaX * t);
            float newY = startY + (deltaY * t);
            piece.setPosition(new PointF(newX, newY));

            float lift = 0.25f * (float) Math.sin(fraction * Math.PI);
            piece.setScale(1.0f + lift);
            piece.setRotation(fraction * 360f);

            requestRender();
        });

        moveAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                piece.setPosition(targetPosition);
                piece.setRotation(0f);
                piece.setMoveHopOffset(0f);

                ValueAnimator squashAnimator = ValueAnimator.ofFloat(1.0f, 0.85f, 1.1f, 1.0f);
                squashAnimator.setDuration(300);
                squashAnimator.addUpdateListener(anim -> {
                    piece.setScale((float) anim.getAnimatedValue());
                    requestRender();
                });
                squashAnimator.start();
            }
        });

        moveAnimator.start();
    }

    /**
     * Hunt-themed movement: directional lean, hop arc, no full spin; heavier landing for dogs.
     */
    private void chaseMoveAnimation(PointF targetPosition, Piece piece) {
        final boolean rabbit = piece instanceof RabbitPiece;
        final float startX = piece.getX();
        final float startY = piece.getY();
        final float deltaX = targetPosition.x - startX;
        final float deltaY = targetPosition.y - startY;
        final float travelAngleDeg = (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
        final float hopHeight = rabbit ? 52f : 30f;
        final long moveDuration = Math.max(220, (long) (duration * (rabbit ? 0.55f : 0.72f)));

        ValueAnimator moveAnimator = ValueAnimator.ofFloat(0f, 1f);
        moveAnimator.setDuration(moveDuration);
        moveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        moveAnimator.addUpdateListener(animation -> {
            float t = (float) animation.getAnimatedValue();
            float fraction = animation.getAnimatedFraction();

            float newX = startX + (deltaX * t);
            float newY = startY + (deltaY * t);
            piece.setPosition(new PointF(newX, newY));

            float hop = (float) Math.sin(fraction * Math.PI);
            piece.setMoveHopOffset(-hopHeight * hop);

            float lean = travelAngleDeg * 0.12f * (float) Math.sin(fraction * Math.PI);
            piece.setRotation(lean);

            float liftScale = rabbit ? 0.08f : 0.05f;
            piece.setScale(1.0f + liftScale * hop);

            requestRender();
        });

        moveAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                piece.setPosition(targetPosition);
                piece.setRotation(0f);
                piece.setMoveHopOffset(0f);

                float landMin = rabbit ? 0.92f : 0.82f;
                float landPeak = rabbit ? 1.05f : 1.08f;
                ValueAnimator squashAnimator = ValueAnimator.ofFloat(1.0f, landMin, landPeak, 1.0f);
                squashAnimator.setDuration(rabbit ? 260 : 320);
                squashAnimator.addUpdateListener(anim -> {
                    piece.setScale((float) anim.getAnimatedValue());
                    requestRender();
                });
                squashAnimator.start();
            }
        });

        moveAnimator.start();
    }
}
