package org.rmorozov.wot_stats;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Region;

public class Bar {
    private int mColor;
    private String mName;
    private final Path mPath;
    private float mValue;
    private String mValueString;
    private int mSelectedColor;
    private final Region mRegion;
    private int mLabelColor;

    public Bar() {
        this.mPath = new Path();
        this.mRegion = new Region();
        this.mColor = 0xFF33B5E5;
        this.mLabelColor = Color.WHITE;
        this.mSelectedColor = Color.WHITE;
        this.mName = null;
        this.mValueString = null;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public float getValue() {
        return mValue;
    }

    public void setValue(float value) {
        mValue = value;
    }

    public Path getPath() {
        return mPath;
    }

    public String getValueString() {
        return mValueString;
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public Region getRegion() {
        return mRegion;
    }

    public int getLabelColor() {
        return mLabelColor;
    }

    public void setValueString(String valueString) {
        mValueString = valueString;
    }

    public void setSelectedColor(int selectedColor) {
        mSelectedColor = selectedColor;
    }

    public void setLabelColor(int labelColor) {
        mLabelColor = labelColor;
    }
}
