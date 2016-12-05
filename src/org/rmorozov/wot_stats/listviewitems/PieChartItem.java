package org.rmorozov.wot_stats.listviewitems;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.formatter.PercentFormatter;
import org.rmorozov.wot_stats.R;

public class PieChartItem extends ChartItem {
    private Typeface mTf;

    private static class ViewHolder {
        PieChart chart;

        private ViewHolder() {
        }
    }

    public PieChartItem(ChartData<?> cd, Context c) {
        super(cd);
        this.mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
    }

    public int getItemType() {
        return 2;
    }

    public View getView(int position, View convertView, Context c) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(c).inflate(R.layout.list_item_piechart, null);
            holder.chart = (PieChart) convertView.findViewById(R.id.chart);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.chart.setDescription("");
        holder.chart.setHoleRadius(52.0f);
        holder.chart.setTransparentCircleRadius(57.0f);
        holder.chart.setCenterText("MPChart\nAndroid");
        holder.chart.setCenterTextTypeface(this.mTf);
        holder.chart.setCenterTextSize(18.0f);
        holder.chart.setUsePercentValues(true);
        String caption = "";
        switch (position) {
            case 5:
                caption = c.getResources().getString(R.string.infogr6);
                break;
            case 6:
                caption = c.getResources().getString(R.string.infogr7);
                break;
        }
        holder.chart.setCenterText(caption);
        this.mChartData.setValueFormatter(new PercentFormatter());
        this.mChartData.setValueTypeface(this.mTf);
        this.mChartData.setValueTextSize(11.0f);
        this.mChartData.setValueTextColor(Color.GRAY);
        holder.chart.setData((PieData) this.mChartData);
        holder.chart.getLegend().setPosition(LegendPosition.RIGHT_OF_CHART);
        holder.chart.animateXY(900, 900);
        return convertView;
    }
}
