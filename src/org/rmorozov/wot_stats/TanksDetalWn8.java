package org.rmorozov.wot_stats;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import org.rmorozov.wot_stats.custom.MyMarkerView;

import java.util.ArrayList;
import java.util.List;

public class TanksDetalWn8 extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    DatabaseHelper dbHelper;
    LineChart mChart;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tanks_detal_tab_dmg, container, false);
        mChart = (LineChart) v.findViewById(R.id.chartDmg);
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
        String[] strArr = new String[3];
        strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
        strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
        strArr[2] = DatabaseHelper.ACTIVE;
        StringBuilder stringBuilder = new StringBuilder();
        Cursor main_player_cursor = sdb.query(str, strArr, stringBuilder.append(DatabaseHelper.ACTIVE).append("= ?").toString(), new String[]{"1"}, null, null, null);
        if (main_player_cursor.moveToFirst()) {
            main_player_cursor.moveToFirst();
            String player_id = main_player_cursor.getString(1);
            StringBuilder append = new StringBuilder().append("select  MAX(");
            append = append.append(DatabaseHelper.WN8_COLUMN).append(") AS VALUE, ");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(" from ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" where ");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' AND ");
            append = append.append(DatabaseHelper.TANK_ID_COLUMN).append(" = '").append(TanksDetailActivity.mTankId).append("' ").append(" GROUP BY ");
            try (Cursor cursor_tanks = sdb.rawQuery(append.append(DatabaseHelper.BATTLES_COLUMN).toString(), null)) {
                if (cursor_tanks.getCount() > 1) {
                    refreshGraph(v);
                    cursor_tanks.close();
                    return v;
                }
                v = inflater.inflate(R.layout.graph_start, container, false);
                main_player_cursor.close();
                cursor_tanks.close();
                return v;
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.no_select), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void refreshGraph(View viewHierarchy) {
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
        String[] strArr = new String[3];
        strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
        strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
        strArr[2] = DatabaseHelper.ACTIVE;
        StringBuilder stringBuilder = new StringBuilder();
        String player_id;
        try (Cursor main_player_cursor = sdb.query(str, strArr, stringBuilder.append(DatabaseHelper.ACTIVE).append("= ?").toString(), new String[]{"1"}, null, null, null)) {
            main_player_cursor.moveToFirst();
            player_id = main_player_cursor.getString(1);
        }
        String selectItem;
        StringBuilder append = new StringBuilder().append("select  MAX(");
        append = append.append(DatabaseHelper.DAMAGE_COLUMN).append("), ");
        append = append.append(DatabaseHelper.BATTLES_COLUMN).append(" from ");
        append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" where ");
        append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' AND ");
        append = append.append(DatabaseHelper.TANK_ID_COLUMN).append(" = '").append(TanksDetailActivity.mTankId).append("' ").append("GROUP BY ");
        Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.BATTLES_COLUMN).toString(), null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            selectItem = DatabaseHelper.WINS_COLUMN;
            mChart.setOnChartGestureListener(this);
            mChart.setOnChartValueSelectedListener(this);
            mChart.setOnChartGestureListener(this);
            mChart.setOnChartValueSelectedListener(this);
            mChart.setDescription("");
            mChart.setNoDataTextDescription("You need to provide data for the chart.");
            mChart.setTouchEnabled(true);
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            mChart.setPinchZoom(true);
            mChart.setMarkerView(new MyMarkerView(viewHierarchy.getContext(), R.layout.custom_marker_view));
            mChart.setGridBackgroundColor(getResources().getColor(R.color.main_white));
            mChart.setBackgroundColor(getResources().getColor(R.color.main_light));
            append = new StringBuilder().append("select MAX(");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(") as maxX, MIN(");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(") as mixX, MAX(").append(selectItem).append(") as maxY, MIN(").append(selectItem).append(") as minY").append(" from ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" where ");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' AND ");
            append = append.append(DatabaseHelper.TANK_ID_COLUMN).append(" = '").append(TanksDetailActivity.mTankId).append("'").append(" GROUP BY ");
            try (Cursor cursorMax = sdb.rawQuery(append.append(DatabaseHelper.PLAYER_ID_COLUMN).toString(), null)) {
                cursorMax.moveToFirst();
                setData(1, player_id);
            }
            cursor.close();
            sdb.close();
            mChart.getLegend().setEnabled(false);
            mChart.getAxisRight().setEnabled(false);
            mChart.getXAxis().setPosition(XAxisPosition.BOTTOM);
        }
    }

    private void setData(int GraphType, String player_id) {
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        StringBuilder append = new StringBuilder().append("select  MAX(");
        append = append.append(DatabaseHelper.WN8_COLUMN).append(") AS VALUE, ");
        append = append.append(DatabaseHelper.BATTLES_COLUMN).append(" from ");
        append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" where ");
        append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' AND ");
        append = append.append(DatabaseHelper.TANK_ID_COLUMN).append(" = '").append(TanksDetailActivity.mTankId).append("' ").append(" GROUP BY ");
        Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.BATTLES_COLUMN).toString(), null);
        cursor.moveToFirst();
        List<String> xVals = new ArrayList<>();
        ArrayList<Entry> yVals = new ArrayList<>();
        float minYY = 1.0E8f;
        float maxYY = 0.0f;
        for (int i = 0; i < cursor.getCount(); i++) {
            if (i >= 0) {
                append = new StringBuilder();
                xVals.add(append.append(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN))).append("").toString());
                yVals.add(new Entry(cursor.getFloat(cursor.getColumnIndex("VALUE")), i));
                if (cursor.getFloat(cursor.getColumnIndex("VALUE")) < minYY) {
                    minYY = cursor.getFloat(cursor.getColumnIndex("VALUE"));
                }
                if (cursor.getFloat(cursor.getColumnIndex("VALUE")) > maxYY) {
                    maxYY = cursor.getFloat(cursor.getColumnIndex("VALUE"));
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        sdb.close();
        LineDataSet set1 = new LineDataSet(yVals, "");
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setCircleColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(2.0f);
        set1.setCircleRadius(4.0f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawValues(false);
        set1.setDrawFilled(true);
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        LineData data = new LineData(xVals, dataSets);
        YAxis leftAxis;
        if (GraphType == 4) {
            leftAxis = mChart.getAxisLeft();
            leftAxis.removeAllLimitLines();
            leftAxis.setAxisMaxValue(0.01f + maxYY);
            leftAxis.setAxisMinValue(minYY - 0.01f);
            leftAxis.resetAxisMinValue();
            leftAxis.setDrawTopYLabelEntry(false);
        } else {
            leftAxis = mChart.getAxisLeft();
            leftAxis.removeAllLimitLines();
            leftAxis.setAxisMaxValue(1.0f + maxYY);
            leftAxis.setAxisMinValue(minYY - 1.0f);
            leftAxis.resetAxisMinValue();
        }
        mChart.setData(data);
    }

    public void onChartGestureStart(MotionEvent me, ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START");
    }

    public void onChartGestureEnd(MotionEvent me, ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);
        if (lastPerformedGesture != ChartGesture.SINGLE_TAP) {
            mChart.highlightValues(null);
        }
    }

    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("", "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
    }

    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}
