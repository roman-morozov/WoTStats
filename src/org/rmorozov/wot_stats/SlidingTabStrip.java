package org.rmorozov.wot_stats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import org.rmorozov.wot_stats.SlidingTabLayout.TabColorizer;

class SlidingTabStrip extends LinearLayout {
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = (byte) 38;
    private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 0;
    private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;
    private static final int SELECTED_INDICATOR_THICKNESS_DIPS = 3;
    private final Paint mBottomBorderPaint;
    private final int mBottomBorderThickness;
    private TabColorizer mCustomTabColorizer;
    private final int mDefaultBottomBorderColor;
    private final SimpleTabColorizer mDefaultTabColorizer;
    private final Paint mSelectedIndicatorPaint;
    private final int mSelectedIndicatorThickness;
    private int mSelectedPosition;
    private float mSelectionOffset;

    private static class SimpleTabColorizer implements TabColorizer {
        private int[] mIndicatorColors;

        private SimpleTabColorizer() {
        }

        public final int getIndicatorColor(int position) {
            return mIndicatorColors[position % mIndicatorColors.length];
        }

        void setIndicatorColors(int... colors) {
            mIndicatorColors = colors;
        }
    }

    SlidingTabStrip(Context context) {
        this(context, null);
    }

    SlidingTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        float density = getResources().getDisplayMetrics().density;
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorForeground, outValue, true);
        mDefaultBottomBorderColor = setColorAlpha(outValue.data, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);
        mDefaultTabColorizer = new SimpleTabColorizer();
        mDefaultTabColorizer.setIndicatorColors(DEFAULT_SELECTED_INDICATOR_COLOR);
        mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
        mBottomBorderPaint = new Paint();
        mBottomBorderPaint.setColor(mDefaultBottomBorderColor);
        mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIPS * density);
        mSelectedIndicatorPaint = new Paint();
    }

    void setCustomTabColorizer(TabColorizer customTabColorizer) {
        mCustomTabColorizer = customTabColorizer;
        invalidate();
    }

    void setSelectedIndicatorColors(int... colors) {
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setIndicatorColors(colors);
        invalidate();
    }

    void onViewPagerPageChanged(int position, float positionOffset) {
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        int height = getHeight();
        int childCount = getChildCount();
        TabColorizer tabColorizer = mCustomTabColorizer != null ? mCustomTabColorizer : mDefaultTabColorizer;
        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);
            int left = selectedTitle.getLeft();
            int right = selectedTitle.getRight();
            int color = tabColorizer.getIndicatorColor(mSelectedPosition);
            if (mSelectionOffset > 0.0f && mSelectedPosition < getChildCount() - 1) {
                int nextColor = tabColorizer.getIndicatorColor(mSelectedPosition + 1);
                if (color != nextColor) {
                    color = blendColors(nextColor, color, mSelectionOffset);
                }
                View nextTitle = getChildAt(mSelectedPosition + 1);
                left = (int) (mSelectionOffset * nextTitle.getLeft() + (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * nextTitle.getRight() + (1.0f - mSelectionOffset) * right);
            }
            mSelectedIndicatorPaint.setColor(color);
            canvas.drawRect(left, height - mSelectedIndicatorThickness, right, height, mSelectedIndicatorPaint);
        }
        canvas.drawRect(0.0f, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);
    }

    private static int setColorAlpha(int color, byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private static int blendColors(int color1, int color2, float ratio) {
        float inverseRation = 1.0f - ratio;
        return Color.rgb((int) (Color.red(color1) * ratio + Color.red(color2) * inverseRation), (int) (Color.green(color1) * ratio + Color.green(color2) * inverseRation), (int) (Color.blue(color1) * ratio + Color.blue(color2) * inverseRation));
    }
}
