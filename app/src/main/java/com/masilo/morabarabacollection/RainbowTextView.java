package com.masilo.morabarabacollection;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class RainbowTextView extends androidx.appcompat.widget.AppCompatTextView {
    private Paint paint;
    private float translateX;
    private ObjectAnimator animator;
    private LinearGradient linearGradient;
    private final Matrix matrix = new Matrix();
    
    public RainbowTextView(Context context) {
        super(context);
        init();
    }

    public RainbowTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RainbowTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = getPaint();
    }
    
    public void setTranslateX(float translateX) {
        this.translateX = translateX;
        invalidate();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float width = getWidth();
        if (width <= 0) width = 1.0f; // Prevent 0 width gradients
        
        if (animator == null && width > 1) {
            animator = ObjectAnimator.ofFloat(this, "translateX", 0, width);
            animator.setDuration(3000);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            animator.start();
        }
        
        linearGradient = new LinearGradient(
                0, 0, width, 0,
                new int[]{
                    0xFFC5A059, // Gold/Brown
                    0xFFFF9800, // Orange
                    0xFF02F0DA, // Bright Teal
                    0xFF00FFFF, // Cyan
                    0xFFC5A059  // Loop back to Gold/Brown
                },
                null,
                Shader.TileMode.MIRROR);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (linearGradient != null) {
            matrix.setTranslate(translateX, 0);
            linearGradient.setLocalMatrix(matrix);
            paint.setShader(linearGradient);
        }
        super.onDraw(canvas);
    }
}
