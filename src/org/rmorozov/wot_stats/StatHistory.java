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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class StatHistory extends Fragment {
    DatabaseHelper dbHelper;
    private ArrayList<String> it_battles1;
    private ArrayList<String> it_battles2;
    private ArrayList<String> it_dmg1;
    private ArrayList<String> it_dmg2;
    private ArrayList<String> it_er1;
    private ArrayList<String> it_er2;
    private ArrayList<String> it_exp1;
    private ArrayList<String> it_exp2;
    private ArrayList<String> it_last_battle;
    private ArrayList<String> it_nomer;
    private ArrayList<String> it_wins1;
    private ArrayList<String> it_wins2;
    private ArrayList<String> it_wn61;
    private ArrayList<String> it_wn62;
    private ArrayList<String> it_wn81;
    private ArrayList<String> it_wn82;

    public StatHistory() {
    }

    public class MyArrayAdapter extends ArrayAdapter<String> {
        private Context context;
        protected ListView mListView;
        private TextView tipoBattles1;
        private TextView tipoBattles2;
        private TextView tipoDmg1;
        private TextView tipoDmg2;
        private TextView tipoErr1;
        private TextView tipoErr2;
        private TextView tipoExp1;
        private TextView tipoExp2;
        private TextView tipoLastBattle;
        private TextView tipoNomer;
        private TextView tipoWins1;
        private TextView tipoWins2;
        private TextView tipoWn61;
        private TextView tipoWn62;
        private TextView tipoWn81;
        private TextView tipoWn82;

        public MyArrayAdapter(Context context, ListView listView, List<String> values) {
            super(context, R.layout.simple_list_comp_item, values);
            tipoNomer = null;
            tipoBattles1 = null;
            tipoBattles2 = null;
            tipoWins1 = null;
            tipoWins2 = null;
            tipoWn81 = null;
            tipoWn82 = null;
            tipoWn61 = null;
            tipoWn62 = null;
            tipoErr1 = null;
            tipoErr2 = null;
            tipoDmg1 = null;
            tipoDmg2 = null;
            tipoExp1 = null;
            tipoExp2 = null;
            tipoLastBattle = null;
            this.context = context;
            mListView = listView;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.stat_history_item, parent, false);
            tipoNomer = (TextView) view.findViewById(R.id.it_nomer);
            tipoBattles1 = (TextView) view.findViewById(R.id.it_battles1);
            tipoBattles2 = (TextView) view.findViewById(R.id.it_battles2);
            tipoWins1 = (TextView) view.findViewById(R.id.it_wins1);
            tipoWins2 = (TextView) view.findViewById(R.id.it_wins2);
            tipoWn81 = (TextView) view.findViewById(R.id.it_wn81);
            tipoWn82 = (TextView) view.findViewById(R.id.it_wn82);
            tipoWn61 = (TextView) view.findViewById(R.id.it_wn61);
            tipoWn62 = (TextView) view.findViewById(R.id.it_wn62);
            tipoErr1 = (TextView) view.findViewById(R.id.it_er1);
            tipoErr2 = (TextView) view.findViewById(R.id.it_er2);
            tipoDmg1 = (TextView) view.findViewById(R.id.it_dmg1);
            tipoDmg2 = (TextView) view.findViewById(R.id.it_dmg2);
            tipoExp1 = (TextView) view.findViewById(R.id.it_exp1);
            tipoExp2 = (TextView) view.findViewById(R.id.it_exp2);
            tipoLastBattle = (TextView) view.findViewById(R.id.it_lastnattle1);
            tipoNomer.setText(it_nomer.get(position));
            tipoBattles1.setText(it_battles1.get(position));
            tipoBattles2.setText(it_battles2.get(position));
            tipoWins1.setText(String.format("%.2f", Float.parseFloat(it_wins1.get(position))));
            tipoWins2.setText(it_wins2.get(position));
            if (tipoWins2.getText().toString().lastIndexOf("-") == -1) {
                tipoWins2.setTextColor(getResources().getColor(R.color.olen_green));
            } else {
                tipoWins2.setTextColor(getResources().getColor(R.color.olen_red));
            }
            tipoWn81.setText(String.format("%.2f", Float.parseFloat(it_wn81.get(position))));
            tipoWn82.setText(it_wn82.get(position));
            if (tipoWn82.getText().toString().lastIndexOf("-") == -1) {
                tipoWn82.setTextColor(getResources().getColor(R.color.olen_green));
            } else {
                tipoWn82.setTextColor(getResources().getColor(R.color.olen_red));
            }
            tipoWn61.setText(String.format("%.2f", Float.parseFloat(it_wn61.get(position))));
            tipoWn62.setText(it_wn62.get(position));
            if (tipoWn62.getText().toString().lastIndexOf("-") == -1) {
                tipoWn62.setTextColor(getResources().getColor(R.color.olen_green));
            } else {
                tipoWn62.setTextColor(getResources().getColor(R.color.olen_red));
            }
            tipoErr1.setText(String.format("%.2f", Float.parseFloat(it_er1.get(position))));
            tipoErr2.setText(it_er2.get(position));
            if (tipoErr2.getText().toString().lastIndexOf("-") == -1) {
                tipoErr2.setTextColor(getResources().getColor(R.color.olen_green));
            } else {
                tipoErr2.setTextColor(getResources().getColor(R.color.olen_red));
            }
            tipoDmg1.setText(String.format("%.2f", Float.parseFloat(it_dmg1.get(position))));
            tipoDmg2.setText(it_dmg2.get(position));
            if (tipoDmg2.getText().toString().lastIndexOf("-") == -1) {
                tipoDmg2.setTextColor(getResources().getColor(R.color.olen_green));
            } else {
                tipoDmg2.setTextColor(getResources().getColor(R.color.olen_red));
            }
            tipoExp1.setText(String.format("%.2f", Float.parseFloat(it_exp1.get(position))));
            tipoExp2.setText(it_exp2.get(position));
            if (tipoExp2.getText().toString().lastIndexOf("-") == -1) {
                tipoExp2.setTextColor(getResources().getColor(R.color.olen_green));
            } else {
                tipoExp2.setTextColor(getResources().getColor(R.color.olen_red));
            }
            tipoLastBattle.setText(it_last_battle.get(position));
            return view;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
    }

    public void onStart() {
        super.onStart();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewHierarchy = inflater.inflate(R.layout.stat_history, container, false);
        ListView listView = (ListView) viewHierarchy.findViewById(R.id.listViewStatHistory);
        it_nomer = new ArrayList<>();
        it_battles1 = new ArrayList<>();
        it_battles2 = new ArrayList<>();
        it_wins1 = new ArrayList<>();
        it_wins2 = new ArrayList<>();
        it_wn81 = new ArrayList<>();
        it_wn82 = new ArrayList<>();
        it_wn61 = new ArrayList<>();
        it_wn62 = new ArrayList<>();
        it_er1 = new ArrayList<>();
        it_er2 = new ArrayList<>();
        it_dmg1 = new ArrayList<>();
        it_dmg2 = new ArrayList<>();
        it_exp1 = new ArrayList<>();
        it_exp2 = new ArrayList<>();
        it_last_battle = new ArrayList<>();
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        SQLiteDatabase sQLiteDatabase = sdb;
        String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
        String[] strArr = new String[3];
        strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
        strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
        strArr[2] = DatabaseHelper.ACTIVE;
        StringBuilder stringBuilder = new StringBuilder();
        Cursor main_player_cursor = sQLiteDatabase.query(str, strArr, stringBuilder.append(DatabaseHelper.ACTIVE).append("= ?").toString(), new String[]{"1"}, null, null, null);
        if (main_player_cursor.moveToFirst()) {
            String player_id = main_player_cursor.getString(1);
            main_player_cursor.close();
            sQLiteDatabase = sdb;
            StringBuilder append = new StringBuilder().append("select *  from ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_SH).append(" where ");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' order by ");
            Cursor cursor = sQLiteDatabase.rawQuery(append.append(DatabaseHelper.BATTLES_COLUMN).append(" DESC").toString(), null);
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                it_nomer.add(i, Integer.toString(i + 1));
                it_battles1.add(i, cursor.getString(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN)));
                it_wins1.add(i, cursor.getString(cursor.getColumnIndex(DatabaseHelper.WINRATE_COLUMN)));
                it_wn81.add(i, cursor.getString(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN)));
                it_wn61.add(i, cursor.getString(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN)));
                it_er1.add(i, cursor.getString(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN)));
                it_dmg1.add(i, cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN)));
                it_exp1.add(i, cursor.getString(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN)));
                it_last_battle.add(i, cursor.getString(cursor.getColumnIndex(DatabaseHelper.LAST_BATTLE_COLUMN)));
                cursor.moveToNext();
            }
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                if (i != cursor.getCount() - 1) {
                    it_battles2.add(i, Long.toString(Long.parseLong(cursor.getString(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN))) - Long.parseLong(it_battles1.get(i + 1))));
                    float temp = Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WINRATE_COLUMN))) - Float.parseFloat(it_wins1.get(i + 1));
                    it_wins2.add(i, String.format("%.2f", temp));
                    temp = Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN))) - Float.parseFloat(it_wn81.get(i + 1));
                    it_wn82.add(i, String.format("%.2f", temp));
                    temp = Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN))) - Float.parseFloat(it_wn61.get(i + 1));
                    it_wn62.add(i, String.format("%.2f", temp));
                    temp = Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN))) - Float.parseFloat(it_er1.get(i + 1));
                    it_er2.add(i, String.format("%.2f", temp));
                    temp = Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN))) - Float.parseFloat(it_dmg1.get(i + 1));
                    it_dmg2.add(i, String.format("%.2f", temp));
                    temp = Float.parseFloat(cursor.getString(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN))) - Float.parseFloat(it_exp1.get(i + 1));
                    it_exp2.add(i, String.format("%.2f", temp));
                } else {
                    it_battles2.add(i, "");
                    it_wins2.add(i, "");
                    it_wn82.add(i, "");
                    it_wn62.add(i, "");
                    it_er2.add(i, "");
                    it_dmg2.add(i, "");
                    it_exp2.add(i, "");
                }
                cursor.moveToNext();
            }
            cursor.close();
            MyArrayAdapter myArrayAdapter = new MyArrayAdapter(getActivity(), listView, it_nomer);
            listView.setAdapter(myArrayAdapter);
            return viewHierarchy;
        }
        Toast.makeText(getActivity(), getString(R.string.no_select), Toast.LENGTH_SHORT).show();
        return null;
    }
}
