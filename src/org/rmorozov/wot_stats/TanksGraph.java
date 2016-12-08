package org.rmorozov.wot_stats;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TabHost.TabSpec;

import java.util.ArrayList;

public class TanksGraph extends Fragment {
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_COUNTER = "battle_count";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    DatabaseHelper dbHelper;
    SharedPreferences mSettings;
    public View mViewHierarchy;

    @SuppressWarnings("SameParameterValue")
    public static TanksGraph newInstance(String param1, String param2) {
        TanksGraph fragment = new TanksGraph();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
    }

    public void onPause() {
        super.onStop();
        Spinner spinner = (Spinner) mViewHierarchy.findViewById(R.id.spinnerBattleCount);
        String[] choose = getResources().getStringArray(R.array.battle_count);
        Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_COUNTER, choose[spinner.getSelectedItemPosition()]);
        editor.apply();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewHierarchy = inflater.inflate(R.layout.fragment_tanks_graph, container, false);
        mViewHierarchy = viewHierarchy;
        TabHost tabs = (TabHost) viewHierarchy;
        Spinner spinner = (Spinner) viewHierarchy.findViewById(R.id.spinnerBattleCount);
        String[] choose = getResources().getStringArray(R.array.battle_count);
        Context context = mViewHierarchy.getContext();
        String str = APP_PREFERENCES;
        mViewHierarchy.getContext();
        mSettings = context.getSharedPreferences(str, 0);
        if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            int i = 0;
            while (i < choose.length && !choose[i].equals(mSettings.getString(APP_PREFERENCES_COUNTER, ""))) {
                i++;
            }
            spinner.setSelection(i);
        }
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] choose = getResources().getStringArray(R.array.battle_count);
                refreshGraph(1, mViewHierarchy.findViewById(R.id.GraphTanks1), choose[position]);
                refreshGraph(2, mViewHierarchy.findViewById(R.id.GraphTanks2), choose[position]);
                refreshGraph(3, mViewHierarchy.findViewById(R.id.GraphTanks3), choose[position]);
                refreshGraph(4, mViewHierarchy.findViewById(R.id.GraphTanks4), choose[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        tabs.setup();
        TabSpec spec = tabs.newTabSpec("tag1");
        View tabIndicator = LayoutInflater.from(getActivity()).inflate(R.layout.apptheme_tab_indicator_holo, tabs.getTabWidget(), false);
        TextView title = (TextView) tabIndicator.findViewById(android.R.id.title);
        title.setText(getString(R.string.ttl_tanks_gr_tab1));
        title.setTextSize(11.0f);
        spec.setContent(R.id.tab1t);
        spec.setIndicator(tabIndicator);
        tabs.addTab(spec);
        spec = tabs.newTabSpec("tag2");
        tabIndicator = LayoutInflater.from(getActivity()).inflate(R.layout.apptheme_tab_indicator_holo, tabs.getTabWidget(), false);
        title = (TextView) tabIndicator.findViewById(android.R.id.title);
        title.setText(getString(R.string.ttl_tanks_gr_tab2));
        title.setTextSize(11.0f);
        spec.setContent(R.id.tab2t);
        spec.setIndicator(tabIndicator);
        tabs.addTab(spec);
        spec = tabs.newTabSpec("tag3");
        tabIndicator = LayoutInflater.from(getActivity()).inflate(R.layout.apptheme_tab_indicator_holo, tabs.getTabWidget(), false);
        title = (TextView) tabIndicator.findViewById(android.R.id.title);
        title.setText(getString(R.string.ttl_tanks_gr_tab3));
        title.setTextSize(11.0f);
        spec.setContent(R.id.tab3t);
        spec.setIndicator(tabIndicator);
        tabs.addTab(spec);
        spec = tabs.newTabSpec("tag4");
        tabIndicator = LayoutInflater.from(getActivity()).inflate(R.layout.apptheme_tab_indicator_holo, tabs.getTabWidget(), false);
        title = (TextView) tabIndicator.findViewById(android.R.id.title);
        title.setText(getString(R.string.ttl_tanks_gr_tab4));
        title.setTextSize(11.0f);
        spec.setContent(R.id.tab4t);
        spec.setIndicator(tabIndicator);
        tabs.addTab(spec);
        tabs.setCurrentTab(0);
        return viewHierarchy;
    }

    public void refreshGraph(int GraphType, View view, String BattleCount) {
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
            String order_by = DatabaseHelper.BATTLES_COLUMN;
            if (GraphType == 1) {
                order_by = DatabaseHelper.DAMAGE_COLUMN;
            }
            if (GraphType == 2) {
                order_by = DatabaseHelper.EXP_COLUMN;
            }
            if (GraphType == 3) {
                order_by = DatabaseHelper.WINS_COLUMN;
            }
            if (GraphType == 4) {
                order_by = DatabaseHelper.WN8_COLUMN;
            }
            StringBuilder append = new StringBuilder().append("select ");
            append = append.append(DatabaseHelper.NAME_RU).append(", ");
            append = append.append(DatabaseHelper.WINS_COLUMN).append(", ");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(", ");
            append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(", ");
            append = append.append(DatabaseHelper.EXP_COLUMN).append(", ");
            append = append.append(DatabaseHelper.WN8_COLUMN).append(" from ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST).append(" where ");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' AND ");
            Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.BATTLES_COLUMN).append(" >= ").append("'").append(BattleCount).append("'").append(" ORDER BY ").append(order_by).append(" DESC").toString(), null);
            cursor.moveToFirst();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = (int) (((double) (cursor.getCount() * getResources().getDisplayMetrics().widthPixels)) / 4.2d);
            view.setLayoutParams(layoutParams);
            if (cursor.getCount() > 0) {
                Resources resources = getResources();
                ArrayList<Bar> aBars = new ArrayList<>();
                for (int i = 0; i < cursor.getCount(); i++) {
                    Bar bar = new Bar();
                    switch (GraphType) {
                        case 1:
                            bar.setColor(resources.getColor(R.color.green_light));
                            bar.setLabelColor(resources.getColor(R.color.abc_search_url_text));
                            bar.setSelectedColor(resources.getColor(R.color.transparent_orange));
                            bar.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_RU)));
                            bar.setValue((float) cursor.getLong(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN)));
                            append = new StringBuilder().append("");
                            bar.setValueString(append.append(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN))).toString());
                            aBars.add(bar);
                            break;
                        case 2:
                            bar.setColor(resources.getColor(R.color.orange));
                            bar.setLabelColor(resources.getColor(R.color.abc_search_url_text));
                            bar.setSelectedColor(resources.getColor(R.color.transparent_orange));
                            bar.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_RU)));
                            bar.setValue((float) cursor.getLong(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN)));
                            append = new StringBuilder().append("");
                            bar.setValueString(append.append(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN))).toString());
                            aBars.add(bar);
                            break;
                        case 3:
                            bar.setColor(resources.getColor(R.color.purple));
                            bar.setLabelColor(resources.getColor(R.color.abc_search_url_text));
                            bar.setSelectedColor(resources.getColor(R.color.green_light));
                            bar.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_RU)));
                            bar.setValue((float) cursor.getLong(cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN)));
                            append = new StringBuilder().append("");
                            bar.setValueString(append.append(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN))).append("%").toString());
                            aBars.add(bar);
                            break;
                        case 4:
                            bar.setColor(resources.getColor(R.color.transparent_blue));
                            bar.setLabelColor(resources.getColor(R.color.abc_search_url_text));
                            bar.setSelectedColor(resources.getColor(R.color.transparent_blue));
                            bar.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_RU)));
                            bar.setValue((float) cursor.getLong(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN)));
                            append = new StringBuilder().append("");
                            bar.setValueString(append.append(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN))).toString());
                            aBars.add(bar);
                            break;
                        default:
                            break;
                    }
                    cursor.moveToNext();
                }
                cursor.close();
                ((BarGraph) view).setBars(aBars);
                return;
            }
            return;
        }
        Toast.makeText(getActivity(), getString(R.string.no_select), Toast.LENGTH_SHORT).show();
    }

}
