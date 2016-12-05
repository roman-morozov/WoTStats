package org.rmorozov.wot_stats;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Toast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import org.rmorozov.wot_stats.custom.MyMarkerView;
import org.rmorozov.wot_stats.custom.MyValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class TanksGraphItem extends Fragment implements OnChartGestureListener, OnChartValueSelectedListener {
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_SESSION_COUNTER = "session_count";
    private static final String ARG_PARAM1 = "section_number";
    public String mSessionCount;
    CheckBox cb;
    DatabaseHelper dbHelper;
    LineChart mChart;
    private int mParam1;
    SharedPreferences mSettings;
    public View mViewHierarchy;
    public Spinner spinnerBattleCount;

    public void onPause() {
        super.onStop();
        try {
            Spinner spinner = (Spinner) mViewHierarchy.findViewById(R.id.spinnerSessionCount);
            if (spinner != null) {
                String[] choose = getResources().getStringArray(R.array.sesion_count);
                Editor editor = mSettings.edit();
                editor.putString(APP_SESSION_COUNTER, choose[spinner.getSelectedItemPosition()]);
                editor.apply();
            }
        } catch (Exception ignored) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
            main_player_cursor.close();
            StringBuilder append = new StringBuilder().append("select _id from ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_SH).append(" where ");
            Cursor cursorMax = sdb.rawQuery(append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("'").toString(), null);
            View viewHierarchy = inflater.inflate(R.layout.fragment_graph_item, container, false);
            mViewHierarchy = viewHierarchy;
            cb = (CheckBox) viewHierarchy.findViewById(R.id.checkBoxShow);
            cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        refreshGraph(mViewHierarchy, mParam1, mViewHierarchy, true);
                    } else {
                        refreshGraph(mViewHierarchy, mParam1, mViewHierarchy, false);
                    }
                }
            });
            spinnerBattleCount = (Spinner) viewHierarchy.findViewById(R.id.spinnerSessionCount);
            String[] choose = getResources().getStringArray(R.array.sesion_count);
            Context context = mViewHierarchy.getContext();
            String str2 = APP_PREFERENCES;
            mViewHierarchy.getContext();
            mSettings = context.getSharedPreferences(str2, 0);
            if (mSettings.contains(APP_SESSION_COUNTER)) {
                int i = 0;
                while (i < choose.length && !choose[i].equals(mSettings.getString(APP_SESSION_COUNTER, ""))) {
                    i++;
                }
                spinnerBattleCount.setSelection(i);
                mSessionCount = mSettings.getString(APP_SESSION_COUNTER, "");
            }
            spinnerBattleCount.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String[] choose = TanksGraphItem.this.getResources().getStringArray(R.array.sesion_count);
                    mSessionCount = choose[position];
                    Editor editor = mSettings.edit();
                    editor.putString(TanksGraphItem.APP_SESSION_COUNTER, mSessionCount);
                    editor.apply();
                    refreshGraph(mViewHierarchy, mParam1, mViewHierarchy, cb.isChecked());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            cursorMax.close();
            sdb.close();
            return viewHierarchy;
        }
        Toast.makeText(getActivity(), getString(R.string.no_select), Toast.LENGTH_SHORT).show();
        return null;
    }

    public void refreshGraph(View viewHierarchy, int GraphType, View view, boolean showLineWn) {
        Cursor cursorMax;
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
        String[] strArr = new String[3];
        strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
        strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
        strArr[2] = DatabaseHelper.ACTIVE;
        StringBuilder stringBuilder = new StringBuilder();
        String playerId;
        try (Cursor mainPlayerCursor = sdb.query(str, strArr, stringBuilder.append(DatabaseHelper.ACTIVE).append("= ?").toString(), new String[]{"1"}, null, null, null)) {
            mainPlayerCursor.moveToFirst();
            playerId = mainPlayerCursor.getString(1);
        }
        String selectItem = DatabaseHelper.WN8_COLUMN;
        StringBuilder append = new StringBuilder().append("select ");
        append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(", ");
        append = append.append(DatabaseHelper.PLAYER_NAME_COLUMN).append(", ");
        append = append.append(DatabaseHelper.BATTLES_COLUMN).append(", ");
        append = append.append(DatabaseHelper.WINRATE_COLUMN).append(", ");
        append = append.append(DatabaseHelper.WN6_COLUMN).append(", ");
        append = append.append(DatabaseHelper.WN8_COLUMN).append(",");
        append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(",");
        append = append.append(DatabaseHelper.REFF_COLUMN).append(" from ");
        append = append.append(DatabaseHelper.DATABASE_TABLE_SH).append(" where ");
        Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(playerId).append("'").toString(), null);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            switch (GraphType) {
                case 1:
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    selectItem = DatabaseHelper.WN8_COLUMN;
                    break;
                case 2:
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    selectItem = DatabaseHelper.WN6_COLUMN;
                    break;
                case 3:
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    selectItem = DatabaseHelper.REFF_COLUMN;
                    break;
                case 4:
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    selectItem = DatabaseHelper.WINRATE_COLUMN;
                    break;
                case 5:
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    mChart = (LineChart) viewHierarchy.findViewById(R.id.chart1);
                    selectItem = DatabaseHelper.DAMAGE_COLUMN;
                    break;
            }
            cursor.close();
            mChart.setOnChartGestureListener(this);
            mChart.setOnChartValueSelectedListener(this);
            mChart.setDescription("");
            mChart.setNoDataTextDescription("You need to provide data for the chart.");
            mChart.setTouchEnabled(true);
            mChart.setDragEnabled(true);
            mChart.setScaleEnabled(true);
            mChart.setPinchZoom(true);
            mChart.setMarkerView(new MyMarkerView(view.getContext(), R.layout.custom_marker_view));
            mChart.setGridBackgroundColor(getResources().getColor(R.color.main_white));
            mChart.setBackgroundColor(getResources().getColor(R.color.main_light));
            if (mSessionCount.equals("All") || mSessionCount.equals("\u0412\u0441\u0435")) {
                append = new StringBuilder().append("select MAX(");
                append = append.append(DatabaseHelper.BATTLES_COLUMN).append(") as maxX, MIN(");
                append = append.append(DatabaseHelper.BATTLES_COLUMN).append(") as mixX, MAX(").append(selectItem).append(") as maxY, MIN(").append(selectItem).append(") as minY").append(" from ");
                append = append.append(DatabaseHelper.DATABASE_TABLE_SH).append(" where ");
                append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(playerId).append("' GROUP BY ");
                cursorMax = sdb.rawQuery(append.append(DatabaseHelper.PLAYER_ID_COLUMN).toString(), null);
            } else {
                append = new StringBuilder().append("select MAX(");
                append = append.append(DatabaseHelper.BATTLES_COLUMN).append(") as maxX, MIN(");
                append = append.append(DatabaseHelper.BATTLES_COLUMN).append(") as mixX, MAX(").append(selectItem).append(") as maxY, MIN(").append(selectItem).append(") as minY").append(" from ");
                append = append.append(DatabaseHelper.DATABASE_TABLE_SH).append(" where ");
                append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = \'").append(playerId).append("\' GROUP BY ");
                append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" ORDER BY ");
                cursorMax = sdb.rawQuery(append.append(DatabaseHelper.BATTLES_COLUMN).append(" DESC LIMIT ").append(mSessionCount).toString(), null);
            }
            try {
                cursorMax.moveToFirst();
                setData(GraphType, showLineWn, playerId, selectItem);
            } finally {
                cursorMax.close();
            }
            sdb.close();
            mChart.getLegend().setEnabled(false);
            mChart.getAxisRight().setEnabled(false);
            mChart.getXAxis().setPosition(XAxisPosition.BOTTOM);
            mChart.invalidate();
        }
    }

    private void setData(int graphType, boolean showLineWn, String player_id, String select_item) {
        int intSC;
        int i;
        LineDataSet lineDataSet;
        List<ILineDataSet> dataSets;
        LineData data;
        YAxisValueFormatter custom;
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        StringBuilder append = new StringBuilder().append("select ");
        append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(", ");
        append = append.append(DatabaseHelper.PLAYER_NAME_COLUMN).append(", ");
        append = append.append(DatabaseHelper.BATTLES_COLUMN).append(", ");
        append = append.append(DatabaseHelper.WINRATE_COLUMN).append(", ");
        append = append.append(DatabaseHelper.WN6_COLUMN).append(", ");
        append = append.append(DatabaseHelper.WN8_COLUMN).append(",");
        append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(",");
        append = append.append(DatabaseHelper.REFF_COLUMN).append(" from ");
        append = append.append(DatabaseHelper.DATABASE_TABLE_SH).append(" where ");
        Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = \'").append(player_id).append("\'").toString(), null);
        cursor.moveToFirst();
        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yVals = new ArrayList<>();
        if (!mSessionCount.equals("All") && !mSessionCount.equals("Все")) {
            int intSessionCount = Integer.parseInt(mSessionCount);
            if (cursor.getCount() > intSessionCount) {
                intSC = cursor.getCount() - intSessionCount;
            } else {
                intSC = 0;
            }
        } else {
            intSC = 0;
        }
        float minYY = 1.0E8f;
        float maxYY = 0.0f;
        for (i = 0; i < cursor.getCount(); i++) {
            if (i >= intSC) {
                xVals.add(String.valueOf(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN))));
                float value = cursor.getFloat(cursor.getColumnIndex(select_item));
                yVals.add(new Entry(value, i - intSC));
                if (value < minYY) {
                    minYY = value;
                }
                if (value > maxYY) {
                    maxYY = value;
                }
            }
            cursor.moveToNext();
        }
        cursor.close();
        sdb.close();
        setLimitLines(graphType, showLineWn, minYY, maxYY);
        mChart.getAxisRight().setEnabled(false);
        lineDataSet = new LineDataSet(yVals, "");
        lineDataSet.setColor(ColorTemplate.getHoloBlue());
        lineDataSet.setCircleColor(ColorTemplate.getHoloBlue());
        lineDataSet.setLineWidth(2.0f);
        lineDataSet.setCircleRadius(4.0f);
        lineDataSet.setFillColor(ColorTemplate.getHoloBlue());
        lineDataSet.setHighLightColor(Color.rgb(244, 117, 117));
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillAlpha(65);
        lineDataSet.setFillColor(ColorTemplate.getHoloBlue());
        dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        data = new LineData(xVals, dataSets);
        mChart.getXAxis().setPosition(XAxisPosition.BOTTOM);
        custom = new MyValueFormatter();
        mChart.getAxisLeft().setValueFormatter(custom);
        mChart.setData(data);
    }

    private void setLimitLines(int graphType, boolean showLineWn, float minYY, float maxYY) {
        float[] p = new float[5];
        int[] color = new int[]{R.color.olen_orange, R.color.olen_yellow, R.color.olen_green, R.color.olen_purpure, R.color.olen_purple};
        int minI = 0;
        int maxI = 4;
        int i;
        YAxis leftAxis = mChart.getAxisLeft();
        if (showLineWn && graphType != 5) {
            float minimum;
            float maximum;
            LimitLine ll1;
            switch (graphType) {
                case 1:
                    p[0] = 370.0f;
                    p[1] = 845.0f;
                    p[2] = 1395.0f;
                    p[3] = 2070.0f;
                    p[4] = 2715.0f;
                    break;
                case 2:
                    p[0] = 460.0f;
                    p[1] = 850.0f;
                    p[2] = 1215.0f;
                    p[3] = 1620.0f;
                    p[4] = 1960.0f;
                    break;
                case 3:
                    p[0] = 615.0f;
                    p[1] = 870.0f;
                    p[2] = 1175.0f;
                    p[3] = 1525.0f;
                    p[4] = 1850.0f;
                    break;
                case 4:
                    p[0] = 47.0f;
                    p[1] = 49.0f;
                    p[2] = 52.5f;
                    p[3] = 58.0f;
                    p[4] = 65.0f;
                    break;
            }
            for (i = 0; i < 5; i++) {
                if (p[i] < minYY) {
                    minI = i;
                }
            }
            for (i = 0; i < 5; i++) {
                if (p[i] > maxYY) {
                    maxI = i;
                    break;
                }
            }
            if (p[minI] < minYY) {
                minimum = p[minI];
            } else {
                minimum = minYY;
            }
            if (p[maxI] > maxYY) {
                maximum = p[maxI];
            } else {
                maximum = maxYY;
            }
            leftAxis.removeAllLimitLines();
            if (graphType != 4) {
                leftAxis.setAxisMaxValue(25.0f + maximum);
                leftAxis.setAxisMinValue(minimum - 25.0f);
                //leftAxis.resetAxisMinValue();
            } else {
                //leftAxis.removeAllLimitLines();
                leftAxis.setAxisMaxValue(0.5f + maximum);
                leftAxis.setAxisMinValue(minimum - 0.5f);
                //leftAxis.resetAxisMinValue();
                leftAxis.setDrawTopYLabelEntry(false);
            }
            for (i = minI; i <= maxI; i++) {
                ll1 = new LimitLine(p[i], String.valueOf(p[i]));
                ll1.setLineWidth(2.0f);
                ll1.setTextSize(10.0f);
                ll1.enableDashedLine(10.0f, 10.0f, 0.0f);
                ll1.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
                ll1.setLineColor(getResources().getColor(color[i]));
                leftAxis.addLimitLine(ll1);
            }
        } else {
            leftAxis.removeAllLimitLines();
            if (graphType != 4) {
                leftAxis.setAxisMaxValue(0.01f + maxYY);
                leftAxis.setAxisMinValue(minYY - 0.01f);
            } else {
                leftAxis.setAxisMaxValue(0.5F + maxYY);
                leftAxis.setAxisMinValue(minYY - 0.5F);
            }
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onDetach() {
        super.onDetach();
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
