package org.rmorozov.wot_stats.listviewitems;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.ChartData;
import org.rmorozov.wot_stats.R;

public class BarChartItem extends ChartItem {
    private Typeface mTf;

    private static class ViewHolder {
        BarChart chart;

        private ViewHolder() {
        }
    }

    public BarChartItem(ChartData<?> cd, Context c) {
        super(cd);
        this.mTf = Typeface.createFromAsset(c.getAssets(), "OpenSans-Regular.ttf");
    }

    public int getItemType() {
        return 0;
    }

    public View getView(int position, View convertView, Context c) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(c).inflate(R.layout.list_item_barchart, null);
            holder.chart = (BarChart) convertView.findViewById(R.id.chart);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.chart.setDescription("");
        holder.chart.setDrawGridBackground(false);
        holder.chart.setDrawBarShadow(false);
        TextView textView;
        switch (position) {
            case 0:
                textView = (TextView) convertView.findViewById(R.id.textCaption);
                textView.setText(c.getResources().getString(R.string.ingogr1));
                textView.setTypeface(mTf);
                break;
            case 1:
                textView = (TextView) convertView.findViewById(R.id.textCaption);
                textView.setText(c.getResources().getString(R.string.ingogr2));
                textView.setTypeface(mTf);
                break;
            case 2:
                textView = (TextView) convertView.findViewById(R.id.textCaption);
                textView.setText(c.getResources().getString(R.string.ingogr3));
                textView.setTypeface(mTf);
                break;
            case 3:
                textView = (TextView) convertView.findViewById(R.id.textCaption);
                textView.setText(c.getResources().getString(R.string.ingogr4));
                textView.setTypeface(mTf);
                break;
            case 4:
                textView = (TextView) convertView.findViewById(R.id.textCaption);
                textView.setText(c.getResources().getString(R.string.ingogr5));
                textView.setTypeface(mTf);
                break;
        }
        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        YAxis leftAxis = holder.chart.getAxisLeft();
        leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(5, true);
        leftAxis.setSpaceTop(20.0f);
        YAxis rightAxis = holder.chart.getAxisRight();
        rightAxis.setEnabled(false);
        rightAxis.setTypeface(mTf);
        rightAxis.setLabelCount(5, true);
        rightAxis.setSpaceTop(20.0f);
        this.mChartData.setValueTypeface(mTf);
        holder.chart.setData((BarData) mChartData);
        holder.chart.getLegend().setEnabled(false);
        holder.chart.getXAxis().setSpaceBetweenLabels(1);
        holder.chart.animateY(700);
        return convertView;
    }
}
