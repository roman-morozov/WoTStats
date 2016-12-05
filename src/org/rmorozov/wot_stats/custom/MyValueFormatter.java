package org.rmorozov.wot_stats.custom;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import java.text.DecimalFormat;

public class MyValueFormatter implements YAxisValueFormatter {
    private DecimalFormat mFormat;

    public MyValueFormatter() {
        mFormat = new DecimalFormat("###,###,###,##0.0");
    }

    public String getFormattedValue(float v, YAxis yAxis) {
        return mFormat.format(v);
    }
}
