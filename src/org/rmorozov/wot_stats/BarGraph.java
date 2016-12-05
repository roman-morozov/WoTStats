package org.rmorozov.wot_stats;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.Path.Direction;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;

public class BarGraph extends View {
    private static final int AXIS_LABEL_FONT_SIZE = 15;
    private static final float LABEL_PADDING_MULTIPLIER = 1.6f;
    private static final int ORIENTATION_HORIZONTAL = 0;
    private static final int ORIENTATION_VERTICAL = 1;
    private static final int VALUE_FONT_SIZE = 30;
    private int mAxisColor;
    private ArrayList<Bar> mBars;
    private Rect mBoundsRect;
    private OnBarClickedListener mListener;
    private Paint mPaint;
    private int mSelectedIndex;
    private boolean mShowAxis;
    private boolean mShowBarText;
    private Rect mTextRect;

    public interface OnBarClickedListener {
        void onClick(int i);
    }

    public BarGraph(Context context) {
        this(context, null);
    }

    public BarGraph(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBars = new ArrayList<>();
        mPaint = new Paint();
        mBoundsRect = new Rect();
        mTextRect = new Rect();
        mShowBarText = true;
        mShowAxis = true;
        mSelectedIndex = -1;
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BarGraph);
        mAxisColor = a.getColor(0, Color.LTGRAY);
        a.recycle();
    }

    public void setBars(ArrayList<Bar> points) {
        mBars = points;
        postInvalidate();
    }

    public void onDraw(Canvas canvas) {
        float usableHeight;
        Resources resources = getContext().getResources();
        canvas.drawColor(Color.TRANSPARENT);
        NinePatchDrawable popup = (NinePatchDrawable) resources.getDrawable(R.drawable.popup_black, null);
        float maxValue = 0.0f;
        float padding = 7.0f * resources.getDisplayMetrics().density;
        float bottomPadding = 30.0f * resources.getDisplayMetrics().density;
        if (mShowBarText) {
            mPaint.setTextSize(VALUE_FONT_SIZE * resources.getDisplayMetrics().scaledDensity);
            mPaint.getTextBounds("$", 0, 1, mTextRect);
            usableHeight = getHeight() - bottomPadding - Math.abs(mTextRect.top - mTextRect.bottom) - 24.0f * resources.getDisplayMetrics().density;
        } else {
            usableHeight = getHeight() - bottomPadding;
        }
        if (mShowAxis) {
            mPaint.setColor(mAxisColor);
            mPaint.setStrokeWidth(2.0f * resources.getDisplayMetrics().density);
            mPaint.setAntiAlias(true);
            canvas.drawLine(0.0f, getHeight() - bottomPadding + (10.0f * resources.getDisplayMetrics().density), getWidth(), getHeight() - bottomPadding + (10.0f * resources.getDisplayMetrics().density), mPaint);
        }
        float barWidth = (getWidth() - 2.0f * padding * mBars.size()) / mBars.size();
        Iterator iterator = mBars.iterator();
        while (iterator.hasNext()) {
            Bar bar = (Bar) iterator.next();
            if (bar.getValue() > maxValue) {
                maxValue = bar.getValue();
            }
        }
        if (maxValue == 0.0f) {
            maxValue = 1.0f;
        }
        int count = 0;
        SparseArray<Float> valueTextSizes = new SparseArray<>(mBars.size());
        iterator = mBars.iterator();
        while (iterator.hasNext()) {
            Bar bar = (Bar) iterator.next();
            int left = (int) (2.0f * padding * count + padding + count * barWidth);
            int right = (int) (2.0f * padding * count + padding + (count + 1.0f) * barWidth);
            mBoundsRect.set(left, (int) ((getHeight() - bottomPadding) - ((bar.getValue() / maxValue) * usableHeight)), right, (int) (((float) getHeight()) - bottomPadding));
            if (count != mSelectedIndex || mListener == null) {
                mPaint.setColor(bar.getColor());
            } else {
                mPaint.setColor(bar.getSelectedColor());
            }
            canvas.drawRect(mBoundsRect, mPaint);
            Path p = bar.getPath();
            p.reset();
            p.addRect(mBoundsRect.left, mBoundsRect.top, mBoundsRect.right, mBoundsRect.bottom, Direction.CW);
            bar.getRegion().set(mBoundsRect.left, mBoundsRect.top, mBoundsRect.right, mBoundsRect.bottom);
            if (mShowAxis) {
                mPaint.setColor(bar.getLabelColor());
                mPaint.setTextSize(AXIS_LABEL_FONT_SIZE * resources.getDisplayMetrics().scaledDensity);
                float textWidth = mPaint.measureText(bar.getName());
                while ((right - left) + (LABEL_PADDING_MULTIPLIER * padding) < textWidth) {
                    mPaint.setTextSize(mPaint.getTextSize() - 1.0f);
                    textWidth = mPaint.measureText(bar.getName());
                }
                int x = (int) ((((mBoundsRect.left + mBoundsRect.right) / 2)) - (textWidth / 2.0f));
                int y = (int) (getHeight() - 3.0f * resources.getDisplayMetrics().scaledDensity);
                canvas.drawText(bar.getName(), x, y, mPaint);
            }
            if (mShowBarText) {
                mPaint.setTextSize(VALUE_FONT_SIZE * resources.getDisplayMetrics().scaledDensity);
                mPaint.setColor(-1);
                mPaint.getTextBounds(bar.getValueString(), ORIENTATION_HORIZONTAL, ORIENTATION_VERTICAL, mTextRect);
                int boundLeft = (int) ((mBoundsRect.left + mBoundsRect.right) / 2.0f - mPaint.measureText(bar.getValueString()) / 2.0f - 10.0f * resources.getDisplayMetrics().density);
                int boundTop = (int) (mBoundsRect.top + mTextRect.top - mTextRect.bottom - 18.0f * resources.getDisplayMetrics().density);
                int boundRight = (int) ((mBoundsRect.left + mBoundsRect.right) / 2.0f + mPaint.measureText(bar.getValueString()) / 2.0f + 10.0f * resources.getDisplayMetrics().density);
                if (boundLeft < mBoundsRect.left) {
                    boundLeft = mBoundsRect.left - ((int) padding / 2);
                }
                if (boundRight > mBoundsRect.right) {
                    boundRight = mBoundsRect.right + ((int) padding / 2);
                }
                if (popup != null) {
                    popup.setBounds(boundLeft, boundTop, boundRight, mBoundsRect.top);
                    popup.draw(canvas);
                }

                if (valueTextSizes.indexOfKey(bar.getValueString().length()) < 0) {
                    while (mPaint.measureText(bar.getValueString()) > ((float) (boundRight - boundLeft))) {
                        mPaint.setTextSize(mPaint.getTextSize() - VALUE_FONT_SIZE);
                    }
                    valueTextSizes.put(bar.getValueString().length(), mPaint.getTextSize());
                } else {
                    mPaint.setTextSize(valueTextSizes.get(bar.getValueString().length()));
                }
                canvas.drawText(bar.getValueString(), (int) ((mBoundsRect.left + mBoundsRect.right) / 2.0f - mPaint.measureText(bar.getValueString()) / 2.0f), mBoundsRect.top - (mBoundsRect.top - boundTop) / 2.0f + Math.abs(mTextRect.top - mTextRect.bottom) / 2.0f * 0.7f, mPaint);
            }
            count++;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    public void setOnBarClickedListener(OnBarClickedListener listener) {
        mListener = listener;
    }
}
