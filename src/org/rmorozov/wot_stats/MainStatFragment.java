package org.rmorozov.wot_stats;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost.TabSpec;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


public class MainStatFragment extends Fragment {
    public static final String APP_PREFERENCES_SORT = "sort_field";
    public static String mainPlayer = null;
    private static final String STATDELTA = "textDelta";
    private static final String STATNAME = "textStatName";
    private static final String STATVALUE = "textStatValue";
    private static final String URL_BATTLES_PLAYER = "/wot/account/info/?fields=statistics.all.battles&account_id=";
    private static final String URL_SITE = "http://api.worldoftanks.";
    private static final String URL_STAT_PLAYER = "/wot/account/info/?account_id=";
    private static final String URL_TANKS_LIST = "/wot/encyclopedia/tanks/?application_id=";
    private static final String URL_TANKS_STAT_LIST = "/wot/tanks/stats/?fields=all,account_id,max_xp,max_frags,mark_of_mastery,tank_id&account_id=";
    private String appId;
    private String serverZone;
    private String[] statName;
    private Tank[] tankArray;
    private DatabaseHelper dbHelper;
    private SharedPreferences mSettings;
    private boolean mStatUpdate;
    private boolean mTanksUpdate;
    private View mViewHierarchy;
    private SQLiteDatabase sdb;
    private ArrayList<HashMap<String, Object>> statList;

    public class MyExpandableAdapter extends BaseExpandableListAdapter {
        private ArrayList<String> child;
        private ArrayList<Object> childItems;
        private Context mContext;
        private ArrayList<String> parentItems;

        class ViewHolder {
            ImageView imageViewClass;
            ImageView imageViewMark;
            ImageView imageViewNation;
            ImageView imageViewTanksDetal;
            LinearLayout linearLayoutGroup;
            int position;
            TextView textLevel;
            TextView textStatValueGroup;
            TextView textTankName;
            TextView textViewDeltaB;
            TextView textViewDeltaW;
            TextView textViewTanksId;
            TextView textWins;

            ViewHolder(View convertView) {
                this.linearLayoutGroup = (LinearLayout) convertView.findViewById(R.id.layoutMainGroup);
                this.imageViewTanksDetal = (ImageView) convertView.findViewById(R.id.imageViewTanksDetal);
                this.textViewTanksId = (TextView) convertView.findViewById(R.id.textViewTanksId);
                this.textTankName = (TextView) convertView.findViewById(R.id.textTankName);
                this.textLevel = (TextView) convertView.findViewById(R.id.textLevel);
                this.textStatValueGroup = (TextView) convertView.findViewById(R.id.textStatValueGroup);
                this.textWins = (TextView) convertView.findViewById(R.id.textWins);
                this.textViewDeltaB = (TextView) convertView.findViewById(R.id.textViewDeltaB);
                this.textViewDeltaW = (TextView) convertView.findViewById(R.id.textViewDeltaW);
                this.imageViewClass = (ImageView) convertView.findViewById(R.id.imageViewClass);
                this.imageViewNation = (ImageView) convertView.findViewById(R.id.imageViewNation);
                this.imageViewMark = (ImageView) convertView.findViewById(R.id.imageViewMark);
            }
        }

        public MyExpandableAdapter(ArrayList<String> parents, ArrayList<Object> children, Context context) {
            this.parentItems = parents;
            this.childItems = children;
            this.mContext = context;
        }

