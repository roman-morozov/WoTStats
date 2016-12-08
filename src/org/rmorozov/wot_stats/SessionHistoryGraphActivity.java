package org.rmorozov.wot_stats;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class SessionHistoryGraphActivity extends Activity {
    private int[] mColors;
    private ArrayList<BarEntry> mEntriesValue;
    private ArrayList<Entry> mEntriesValueAvg;

    public SessionHistoryGraphActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_history_graph);
        String statName = getIntent().getStringExtra("statistic_type");
        setTitle(statName);
        DatabaseHelper dbHelper = DatabaseHelper.createDatabaseHelper(this);
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
        String[] strArr = new String[3];
        strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
        strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
        strArr[2] = DatabaseHelper.ACTIVE;
        StringBuilder stringBuilder = new StringBuilder();
        Cursor mainPlayerCursor = sdb.query(str, strArr, stringBuilder.append(DatabaseHelper.ACTIVE).append("= ?").toString(), new String[]{"1"}, null, null, null);
        if (mainPlayerCursor.moveToFirst()) {
            mainPlayerCursor.moveToFirst();
            String player_id = mainPlayerCursor.getString(1);
            mainPlayerCursor.close();
            String SelectItem = DatabaseHelper.BATTLES_COLUMN;
            if (statName.equals(getResources().getString(R.string.session_history1))) {
                SelectItem = DatabaseHelper.BATTLES_COLUMN;
            }
            if (statName.equals(getResources().getString(R.string.session_history2))) {
                SelectItem = DatabaseHelper.WINRATE_COLUMN;
            }
            if (statName.equals(getResources().getString(R.string.session_history3))) {
                SelectItem = DatabaseHelper.DAMAGE_COLUMN;
            }
            if (statName.equals(getResources().getString(R.string.session_history4))) {
                SelectItem = DatabaseHelper.KILLS_COLUMN;
            }
            if (statName.equals(getResources().getString(R.string.session_history5))) {
                SelectItem = DatabaseHelper.SPOT_COLUMN;
            }
            if (statName.equals(getResources().getString(R.string.session_history6))) {
                SelectItem = DatabaseHelper.EXP_COLUMN;
            }
            if (statName.equals(getResources().getString(R.string.session_history7))) {
                SelectItem = DatabaseHelper.REFF_COLUMN;
            }
            if (statName.equals(getResources().getString(R.string.session_history8))) {
                SelectItem = DatabaseHelper.WN6_COLUMN;
            }
            if (statName.equals(getResources().getString(R.string.session_history9))) {
                SelectItem = DatabaseHelper.WN8_COLUMN;
            }
            StringBuilder append = new StringBuilder().append("select ").append(SelectItem).append(", ");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(" from ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_SH).append(" where ");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' ").append(" order by ");
            Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.BATTLES_COLUMN).append(" DESC LIMIT 10").toString(), null);
            try {
                cursor.moveToFirst();
                List<String> battleList = new ArrayList<>();
                double[] valueAvg = new double[cursor.getCount()];
                long[] battles = new long[cursor.getCount()];
                mColors = new int[cursor.getCount()];
                int i = 0;
                while (i < cursor.getCount()) {
                    if (i < cursor.getCount() - 1) {
                        valueAvg[i] = cursor.getDouble(cursor.getColumnIndex(SelectItem));
                        battles[i] = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN));
                        cursor.moveToNext();
                        i++;
                    } else {
                        valueAvg[i] = cursor.getDouble(cursor.getColumnIndex(SelectItem));
                        battles[i] = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN));
                        cursor.moveToNext();
                        i++;
                    }
                }
                if (valueAvg.length > 0) {
                    mEntriesValue = new ArrayList<>();
                    mEntriesValueAvg = new ArrayList<>();
                    double valueMin1;
                    double valueMax1;
                    double valueAvgMin = valueAvg[0];
                    double valueMin = valueAvg[0];
                    double valueAvgMax = valueAvg[0];
                    double valueMax = valueAvg[0];
                    for (i = 0; i < valueAvg.length - 1; i++) {
                        double ValueFirst = valueAvg[i];
                        double ValueSecond = valueAvg[i + 1];
                        long BattlesFirst = battles[i];
                        long BattlesSecond = battles[i + 1];
                        double param2 = ((double) (BattlesFirst - BattlesSecond)) / ((double) BattlesFirst);
                        double rez = (ValueFirst - (ValueSecond * (((double) BattlesSecond) / ((double) BattlesFirst)))) / param2;
                        mEntriesValue.add(new BarEntry((float) Math.abs(rez), i));
                        mEntriesValueAvg.add(new Entry((float) valueAvg[i], i));
                        if (rez < valueAvg[i]) {
                            mColors[i] = Color.RED;
                        } else {
                            mColors[i] = Color.GREEN;
                        }
                        battleList.add(String.valueOf(BattlesFirst));
                        if (rez < valueMin) {
                            valueMin = rez;
                        }
                        if (rez > valueMax) {
                            valueMax = rez;
                        }
                        if (ValueFirst < valueAvgMin) {
                            valueAvgMin = ValueFirst;
                        }
                        if (ValueFirst > valueAvgMax) {
                            valueAvgMax = ValueFirst;
                        }
                    }
                    if (valueMin < valueAvgMin) {
                        valueMin1 = valueMin;
                    } else {
                        valueMin1 = valueAvgMin;
                    }
                    if (valueMax > valueAvgMax) {
                        valueMax1 = valueMax;
                    } else {
                        valueMax1 = valueAvgMax;
                    }
                    CombinedChart chart = (CombinedChart) findViewById(R.id.chartSessionHistoryGraph);
                    chart.setDescription("");
                    chart.setBackgroundColor(Color.WHITE);
                    chart.setDrawGridBackground(false);
                    chart.setDrawBarShadow(false);
                    chart.setDrawGridBackground(false);
                    chart.setDrawGridBackground(false);
                    chart.setDrawOrder(new DrawOrder[]{DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.CANDLE, DrawOrder.LINE, DrawOrder.SCATTER});
                    YAxis rightAxis = chart.getAxisRight();
                    rightAxis.setDrawGridLines(false);
                    rightAxis.setAxisMaxValue((float) (valueMax1 + (valueMax1 / 10.0d)));
                    rightAxis.setAxisMinValue((float) Math.abs(valueMin1 - (valueMin1 / 10.0d)));
                    rightAxis.resetAxisMinValue();
                    YAxis leftAxis = chart.getAxisLeft();
                    leftAxis.setDrawGridLines(false);
                    leftAxis.setAxisMaxValue((float) (valueMax1 + (valueMax1 / 10.0d)));
                    leftAxis.setAxisMinValue((float) Math.abs(valueMin1 - (valueMin1 / 10.0d)));
                    leftAxis.resetAxisMinValue();
                    XAxis xAxis = chart.getXAxis();
                    xAxis.setPosition(XAxisPosition.BOTH_SIDED);
                    xAxis.setDrawGridLines(false);
                    CombinedData combinedData = new CombinedData(battleList);
                    combinedData.setData(generateLineData());
                    combinedData.setData(generateBarData());
                    chart.setClickable(false);
                    chart.setData(combinedData);
                    chart.animateY(2000);
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        } else {
            Toast.makeText(this, getString(R.string.no_select), Toast.LENGTH_SHORT).show();
        }
    }

    private LineData generateLineData() {
        LineData d = new LineData();
        LineDataSet set = new LineDataSet(mEntriesValueAvg, getResources().getString(R.string.session_history_text_average));
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2.5f);
        set.setCircleColor(ColorTemplate.getHoloBlue());
        set.setCircleRadius(5.0f);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setDrawCubic(false);
        set.setDrawValues(true);
        set.setDrawHighlightIndicators(false);
        set.setValueTextSize(10.0f);
        set.setValueTextColor(ColorTemplate.getHoloBlue());
        set.setAxisDependency(AxisDependency.LEFT);
        d.addDataSet(set);
        return d;
    }

    private BarData generateBarData() {
        BarData d = new BarData();
        BarDataSet set = new BarDataSet(mEntriesValue, getResources().getString(R.string.session_history_text_bysession));
        set.setValueTextColor(0xFF444444);
        set.setValueTextSize(10.0f);
        set.setColors(mColors);
        d.addDataSet(set);
        set.setAxisDependency(AxisDependency.LEFT);
        return d;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id != android.R.id.home) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
