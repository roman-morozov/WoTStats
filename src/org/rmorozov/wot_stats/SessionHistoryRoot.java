package org.rmorozov.wot_stats;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class SessionHistoryRoot extends Fragment {
    int numOfTabs;
    String[] mTitles;
    SessionHistoryMainFragment adapter;
    DatabaseHelper dbHelper;
    ViewPager pager;
    SlidingTabLayout tabs;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
        SQLiteDatabase sdb = this.dbHelper.getReadableDatabase();
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
            StringBuilder append = new StringBuilder().append("select ");
            append = append.append(DatabaseHelper.LAST_BATTLE_COLUMN).append(" from ");
            append = append.append(DatabaseHelper.DATABASE_TABLE_SH).append(" where ");
            append = append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("'").append(" ORDER BY ");
            Cursor cursorMax = sdb.rawQuery(append.append(DatabaseHelper.BATTLES_COLUMN).append(" DESC LIMIT 10").toString(), null);
            if (cursorMax.getCount() <= 1) {
                return inflater.inflate(R.layout.graph_start, container, false);
            }
            View viewHierarchy = inflater.inflate(R.layout.activity_tanks_detal, container, false);
            numOfTabs = cursorMax.getCount() - 1;
            mTitles = new String[(cursorMax.getCount() - 1)];
            cursorMax.moveToFirst();
            for (int i = 0; i < cursorMax.getCount() - 1; i++) {
                String[] strArr2 = mTitles;
                strArr2[i] = cursorMax.getString(cursorMax.getColumnIndex(DatabaseHelper.LAST_BATTLE_COLUMN));
                cursorMax.moveToNext();
            }
            cursorMax.close();
            this.adapter = new SessionHistoryMainFragment(getActivity().getFragmentManager(), mTitles, numOfTabs);
            this.pager = (ViewPager) viewHierarchy.findViewById(R.id.pager);
            this.pager.setAdapter(adapter);
            this.tabs = (SlidingTabLayout) viewHierarchy.findViewById(R.id.tabs);
            this.tabs.setDistributeEvenly(false);
            this.tabs.setViewPager(this.pager);
            return viewHierarchy;
        }
        Toast.makeText(getActivity(), getString(R.string.no_select), Toast.LENGTH_SHORT).show();
        return null;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onDetach() {
        super.onDetach();
    }
}
