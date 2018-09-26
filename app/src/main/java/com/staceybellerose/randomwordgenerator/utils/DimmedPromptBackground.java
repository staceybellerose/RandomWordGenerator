package com.staceybellerose.randomwordgenerator.utils;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptions;
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.CirclePromptBackground;

/**
 * Subclass of CirclePromptBackground which inserts a dimmer shim behind the prompt
 */
public class DimmedPromptBackground extends CirclePromptBackground {
    /**
     * Alpha to use for dimmed shim
     */
    private static final int ALPHA_SCALE = 128;
    /**
     * Bounds of the dimmed shim
     */
    private RectF mDimBounds = new RectF();
    /**
     * Paint to use when coloring the dimmed shim
     */
    private Paint mDimPaint;

    /**
     * Constructor
     */
    public DimmedPromptBackground() {
        mDimPaint = new Paint();
        mDimPaint.setColor(Color.BLACK);
    }

    @Override
    public void prepare(@NonNull final PromptOptions options, final boolean clipToBounds,
                        @NonNull final Rect clipBounds) {
        super.prepare(options, clipToBounds, clipBounds);
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        mDimBounds.set(0, 0, metrics.widthPixels, metrics.heightPixels);
    }

    @Override
    public void update(@NonNull final PromptOptions options, final float revealModifier, final float alphaModifier) {
        super.update(options, revealModifier, alphaModifier);
        this.mDimPaint.setAlpha((int) (ALPHA_SCALE * alphaModifier));
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        canvas.drawRect(this.mDimBounds, this.mDimPaint);
        super.draw(canvas);
    }
}
