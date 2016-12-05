package org.rmorozov.wot_stats.listviewitems;

import android.content.Context;
import android.view.View;
import com.github.mikephil.charting.data.ChartData;

public abstract class ChartItem {
    protected ChartData<?> mChartData;

    public abstract int getItemType();

    public abstract View getView(int i, View view, Context context);

    public ChartItem(ChartData<?> cd) {
        this.mChartData = cd;
    }
}
