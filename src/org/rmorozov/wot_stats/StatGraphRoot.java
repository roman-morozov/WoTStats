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

public class StatGraphRoot extends Fragment {
    int mNumOfTabs;
    String[] mTitles;
    StatGraphMainFragment adapter;
    DatabaseHelper dbHelper;
    ViewPager pager;
    SlidingTabLayout tabs;

    public StatGraphRoot() {
        mNumOfTabs = 5;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbHelper = DatabaseHelper.createDatabaseHelper(getActivity());
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
            if (sdb.rawQuery(append.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = '").append(player_id).append("'").toString(), null).getCount() <= 1) {
                return inflater.inflate(R.layout.graph_start, container, false);
            }
            View viewHierarchy = inflater.inflate(R.layout.activity_tanks_detal, container, false);
            mTitles = new String[5];
            mTitles[0] = getString(R.string.stat_graph_ttl1);
            mTitles[1] = getString(R.string.stat_graph_ttl2);
            mTitles[2] = getString(R.string.stat_graph_ttl3);
            mTitles[3] = getString(R.string.stat_graph_ttl4);
            mTitles[4] = getString(R.string.stat_graph_ttl5);

            adapter = new StatGraphMainFragment(getActivity().getFragmentManager(), mTitles, mNumOfTabs);
            pager = (ViewPager) viewHierarchy.findViewById(R.id.pager);
            pager.setAdapter(adapter);
            tabs = (SlidingTabLayout) viewHierarchy.findViewById(R.id.tabs);
            tabs.setDistributeEvenly(false);
            tabs.setViewPager(pager);
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
