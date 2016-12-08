package org.rmorozov.wot_stats;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;


public class SessionHistoryItem extends Fragment {
    private static final String ARG_PARAM1 = "section_number";
    DatabaseHelper dbHelper;
    private int mParam1;
    public View mViewHierarchy;
    List<String> statListAvg;
    List<String> statListDelta;
    List<String> statListName;
    List<String> statListValue;
    TanksSession[] tanksFirst;
    TanksSession[] tanksSecond;

    private int getColorByEFF(float value){
        Resources resources = mViewHierarchy.getResources();
        if (value < 615.0F) {
            return resources.getColor(R.color.olen_red);
        } else if (value >= 615.0F && value < 870.0F) {
            return resources.getColor(R.color.olen_orange);
        } else if (value >= 870.0F && value < 1175.0F) {
            return resources.getColor(R.color.olen_yellow);
        } else if (value >= 1175.0F && value < 1525.0F) {
            return resources.getColor(R.color.olen_green);
        } else if (value >= 1525.0F && value < 1850.0F) {
            return resources.getColor(R.color.olen_purpure);
        } else if (value >= 1850.0F) {
            return resources.getColor(R.color.olen_purple);
        }
        return 0;
    }

    private int getColorByWN6(float value) {
        Resources resources = mViewHierarchy.getResources();
        if (value < 460.0F) {
            return resources.getColor(R.color.olen_red);
        } else if (value >= 460.0F && value < 850.0F) {
            return resources.getColor(R.color.olen_orange);
        } else if (value >= 850.0F && value < 1215.0F) {
            return resources.getColor(R.color.olen_yellow);
        } else if (value >= 1215.0F && value < 1620.0F) {
            return resources.getColor(R.color.olen_green);
        } else if (value >= 1620.0F && value < 1960.0F) {
            return resources.getColor(R.color.olen_purpure);
        } else if (value >= 1960.0F) {
            return resources.getColor(R.color.olen_purple);
        }
        return 0;
    }

    private int getColorByWN8(float value) {
        Resources resources = mViewHierarchy.getResources();
        if (value < 370.0F) {
            return resources.getColor(R.color.olen_red);
        } else if (value >= 370.0F && value < 845.0F) {
            return resources.getColor(R.color.olen_orange);
        } else if (value >= 845.0F && value < 1395.0F) {
            return resources.getColor(R.color.olen_yellow);
        } else if (value >= 1395.0F && value < 2070.0F) {
            return resources.getColor(R.color.olen_green);
        } else if (value >= 2070.0F && value < 2715.0F) {
            return resources.getColor(R.color.olen_purpure);
        } else if (value >= 2715.0F) {
            return resources.getColor(R.color.olen_purple);
        }
        return 0;
    }

    private int getColorByWins(float value) {
        Resources resources = mViewHierarchy.getResources();
        if (value < 47.0F) {
            return resources.getColor(R.color.olen_red);
        } else if (value >= 47.0F && value < 49.0F) {
            return resources.getColor(R.color.olen_orange);
        } else if (value >= 49.0F && value < 52.5D) {
            return resources.getColor(R.color.olen_yellow);
        } else if (value >= 52.5D && value < 58.0F) {
            return resources.getColor(R.color.olen_green);
        } else if (value >= 58.0F && value < 65.0F) {
            return resources.getColor(R.color.olen_purpure);
        } else if (value >= 65.0F) {
            return resources.getColor(R.color.olen_purple);
        }
        return 0;
    }

