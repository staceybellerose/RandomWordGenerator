package com.staceybellerose.randomwordgenerator.utils;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import uk.co.samuelwall.materialtaptargetprompt.extras.PromptBackground;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptOptions;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptText;
import uk.co.samuelwall.materialtaptargetprompt.extras.PromptUtils;

/**
 * Subclass of CirclePromptBackground which inserts a dimmer shim behind the prompt
 */
public class DimmedPromptBackground extends PromptBackground {
    /**
     * Alpha to use for dimmed shim
     */
    private static final int ALPHA_SCALE = 128;
    /**
     * Bounds of the dimmed shim
     */
    private final RectF mDimBounds = new RectF();
    /**
     * Paint to use when coloring the dimmed shim
     */
    private final Paint mDimPaint;

    /**
     * The current circle centre position.
     */
    private final PointF mPosition;
    /**
     * The current radius for the circle.
     */
    private float mRadius;
    /**
     * The position for circle centre at 1.0 scale.
     */
    private final PointF mBasePosition;

    /**
     * The radius for the circle at 1.0 scale.
     */
    private float mBaseRadius;

    /**
     * The paint to use to render the circle.
     */
    private final Paint mPaint;

    /**
     * The alpha value to use at 1.0 scale.
     */
    @IntRange(from = 0, to = 255)
    private int mBaseColourAlpha;

    /**
     * Constructor
     */
    public DimmedPromptBackground() {
        mDimPaint = new Paint();
        mDimPaint.setColor(Color.BLACK);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPosition = new PointF();
        mBasePosition = new PointF();
    }

    @Override
    public void setColour(@ColorInt final int colour)
    {
        mPaint.setColor(colour);
        mBaseColourAlpha = Color.alpha(colour);
        mPaint.setAlpha(mBaseColourAlpha);
    }

    @Override
    public void prepare(@NonNull final PromptOptions options, final boolean clipToBounds,
                        @NonNull final Rect clipBounds) {
        final DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        mDimBounds.set(0, 0, metrics.widthPixels, metrics.heightPixels);

        final PromptText promptText = options.getPromptText();
        final RectF focalBounds = options.getPromptFocal().getBounds();
        float focalCentreX = focalBounds.centerX();
        float focalCentreY = focalBounds.centerY();
        final float focalPadding = options.getFocalPadding();
        final RectF textBounds = promptText.getBounds();
        final float textPadding = options.getTextPadding();

        // if the target is approximately centered on the X axis (within 10 dp), shift it to 1/4 the screen width
        if (Math.abs(focalCentreX - metrics.widthPixels / 2) < 10 * metrics.density) {
            focalCentreX = metrics.widthPixels / 4;
        }

        // if the target is approximately centered on the Y axis (within 10 dp), shift it slightly
        if (Math.abs(focalCentreY - metrics.heightPixels / 2) < 10 * metrics.density) {
            focalCentreY = focalCentreY + 10 * metrics.density;
        }

        mBasePosition.set(focalCentreX, focalCentreY);
        // Calculate the furthest distance from the center based on the text size.
        final float length = Math.max(
                Math.abs(textBounds.right - focalCentreX),
                Math.abs(textBounds.left - focalCentreX)
        ) + textPadding;
        // Calculate the height based on the distance from the focal centre to the furthest text y position.
        final float height = (focalBounds.height() / 2) + focalPadding + textBounds.height();
        // Calculate the radius based on the calculated width and height
        mBaseRadius = (float) Math.sqrt(Math.pow(length, 2) + Math.pow(height, 2));

        mPosition.set(mBasePosition);
    }

    @Override
    public void update(@NonNull final PromptOptions options, final float revealModifier, final float alphaModifier) {
        final RectF focalBounds = options.getPromptFocal().getBounds();
        final float focalCentreX = focalBounds.centerX();
        final float focalCentreY = focalBounds.centerY();
        mRadius = mBaseRadius * revealModifier;
        mPaint.setAlpha((int) (mBaseColourAlpha * alphaModifier));
        // Change the current centre position to be a position scaled from the focal to the base.
        mPosition.set(focalCentreX + ((mBasePosition.x - focalCentreX) * revealModifier),
                focalCentreY + ((mBasePosition.y - focalCentreY) * revealModifier));

        this.mDimPaint.setAlpha((int) (ALPHA_SCALE * alphaModifier));
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {
        canvas.drawRect(this.mDimBounds, this.mDimPaint);

        canvas.drawCircle(mPosition.x, mPosition.y, mRadius, mPaint);
    }

    @Override
    public boolean contains(final float targetX, final float targetY)
    {
        return PromptUtils.isPointInCircle(targetX, targetY, mPosition, mRadius);
    }
}
