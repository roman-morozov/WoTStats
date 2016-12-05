package org.rmorozov.wot_stats;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import org.rmorozov.wot_stats.listviewitems.BarChartItem;
import org.rmorozov.wot_stats.listviewitems.ChartItem;
import org.rmorozov.wot_stats.listviewitems.PieChartItem;

import java.util.ArrayList;
import java.util.List;

public class Infographics extends Fragment {
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sdb;

    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {
        public ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(position, convertView, getContext());
        }

        public int getItemViewType(int position) {
            return getItem(position).getItemType();
        }

        public int getViewTypeCount() {
            return 3;
        }
    }

    private BarData getAvgBarData(String column, String playerId) {
        BarDataSet barDataSet = new BarDataSet(getEntries(column, playerId), "");
        barDataSet.setBarSpacePercent(20.0f);
        barDataSet.setHighLightAlpha(255);
        barDataSet.setColors(getColors());
        return new BarData(getLevel(), barDataSet);
    }

    private ArrayList<BarEntry> getEntries(String column, String playerId) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long valueY;
            try (Cursor cursor = sdb.rawQuery("SELECT " + DatabaseHelper.BATTLES_COLUMN +
                    ", " + column +
                    ", " + DatabaseHelper.TANK_LEVEL +
                    " FROM " + DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST +
                    " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerId + "\' AND " +
                    DatabaseHelper.TANK_LEVEL + " = \'" + (i + 1) + "\'" +
                    " ORDER BY " + DatabaseHelper.TANK_LEVEL, null)) {
                if (cursor.moveToFirst()) {
                    double dblSumValue = 0.0;
                    double dblSumBattles = 0.0;
                    for (int j = 0; j < cursor.getCount(); j++) {
                        double tmp1 = cursor.getDouble(cursor.getColumnIndex(column));
                        double tmp2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN));
                        dblSumValue += tmp1 * tmp2;
                        dblSumBattles += cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN));
                        cursor.moveToNext();
                    }
                    valueY = (long) (dblSumValue / dblSumBattles);
                } else {
                    valueY = 0;
                }
                entries.add(new BarEntry(valueY, i));
            }
        }
        return entries;
    }

    private BarData countTanksByLevel(String playerId) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long valueY;
            try (Cursor cursor = sdb.rawQuery("SELECT SUM(" + DatabaseHelper.BATTLES_COLUMN +
                    ") AS SUM_BATTLES, " + DatabaseHelper.TANK_LEVEL +
                    " FROM " + DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST +
                    " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerId + "\' AND " +
                    DatabaseHelper.TANK_LEVEL + " = \'" + (i + 1) + "\'" +
                    " GROUP BY " + DatabaseHelper.TANK_LEVEL +
                    " ORDER BY " + DatabaseHelper.TANK_LEVEL, null)) {
                if (cursor.moveToFirst()) {
                    valueY = cursor.getLong(cursor.getColumnIndex("SUM_BATTLES"));
                } else {
                    valueY = 0;
                }
                entries.add(new BarEntry(valueY, i));
            }
        }
        BarDataSet d = new BarDataSet(entries, "");
        d.setBarSpacePercent(20.0f);
        d.setHighLightAlpha(255);
        d.setColors(getColors());
        return new BarData(getLevel(), d);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
    }

    public void onStart() {
        super.onStart();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewHierarchy = inflater.inflate(R.layout.infographics, container, false);
        sdb = dbHelper.getReadableDatabase();
        String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
        String[] strArr = new String[3];
        strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
        strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
        strArr[2] = DatabaseHelper.ACTIVE;
        StringBuilder stringBuilder = new StringBuilder();
        Cursor main_player_cursor = sdb.query(str, strArr, stringBuilder.append(DatabaseHelper.ACTIVE).append("= ?").toString(), new String[]{"1"}, null, null, null);
        if (main_player_cursor.moveToFirst()) {
            String player_id = main_player_cursor.getString(1);
            main_player_cursor.close();
            ListView lv = (ListView) viewHierarchy.findViewById(R.id.listViewInfographics);
            ArrayList<ChartItem> list = new ArrayList<>();
            list.add(new BarChartItem(countTanksByLevel(player_id), viewHierarchy.getContext()));
            list.add(new BarChartItem(getAvgBarData(DatabaseHelper.WINS_COLUMN, player_id), viewHierarchy.getContext()));
            list.add(new BarChartItem(getAvgBarData(DatabaseHelper.DAMAGE_COLUMN, player_id), viewHierarchy.getContext()));
            list.add(new BarChartItem(getAvgBarData(DatabaseHelper.EXP_COLUMN, player_id), viewHierarchy.getContext()));
            list.add(new BarChartItem(getAvgBarData(DatabaseHelper.WN8_COLUMN, player_id), viewHierarchy.getContext()));
            list.add(new PieChartItem(countTanksByNations(player_id), viewHierarchy.getContext()));
            list.add(new PieChartItem(countTanksByClass(player_id), viewHierarchy.getContext()));
            lv.setAdapter(new ChartDataAdapter(viewHierarchy.getContext(), list));
            lv.setOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });
            return viewHierarchy;
        }
        Toast.makeText(getActivity(), getString(R.string.no_select), Toast.LENGTH_SHORT).show();
        return null;
    }

    private PieData countTanksByNations(String player_id) {
        ArrayList<Entry> entries = new ArrayList<>();
        List<String> valueX = new ArrayList<>();
        StringBuilder append = new StringBuilder().append("select  SUM(");
        append = append.append(DatabaseHelper.BATTLES_COLUMN).append(") AS SUM_BATTLES, ");
        append = append.append(DatabaseHelper.TANK_NATION).append(" ").append(" from ");
        append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST).append(" where ");
        append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("'").append(" GROUP BY ");
        append = append.append(DatabaseHelper.TANK_NATION).append(" ").append(" ORDER BY ");
        try (Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.TANK_NATION).append(" ").toString(), null)) {
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    valueX.add(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NATION)));
                    entries.add(new BarEntry(cursor.getLong(cursor.getColumnIndex("SUM_BATTLES")), i));
                    cursor.moveToNext();
                }
            }
            cursor.close();
            PieDataSet d = new PieDataSet(entries, "");
            d.setSliceSpace(5.0f);
            d.setColors(getColors());
            return new PieData(valueX, d);
        }
    }

    private PieData countTanksByClass(String player_id) {
        ArrayList<Entry> entries = new ArrayList<>();
        List<String> valueX = new ArrayList<>();
        StringBuilder append = new StringBuilder().append("select  SUM(");
        append = append.append(DatabaseHelper.BATTLES_COLUMN).append(") AS SUM_BATTLES, ");
        append = append.append(DatabaseHelper.TANK_TYPE).append(" ").append(" from ");
        append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST).append(" where ");
        append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("'").append(" GROUP BY ");
        append = append.append(DatabaseHelper.TANK_TYPE).append(" ").append(" ORDER BY ");
        Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.TANK_TYPE).append(" ").toString(), null);
        try {
            if (cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    String tankClass = getResources().getString(R.string.classes1);
                    if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE)).equals("heavyTank")) {
                        tankClass = getResources().getString(R.string.classes1);
                    }
                    if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE)).equals("mediumTank")) {
                        tankClass = getResources().getString(R.string.classes2);
                    }
                    if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE)).equals("SPG")) {
                        tankClass = getResources().getString(R.string.classes3);
                    }
                    if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE)).equals("AT-SPG")) {
                        tankClass = getResources().getString(R.string.classes4);
                    }
                    if (cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE)).equals("lightTank")) {
                        tankClass = getResources().getString(R.string.classes5);
                    }
                    valueX.add(tankClass);
                    entries.add(new BarEntry(cursor.getLong(cursor.getColumnIndex("SUM_BATTLES")), i));
                    cursor.moveToNext();
                }
            }
            cursor.close();
            PieDataSet d = new PieDataSet(entries, "");
            d.setSliceSpace(5.0f);
            d.setColors(getColors());
            return new PieData(valueX, d);
        } catch (Throwable th) {
            cursor.close();
        }
        return null;
    }

    private ArrayList<String> getLevel() {
        ArrayList<String> m = new ArrayList<>();
        m.add("I");
        m.add("II");
        m.add("III");
        m.add("IV");
        m.add("V");
        m.add("VI");
        m.add("VII");
        m.add("VIII");
        m.add("IX");
        m.add("X");
        return m;
    }

    private ArrayList<Integer> getColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.JOYFUL_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.COLORFUL_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.LIBERTY_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.PASTEL_COLORS) {
            colors.add(color);
        }
        colors.add(ColorTemplate.getHoloBlue());
        return colors;
    }
}