    private String getLevelString(String strLevelArab) {
        switch (Integer.parseInt(strLevelArab)) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            default: return "X";
        }
    }

    private void setLabelColor(TextView textView) {
        if (textView.getText().toString().lastIndexOf("-") < 0) {
            textView.setTextColor(getResources().getColor(R.color.plus));
            textView.setText("+" + textView.getText());
            return;
        }
        textView.setTextColor(getResources().getColor(R.color.minus));
    }

    private View setValueToDetailView(double firstValue, double secondValue, double param1, double param2,
                                      String statName, LayoutInflater inflater, String spec) {
        @SuppressLint("InflateParams") View viewDetail = inflater.inflate(R.layout.fragment_session_history_tank_detal_item, null);
        TextView textViewStatName = (TextView) viewDetail.findViewById(R.id.textViewDetalStatName);
        textViewStatName.setText(statName);
        TextView textViewStatValue = (TextView) viewDetail.findViewById(R.id.textViewDetalStatValue);
        TextView textViewStatDelta = (TextView) viewDetail.findViewById(R.id.textViewDetalStatDelta);
        TextView textViewStatAvg = (TextView) viewDetail.findViewById(R.id.textViewDetalStatAvg);
        if (spec.equals(DatabaseHelper.WN8_COLUMN)) {
            textViewStatValue.setText(String.format("%.2f", param1));
            textViewStatDelta.setText(String.format("%.2f", firstValue - secondValue));
            textViewStatAvg.setText(String.format("%.2f", firstValue));
        }
        else {
            textViewStatValue.setText(String.format("%.2f", Math.abs((firstValue - secondValue * param1) / param2)));
            textViewStatDelta.setText(String.format("%.2f", firstValue - secondValue));
            textViewStatAvg.setText(String.format("%.2f", firstValue));
        }
        setLabelColor(textViewStatDelta);
        if (statName.equals(mViewHierarchy.getResources().getString(R.string.session_history2))) {
            textViewStatValue.setTextColor(getColorByWins(Float.parseFloat(textViewStatValue.getText().toString().replace(",", "."))));
            textViewStatAvg.setTextColor(getColorByWins(Float.parseFloat(textViewStatAvg.getText().toString().replace(",", "."))));
        }
        if (statName.equals(mViewHierarchy.getResources().getString(R.string.session_history9))) {
            textViewStatValue.setTextColor(getColorByWN8(Float.parseFloat(textViewStatValue.getText().toString().replace(",", "."))));
            textViewStatAvg.setTextColor(getColorByWN8(Float.parseFloat(textViewStatAvg.getText().toString().replace(",", "."))));
        }
        return viewDetail;
    }

    private void showPlayerSession(String playerId) {
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        long battlesCountFirst = 0;
        long battlesCountSecond = 0;
        long battles = 0;
        double wins = 0;
        double damage = 0;
        double kills = 0;
        double spots = 0;
        //double caps = 0;
        //double dropCaps = 0;
        double exp = 0;
        double wn6 = 0;
        double re = 0;
        double wn8 = 0;
        double winsAvg1 = 0;
        double damageAvg1 = 0;
        double killsAvg1 = 0;
        double spotsAvg1 = 0;
        //double capsAvg1 = 0;
        //double dropCapsAvg1 = 0;
        double expAvg1 = 0;
        double wn6Avg1 = 0;
        double reAvg1 = 0;
        double wn8Avg1 = 0;
        double winsAvg2 = 0;
        double damageAvg2 = 0;
        double killsAvg2 = 0;
        double spotsAvg2 = 0;
        //double capsAvg2 = 0;
        //double dropCapsAvg2 = 0;
        double expAvg2 = 0;
        double wn6Avg2 = 0;
        double reAvg2 = 0;
        double wn8Avg2 = 0;

        statListName = new ArrayList<>();
        statListValue = new ArrayList<>();
        statListDelta = new ArrayList<>();
        statListAvg = new ArrayList<>();

        Cursor cursor = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_SH +
                " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN +
                " = \'" + playerId + "\' ORDER BY " + DatabaseHelper.BATTLES_COLUMN + " DESC", null);
        try {
            if (cursor.moveToFirst()) {
                for (int i = 0; i <= mParam1; i++) {
                    battlesCountFirst = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN));
                    winsAvg1 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WINRATE_COLUMN));
                    damageAvg1 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN));
                    killsAvg1 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN));
                    spotsAvg1 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN));
                    expAvg1 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN));
                    wn6Avg1 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN));
                    reAvg1 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN));
                    wn8Avg1 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN));
                    cursor.moveToNext();
                }
                battlesCountSecond = cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN));
                winsAvg2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WINRATE_COLUMN));
                damageAvg2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN));
                killsAvg2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN));
                spotsAvg2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN));
                expAvg2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN));
                wn6Avg2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN));
                reAvg2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN));
                wn8Avg2 = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN8_COLUMN));
                double param1 = ((double) battlesCountSecond) / battlesCountFirst;
                double param2 = ((double) (battlesCountFirst - battlesCountSecond))/battlesCountFirst;
                wins = (winsAvg1 - winsAvg2 * param1) / param2;
                damage = (damageAvg1 - damageAvg2 * param1) / param2;
                kills = (killsAvg1 - killsAvg2 * param1) / param2;
                spots = (spotsAvg1 - spotsAvg2 * param1) / param2;
                exp = (expAvg1 - expAvg2 * param1) / param2;
                wn6 = (wn6Avg1 - wn6Avg2 * param1) / param2;
                re = (reAvg1 - reAvg2 * param1) / param2;
                wn8 = (wn8Avg1 - wn8Avg2 * param1) / param2;
            }
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history1));
            statListValue.add(String.valueOf(battlesCountFirst - battlesCountSecond));
            statListDelta.add("");
            statListAvg.add("");
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history2));
            statListValue.add(String.format("%.2f", Math.abs(wins)));
            statListDelta.add(String.format("%.2f", winsAvg1 - winsAvg2));
            statListAvg.add(String.format("%.2f", winsAvg1));
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history3));
            statListValue.add(String.format("%.2f", damage));
            statListDelta.add(String.format("%.2f", damageAvg1 - damageAvg2));
            statListAvg.add(String.format("%.2f", damageAvg1));
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history4));
            statListValue.add(String.format("%.2f", kills));
            statListDelta.add(String.format("%.2f", killsAvg1 - killsAvg2));
            statListAvg.add(String.format("%.2f", killsAvg1));
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history5));
            statListValue.add(String.format("%.2f", spots));
            statListDelta.add(String.format("%.2f", spotsAvg1 - spotsAvg2));
            statListAvg.add(String.format("%.2f", spotsAvg1));
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history6));
            statListValue.add(String.format("%.2f", exp));
            statListDelta.add(String.format("%.2f", expAvg1 - expAvg2));
            statListAvg.add(String.format("%.2f", expAvg1));
        } finally {
            cursor.close();
        }
        try (Cursor cursorMax = sdb.rawQuery("SELECT " + DatabaseHelper.BATTLES_ALL +
                " FROM " + DatabaseHelper.DATABASE_TABLE_TANK_STAT +
                " WHERE " + DatabaseHelper.PLAYER_ID_COLUMN +
                " = \'" + playerId + "\' GROUP BY " + DatabaseHelper.BATTLES_ALL +
                " ORDER BY " + DatabaseHelper.BATTLES_ALL + " DESC", null)) {
            if (mParam1 < cursorMax.getCount() - 1) {
                if (cursorMax.moveToFirst()) {
                    for (int i = 0; i <= mParam1; i++) {
                        battlesCountFirst = cursorMax.getLong(cursorMax.getColumnIndex(DatabaseHelper.BATTLES_ALL));
                        cursorMax.moveToNext();
                        battlesCountSecond = cursorMax.getLong(cursorMax.getColumnIndex(DatabaseHelper.BATTLES_ALL));
                    }
                }
                cursor = sdb.rawQuery("SELECT t1." + DatabaseHelper.TANK_ID_COLUMN +
                        ", t1." + DatabaseHelper.PLAYER_ID_COLUMN +
                        ", t1." + DatabaseHelper.NAME_RU +
                        ", t1." + DatabaseHelper.NAME_EU +
                        ", t1." + DatabaseHelper.TANK_LEVEL +
                        ", t1." + DatabaseHelper.TANK_TYPE +
                        ", t1." + DatabaseHelper.TANK_NATION +
                        ", t1." + DatabaseHelper.MAX_XP +
                        ", t1." + DatabaseHelper.MAX_FRAG +
                        ", t1." + DatabaseHelper.MARK_OF_MASTER +
                        ", t1." + DatabaseHelper.IN_GAR +
                        ", t1." + DatabaseHelper.BATTLES_COLUMN +
                        ", t1." + DatabaseHelper.WINS_COLUMN +
                        ", t1." + DatabaseHelper.DAMAGE_COLUMN +
                        ", t1." + DatabaseHelper.KILLS_COLUMN +
                        ", t1." + DatabaseHelper.SPOT_COLUMN +
                        ", t1." + DatabaseHelper.CAPS_COLUMN +
                        ", t1." + DatabaseHelper.DROP_CAPS +
                        ", t1." + DatabaseHelper.EXP_COLUMN +
                        ", t1." + DatabaseHelper.WN6_COLUMN +
                        ", t1." + DatabaseHelper.REFF_COLUMN +
                        ", t1." + DatabaseHelper.WN8_COLUMN +
                        ", t5." + DatabaseHelper.WINS_COLUMN + " AS T5_" + DatabaseHelper.WINS_COLUMN +
                        ", t5." + DatabaseHelper.DAMAGE_COLUMN + " AS T5_" + DatabaseHelper.DAMAGE_COLUMN +
                        ", t5." + DatabaseHelper.SPOT_COLUMN + " AS T5_" + DatabaseHelper.SPOT_COLUMN +
                        ", t5." + DatabaseHelper.EXP_DEF_COLUMN + " AS T5_" + DatabaseHelper.EXP_DEF_COLUMN +
                        ", t5." + DatabaseHelper.KILLS_COLUMN + " AS T5_" + DatabaseHelper.KILLS_COLUMN +
                        ", t2." + DatabaseHelper.BATTLES_COLUMN + " AS T2_" + DatabaseHelper.BATTLES_COLUMN +
                        ", t2." + DatabaseHelper.WINS_COLUMN + " AS T2_" + DatabaseHelper.WINS_COLUMN +
                        ", t2." + DatabaseHelper.DAMAGE_COLUMN + " AS T2_" + DatabaseHelper.DAMAGE_COLUMN +
                        ", t2." + DatabaseHelper.KILLS_COLUMN + " AS T2_" + DatabaseHelper.KILLS_COLUMN +
                        ", t2." + DatabaseHelper.SPOT_COLUMN + " AS T2_" + DatabaseHelper.SPOT_COLUMN +
                        ", t2." + DatabaseHelper.CAPS_COLUMN + " AS T2_" + DatabaseHelper.CAPS_COLUMN +
                        ", t2." + DatabaseHelper.DROP_CAPS + " AS T2_" + DatabaseHelper.DROP_CAPS +
                        ", t2." + DatabaseHelper.EXP_COLUMN + " AS T2_" + DatabaseHelper.EXP_COLUMN +
                        ", t2." + DatabaseHelper.WN6_COLUMN + " AS T2_" + DatabaseHelper.WN6_COLUMN +
                        ", t2." + DatabaseHelper.REFF_COLUMN + " AS T2_" + DatabaseHelper.REFF_COLUMN +
                        ", t2." + DatabaseHelper.WN8_COLUMN + " AS T2_" + DatabaseHelper.WN8_COLUMN +
                        ", t2." + DatabaseHelper.BATTLES_ALL + " AS T2_" + DatabaseHelper.BATTLES_ALL +
                        " FROM " + DatabaseHelper.DATABASE_TABLE_TANK_STAT + " t1 LEFT JOIN " +
                        "(SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_TANK_STAT +
                        " t4 WHERE t4." + DatabaseHelper.BATTLES_ALL + " = " + battlesCountSecond + ") " +
                        "t2 ON t2." + DatabaseHelper.PLAYER_ID_COLUMN + " = t1." + DatabaseHelper.PLAYER_ID_COLUMN + " AND " +
                        "t2." + DatabaseHelper.TANK_ID_COLUMN + " = t1." + DatabaseHelper.TANK_ID_COLUMN +
                        " LEFT JOIN " + DatabaseHelper.DATABASE_TABLE_TANKS_EXP +
                        " t5 ON t1." + DatabaseHelper.TANK_ID_COLUMN + " = t5." + DatabaseHelper.TANK_ID_COLUMN +
                        " WHERE t1." + DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + playerId + "\' AND " +
                        "t1." + DatabaseHelper.BATTLES_ALL + " = " + battlesCountFirst + " AND " +
                        "(NOT t1." + DatabaseHelper.BATTLES_COLUMN + " = t2." + DatabaseHelper.BATTLES_COLUMN +
                        " OR t2." + DatabaseHelper.BATTLES_COLUMN + " IS NULL) " +
                        "ORDER BY t1." + DatabaseHelper.TANK_LEVEL + " DESC", null);
                try {
                    if (cursor.moveToFirst()) {
                        tanksFirst = new TanksSession[cursor.getCount()];
                        tanksSecond = new TanksSession[cursor.getCount()];
                        for (int i = 0; i < cursor.getCount(); i++) {
                            TanksSession session = new TanksSession();
                            session.setTankId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_ID_COLUMN)));
                            session.setNation(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NATION)));
                            session.setType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_TYPE)));
                            session.setLevelT(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_LEVEL)));
                            session.setMarkOfMastery(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.MARK_OF_MASTER)));
                            if (mViewHierarchy.getResources().getConfiguration().locale.getCountry().equals("RU")) {
                                session.setNameT(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_RU)));
                            } else {
                                session.setNameT(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_EU)));
                            }
                            session.setBattles(cursor.getLong(cursor.getColumnIndex(DatabaseHelper.BATTLES_COLUMN)));
                            session.setWins(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WINS_COLUMN)));
                            session.setDamageDealt(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.DAMAGE_COLUMN)));
                            session.setFrags(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.KILLS_COLUMN)));
                            session.setSpotted(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.SPOT_COLUMN)));
                            session.setCapturePoints(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.CAPS_COLUMN)));
                            session.setDroppedCapturePoints(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.DROP_CAPS)));
                            session.setBattleAvgXp(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.EXP_COLUMN)));
                            session.setWn6(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.WN6_COLUMN)));
                            session.setEr(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.REFF_COLUMN)));
                            session.setExpWinrate(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.WINS_COLUMN)));
                            session.setExpDamage(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.DAMAGE_COLUMN)));
                            session.setExpKills(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.KILLS_COLUMN)));
                            session.setExpSpots(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.SPOT_COLUMN)));
                            session.setExpDrop(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.EXP_DEF_COLUMN)));
                            session.setWn8(getCalcWn8(session));
                            tanksFirst[i] = session;
                            session = new TanksSession();
                            session.setBattles(cursor.getLong(cursor.getColumnIndex("T2_" + DatabaseHelper.BATTLES_COLUMN)));
                            session.setWins(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.WINS_COLUMN)));
                            session.setDamageDealt(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.DAMAGE_COLUMN)));
                            session.setFrags(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.KILLS_COLUMN)));
                            session.setSpotted(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.SPOT_COLUMN)));
                            session.setCapturePoints(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.CAPS_COLUMN)));
                            session.setDroppedCapturePoints(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.DROP_CAPS)));
                            session.setBattleAvgXp(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.EXP_COLUMN)));
                            session.setWn6(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.WN6_COLUMN)));
                            session.setEr(cursor.getDouble(cursor.getColumnIndex("T2_" + DatabaseHelper.REFF_COLUMN)));
                            session.setExpWinrate(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.WINS_COLUMN)));
                            session.setExpDamage(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.DAMAGE_COLUMN)));
                            session.setExpKills(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.KILLS_COLUMN)));
                            session.setExpSpots(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.SPOT_COLUMN)));
                            session.setExpDrop(cursor.getDouble(cursor.getColumnIndex("T5_" + DatabaseHelper.EXP_DEF_COLUMN)));
                            session.setWn8(getCalcWn8(session));
                            tanksSecond[i] = session;
                            tanksFirst[i].setWn8Session(getCalcWn8Session(tanksFirst[i], tanksSecond[i]));
                            battles += tanksFirst[i].getBattles() - tanksSecond[i].getBattles();
                            cursor.moveToNext();
                        }
                        for (int i = 0; i < tanksFirst.length; i++) {
                            double param1 = ((double) tanksSecond[i].getBattles()) / tanksFirst[i].getBattles();
                            double param2 = ((double) tanksFirst[i].getBattles() - tanksSecond[i].getBattles()) / tanksFirst[i].getBattles();
                            double param3 = ((double) tanksFirst[i].getBattles() - tanksSecond[i].getBattles()) / battles;
                            wins += (tanksFirst[i].getWins() - tanksSecond[i].getWins() * param1) / param2 * param3;
                            damage += (tanksFirst[i].getDamageDealt() - tanksSecond[i].getDamageDealt() * param1) / param2 * param3;
                            kills += (tanksFirst[i].getFrags() - tanksSecond[i].getFrags() * param1) / param2 * param3;
                            spots += (tanksFirst[i].getSpotted() - tanksSecond[i].getSpotted() * param1) / param2 * param3;
                            //caps += (tanksFirst[i].getCapturePoints() - tanksSecond[i].getCapturePoints() * param1) / param2 * param3;
                            //dropCaps += (tanksFirst[i].getDroppedCapturePoints() - tanksSecond[i].getDroppedCapturePoints() * param1) / param2 * param3;
                            exp += (tanksFirst[i].getBattleAvgXp() - tanksSecond[i].getBattleAvgXp() * param1) / param2 * param3;
                            wn6 += (tanksFirst[i].getWn6() - tanksSecond[i].getWn6() * param1) / param2 * param3;
                            re += (tanksFirst[i].getEr() - tanksSecond[i].getEr() * param1) / param2 * param3;
                            wn8 += (tanksFirst[i].getWn8() - tanksSecond[i].getWn8() * param1) / param2 * param3;
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        long tempBattlesAll = 0;
        double tempWn8 = 0;
        double tempWn6 = 0;
        double tempEr = 0;
        if (tanksFirst != null) {
            for (int i = 0; i < tanksFirst.length; i++) {
                tempBattlesAll += tanksFirst[i].getBattles() - tanksSecond[i].getBattles();
            }
            for (int i = 0; i < tanksFirst.length; i++) {
                double param1 = ((double) tanksSecond[i].getBattles()) / tanksFirst[i].getBattles();
                double param2 = ((double) tanksFirst[i].getBattles() - tanksSecond[i].getBattles()) / tanksFirst[i].getBattles();
                double param3 = ((double) tanksFirst[i].getBattles() - tanksSecond[i].getBattles()) / tempBattlesAll;
                tempWn6 += (tanksFirst[i].getWn6() - tanksSecond[i].getWn6() * param1) / param2 * param3;
                tempEr += (tanksFirst[i].getEr() - tanksSecond[i].getEr() * param1) / param2 * param3;
            }
            tempBattlesAll = 0;
            for (int i = 0; i < tanksFirst.length; i++) {
                if (tanksFirst[i].getExpWinrate() != 0) {
                    tempBattlesAll += tanksFirst[i].getBattles() - tanksSecond[i].getBattles();
                }
            }
            if (tempBattlesAll != 0) {
                for (int i = 0; i < tanksFirst.length; i++) {
                    if (tanksFirst[i].getExpWinrate() != 0) {
                        double param3 = ((double) tanksFirst[i].getBattles() - tanksSecond[i].getBattles()) / tempBattlesAll;
                        tempWn8 += tanksFirst[i].getWn8Session() * param3;
                    }
                }

            }
            else {
                tempWn8 = 0;
            }
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history7));
            statListValue.add(String.format("%.2f", tempEr));
            statListDelta.add(String.format("%.2f", reAvg1 - reAvg2));
            statListAvg.add(String.format("%.2f", reAvg1));
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history8));
            statListValue.add(String.format("%.2f", tempWn6));
            statListDelta.add(String.format("%.2f", wn6Avg1 - wn6Avg2));
            statListAvg.add(String.format("%.2f", wn6Avg1));
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history9));
            statListValue.add(String.format("%.2f", tempWn8));
            statListDelta.add(String.format("%.2f", wn8Avg1 - wn8Avg2));
            statListAvg.add(String.format("%.2f", wn8Avg1));
            for (int i = 0; i < tanksFirst.length; i++) {
                statListName.add(DatabaseHelper.DATABASE_TABLE_TANKS);
                statListValue.add(String.valueOf(i));
            }
        } else {
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history7));
            statListValue.add(String.format("%.2f", re));
            statListDelta.add(String.format("%.2f", reAvg1 - reAvg2));
            statListAvg.add(String.format("%.2f", reAvg1));
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history8));
            statListValue.add(String.format("%.2f", wn6));
            statListDelta.add(String.format("%.2f", wn6Avg1 - wn6Avg2));
            statListAvg.add(String.format("%.2f", wn6Avg1));
            statListName.add(mViewHierarchy.getResources().getString(R.string.session_history9));
            statListValue.add(String.format("%.2f", wn8));
            statListDelta.add(String.format("%.2f", wn8Avg1 - wn8Avg2));
            statListAvg.add(String.format("%.2f", wn8Avg1));
        }
        ListView listView = (ListView) mViewHierarchy.findViewById(R.id.listViewSessionInfo);
        listView.setAdapter(new MyArrayAdapter(mViewHierarchy.getContext(), statListName));
    }

    double getCalcWn8(TanksSession tank) {
        double dblDef = tank.getDroppedCapturePoints();
        if (dblDef > 2.2d) {
            dblDef = 2.2d;
        }
        if (tank.getExpDamage() == 0.0d) {
            return 0.0d;
        }
        double rDAMAGE = tank.getDamageDealt() / tank.getExpDamage();
        double rSPOT = tank.getSpotted() / tank.getExpSpots();
        double rFRAG = tank.getFrags() / tank.getExpKills();
        double rDEF = dblDef / tank.getExpDrop();
        double rWINc = Math.max(0.0d, ((tank.getWins() / tank.getExpWinrate()) - 0.71d) / 0.29000000000000004d);
        double rDAMAGEc = Math.max(0.0d, (rDAMAGE - 0.22d) / 0.78d); //3.127
        double rFRAGc = Math.max(0.0d, Math.min(0.2d + rDAMAGEc, (rFRAG - 0.12d) / 0.88d));//3.327
        return ((((980.0d * rDAMAGEc) + ((210.0d * rDAMAGEc) * rFRAGc)) + ((155.0d * rFRAGc) * Math.max(0.0d, Math.min(0.1d + rDAMAGEc, (rSPOT - 0.38d) / 0.62d)))) + ((75.0d * Math.max(0.0d, Math.min(0.1d + rDAMAGEc, (rDEF - 0.1d) / 0.9d))) * rFRAGc)) + (145.0d * Math.min(1.8d, rWINc));
    }

    double getCalcWn8Session(TanksSession tankFirst, TanksSession tankSecond) {
        double dblFrags = (tankFirst.getFrags() * tankFirst.getBattles() - tankSecond.getFrags() * tankSecond.getBattles()) / (tankFirst.getBattles() - tankSecond.getBattles());
        double dblDamage = (tankFirst.getDamageDealt() * tankFirst.getBattles() - tankSecond.getDamageDealt() * tankSecond.getBattles()) / (tankFirst.getBattles() - tankSecond.getBattles());
        double dblSpot = (tankFirst.getSpotted() * tankFirst.getBattles() - tankSecond.getSpotted() * tankSecond.getBattles()) / (tankFirst.getBattles() - tankSecond.getBattles());
        double dblWinrate = (tankFirst.getWins() * tankFirst.getBattles() - tankSecond.getWins() * tankSecond.getBattles()) / (tankFirst.getBattles() - tankSecond.getBattles());
        double dblDef = (tankFirst.getDroppedCapturePoints() * tankFirst.getBattles() - tankSecond.getDroppedCapturePoints() * tankSecond.getBattles()) / (tankFirst.getBattles() - tankSecond.getBattles());
        if (dblDef > 2.2d) {
            dblDef = 2.2d;
        }
        if (tankFirst.getExpDamage() == 0.0d) {
            return 0.0d;
        }
        double rDAMAGE = dblDamage / tankFirst.getExpDamage();
        double rSPOT = dblSpot / tankFirst.getExpSpots();
        double rFRAG = dblFrags / tankFirst.getExpKills();
        double rDEF = dblDef / tankFirst.getExpDrop();
        double rWINc = Math.max(0.0d, ((dblWinrate / tankFirst.getExpWinrate()) - 0.71d) / 0.29000000000000004d);
        double rDAMAGEc = Math.max(0.0d, (rDAMAGE - 0.22d) / 0.78d);
        double rFRAGc = Math.max(0.0d, Math.min(0.2d + rDAMAGEc, (rFRAG - 0.12d) / 0.88d));
        return ((((980.0d * rDAMAGEc) + ((210.0d * rDAMAGEc) * rFRAGc)) + ((155.0d * rFRAGc) * Math.max(0.0d, Math.min(0.1d + rDAMAGEc, (rSPOT - 0.38d) / 0.62d)))) + ((75.0d * Math.max(0.0d, Math.min(0.1d + rDAMAGEc, (rDEF - 0.1d) / 0.9d))) * rFRAGc)) + (145.0d * Math.min(1.8d, rWINc));
    }

    @Override
    public void onPause() {
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SQLiteDatabase sdb = dbHelper.getReadableDatabase();
        String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
        String[] strArr = new String[3];
        strArr[0] = DatabaseHelper.PLAYER_NAME_COLUMN;
        strArr[1] = DatabaseHelper.PLAYER_ID_COLUMN;
        strArr[2] = DatabaseHelper.ACTIVE;
        Cursor cursor = sdb.query(str, strArr, DatabaseHelper.ACTIVE + " = ?", new String[]{"1"}, null, null, null);
        if (cursor.moveToFirst()) {
            String playerId = cursor.getString(1);
            cursor.close();
            View viewHierarchy = inflater.inflate(R.layout.fragment_session_history, container, false);
            mViewHierarchy = viewHierarchy;
            showPlayerSession(playerId);
            return viewHierarchy;
        }
        Toast.makeText(getActivity(), getString(R.string.no_select), Toast.LENGTH_SHORT).show();
        return null;
    }

    @SuppressWarnings("Convert2Lambda")
    public class MyArrayAdapter extends ArrayAdapter<String> {
        private final Context context;

        public MyArrayAdapter(Context context, List<String> values) {
            super(context, R.layout.simple_list_avg, values);
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view;
            if (statListName.get(position).equals(DatabaseHelper.DATABASE_TABLE_TANKS)) {
                view = inflater.inflate(R.layout.fragment_session_history_tank_item, parent, false);
                int i = Integer.parseInt(statListValue.get(position));
                TextView textViewTankLevel = (TextView) view.findViewById(R.id.textLevelS);
                TextView textViewTankName = (TextView) view.findViewById(R.id.textViewTankName);
                TextView textViewTankBattles = (TextView) view.findViewById(R.id.textViewTankDetalName);
                TextView textViewTankId = (TextView) view.findViewById(R.id.textViewDetalTankID);
                textViewTankName.setText(tanksFirst[i].getNameT());
                textViewTankLevel.setText(getLevelString(tanksFirst[i].getLevelT()));
                textViewTankBattles.setText(String.valueOf(tanksFirst[i].getBattles() - tanksSecond[i].getBattles()));
                textViewTankId.setText(tanksFirst[i].getTankId());

                LinearLayout linearLayoutDetal = (LinearLayout) view.findViewById(R.id.viewTanksDetal);
                double param1 = ((double) tanksSecond[i].getBattles()) / tanksFirst[i].getBattles();
                double param2 = ((double) tanksFirst[i].getBattles() - tanksSecond[i].getBattles()) / tanksFirst[i].getBattles();
                linearLayoutDetal.addView(setValueToDetailView(tanksFirst[i].getWins(), tanksSecond[i].getWins(), param1, param2, mViewHierarchy.getResources().getString(R.string.session_history2), inflater, ""));
                linearLayoutDetal.addView(setValueToDetailView(tanksFirst[i].getDamageDealt(), tanksSecond[i].getDamageDealt(), param1, param2, mViewHierarchy.getResources().getString(R.string.session_history3), inflater, ""));
                linearLayoutDetal.addView(setValueToDetailView(tanksFirst[i].getWn8(), tanksSecond[i].getWn8(), tanksFirst[i].getWn8Session(), param2, mViewHierarchy.getResources().getString(R.string.session_history9), inflater, DatabaseHelper.WN8_COLUMN));

                ImageView imageViewClass = (ImageView) view.findViewById(R.id.imageViewClassS);
                String type = tanksFirst[i].getType();
                switch (type) {
                    case "heavyTank":
                        imageViewClass.setImageResource(R.drawable.tank_ico_tt);
                        break;
                    case "mediumTank":
                        imageViewClass.setImageResource(R.drawable.tank_ico_st);
                        break;
                    case "SPG":
                        imageViewClass.setImageResource(R.drawable.tank_ico_ar);
                        break;
                    case "AT-SPG":
                        imageViewClass.setImageResource(R.drawable.tank_ico_pt);
                        break;
                    case "lightTank":
                        imageViewClass.setImageResource(R.drawable.tank_ico_lt);
                        break;
                }
                ImageView imageViewNation = (ImageView) view.findViewById(R.id.imageViewNationS);
                String nation = tanksFirst[i].getNation();
                switch (nation) {
                    case "СССР":
                    case "U.S.S.R.":
                        imageViewNation.setImageResource(R.drawable.nation_ussr);
                        break;
                    case "Германия":
                    case "Germany":
                        imageViewNation.setImageResource(R.drawable.nation_ger);
                        break;
                    case "США":
                    case "U.S.A.":
                        imageViewNation.setImageResource(R.drawable.nation_usa);
                        break;
                    case "Франция":
                    case "France":
                        imageViewNation.setImageResource(R.drawable.nation_franc);
                        break;
                    case "Япония":
                    case "Japan":
                        imageViewNation.setImageResource(R.drawable.nation_jap);
                        break;
                    case "Китай":
                    case "China":
                        imageViewNation.setImageResource(R.drawable.nation_china);
                        break;
                    case "Великобритания":
                    case "U.K.":
                        imageViewNation.setImageResource(R.drawable.nation_eng);
                        break;
                    case "Чехословакия":
                    case "Czechoslovakia":
                        imageViewNation.setImageResource(R.drawable.nation_czech);
                        break;
                }
                ImageView imageViewMark = (ImageView) view.findViewById(R.id.imageViewMarkS);
                long mark = tanksFirst[i].getMarkOfMastery();
                switch ((int)mark) {
                    case 1:
                        imageViewMark.setImageResource(R.drawable.mark_3);
                        break;
                    case 2:
                        imageViewMark.setImageResource(R.drawable.mark_2);
                        break;
                    case 3:
                        imageViewMark.setImageResource(R.drawable.mark_1);
                        break;
                    case 4:
                        imageViewMark.setImageResource(R.drawable.mark_4);
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mViewHierarchy.getContext(), TanksDetailActivity.class);
                        intent.putExtra(DatabaseHelper.TANK_ID_COLUMN, ((TextView) v.findViewById(R.id.viewSessionDetalTanksMain).findViewById(R.id.textViewDetalTankID)).getText());
                        startActivity(intent);
                    }
                });
            } else {
                view = inflater.inflate(R.layout.fragment_session_history_item, parent, false);
                TextView textViewStatName = (TextView) view.findViewById(R.id.textViewSessionName);
                TextView textViewStatValue = (TextView) view.findViewById(R.id.textViewSessionValue);
                TextView textViewStatDelta = (TextView) view.findViewById(R.id.textViewSessionDelta);
                TextView textViewStatAvg = (TextView) view.findViewById(R.id.textViewSessionAvg);
                textViewStatName.setText(statListName.get(position));
                textViewStatValue.setText(statListValue.get(position));
                textViewStatAvg.setText(statListAvg.get(position));
                if (statListDelta.get(position).equals("")) {
                    textViewStatDelta.setText("");
                } else if (statListDelta.get(position).lastIndexOf("-") == -1) {
                    textViewStatDelta.setText("+" + statListDelta.get(position));
                    textViewStatDelta.setTextColor(mViewHierarchy.getResources().getColor(R.color.plus));
                } else {
                    textViewStatDelta.setText(statListDelta.get(position));
                    textViewStatDelta.setTextColor(mViewHierarchy.getResources().getColor(R.color.minus));
                }
                if (statListName.get(position).equals(mViewHierarchy.getResources().getString(R.string.session_history2))) {
                    textViewStatValue.setTextColor(getColorByWins(Float.parseFloat(textViewStatValue.getText().toString().replace(",", "."))));
                    textViewStatAvg.setTextColor(getColorByWins(Float.parseFloat(textViewStatAvg.getText().toString().replace(",", "."))));
                } else if (statListName.get(position).equals(mViewHierarchy.getResources().getString(R.string.session_history7))) {
                    textViewStatValue.setTextColor(getColorByWN6(Float.parseFloat(textViewStatValue.getText().toString().replace(",", "."))));
                    textViewStatAvg.setTextColor(getColorByWN6(Float.parseFloat(textViewStatAvg.getText().toString().replace(",", "."))));
                } else if (statListName.get(position).equals(mViewHierarchy.getResources().getString(R.string.session_history8))) {
                    textViewStatValue.setTextColor(getColorByEFF(Float.parseFloat(textViewStatValue.getText().toString().replace(",", "."))));
                    textViewStatAvg.setTextColor(getColorByEFF(Float.parseFloat(textViewStatAvg.getText().toString().replace(",", "."))));

                } else if (statListName.get(position).equals(mViewHierarchy.getResources().getString(R.string.session_history9))) {
                    textViewStatValue.setTextColor(getColorByWN8(Float.parseFloat(textViewStatValue.getText().toString().replace(",", "."))));
                    textViewStatAvg.setTextColor(getColorByWN8(Float.parseFloat(textViewStatAvg.getText().toString().replace(",", "."))));
                }
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView textView = (TextView) v.findViewById(R.id.viewSessionDetalSMain).findViewById(R.id.textViewSessionName);
                        if (!textView.getText().equals(mViewHierarchy.getResources().getString(R.string.session_history1))) {
                            Intent intent = new Intent(mViewHierarchy.getContext(), SessionHistoryGraphActivity.class);
                            intent.putExtra("statistic_type", textView.getText());
                            startActivity(intent);
                        }
                    }
                });
            }
            return view;
        }
    }
}