        @SuppressWarnings("unchecked")
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            child = (ArrayList<String>) childItems.get(groupPosition);
            if (convertView == null) {
                convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.simple_list_tanks_item, null);
            }
            String[] param_array = child.get(childPosition).split("\\$");
            ((TextView) convertView.findViewById(R.id.textStatName)).setText(param_array[0]);
            TextView textView = (TextView) convertView.findViewById(R.id.textStatValue);
            textView.setText(param_array[1]);
            textView.setTextColor(getResources().getColor(R.color.main_gray));
            if (param_array[0].equals(getResources().getString(R.string.ttl_tanks_stat7))) {
                textView.setTextColor(getColorByWN6(Float.parseFloat(param_array[1].replace(",", "."))));
            }
            if (param_array[0].equals(getResources().getString(R.string.ttl_tanks_stat8))) {
                textView.setTextColor(getColorByEFF(Float.parseFloat(param_array[1].replace(",", "."))));
            }
            if (param_array[0].equals(getResources().getString(R.string.ttl_tanks_stat9))) {
                textView.setTextColor(getColorByWN8(Float.parseFloat(param_array[1].replace(",", "."))));
            }
            textView = (TextView) convertView.findViewById(R.id.textDelta);
            if (param_array[3].equals("0") || param_array[0].equals(getResources().getString(R.string.ttl_tanks_stat4)) || param_array[0].equals(getResources().getString(R.string.ttl_tanks_stat6))) {
                textView.setText("");
            } else {
                textView.setText(param_array[2] + "  ");
                setLableColor(textView);
            }
            return convertView;
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.simple_list_tanks_item_group, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (groupPosition % 2 == 0) {
                holder.linearLayoutGroup.setBackgroundColor(mContext.getResources().getColor(R.color.tanks_list1));
            } else {
                holder.linearLayoutGroup.setBackgroundColor(mContext.getResources().getColor(R.color.tanks_list2));
            }
            holder.imageViewTanksDetal.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mViewHierarchy.getContext(), TanksDetailActivity.class);
                    intent.putExtra(DatabaseHelper.TANK_ID_COLUMN, ((TextView) ((LinearLayout) v.getParent()).getChildAt(2)).getText());
                    startActivity(intent);
                }
            });
            String[] param_array = parentItems.get(groupPosition).split("\\$");
            holder.textViewTanksId.setText(param_array[7]);
            holder.textTankName.setText(param_array[0]);
            holder.textLevel.setText(getLevelString(param_array[5]));
            holder.textStatValueGroup.setText(param_array[1]);
            if (param_array[2].equals("100,00")) {
                param_array[2] = "100,0";
            }
            if (param_array[2].equals("0,00")) {
                param_array[2] = "00,00";
            }
            holder.textWins.setText(param_array[2] + "%");
            if (param_array[8].equals("0")) {
                holder.textViewDeltaB.setText("");
                holder.textViewDeltaW.setText("");
            } else {
                holder.textViewDeltaB.setText(param_array[8]);
                setLableColor(holder.textViewDeltaB);
                holder.textViewDeltaW.setText(param_array[9]);
                setLableColor(holder.textViewDeltaW);
            }
            if (param_array[4].equals("heavyTank")) {
                holder.imageViewClass.setImageResource(R.drawable.tank_ico_tt);
            }
            if (param_array[4].equals("mediumTank")) {
                holder.imageViewClass.setImageResource(R.drawable.tank_ico_st);
            }
            if (param_array[4].equals("SPG")) {
                holder.imageViewClass.setImageResource(R.drawable.tank_ico_ar);
            }
            if (param_array[4].equals("AT-SPG")) {
                holder.imageViewClass.setImageResource(R.drawable.tank_ico_pt);
            }
            if (param_array[4].equals("lightTank")) {
                holder.imageViewClass.setImageResource(R.drawable.tank_ico_lt);
            }
            if (param_array[3].equals("СССР") || param_array[3].equals("U.S.S.R.")) {
                holder.imageViewNation.setImageResource(R.drawable.nation_ussr);
            }
            if (param_array[3].equals("Германия") || param_array[3].equals("Germany")) {
                holder.imageViewNation.setImageResource(R.drawable.nation_ger);
            }
            if (param_array[3].equals("США") || param_array[3].equals("U.S.A.")) {
                holder.imageViewNation.setImageResource(R.drawable.nation_usa);
            }
            if (param_array[3].equals("Франция") || param_array[3].equals("France")) {
                holder.imageViewNation.setImageResource(R.drawable.nation_franc);
            }
            if (param_array[3].equals("Япония") || param_array[3].equals("Japan")) {
                holder.imageViewNation.setImageResource(R.drawable.nation_jap);
            }
            if (param_array[3].equals("Китай") || param_array[3].equals("China")) {
                holder.imageViewNation.setImageResource(R.drawable.nation_china);
            }
            if (param_array[3].equals("Великобритания") || param_array[3].equals("U.K.")) {
                holder.imageViewNation.setImageResource(R.drawable.nation_eng);
            }
            if (param_array[3].equals("Чехословакия") || param_array[3].equals("Czechoslovakia")) {
                holder.imageViewNation.setImageResource(R.drawable.nation_czech);
            }
            if (param_array[6].equals("1")) {
                holder.imageViewMark.setImageResource(R.drawable.mark_3);
            }
            if (param_array[6].equals("2")) {
                holder.imageViewMark.setImageResource(R.drawable.mark_2);
            }
            if (param_array[6].equals("3")) {
                holder.imageViewMark.setImageResource(R.drawable.mark_1);
            }
            if (param_array[6].equals("4")) {
                holder.imageViewMark.setImageResource(R.drawable.mark_4);
            }
            if (param_array[10].equals("true")) {
                holder.imageViewTanksDetal.setImageResource(R.drawable.graph_true);
            } else {
                holder.imageViewTanksDetal.setImageResource(R.drawable.graph_false);
            }
            return convertView;
        }

        String getLevelString(String strLevelArab) {
            switch (Integer.parseInt(strLevelArab)) {
                case 1:
                    return "I";
                case 2:
                    return "II";
                case 3:
                    return "III";
                case 4:
                    return "IV";
                case 5:
                    return "V";
                case 6:
                    return "VI";
                case 7:
                    return "VII";
                case 8:
                    return "VIII";
                case 9:
                    return "IX";
                case 10:
                    return "X";
                default:
                    return "I";
            }
        }

        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        public int getChildrenCount(int groupPosition) {
            return ((ArrayList) childItems.get(groupPosition)).size();
        }

        public Object getGroup(int groupPosition) {
            return null;
        }

        public int getGroupCount() {
            return parentItems.size();
        }

        public void onGroupCollapsed(int groupPosition) {
            super.onGroupCollapsed(groupPosition);
        }

        public void onGroupExpanded(int groupPosition) {
            super.onGroupExpanded(groupPosition);
        }

        public long getGroupId(int groupPosition) {
            return 0;
        }

        public boolean hasStableIds() {
            return false;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    private class PrefetchDataCurrency extends AsyncTask<Void, Void, Void> {
        private String dataname;
        private ProgressDialog progressDialog;

        public class MyArrayAdapter extends ArrayAdapter<String> {
            private Context context;
            private TextView deltaEditText;
            protected ListView mListView;
            private TextView nameEditText;
            private TextView valueEditText;

            public MyArrayAdapter(Context context, ListView listView, List<String> values) {
                super(context, R.layout.simple_list_stat_item, values);
                valueEditText = null;
                nameEditText = null;
                deltaEditText = null;
                this.context = context;
                mListView = listView;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.simple_list_stat_item, parent, false);
                valueEditText = (TextView) view.findViewById(R.id.textStatValue);
                nameEditText = (TextView) view.findViewById(R.id.textStatName);
                deltaEditText = (TextView) view.findViewById(R.id.textStatDelta);
                Object[] resultArray = new Object[]{statList.get(position).get(MainStatFragment.STATVALUE), statList.get(position).get(MainStatFragment.STATNAME), statList.get(position).get(MainStatFragment.STATDELTA)};
                nameEditText.setText(resultArray[1].toString());
                if (resultArray[0] != null) {
                    valueEditText.setText(resultArray[0].toString());
                    if (!resultArray[2].toString().equals("NO_DATA")) {
                        deltaEditText.setText(resultArray[2].toString());
                        setLableColor(deltaEditText);
                    }
                    switch (position) {
                        case 3:
                            valueEditText.setTextColor(getColorByWins(Float.parseFloat(resultArray[0].toString().replace("%", "").replace(",", "."))));
                            break;
                        case 11:
                            valueEditText.setTextColor(getColorByEFF(Float.parseFloat(resultArray[0].toString().replace(",", "."))));
                            break;
                        case 12:
                            valueEditText.setTextColor(getColorByWN6(Float.parseFloat(resultArray[0].toString().replace(",", "."))));
                            break;
                        case 13:
                            valueEditText.setTextColor(getColorByWN8(Float.parseFloat(resultArray[0].toString().replace(",", "."))));
                            break;
                    }
                }
                return view;
            }
        }

        private PrefetchDataCurrency() {
            this.dataname = "data";
        }

        protected void refreshAllTanksList(String ServerZone, String AppId) {
            JSONObject jarrayObj = new JSONParser().getJSONObjFromUrl(MainStatFragment.URL_SITE + ServerZone + MainStatFragment.URL_TANKS_LIST + AppId);
            if (jarrayObj != null) {
                try {
                    JSONObject jarrayObjT = jarrayObj.getJSONObject(dataname);
                    JSONArray jarrayObjTank = jarrayObjT.names();
                    for (int i = 0; i < jarrayObjTank.length(); i++) {
                        JSONObject jobjTankInfo = jarrayObjT.getJSONObject(jarrayObjTank.getString(i));
                        Tank tank = new Tank();
                        tank.nation = jobjTankInfo.getString(DatabaseHelper.TANK_NATION_COLUMN);
                        tank.nation_i18n = jobjTankInfo.getString(DatabaseHelper.TANK_NATION_I18N);
                        tank.name_i18n = jobjTankInfo.getString(DatabaseHelper.TANK_NAME_I18N);
                        tank.level_t = jobjTankInfo.getString(DatabaseHelper.LEVEL_COLUMN);
                        tank.is_premium = jobjTankInfo.getBoolean(DatabaseHelper.TANK_PREMIUM_COLUMN);
                        tank.type = jobjTankInfo.getString(DatabaseHelper.TANK_TYPE_COLUMN);
                        tank.tank_id = jobjTankInfo.getString(DatabaseHelper.TANK_ID_COLUMN);
                        tank.name_t = jobjTankInfo.getString(DatabaseHelper.TANK_NAME_COLUMN);
                    }
                } catch (JSONException ignored) {
                }
            }
        }

        protected Void doInBackground(Void... voids) {
            sdb = dbHelper.getWritableDatabase();
            String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
            String[] strArg = new String[4];
            strArg[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
            strArg[1] = DatabaseHelper.PLAYER_ID_COLUMN;
            strArg[2] = DatabaseHelper.ACTIVE;
            strArg[3] = DatabaseHelper.SERVER;
            Cursor cursor = sdb.query(str, strArg, DatabaseHelper.ACTIVE + " = ?", new String[]{"1"}, null, null, null);
            if (cursor.getCount() > 0) {
                JSONObject jArrayObj;
                JSONObject jObjData;
                JSONObject jObjIdPlayer;
                JSONObject jObjStatistics;
                cursor.moveToFirst();
                ServersChangeHelper sch = new ServersChangeHelper();
                serverZone = sch.GetZoneByServerName(cursor.getString(3));
                appId = sch.GetAppIdByServerName(cursor.getString(3));
                Cursor cursorStat = sdb.rawQuery("SELECT " + DatabaseHelper.BATTLES_COLUMN +
                        " FROM " + DatabaseHelper.DATABASE_TABLE_SH +
                        " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + cursor.getString(1) + "\'" +
                        " ORDER BY " + DatabaseHelper.BATTLES_COLUMN + " DESC", null);
                if (cursorStat.getCount() > 0) {
                    cursorStat.moveToFirst();
                    long mainPlayerBattlesInStat = Long.parseLong(cursorStat.getString(0));
                    cursorStat.close();
                    jArrayObj = new JSONParser().getJSONObjFromUrl(MainStatFragment.URL_SITE + serverZone + MainStatFragment.URL_BATTLES_PLAYER + cursor.getString(1) + "&application_id=" + appId);
                    if (jArrayObj != null) {
                        try {
                            jObjData = jArrayObj.getJSONObject(dataname);
                            jObjIdPlayer = jObjData.getJSONObject(cursor.getString(1));
                            jObjStatistics = jObjIdPlayer.getJSONObject("statistics");
                            if (mainPlayerBattlesInStat == Long.parseLong(jObjStatistics.getJSONObject("all").getString(DatabaseHelper.BATTLES_COLUMN))) {
                                if (sdb.rawQuery("SELECT " + DatabaseHelper.PLAYER_ID_COLUMN +
                                        " FROM " + DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST +
                                        " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN +
                                        " = \'" + cursor.getString(1) + "\'", null).getCount() != 0) {
                                    mStatUpdate = false;
                                    return null;
                                }
                            }
                            mStatUpdate = true;
                        } catch (JSONException ignored) {
                        }
                    }
                }
                jArrayObj = new JSONParser().getJSONObjFromUrl(MainStatFragment.URL_SITE + serverZone + MainStatFragment.URL_STAT_PLAYER + cursor.getString(1) + "&application_id=" + MainStatFragment.this.appId);
                if (jArrayObj != null) {
                    statName = new String[14];
                    try {
                        jObjData = jArrayObj.getJSONObject("data");
                        jObjIdPlayer = jObjData.getJSONObject(cursor.getString(1));
                        jObjStatistics = jObjIdPlayer.getJSONObject("statistics");
                        JSONObject jObjStatisticsAll = jObjStatistics.getJSONObject("all");
                        statName[0] = jObjStatisticsAll.getString(DatabaseHelper.BATTLES_COLUMN);
                        statName[1] = jObjStatisticsAll.getString(DatabaseHelper.WINS_COLUMN);
                        statName[2] = jObjStatisticsAll.getString(DatabaseHelper.LOSSES);
                        statName[3] = jObjStatisticsAll.getString(DatabaseHelper.DAMAGE_DEALT);
                        statName[4] = jObjStatisticsAll.getString(DatabaseHelper.FRAGS);
                        statName[5] = jObjStatisticsAll.getString(DatabaseHelper.SPOTTED);
                        statName[6] = jObjStatisticsAll.getString(DatabaseHelper.CAPTURE_POINTS);
                        statName[7] = jObjStatisticsAll.getString(DatabaseHelper.DROP_CAP);
                        statName[8] = jObjStatisticsAll.getString(DatabaseHelper.XP);
                        statName[9] = jObjIdPlayer.getString("global_rating");
                        statName[10] = jObjIdPlayer.getString("last_battle_time");
                        statName[11] = "0";
                        statName[12] = cursor.getString(1);
                        statName[13] = cursor.getString(0);
                    } catch (JSONException ignored) {
                    }
                }
                refreshPlayerTanksNew(cursor.getString(1), serverZone, appId);
                cursor.close();
            }
            if (mTanksUpdate) {
                refreshAllTanksList(serverZone, appId);
            }
            return null;
        }

        protected void refreshPlayerTanksNew(String playerId, String serverZone, String appId) {
            JSONObject jArrayObj = new JSONParser().getJSONObjFromUrl(MainStatFragment.URL_SITE + serverZone + MainStatFragment.URL_TANKS_STAT_LIST + playerId + "&application_id=" + appId);
            if (jArrayObj != null) {
                try {
                    JSONArray jObjIdPlayer = jArrayObj.getJSONObject(dataname).getJSONArray(playerId);
                    tankArray = new Tank[jObjIdPlayer.length()];
                    for (int i = 0; i < jObjIdPlayer.length(); i++) {
                        JSONObject objTanks = jObjIdPlayer.getJSONObject(i);
                        JSONObject objTanksAll = objTanks.getJSONObject("all");
                        Tank tank = new Tank();
                        tank.max_xp = objTanks.getString(DatabaseHelper.MAX_XP);
                        tank.max_frags = objTanks.getString(DatabaseHelper.MAX_FRAG);
                        tank.mark_of_mastery = objTanks.getString(DatabaseHelper.MARK_OF_MASTER);
                        tank.tank_id = objTanks.getString(DatabaseHelper.TANK_ID_COLUMN);
                        tank.spotted = objTanksAll.getString(DatabaseHelper.SPOTTED);
                        tank.hits = objTanksAll.getString(DatabaseHelper.HITS);
                        tank.battle_avg_xp = objTanksAll.getString(DatabaseHelper.BATTLES_AVG_XP);
                        tank.draws = objTanksAll.getString(DatabaseHelper.DRAWS);
                        tank.wins = objTanksAll.getString(DatabaseHelper.WINS_COLUMN);
                        tank.losses = objTanksAll.getString(DatabaseHelper.LOSSES);
                        tank.capture_points = objTanksAll.getString(DatabaseHelper.CAPTURE_POINTS);
                        tank.battles = objTanksAll.getString(DatabaseHelper.BATTLES_COLUMN);
                        tank.damage_dealt = objTanksAll.getString(DatabaseHelper.DAMAGE_DEALT);
                        tank.hits_percents = objTanksAll.getString(DatabaseHelper.HITS_PERCENTS);
                        tank.damage_received = objTanksAll.getString(DatabaseHelper.DAMAGE_RECEIVED);
                        tank.shots = objTanksAll.getString(DatabaseHelper.SHOTS);
                        tank.xp = objTanksAll.getString(DatabaseHelper.XP);
                        tank.frags = objTanksAll.getString(DatabaseHelper.FRAGS);
                        tank.survived_battles = objTanksAll.getString(DatabaseHelper.SURVIVED_BATTLES);
                        tank.dropped_capture_points = objTanksAll.getString(DatabaseHelper.DROP_CAP);
                        tankArray[i] = tank;
                    }
                } catch (JSONException ignored) {
                }
            }
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Cursor cursor;
            Cursor cursor_delta;
            double statValue;
            double statDelta;
            HashMap<String, Object> hm;
            List<String> statListForView;
            String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
            String[] strArr = new String[3];
            strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
            strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
            strArr[2] = DatabaseHelper.ACTIVE;
            Cursor main_player_cursor = sdb.query(str, strArr, DatabaseHelper.ACTIVE + "= ?", new String[]{"1"}, null, null, null);
            main_player_cursor.moveToFirst();
            String playerId = main_player_cursor.getString(1);
            String playerName = main_player_cursor.getString(0);
            main_player_cursor.close();
            ListView statListView;
            if (statName == null || tankArray == null || !mStatUpdate) {
                cursor = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_SH +
                        " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN +
                        " = \'" + playerId + "\'" +
                        " ORDER BY " + DatabaseHelper.BATTLES_COLUMN + " DESC", null);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    cursor_delta = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_SH +
                                    " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN +
                            " = \'" + playerId + "\'" +
                                    " ORDER BY " + DatabaseHelper.BATTLES_COLUMN + " DESC LIMIT 2", null);
                    if (cursor_delta.getCount() > 1) {
                        cursor_delta.moveToFirst();
                        cursor_delta.moveToNext();
                    }
                    ((TextView) mViewHierarchy.findViewById(R.id.textViewMainPlayer)).setText(playerName);
                    TextView textView = (TextView) mViewHierarchy.findViewById(R.id.textViewMainBattless);
                    textView.setText(mViewHierarchy.getResources().getString(R.string.ttl_main_stat_name1) +
                            ": " + cursor.getString(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN)));
                    statListView = (ListView) mViewHierarchy.findViewById(R.id.listViewStatistic);
                    statListForView = new ArrayList<>();
                    statList = new ArrayList<>();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name1));
                    hm.put(MainStatFragment.STATVALUE, cursor.getString(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN)));
                    statListForView.add(DatabaseHelper.BATTLES_COLUMN);
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%d", (long) statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name2));
                    hm.put(MainStatFragment.STATVALUE, String.format("%d", cursor.getLong((cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN)))));
                    statListForView.add(DatabaseHelper.WINS_COLUMN);
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%d", (long) statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    textView = (TextView) mViewHierarchy.findViewById(R.id.textViewMainWins);
                    textView.setText(mViewHierarchy.getResources().getString(R.string.ttl_main_stat_name2) + ": " +
                            String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WINRATE_COLUMN))) + "%");
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name3));
                    hm.put(MainStatFragment.STATVALUE, String.format("%d", cursor.getLong(cursor.getColumnIndex(DatabaseHelper.LOSS_COLUMN))));
                    statListForView.add(DatabaseHelper.LOSS_COLUMN);
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOSS_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.LOSS_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%d", (long) statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name4));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WINRATE_COLUMN))));
                    statListForView.add(DatabaseHelper.WINRATE_COLUMN);
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WINRATE_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.WINRATE_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name5));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN))));
                    statListForView.add(DatabaseHelper.DAMAGE_COLUMN);
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name6));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN))));
                    statListForView.add(DatabaseHelper.KILLS_COLUMN);
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name7));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN))));
                    statListForView.add("obn");
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name8));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.CAPS_COLUMN))));
                    statListForView.add(DatabaseHelper.CAPS_COLUMN);
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CAPS_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.CAPS_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name9));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.EXP_DEF_COLUMN))));
                    statListForView.add("defcap");
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.EXP_DEF_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.EXP_DEF_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name10));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN))));
                    statListForView.add(DatabaseHelper.EXP_COLUMN);
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name12));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN))));
                    statListForView.add("tanks_level");
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name13));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN))));
                    statListForView.add("RE");
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    MainStatFragment.this.statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name14));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN))));
                    statListForView.add("WN6");
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name15));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN))));
                    statListForView.add("WN8");
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name16));
                    hm.put(MainStatFragment.STATVALUE, cursor.getString(cursor.getColumnIndex(DatabaseHelper.WG_COLUMN)));
                    statListForView.add("WG");
                    if (cursor_delta.getCount() > 1) {
                        statValue = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WG_COLUMN)));
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor.getColumnIndex(DatabaseHelper.WG_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    cursor_delta.close();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name17));
                    int columnIndex = cursor.getColumnIndex(DatabaseHelper.LAST_BATTLE_COLUMN);
                    hm.put(MainStatFragment.STATVALUE, cursor.getString(columnIndex));
                    cursor.close();
                    statListForView.add("lastbattles");
                    hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    statList.add(hm);
                    statListView.setAdapter(new MyArrayAdapter(mViewHierarchy.getContext(), statListView, statListForView));
                }
            }
            if (statName != null && tankArray != null) {
                updateTanksTable();
                if (!statName[0].equals("0")) {
                    int i;
                    cursor_delta = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_SH +
                             " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerId + "\'"+
                            " ORDER BY " + DatabaseHelper.BATTLES_COLUMN + " DESC LIMIT 1", null);
                    if (cursor_delta.getCount() > 0) {
                        cursor_delta.moveToFirst();
                    }
                    statListView = (ListView) mViewHierarchy.findViewById(R.id.listViewStatistic);
                    statListForView = new ArrayList<>();
                    statList = new ArrayList<>();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name1));
                    hm.put(MainStatFragment.STATVALUE, statName[0]);
                    statListForView.add(DatabaseHelper.BATTLES_COLUMN);
                    if (cursor_delta.getCount() > 0) {
                        statValue = Double.parseDouble(statName[0]);
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.BATTLES_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%d", (long) statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name2));
                    hm.put(MainStatFragment.STATVALUE, statName[1]);
                    statListForView.add(DatabaseHelper.WINS_COLUMN);
                    if (cursor_delta.getCount() > 0) {
                        statValue = Double.parseDouble(statName[1]);
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.WINS_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%d", (long) statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name3));
                    hm.put(MainStatFragment.STATVALUE, statName[2]);
                    statListForView.add(DatabaseHelper.LOSS_COLUMN);
                    if (cursor_delta.getCount() > 0) {
                        statValue = Double.parseDouble(statName[2]);
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.LOSS_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%d", (long) statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    double persent_win_rez = (double) new BigDecimal((Double.parseDouble(statName[1]) / Double.parseDouble(statName[0])) * 100.0d).setScale(2, 5).floatValue();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name4));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", persent_win_rez) + "%");
                    statListForView.add(DatabaseHelper.WINRATE_COLUMN);
                    if (cursor_delta.getCount() > 0) {
                        statValue = persent_win_rez;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.WINRATE_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    persent_win_rez = (double) new BigDecimal(Double.parseDouble(statName[3]) / Double.parseDouble(statName[0])).setScale(2, 5).floatValue();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name5));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", persent_win_rez));
                    statListForView.add(DatabaseHelper.DAMAGE_COLUMN);
                    if (cursor_delta.getCount() > 0) {
                        statValue = persent_win_rez;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    persent_win_rez = (double) new BigDecimal(Double.parseDouble(statName[4]) / Double.parseDouble(statName[0])).setScale(2, 5).floatValue();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, MainStatFragment.this.mViewHierarchy.getResources().getString(R.string.ttl_stat_name6));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", persent_win_rez));
                    statListForView.add(DatabaseHelper.KILLS_COLUMN);
                    if (cursor_delta.getCount() > 0) {
                        statValue = persent_win_rez;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.KILLS_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    persent_win_rez = (double) new BigDecimal(Double.parseDouble(statName[5]) / Double.parseDouble(statName[0])).setScale(2, 5).floatValue();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name7));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", persent_win_rez));
                    statListForView.add("obn");
                    if (cursor_delta.getCount() > 0) {
                        statValue = persent_win_rez;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.SPOT_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    persent_win_rez = (double) new BigDecimal(Double.parseDouble(statName[6]) / Double.parseDouble(statName[0])).setScale(2, 5).floatValue();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name8));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", persent_win_rez));
                    statListForView.add(DatabaseHelper.CAPS_COLUMN);
                    if (cursor_delta.getCount() > 0) {
                        statValue = persent_win_rez;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.CAPS_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    persent_win_rez = (double) new BigDecimal(Double.parseDouble(statName[7]) / Double.parseDouble(statName[0])).setScale(2, 5).floatValue();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name9));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", persent_win_rez));
                    statListForView.add("defcap");
                    if (cursor_delta.getCount() > 0) {
                        statValue = persent_win_rez;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.EXP_DEF_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.3f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    persent_win_rez = (double) new BigDecimal(Double.parseDouble(statName[8]) / Double.parseDouble(statName[0])).setScale(2, 5).floatValue();
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name10));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", persent_win_rez));
                    statListForView.add(DatabaseHelper.EXP_COLUMN);
                    if (cursor_delta.getCount() > 0) {
                        statValue = persent_win_rez;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.EXP_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    Double level_sum = 0.0d;
                    for (i = 0; i < tankArray.length; i++) {
                        cursor = sdb.rawQuery("SELECT level FROM " + DatabaseHelper.DATABASE_TABLE_TANKS +
                                " WHERE "+ DatabaseHelper.TANK_ID_COLUMN + " = \'" + tankArray[i].tank_id + "\'", null);
                        cursor.moveToFirst();
                        if (cursor.getCount() > 0) {
                            level_sum = level_sum + ((double) (Long.parseLong(cursor.getString(0)) * Long.parseLong(tankArray[i].battles)));
                        }
                        cursor.close();
                    }
                    level_sum = level_sum / ((double) Long.parseLong(statName[0]));
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name12));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", level_sum));
                    statListForView.add("tanks_level");
                    if (cursor_delta.getCount() > 0) {
                        statValue = level_sum;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.LEVEL_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    MainStatFragment.this.statList.add(hm);
                    Double dblFrags = Double.parseDouble(statName[4]) / Double.parseDouble(statName[0]);
                    Double dblDamage = Double.parseDouble(statName[3]) / Double.parseDouble(statName[0]);
                    Double dblSpot = Double.parseDouble(statName[5]) / Double.parseDouble(statName[0]);
                    Double dblWinrate = (Double.parseDouble(statName[1]) / Double.parseDouble(statName[0])) * 100.0d;
                    Double dblDef = Double.parseDouble(statName[7]) / Double.parseDouble(statName[0]);
                    Double dblCap = Double.parseDouble(statName[6]) / Double.parseDouble(statName[0]);
                    Double dblMintier = level_sum;
                    if (level_sum > 6.0d) {
                        dblMintier = 6.0d;
                    }
                    if (dblDef > 2.2d) {
                        dblDef = 2.2d;
                    }
                    Double dblWN6 = ((((((1240.0d - (1040.0d / Math.pow(dblMintier, 0.164d))) * dblFrags) + ((dblDamage * 530.0d) / ((184.0d * Math.exp(0.24d * level_sum)) + 130.0d))) + (dblSpot * 125.0d)) + (dblDef * 100.0d)) + (((185.0d / (0.17d + Math.exp((dblWinrate - 35.0d) * -0.134d))) - 500.0d) * 0.45d)) + ((6.0d - dblMintier) * -60.0d);
                    Double dblRE = (((((dblDamage * (10.0d / (level_sum + 2.0d))) * (0.23d + ((2.0d * level_sum) / 100.0d))) + (dblFrags * 250.0d)) + (dblSpot * 150.0d)) + ((Math.log(dblCap + 1.0d) / Math.log(1.732d)) * 150.0d)) + (dblDef * 150.0d);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name13));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", dblRE));
                    statListForView.add("RE");
                    if (cursor_delta.getCount() > 0) {
                        statValue = dblRE;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.REFF_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name14));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", dblWN6));
                    statListForView.add("WN6");
                    if (cursor_delta.getCount() > 0) {
                        statValue = dblWN6;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.WN6_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    Double dblBattles = Double.parseDouble(statName[0]);
                    Double expDamage = 0.0d;
                    Double expWinrate = 0.0d;
                    Double expSpot = 0.0d;
                    Double expFrag = 0.0d;
                    Double expDef = 0.0d;
                    for (i = 0; i < tankArray.length; i++) {
                        cursor = sdb.rawQuery("SELECT " + DatabaseHelper.TANK_ID_COLUMN +
                                ", " + DatabaseHelper.TANK_NAME_COLUMN +
                                ", " + DatabaseHelper.LEVEL_COLUMN +
                                ", " + DatabaseHelper.DAMAGE_COLUMN +
                                ", " + DatabaseHelper.WINS_COLUMN +
                                ", " + DatabaseHelper.SPOT_COLUMN +
                                ", " + DatabaseHelper.KILLS_COLUMN +
                                ", " + DatabaseHelper.EXP_DEF_COLUMN +
                                " FROM " + DatabaseHelper.DATABASE_TABLE_TANKS_EXP +
                                " WHERE " + DatabaseHelper.TANK_ID_COLUMN + " = \'" + tankArray[i].tank_id + "\'", null);
                        cursor.moveToFirst();
                        if (cursor.getCount() > 0) {
                            double doubleValue = expDamage;
                            expDamage = doubleValue + (Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN))) * Double.parseDouble(tankArray[i].battles));
                            doubleValue = expSpot;
                            expSpot = doubleValue + (Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN))) * Double.parseDouble(tankArray[i].battles));
                            doubleValue = expFrag;
                            expFrag = doubleValue + (Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN))) * Double.parseDouble(tankArray[i].battles));
                            doubleValue = expDef;
                            expDef = doubleValue + (Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.EXP_DEF_COLUMN))) * Double.parseDouble(tankArray[i].battles));
                            doubleValue = expWinrate;
                            expWinrate = doubleValue + (Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN))) * ((double) Long.parseLong(tankArray[i].battles)));
                        }
                        cursor.close();
                    }
                    expWinrate = expWinrate / Double.parseDouble(statName[0]);
                    Double rDAMAGE = Double.parseDouble(statName[3]) / expDamage;
                    Double rSPOT = Double.parseDouble(statName[5]) / expSpot;
                    Double rFRAG = Double.parseDouble(statName[4]) / expFrag;
                    Double rDEF = Double.parseDouble(statName[7]) / expDef;
                    Double rWINc = Math.max(0.0d, (dblWinrate / expWinrate - 0.71d) / 0.29000000000000004d);
                    Double rDAMAGEc = Math.max(0.0d, (rDAMAGE - 0.22d) / 0.78d);
                    Double rFRAGc = Math.max(0.0d, Math.min(rDAMAGEc + 0.2d, (rFRAG - 0.12d) / 0.88d));
                    Double dblWN8 = ((((980.0d * rDAMAGEc) + ((210.0d * rDAMAGEc) * rFRAGc)) + ((155.0d * rFRAGc) * Math.max(0.0d, Math.min(rDAMAGEc + 0.1d, (rSPOT - 0.38d) / 0.62d)))) + ((75.0d * Math.max(0.0d, Math.min(rDAMAGEc + 0.1d, (rDEF - 0.1d) / 0.9d))) * rFRAGc)) + (145.0d * Math.min(1.8d, rWINc));
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name15));
                    hm.put(MainStatFragment.STATVALUE, String.format("%.2f", dblWN8));
                    statListForView.add("WN8");
                    if (cursor_delta.getCount() > 0) {
                        statValue = dblWN8;
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.WN8_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name16));
                    hm.put(MainStatFragment.STATVALUE, statName[9]);
                    statListForView.add("WG");
                    if (cursor_delta.getCount() > 0) {
                        statValue = Double.parseDouble(statName[9]);
                        statDelta = statValue - Double.parseDouble(cursor_delta.getString(cursor_delta.getColumnIndex(DatabaseHelper.WG_COLUMN)));
                        hm.put(MainStatFragment.STATDELTA, String.format("%.2f", statDelta));
                    } else {
                        hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    }
                    statList.add(hm);
                    cursor_delta.close();
                    Date LastDate = new Date(Long.parseLong(statName[10]) * 1000);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.US);
                    hm = new HashMap<>();
                    hm.put(MainStatFragment.STATNAME, mViewHierarchy.getResources().getString(R.string.ttl_stat_name17));
                    hm.put(MainStatFragment.STATVALUE, "" + simpleDateFormat.format(LastDate));
                    statListForView.add("lastbattles");
                    hm.put(MainStatFragment.STATDELTA, "NO_DATA");
                    statList.add(hm);
                    playerId = statName[12];
                    playerName = statName[13];
                    cursor = sdb.rawQuery("SELECT " + DatabaseHelper.PLAYER_ID_COLUMN +
                            ", " + DatabaseHelper.BATTLES_COLUMN +
                            " FROM " + DatabaseHelper.DATABASE_TABLE_SH +
                            " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerId + "\' AND " +
                            DatabaseHelper.BATTLES_COLUMN + " = \'" + statName[0] + "\'", null);
                    cursor.moveToFirst();
                    if (cursor.getCount() == 0) {
                        ContentValues newValues = new ContentValues();
                        newValues.put(DatabaseHelper.PLAYER_ID_COLUMN, playerId);
                        newValues.put(DatabaseHelper.PLAYER_NAME_COLUMN, playerName);
                        newValues.put(DatabaseHelper.BATTLES_COLUMN, dblBattles);
                        newValues.put(DatabaseHelper.WINS_COLUMN, Double.parseDouble(statName[1]));
                        newValues.put(DatabaseHelper.LOSS_COLUMN, Double.parseDouble(statName[2]));
                        newValues.put(DatabaseHelper.WINRATE_COLUMN, dblWinrate);
                        newValues.put(DatabaseHelper.DAMAGE_COLUMN, dblDamage);
                        newValues.put(DatabaseHelper.KILLS_COLUMN, dblFrags);
                        newValues.put(DatabaseHelper.SPOT_COLUMN, dblSpot);
                        newValues.put(DatabaseHelper.CAPS_COLUMN, dblCap);
                        newValues.put(DatabaseHelper.EXP_DEF_COLUMN, dblDef);
                        newValues.put(DatabaseHelper.EXP_COLUMN, persent_win_rez);
                        newValues.put(DatabaseHelper.MAX_EXP_COLUMN, statName[11]);
                        newValues.put(DatabaseHelper.LEVEL_COLUMN, level_sum);
                        newValues.put(DatabaseHelper.REFF_COLUMN, dblRE);
                        newValues.put(DatabaseHelper.WN6_COLUMN, dblWN6);
                        newValues.put(DatabaseHelper.WN8_COLUMN, dblWN8);
                        newValues.put(DatabaseHelper.LAST_BATTLE_COLUMN, "" + simpleDateFormat.format(LastDate));
                        newValues.put(DatabaseHelper.WG_COLUMN, statName[9]);
                        sdb.insert(DatabaseHelper.DATABASE_TABLE_SH, null, newValues);
                    }
                    cursor.close();
                    ((TextView) mViewHierarchy.findViewById(R.id.textViewMainPlayer)).setText(playerName);
                    ((TextView) mViewHierarchy.findViewById(R.id.textViewMainBattless)).setText(mViewHierarchy.getResources().getString(R.string.ttl_main_stat_name1) + ": " + String.format("%d", dblBattles.intValue()));
                    ((TextView) mViewHierarchy.findViewById(R.id.textViewMainWins)).setText(mViewHierarchy.getResources().getString(R.string.ttl_main_stat_name2) + ": " + String.format("%.2f", dblWinrate) + "%");
                    statListView.setAdapter(new MyArrayAdapter(mViewHierarchy.getContext(), statListView, statListForView));
                    tankArray = null;
                }
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        void updateTanksTable() {
            if (MainStatFragment.this.tankArray != null) {
                int i;
                SQLiteDatabase sdb = dbHelper.getWritableDatabase();
                String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
                String[] strArgs = new String[4];
                strArgs[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
                strArgs[1] = DatabaseHelper.PLAYER_ID_COLUMN;
                strArgs[2] = DatabaseHelper.ACTIVE;
                strArgs[3] = DatabaseHelper.SERVER;
                Cursor main_player_cursor = sdb.query(str, strArgs, DatabaseHelper.ACTIVE + "= ?", new String[]{"1"}, null, null, null);
                main_player_cursor.moveToFirst();
                String player_id = main_player_cursor.getString(1);
                String server_name = main_player_cursor.getString(3);
                main_player_cursor.close();
                long battleAll = 0;
                str = DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST;
                sdb.delete(str, DatabaseHelper.PLAYER_ID_COLUMN + " = ?", new String[]{player_id});
                for (Tank tank : tankArray) {
                    battleAll += Long.parseLong(tank.battles);
                }
                for (i = 0; i < tankArray.length; i++) {
                    StringBuilder append;
                    Cursor cursor;
                    if (server_name.equals("RU")) {
                        cursor = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_TANKS +
                                " WHERE " + DatabaseHelper.TANK_ID_COLUMN + " = \'" + tankArray[i].tank_id + "\'", null);
                    } else {
                        cursor = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_TANKS_EN +
                                " WHERE " + DatabaseHelper.TANK_ID_COLUMN + " = \'" + tankArray[i].tank_id + "\'", null);
                    }
                    cursor.moveToFirst();
                    if (cursor.getCount() > 0) {
                        Double dblWN8;
                        ContentValues newValues;
                        Cursor cursor_stat_tanks_prov = sdb.rawQuery("SELECT " + DatabaseHelper.BATTLES_COLUMN +
                                " FROM " + DatabaseHelper.DATABASE_TABLE_TANK_STAT +
                                " WHERE " + DatabaseHelper.TANK_ID_COLUMN + " = \'" + tankArray[i].tank_id + "\' AND " +
                                DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + player_id + "\' AND " +
                                DatabaseHelper.BATTLES_ALL + " = \'" + String.valueOf(battleAll) + "\'", null);
                        Double dblFrags = Double.parseDouble(tankArray[i].frags) / Double.parseDouble(tankArray[i].battles);
                        Double dblDamage = Double.parseDouble(tankArray[i].damage_dealt) / Double.parseDouble(tankArray[i].battles);
                        Double dblSpot = Double.parseDouble(tankArray[i].spotted) / Double.parseDouble(tankArray[i].battles);
                        Double dblWinrate = Double.parseDouble(tankArray[i].wins) / Double.parseDouble(tankArray[i].battles) * 100.0d;
                        Double dblDef = Double.parseDouble(tankArray[i].dropped_capture_points) / Double.parseDouble(tankArray[i].battles);
                        Double dblCap = Double.parseDouble(tankArray[i].capture_points) / Double.parseDouble(tankArray[i].battles);
                        Double dblMintier = Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN)));
                        if (dblMintier > 6.0d) {
                            dblMintier = 6.0d;
                        }
                        if (dblDef > 2.2d) {
                            dblDef = 2.2d;
                        }
                        double pow = (1240.0d - (1040.0d / Math.pow(dblMintier, 0.164d))) * dblFrags;
                        double doubleValue = dblDamage * 530.0d;
                        Double dblWN6 = ((((pow + (doubleValue / ((184.0d * Math.exp(0.24d * cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN)))) + 130.0d))) + (dblSpot * 125.0d)) + (dblDef * 100.0d)) + (((185.0d / (0.17d + Math.exp((dblWinrate - 35.0d) * -0.134d))) - 500.0d) * 0.45d)) + ((6.0d - dblMintier) * -60.0d);
                        pow = dblDamage;
                        pow *= 10.0d / (cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN)) + 2.0d);
                        Double dblRE = ((((pow * (0.23d + ((2.0d * cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN))) / 100.0d))) + (dblFrags * 250.0d)) + (dblSpot * 150.0d)) + ((Math.log(dblCap + 1.0d) / Math.log(1.732d)) * 150.0d)) + (dblDef * 150.0d);
                        Double expDamage;
                        Double expWinrate = 0.0d;
                        Double expSpot;
                        Double expFrag;
                        Double expDef;
                        append = new StringBuilder().append("select ");
                        append = append.append(DatabaseHelper.TANK_ID_COLUMN).append(", ");
                        append = append.append(DatabaseHelper.TANK_NAME_COLUMN).append(", ");
                        append = append.append(DatabaseHelper.LEVEL_COLUMN).append(", ");
                        append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(", ");
                        append = append.append(DatabaseHelper.WINS_COLUMN).append(", ");
                        append = append.append(DatabaseHelper.SPOT_COLUMN).append(", ");
                        append = append.append(DatabaseHelper.KILLS_COLUMN).append(", ");
                        append = append.append(DatabaseHelper.EXP_DEF_COLUMN).append(" from ");
                        append = append.append(DatabaseHelper.DATABASE_TABLE_TANKS_EXP).append(" where ");
                        Cursor cursor_wn8 = sdb.rawQuery(append.append(DatabaseHelper.TANK_ID_COLUMN).append(" = '").append(tankArray[i].tank_id).append("'").toString(), null);
                        cursor_wn8.moveToFirst();
                        if (cursor_wn8.getCount() > 0) {
                            expDamage = Double.parseDouble(cursor_wn8.getString(cursor_wn8.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN)));
                            expSpot = Double.parseDouble(cursor_wn8.getString(cursor_wn8.getColumnIndex(DatabaseHelper.SPOT_COLUMN)));
                            expFrag = Double.parseDouble(cursor_wn8.getString(cursor_wn8.getColumnIndex(DatabaseHelper.KILLS_COLUMN)));
                            expDef = Double.parseDouble(cursor_wn8.getString(cursor_wn8.getColumnIndex(DatabaseHelper.EXP_DEF_COLUMN)));
                            pow = expWinrate;
                            expWinrate = pow + Double.parseDouble(cursor_wn8.getString(cursor_wn8.getColumnIndex(DatabaseHelper.WINS_COLUMN)));
                            Double rDAMAGE = dblDamage / expDamage;
                            Double rSPOT = dblSpot / expSpot;
                            Double rFRAG = dblFrags / expFrag;
                            Double rDEF = dblDef / expDef;
                            Double rWINc = Math.max(0.0d, (dblWinrate / expWinrate - 0.71d) / 0.29000000000000004d);
                            Double rDAMAGEc = Math.max(0.0d, (rDAMAGE - 0.22d) / 0.78d);
                            Double rFRAGc = Math.max(0.0d, Math.min(rDAMAGEc + 0.2d, (rFRAG - 0.12d) / 0.88d));
                            dblWN8 = ((((980.0d * rDAMAGEc) + ((210.0d * rDAMAGEc) * rFRAGc)) + ((155.0d * rFRAGc) * Math.max(0.0d, Math.min(rDAMAGEc + 0.1d, (rSPOT - 0.38d) / 0.62d)))) + ((75.0d * Math.max(0.0d, Math.min(rDAMAGEc + 0.1d, (rDEF - 0.1d) / 0.9d))) * rFRAGc)) + (145.0d * Math.min(1.8d, rWINc));
                        } else {
                            dblWN8 = 0.0d;
                        }
                        cursor_wn8.close();
                        if (cursor_stat_tanks_prov.getCount() == 0) {
                            newValues = new ContentValues();
                            str = DatabaseHelper.TANK_ID_COLUMN;
                            newValues.put(str, cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TANK_ID_COLUMN)));
                            newValues.put(DatabaseHelper.PLAYER_ID_COLUMN, player_id);
                            str = DatabaseHelper.NAME_RU;
                            newValues.put(str, cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NAME_I18N)));
                            str = DatabaseHelper.NAME_EU;
                            newValues.put(str, cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NAME_I18N)));
                            str = DatabaseHelper.TANK_LEVEL;
                            newValues.put(str, Long.valueOf(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN))));
                            str = DatabaseHelper.TANK_TYPE;
                            newValues.put(str, cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE_COLUMN)));
                            str = DatabaseHelper.TANK_NATION;
                            newValues.put(str, cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NATION_I18N)));
                            newValues.put(DatabaseHelper.MAX_XP, Long.valueOf(tankArray[i].max_xp));
                            newValues.put(DatabaseHelper.MAX_FRAG, Long.valueOf(tankArray[i].max_frags));
                            newValues.put(DatabaseHelper.MARK_OF_MASTER, Long.valueOf(tankArray[i].mark_of_mastery));
                            newValues.put(DatabaseHelper.IN_GAR, false);
                            newValues.put(DatabaseHelper.BATTLES_COLUMN, Double.parseDouble(tankArray[i].battles));
                            newValues.put(DatabaseHelper.WINS_COLUMN, dblWinrate);
                            newValues.put(DatabaseHelper.DAMAGE_COLUMN, dblDamage);
                            newValues.put(DatabaseHelper.KILLS_COLUMN, dblFrags);
                            newValues.put(DatabaseHelper.SPOT_COLUMN, dblSpot);
                            newValues.put(DatabaseHelper.CAPS_COLUMN, dblCap);
                            newValues.put(DatabaseHelper.DROP_CAPS, dblDef);
                            newValues.put(DatabaseHelper.EXP_COLUMN, Double.parseDouble(tankArray[i].battle_avg_xp));
                            newValues.put(DatabaseHelper.WN6_COLUMN, dblWN6);
                            newValues.put(DatabaseHelper.REFF_COLUMN, dblRE);
                            newValues.put(DatabaseHelper.WN8_COLUMN, dblWN8);
                            newValues.put(DatabaseHelper.BATTLES_ALL, battleAll);
                            sdb = dbHelper.getWritableDatabase();
                            sdb.insert(DatabaseHelper.DATABASE_TABLE_TANK_STAT, null, newValues);
                        }
                        newValues = new ContentValues();
                        str = DatabaseHelper.TANK_ID_COLUMN;
                        newValues.put(str, cursor.getLong(cursor.getColumnIndex(DatabaseHelper.TANK_ID_COLUMN)));
                        newValues.put(DatabaseHelper.PLAYER_ID_COLUMN, player_id);
                        str = DatabaseHelper.NAME_RU;
                        newValues.put(str, cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NAME_I18N)));
                        str = DatabaseHelper.NAME_EU;
                        newValues.put(str, cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NAME_I18N)));
                        str = DatabaseHelper.TANK_LEVEL;
                        newValues.put(str, Long.valueOf(cursor.getString(cursor.getColumnIndex(DatabaseHelper.LEVEL_COLUMN))));
                        str = DatabaseHelper.TANK_TYPE;
                        newValues.put(str, cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE_COLUMN)));
                        str = DatabaseHelper.TANK_NATION;
                        newValues.put(str, cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NATION_I18N)));
                        newValues.put(DatabaseHelper.MAX_XP, Long.valueOf(tankArray[i].max_xp));
                        newValues.put(DatabaseHelper.MAX_FRAG, Long.valueOf(tankArray[i].max_frags));
                        newValues.put(DatabaseHelper.MARK_OF_MASTER, Long.valueOf(tankArray[i].mark_of_mastery));
                        newValues.put(DatabaseHelper.IN_GAR, false);
                        newValues.put(DatabaseHelper.BATTLES_COLUMN, Double.parseDouble(tankArray[i].battles));
                        newValues.put(DatabaseHelper.WINS_COLUMN, dblWinrate);
                        newValues.put(DatabaseHelper.DAMAGE_COLUMN, dblDamage);
                        newValues.put(DatabaseHelper.KILLS_COLUMN, dblFrags);
                        newValues.put(DatabaseHelper.SPOT_COLUMN, dblSpot);
                        newValues.put(DatabaseHelper.CAPS_COLUMN, dblCap);
                        newValues.put(DatabaseHelper.DROP_CAPS, dblDef);
                        newValues.put(DatabaseHelper.EXP_COLUMN, Double.parseDouble(tankArray[i].battle_avg_xp));
                        newValues.put(DatabaseHelper.WN6_COLUMN, dblWN6);
                        newValues.put(DatabaseHelper.REFF_COLUMN, dblRE);
                        newValues.put(DatabaseHelper.WN8_COLUMN, dblWN8);
                        newValues.put(DatabaseHelper.BATTLES_ALL, battleAll);
                        sdb.insert(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST, null, newValues);
                        cursor_stat_tanks_prov.close();
                    }
                    cursor.close();
                }
            } else {
                Toast.makeText(MainStatFragment.this.getActivity(), MainStatFragment.this.getString(R.string.err_load_data_msg), Toast.LENGTH_SHORT).show();
            }
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MainStatFragment.this.getActivity(), "", MainStatFragment.this.getString(R.string.msg_wait1) + "\n" + MainStatFragment.this.getString(R.string.msg_wait2));
            super.onPreExecute();
        }
    }

    public MainStatFragment() {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
    }

    public void onResume() {
        super.onResume();
        try {
            sdb = dbHelper.getReadableDatabase();
            StringBuilder append = new StringBuilder().append("SELECT * FROM ");
            if (sdb.rawQuery(append.append(DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER).append(" WHERE active = 1").toString(), null).getCount() > 0) {
                mViewHierarchy.findViewById(R.id.scrollViewMain).getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ((ScrollView) mViewHierarchy.findViewById(R.id.scrollViewMain)).fullScroll(33);
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }

    public void refreshPlayerStats() {
        if (isAdded()) {
            new PrefetchDataCurrency().execute();
        }
    }

    public void onStart() {
        super.onStart();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        sdb = dbHelper.getReadableDatabase();
        StringBuilder append = new StringBuilder().append("SELECT * FROM ");
        Cursor cursor = sdb.rawQuery(append.append(DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER).append(" WHERE active = 1").toString(), null);
        mStatUpdate = true;
        if (cursor.getCount() > 0) {
            refreshPlayerStats();
        }
        cursor.close();
        super.onActivityCreated(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sdb = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER + " WHERE active = 1";
        Cursor cursor = sdb.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            View viewHierarchy = inflater.inflate(R.layout.fragment_stat_main, container, false);
            mViewHierarchy = viewHierarchy;
            mainPlayer = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PLAYER_ID_COLUMN));
            TabHost tabs = (TabHost) viewHierarchy;
            tabs.setup();
            TabSpec spec = tabs.newTabSpec("tagMain");
            View tabIndicator = LayoutInflater.from(getActivity()).inflate(R.layout.apptheme_tab_indicator_holo, tabs.getTabWidget(), false);
            ((TextView) tabIndicator.findViewById(android.R.id.title)).setText(getString(R.string.ttl_stat_tab1));
            spec.setContent(R.id.tabMain);
            spec.setIndicator(tabIndicator);
            tabs.addTab(spec);
            spec = tabs.newTabSpec("tagTanks");
            tabIndicator = LayoutInflater.from(getActivity()).inflate(R.layout.apptheme_tab_indicator_holo, tabs.getTabWidget(), false);
            ((TextView) tabIndicator.findViewById(android.R.id.title)).setText(getString(R.string.ttl_stat_tab2));
            spec.setContent(R.id.tabTanks);
            spec.setIndicator(tabIndicator);
            tabs.addTab(spec);
            tabs.setCurrentTab(0);
            mTanksUpdate = false;
            mViewHierarchy.findViewById(R.id.textViewMainPlayer).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshPlayerStats();
                }
            });
            Context context = mViewHierarchy.getContext();
            String str2 = APP_PREFERENCES_SORT;
            mViewHierarchy.getContext();
            mSettings = context.getSharedPreferences(str2, 0);
            Spinner spinner = (Spinner) viewHierarchy.findViewById(R.id.spinnerSortTank);
            spinner.setSelection(mSettings.getInt(APP_PREFERENCES_SORT, 0));
            spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
                    String[] strArr = new String[3];
                    strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
                    strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
                    strArr[2] = DatabaseHelper.ACTIVE;
                    Cursor main_player_cursor = sdb.query(str, strArr, DatabaseHelper.ACTIVE + "= ?", new String[]{"1"}, null, null, null);
                    main_player_cursor.moveToFirst();
                    String player_id = main_player_cursor.getString(1);
                    main_player_cursor.close();
                    viewPlayerTank(getResources().getStringArray(R.array.sortlist)[position], player_id);
                    Editor editor = mSettings.edit();
                    editor.putInt(MainStatFragment.APP_PREFERENCES_SORT, position);
                    editor.apply();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            ((CheckBox) viewHierarchy.findViewById(R.id.checkBoxChengeOnly)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
                    String[] strArr = new String[3];
                    strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
                    strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
                    strArr[2] = DatabaseHelper.ACTIVE;
                    Cursor main_player_cursor = sdb.query(str, strArr, DatabaseHelper.ACTIVE + "= ?", new String[]{"1"}, null, null, null);
                    main_player_cursor.moveToFirst();
                    String player_id = main_player_cursor.getString(1);
                    main_player_cursor.close();
                    Spinner sortTank = (Spinner) mViewHierarchy.findViewById(R.id.spinnerSortTank);
                    viewPlayerTank(getResources().getStringArray(R.array.sortlist)[sortTank.getSelectedItemPosition()], player_id);
                }
            });
            FrameLayout ln = (FrameLayout) mViewHierarchy.findViewById(R.id.frameMainStat);
            ViewGroup.LayoutParams lp = ln.getLayoutParams();
            lp.height = (getResources().getDisplayMetrics().densityDpi * 1000) / 290;
            ln.setLayoutParams(lp);
            cursor.close();
            return viewHierarchy;
        }
        mViewHierarchy = inflater.inflate(R.layout.introduce, container, false);
        return mViewHierarchy;
    }

    private int getColorByWins(float value) {
        Resources resources = this.mViewHierarchy.getResources();
        if (value < 47.0f) {
            return resources.getColor(R.color.olen_red);
        }
        if (value >= 47.0f && value < 49.0f) {
            return resources.getColor(R.color.olen_orange);
        }
        if (value >= 49.0f && ((double) value) < 52.5d) {
            return resources.getColor(R.color.olen_yellow);
        }
        if (((double) value) >= 52.5d && value < 58.0f) {
            return resources.getColor(R.color.olen_green);
        }
        if (value >= 58.0f && value < 65.0f) {
            return resources.getColor(R.color.olen_purpure);
        }
        if (value >= 65.0f) {
            return resources.getColor(R.color.olen_purple);
        }
        return 0;
    }

    private int getColorByEFF(float value) {
        Resources resources = this.mViewHierarchy.getResources();
        if (value < 615.0f) {
            return resources.getColor(R.color.olen_red);
        }
        if (value >= 615.0f && value < 870.0f) {
            return resources.getColor(R.color.olen_orange);
        }
        if (value >= 870.0f && value < 1175.0f) {
            return resources.getColor(R.color.olen_yellow);
        }
        if (value >= 1175.0f && value < 1525.0f) {
            return resources.getColor(R.color.olen_green);
        }
        if (value >= 1525.0f && value < 1850.0f) {
            return resources.getColor(R.color.olen_purpure);
        }
        if (value >= 1850.0f) {
            return resources.getColor(R.color.olen_purple);
        }
        return 0;
    }

    private int getColorByWN6(float value) {
        Resources resources = this.mViewHierarchy.getResources();
        if (value < 460.0f) {
            return resources.getColor(R.color.olen_red);
        }
        if (value >= 460.0f && value < 850.0f) {
            return resources.getColor(R.color.olen_orange);
        }
        if (value >= 850.0f && value < 1215.0f) {
            return resources.getColor(R.color.olen_yellow);
        }
        if (value >= 1215.0f && value < 1620.0f) {
            return resources.getColor(R.color.olen_green);
        }
        if (value >= 1620.0f && value < 1960.0f) {
            return resources.getColor(R.color.olen_purpure);
        }
        if (value >= 1960.0f) {
            return resources.getColor(R.color.olen_purple);
        }
        return 0;
    }

    private int getColorByWN8(float value) {
        Resources resources = this.mViewHierarchy.getResources();
        if (value < 370.0f) {
            return resources.getColor(R.color.olen_red);
        }
        if (value >= 370.0f && value < 845.0f) {
            return resources.getColor(R.color.olen_orange);
        }
        if (value >= 845.0f && value < 1395.0f) {
            return resources.getColor(R.color.olen_yellow);
        }
        if (value >= 1395.0f && value < 2070.0f) {
            return resources.getColor(R.color.olen_green);
        }
        if (value >= 2070.0f && value < 2715.0f) {
            return resources.getColor(R.color.olen_purpure);
        }
        if (value >= 2715.0f) {
            return resources.getColor(R.color.olen_purple);
        }
        return 0;
    }

    public void viewPlayerTank(String sortField, String player_id) {
        Cursor cursor;
        Cursor cursorGraph;
        StringBuilder append = new StringBuilder().append("t1.");
        String order_by = append.append(DatabaseHelper.BATTLES_COLUMN).toString();
        if (sortField.equals(getString(R.string.ttl_sort_fields1))) {
            append = new StringBuilder().append("t1.");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(" DESC, t1.");
            order_by = append.append(DatabaseHelper.TANK_ID_COLUMN).toString();
        }
        if (sortField.equals(getString(R.string.ttl_sort_fields2))) {
            append = new StringBuilder().append("t1.");
            append = append.append(DatabaseHelper.TANK_LEVEL).append(" DESC,").append("t1.");
            order_by = append.append(DatabaseHelper.BATTLES_COLUMN).toString();
        }
        if (sortField.equals(getString(R.string.ttl_sort_fields3))) {
            append = new StringBuilder().append("t1.");
            append = append.append(DatabaseHelper.WINS_COLUMN).append(" DESC,").append("t1.");
            order_by = append.append(DatabaseHelper.BATTLES_COLUMN).toString();
        }
        if (sortField.equals(getString(R.string.ttl_sort_fields4))) {
            append = new StringBuilder().append("t1.");
            append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(" DESC,").append("t1.");
            order_by = append.append(DatabaseHelper.BATTLES_COLUMN).toString();
        }
        if (sortField.equals(getString(R.string.ttl_sort_fields5))) {
            append = new StringBuilder().append("t1.");
            order_by = append.append(DatabaseHelper.EXP_COLUMN).toString();
        }
        if (sortField.equals(getString(R.string.ttl_sort_fields6))) {
            append = new StringBuilder().append("t1.");
            order_by = append.append(DatabaseHelper.WN6_COLUMN).toString();
        }
        if (sortField.equals(getString(R.string.ttl_sort_fields7))) {
            append = new StringBuilder().append("t1.");
            order_by = append.append(DatabaseHelper.REFF_COLUMN).toString();
        }
        if (sortField.equals(getString(R.string.ttl_sort_fields8))) {
            append = new StringBuilder().append("t1.");
            order_by = append.append(DatabaseHelper.WN8_COLUMN).toString();
        }
        SQLiteDatabase fdb = this.dbHelper.getReadableDatabase();
        append = new StringBuilder().append("SELECT t3.");
        append = append.append(DatabaseHelper.BATTLES_ALL).append(" FROM ");
        append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" t3 ").append(" WHERE t3.");
        append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("'").append(" GROUP BY t3.");
        Cursor cursor_prov = fdb.rawQuery(append.append(DatabaseHelper.BATTLES_ALL).toString(), null);
        long zam_count = (long) cursor_prov.getCount();
        cursor_prov.close();
        StringBuilder append2;
        if (zam_count > 1) {
            append = new StringBuilder().append("SELECT  t1.");
            append = append.append(DatabaseHelper.TANK_ID_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.NAME_RU).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.NAME_EU).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.TANK_LEVEL).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.TANK_TYPE).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.TANK_NATION).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.MAX_XP).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.MAX_FRAG).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.MARK_OF_MASTER).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.IN_GAR).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.WINS_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.KILLS_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.SPOT_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.CAPS_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.DROP_CAPS).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.EXP_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.WN6_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.REFF_COLUMN).append(", ").append(" t1.");
            append = append.append(DatabaseHelper.WN8_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.WINS_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.WINS_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.KILLS_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.KILLS_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.SPOT_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.SPOT_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.CAPS_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.CAPS_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.DROP_CAPS).append(" as T2_");
            append = append.append(DatabaseHelper.DROP_CAPS).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.EXP_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.EXP_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.WN6_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.WN6_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.REFF_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.REFF_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.WN8_COLUMN).append(" as T2_");
            append = append.append(DatabaseHelper.WN8_COLUMN).append(", ").append(" t2.");
            append = append.append(DatabaseHelper.BATTLES_ALL).append(" as T2_");
            append = append.append(DatabaseHelper.BATTLES_ALL).append(" ").append(" FROM ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST).append(" t1 ").append(" LEFT JOIN (SELECT * FROM ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" t4 WHERE t4.");
            append = append.append(DatabaseHelper.BATTLES_ALL).append(" = ").append(" (SELECT t3.");
            append = append.append(DatabaseHelper.BATTLES_ALL).append(" FROM ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" t3 ").append(" WHERE t3.");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("'").append(" GROUP BY t3.");
            append = append.append(DatabaseHelper.BATTLES_ALL).append(" ORDER BY t3.");
            append = append.append(DatabaseHelper.BATTLES_ALL).append(" DESC LIMIT 1 OFFSET 1) ").append(") t2 on t2.");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = t1.");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" and ").append(" t2.");
            append = append.append(DatabaseHelper.TANK_ID_COLUMN).append(" = t1.");
            append = append.append(DatabaseHelper.TANK_ID_COLUMN).append("  ").append(" WHERE t1.");
            cursor = fdb.rawQuery(append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("'").append(" ORDER BY ").append(order_by).append(" DESC").toString(), null);
            append2 = new StringBuilder().append("select * from( SELECT ");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(", ").append(" COUNT( ");
            append2 = append2.append(DatabaseHelper.BATTLES_COLUMN).append(") AS CB ").append(" FROM (SELECT ");
            append2 = append2.append(DatabaseHelper.BATTLES_COLUMN).append(", ");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(" FROM ");
            append2 = append2.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" ").append(" WHERE ");
            append2 = append2.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' ").append(" GROUP BY ");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(", ");
            append2 = append2.append(DatabaseHelper.BATTLES_COLUMN).append(") ").append(" GROUP BY ");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(") t2 ").append(" LEFT JOIN ");
            append2 = append2.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST).append(" t1 ").append(" on t2.");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(" = t1.");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(" ").append(" WHERE t1.");
            cursorGraph = sdb.rawQuery(append2.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' ").append(" ORDER BY ").append(order_by).append(" DESC").toString(), null);
        } else {
            append = new StringBuilder().append("select ");
            append = append.append(DatabaseHelper.TANK_ID_COLUMN).append(", ");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(", ");
            append = append.append(DatabaseHelper.NAME_RU).append(", ");
            append = append.append(DatabaseHelper.NAME_EU).append(", ");
            append = append.append(DatabaseHelper.TANK_LEVEL).append(", ");
            append = append.append(DatabaseHelper.TANK_TYPE).append(", ");
            append = append.append(DatabaseHelper.TANK_NATION).append(", ");
            append = append.append(DatabaseHelper.MAX_XP).append(", ");
            append = append.append(DatabaseHelper.MAX_FRAG).append(", ");
            append = append.append(DatabaseHelper.MARK_OF_MASTER).append(", ");
            append = append.append(DatabaseHelper.IN_GAR).append(", ");
            append = append.append(DatabaseHelper.BATTLES_COLUMN).append(", ");
            append = append.append(DatabaseHelper.WINS_COLUMN).append(", ");
            append = append.append(DatabaseHelper.DAMAGE_COLUMN).append(", ");
            append = append.append(DatabaseHelper.KILLS_COLUMN).append(", ");
            append = append.append(DatabaseHelper.SPOT_COLUMN).append(", ");
            append = append.append(DatabaseHelper.CAPS_COLUMN).append(", ");
            append = append.append(DatabaseHelper.DROP_CAPS).append(", ");
            append = append.append(DatabaseHelper.EXP_COLUMN).append(", ");
            append = append.append(DatabaseHelper.WN6_COLUMN).append(", ");
            append = append.append(DatabaseHelper.REFF_COLUMN).append(", ");
            append = append.append(DatabaseHelper.WN8_COLUMN).append(", ");
            append = append.append(DatabaseHelper.BATTLES_ALL).append(" from ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST).append(" t1 where ");
            cursor = fdb.rawQuery(append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' ORDER BY ").append(order_by).append(" DESC").toString(), null);
            append2 = new StringBuilder().append("select * from( SELECT ");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(", ").append(" COUNT( ");
            append2 = append2.append(DatabaseHelper.BATTLES_COLUMN).append(") AS CB ").append(" FROM (SELECT ");
            append2 = append2.append(DatabaseHelper.BATTLES_COLUMN).append(", ");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(" FROM ");
            append2 = append2.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT).append(" ").append(" WHERE ");
            append2 = append2.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' ").append(" GROUP BY ");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(", ");
            append2 = append2.append(DatabaseHelper.BATTLES_COLUMN).append(") ").append(" GROUP BY ");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(") t2 ").append(" LEFT JOIN ");
            append2 = append2.append(DatabaseHelper.DATABASE_TABLE_TANK_STAT_LAST).append(" t1 ").append(" on t2.");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(" = t1.");
            append2 = append2.append(DatabaseHelper.TANK_ID_COLUMN).append(" ").append(" WHERE t1.");
            cursorGraph = sdb.rawQuery(append2.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("' ").append(" ORDER BY ").append(order_by).append(" DESC").toString(), null);
        }
        cursor.moveToFirst();
        cursorGraph.moveToFirst();
        if (cursor.getCount() != 0) {
            ExpandableListView expandableList = (ExpandableListView) getActivity().findViewById(R.id.expandableListViewTanks);
            ArrayList<String> groups = new ArrayList<>();
            ArrayList<Object> childItems = new ArrayList<>();
            do {
                if (cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN)) != 0) {
                    long battles = 0;
                    double wins = 0.0d;
                    double damage = 0.0d;
                    double kills = 0.0d;
                    double spots = 0.0d;
                    double exp = 0.0d;
                    double wn6 = 0.0d;
                    double er = 0.0d;
                    double wn8 = 0.0d;
                    if (zam_count > 1) {
                        long j = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN));
                        StringBuilder append3 = new StringBuilder().append("T2_");
                        battles = j - cursor.getLong(cursor.getColumnIndex(append3.append(DatabaseHelper.BATTLES_COLUMN).toString()));
                        double d = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN));
                        append3 = new StringBuilder().append("T2_");
                        wins = d - cursor.getDouble(cursor.getColumnIndex(append3.append(DatabaseHelper.WINS_COLUMN).toString()));
                        d = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN));
                        append3 = new StringBuilder().append("T2_");
                        damage = d - cursor.getDouble(cursor.getColumnIndex(append3.append(DatabaseHelper.DAMAGE_COLUMN).toString()));
                        d = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN));
                        append3 = new StringBuilder().append("T2_");
                        kills = d - cursor.getDouble(cursor.getColumnIndex(append3.append(DatabaseHelper.KILLS_COLUMN).toString()));
                        d = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN));
                        append3 = new StringBuilder().append("T2_");
                        spots = d - cursor.getDouble(cursor.getColumnIndex(append3.append(DatabaseHelper.SPOT_COLUMN).toString()));
                        d = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN));
                        append3 = new StringBuilder().append("T2_");
                        exp = d - cursor.getDouble(cursor.getColumnIndex(append3.append(DatabaseHelper.EXP_COLUMN).toString()));
                        d = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN));
                        append3 = new StringBuilder().append("T2_");
                        wn6 = d - cursor.getDouble(cursor.getColumnIndex(append3.append(DatabaseHelper.WN6_COLUMN).toString()));
                        d = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN));
                        append3 = new StringBuilder().append("T2_");
                        er = d - cursor.getDouble(cursor.getColumnIndex(append3.append(DatabaseHelper.REFF_COLUMN).toString()));
                        d = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN));
                        append3 = new StringBuilder().append("T2_");
                        wn8 = d - cursor.getDouble(cursor.getColumnIndex(append3.append(DatabaseHelper.WN8_COLUMN).toString()));
                    }
                    CheckBox checkBox = (CheckBox) this.mViewHierarchy.findViewById(R.id.checkBoxChengeOnly);
                    if ((checkBox.isChecked() && battles != 0) || !checkBox.isChecked()) {
                        double persent_win_rez = (double) new BigDecimal(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN))).setScale(2, 5).floatValue();
                        String graphStatExist = "false";
                        if (cursorGraph.getLong(cursorGraph.getColumnIndex("CB")) > 1) {
                            graphStatExist = "true";
                        }
                        append = new StringBuilder();
                        append = append.append(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_RU))).append("$");
                        append = append.append(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN))).append("$").append(String.format("%.2f", persent_win_rez)).append("$");
                        append = append.append(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NATION))).append("$");
                        append = append.append(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE))).append("$");
                        append = append.append(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_LEVEL))).append("$");
                        append = append.append(cursor.getString(cursor.getColumnIndex(DatabaseHelper.MARK_OF_MASTER))).append("$");
                        groups.add(append.append(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_ID_COLUMN))).append("$").append(String.valueOf(battles)).append("$").append(String.format("%.2f", wins)).append("$").append(graphStatExist).toString());
                        ArrayList<String> child = new ArrayList<>();
                        double persent_rez = (double) new BigDecimal(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN))).setScale(2, 5).floatValue();
                        child.add(getString(R.string.ttl_tanks_stat1) + "$" + String.format("%.2f", persent_rez) + "$" + String.format("%.2f", damage) + "$" + String.valueOf(battles));
                        persent_rez = (double) new BigDecimal(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN))).setScale(2, 5).floatValue();
                        child.add(getString(R.string.ttl_tanks_stat2) + "$" + String.format("%.2f", persent_rez) + "$" + String.format("%.2f", kills) + "$" + String.valueOf(battles));
                        persent_rez = (double) new BigDecimal(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN))).setScale(2, 5).floatValue();
                        child.add(getString(R.string.ttl_tanks_stat3) + "$" + String.format("%.2f", persent_rez) + "$" + String.format("%.2f", spots) + "$" + String.valueOf(battles));
                        append = new StringBuilder().append(getString(R.string.ttl_tanks_stat4)).append("$");
                        child.add(append.append(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MAX_FRAG))).append("$").append(String.format("%.2f", exp)).append("$").append(String.valueOf(battles)).toString());
                        append = new StringBuilder().append(getString(R.string.ttl_tanks_stat5)).append("$");
                        child.add(append.append(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN))).append("$").append(String.format("%.2f", exp)).append("$").append(String.valueOf(battles)).toString());
                        append = new StringBuilder().append(getString(R.string.ttl_tanks_stat6)).append("$");
                        child.add(append.append(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.MAX_XP))).append("$").append(String.format("%.2f", exp)).append("$").append(String.valueOf(battles)).toString());
                        append = new StringBuilder().append(getString(R.string.ttl_tanks_stat7)).append("$");
                        double tmp = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN));
                        child.add(append.append(String.format("%.2f", tmp)).append("$").append(String.format("%.2f", wn6)).append("$").append(String.valueOf(battles)).toString());
                        append = new StringBuilder().append(getString(R.string.ttl_tanks_stat8)).append("$");
                        tmp = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN));
                        child.add(append.append(String.format("%.2f", tmp)).append("$").append(String.format("%.2f", er)).append("$").append(String.valueOf(battles)).toString());
                        append = new StringBuilder().append(getString(R.string.ttl_tanks_stat9)).append("$");
                        tmp = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN));
                        child.add(append.append(String.format("%.2f", tmp)).append("$").append(String.format("%.2f", wn8)).append("$").append(String.valueOf(battles)).toString());
                        childItems.add(child);
                    }
                }
                cursorGraph.moveToNext();
            } while (cursor.moveToNext());
            cursorGraph.close();
            cursor.close();
            MyExpandableAdapter adapter = new MyExpandableAdapter(groups, childItems, getActivity());
            expandableList.setGroupIndicator(null);
            expandableList.setAdapter(adapter);
        }
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
