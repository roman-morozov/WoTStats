package org.rmorozov.wot_stats.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.Utils;
import org.rmorozov.wot_stats.R;

@SuppressLint("ViewConstructor")
public class MyMarkerView extends MarkerView {
    private final TextView tvContent;

    @SuppressWarnings("SameParameterValue")
    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        this.tvContent = (TextView) findViewById(R.id.tvContent);
    }

    public void refreshContent(Entry entry, Highlight highlight) {
        if (entry instanceof CandleEntry) {
            tvContent.setText(Utils.formatNumber(((CandleEntry) entry).getHigh(), 0, true));
            return;
        }
        this.tvContent.setText(Utils.formatNumber(entry.getVal(), 2, true));
    }

    @Override
    public int getYOffset(float ypos) {
        return -getHeight();
    }

    @Override
    public int getXOffset(float xpos) {
        return -(getWidth() / 2);
    }
}
