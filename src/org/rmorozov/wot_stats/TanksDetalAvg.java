package org.rmorozov.wot_stats;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TanksDetalAvg extends Fragment {
    private ArrayList<String> mListValue;
    private ArrayList<String> mListValueAvg;
    private ArrayList<String> mListValueDelta;
    private ArrayList<String> mListValueName;
    DatabaseHelper dbHelper;
    SQLiteDatabase sdb;

    public class MyArrayAdapter extends ArrayAdapter<String> {
        private Context context;
        protected ListView mListView;
        private TextView tipoTextValue;
        private TextView tipoTextValueAvg;
        private TextView tipoTextValueDelta;
        private TextView tipoTextValueName;

        public MyArrayAdapter(Context context, ListView listView, List<String> values) {
            super(context, R.layout.simple_list_avg, values);
            this.tipoTextValueName = null;
            this.tipoTextValue = null;
            this.tipoTextValueAvg = null;
            this.tipoTextValueDelta = null;
            this.context = context;
            this.mListView = listView;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.simple_list_avg, parent, false);
            tipoTextValueName = (TextView) view.findViewById(R.id.textViewValueName);
            tipoTextValue = (TextView) view.findViewById(R.id.textViewAvgValue);
            tipoTextValueAvg = (TextView) view.findViewById(R.id.textViewAvg);
            tipoTextValueDelta = (TextView) view.findViewById(R.id.textViewDelta);
            tipoTextValueName.setText(mListValueName.get(position));
            tipoTextValueAvg.setText(mListValue.get(position));
            tipoTextValue.setText(mListValueAvg.get(position));
            tipoTextValueDelta.setText(mListValueDelta.get(position));
            setLableColor(tipoTextValueDelta);
            return view;
        }

        private void setLableColor(TextView textView) {
            if (textView.getText().toString().lastIndexOf("-") == -1) {
                textView.setTextColor(getResources().getColor(R.color.plus));
                textView.setText("+" + textView.getText());
                return;
            }
            textView.setTextColor(getResources().getColor(R.color.minus));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tanks_detal_tab_avg, container, false);
        dbHelper = DatabaseHelper.createDatabaseHelper(view.getContext());
        sdb = dbHelper.getWritableDatabase();
        mListValueName = new ArrayList<>();
        mListValue = new ArrayList<>();
        mListValueAvg = new ArrayList<>();
        mListValueDelta = new ArrayList<>();
        mListValueName.add(getString(R.string.tanks_avg_valuename1));
        mListValueName.add(getString(R.string.tanks_avg_valuename2));
        mListValueName.add(getString(R.string.tanks_avg_valuename3));
        mListValueName.add(getString(R.string.tanks_avg_valuename4));
        mListValueName.add(getString(R.string.tanks_avg_valuename5));
        String selectQuery = "SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER +
                " WHERE " + DatabaseHelper.ACTIVE + " = 1";
        try (Cursor cursor = sdb.rawQuery(selectQuery, null)) {
            if (cursor.moveToFirst()) {
                try (Cursor cursorTank = sdb.rawQuery("SELECT " + DatabaseHelper.TANK_ID_COLUMN +
                        ", " + DatabaseHelper.PLAYER_ID_COLUMN +
                        ", " + DatabaseHelper.NAME_RU +
                        ", " + DatabaseHelper.NAME_EU +
                        ", " + DatabaseHelper.TANK_LEVEL +
                        ", " + DatabaseHelper.TANK_TYPE +
                        ", " + DatabaseHelper.TANK_NATION +
                        ", " + DatabaseHelper.MAX_XP +
                        ", " + DatabaseHelper.MAX_FRAG +
                        ", " + DatabaseHelper.MARK_OF_MASTER +
                        ", " + DatabaseHelper.IN_GAR +
                        ", " + DatabaseHelper.BATTLES_COLUMN +
                        ", " + DatabaseHelper.WINS_COLUMN +
                        ", " + DatabaseHelper.DAMAGE_COLUMN +
                        ", " + DatabaseHelper.KILLS_COLUMN +
                        ", " + DatabaseHelper.SPOT_COLUMN +
                        ", " + DatabaseHelper.CAPS_COLUMN +
                        ", " + DatabaseHelper.DROP_CAPS +
                        ", " + DatabaseHelper.EXP_COLUMN +
                        ", " + DatabaseHelper.WN6_COLUMN +
                        ", " + DatabaseHelper.REFF_COLUMN +
                        ", " + DatabaseHelper.WN8_COLUMN +
                        ", " + DatabaseHelper.BATTLES_ALL +
                        " FROM " + DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST +
                        " WHERE " + DatabaseHelper.TANK_ID_COLUMN + " = \'" + TanksDetailActivity.mTankId + "\'" +
                        " AND " + DatabaseHelper.PLAYER_ID_COLUMN + " = \'" +
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYER_ID_COLUMN)) + "\'", null)) {

                    if (cursorTank.moveToFirst()) {
                        mListValue.add(String.format("%.2f", cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.WINS_COLUMN))));
                        mListValue.add(String.format("%d", (long) cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN))));
                        mListValue.add(String.format("%.2f", cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.KILLS_COLUMN))));
                        mListValue.add(String.format("%.2f", cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.SPOT_COLUMN))));
                        mListValue.add(String.format("%.2f", cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.DROP_CAPS))));
                        Cursor cursorWN8 = sdb.rawQuery("SELECT " + DatabaseHelper.TANK_ID_COLUMN +
                                ", " + DatabaseHelper.TANK_NAME_COLUMN +
                                ", " + DatabaseHelper.LEVEL_COLUMN +
                                ", " + DatabaseHelper.DAMAGE_COLUMN +
                                ", " + DatabaseHelper.WINS_COLUMN +
                                ", " + DatabaseHelper.SPOT_COLUMN +
                                ", " + DatabaseHelper.KILLS_COLUMN +
                                ", " + DatabaseHelper.EXP_DEF_COLUMN +
                                " FROM " + DatabaseHelper.DATABASE_TABLE_TANKS_EXP +
                                " WHERE " + DatabaseHelper.TANK_ID_COLUMN + " = \'" + TanksDetailActivity.mTankId + "\'", null);
                        cursorWN8.moveToFirst();
                        if (cursorWN8.getCount() > 0) {
                            mListValueAvg.add(String.format("%.2f", cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.WINS_COLUMN))));
                            mListValueAvg.add(String.format("%d", (long) cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN))));
                            mListValueAvg.add(String.format("%.2f", cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.KILLS_COLUMN))));
                            mListValueAvg.add(String.format("%.2f", cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.SPOT_COLUMN))));
                            mListValueAvg.add(String.format("%.2f", cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.EXP_DEF_COLUMN))));
                            double expDamage = cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN)) -
                                    cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN));
                            double expSpot = cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.SPOT_COLUMN)) -
                                    cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.SPOT_COLUMN));
                            double expFrag = cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.KILLS_COLUMN)) -
                                    cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.KILLS_COLUMN));
                            double expDef = cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.DROP_CAPS)) -
                                    cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.EXP_DEF_COLUMN));
                            double expWinRate = cursorTank.getDouble(cursorTank.getColumnIndex(DatabaseHelper.WINS_COLUMN)) -
                                    cursorWN8.getDouble(cursorWN8.getColumnIndex(DatabaseHelper.WINS_COLUMN));
                            mListValueDelta.add(String.format("%.2f", expWinRate));
                            mListValueDelta.add(String.format("%d", (long) expDamage));
                            mListValueDelta.add(String.format("%.2f", expFrag));
                            mListValueDelta.add(String.format("%.2f", expSpot));
                            mListValueDelta.add(String.format("%.2f", expDef));
                        } else {
                            mListValueAvg.add(String.format("%.2f", 0.0));
                            mListValueAvg.add(String.format("%d", 0));
                            mListValueAvg.add(String.format("%.2f", 0.0));
                            mListValueAvg.add(String.format("%.2f", 0.0));
                            mListValueAvg.add(String.format("%.2f", 0.0));
                            mListValueDelta.add(String.format("%.2f", 0.0));
                            mListValueDelta.add(String.format("%d", 0));
                            mListValueDelta.add(String.format("%.2f", 0.0));
                            mListValueDelta.add(String.format("%.2f", 0.0));
                            mListValueDelta.add(String.format("%.2f", 0.0));
                        }
                        cursorWN8.close();
                    }
                }
                ListView listView = (ListView) view.findViewById(R.id.listViewAvgMain);
                MyArrayAdapter arrayAdapter = new MyArrayAdapter(view.getContext(), listView, mListValueName);
                listView.setAdapter(arrayAdapter);
            } else {
                view = null;
            }
        }
        return view;
    }
}
