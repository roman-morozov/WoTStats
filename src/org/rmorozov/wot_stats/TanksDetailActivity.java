package org.rmorozov.wot_stats;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class TanksDetailActivity extends Activity {
    public static String mTankId;
    final int mNumOfTabs;
    String[] mTitles;
    ViewPagerAdapter adapter;
    ViewPager mPager;
    SlidingTabLayout mTabs;

    public class ViewPagerAdapter extends FragmentStatePagerAdapter {
        final int mNumOfTabs;
        final CharSequence[] mTitles;

        public ViewPagerAdapter(FragmentManager fm, CharSequence[] mTitles, int mNumOfTabs) {
            super(fm);
            this.mTitles = mTitles;
            this.mNumOfTabs = mNumOfTabs;
        }

        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TanksDetalWn8();
                case 1:
                    return new TanksDetalWins();
                case 2:
                    return new TanksDetalDmg();
                case 3:
                    return new TanksDetalAvg();
                default:
                    return null;
            }
        }

        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }

        public int getCount() {
            return mNumOfTabs;
        }
    }

    public TanksDetailActivity() {
        mNumOfTabs = 4;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tanks_detal);
        mTitles = new String[4];
        mTitles[0] = getString(R.string.tanks_avg1);
        mTitles[1] = getString(R.string.tanks_avg2);
        mTitles[2] = getString(R.string.tanks_avg3);
        mTitles[3] = getString(R.string.tanks_avg4);
        adapter = new ViewPagerAdapter(getFragmentManager(), mTitles, mNumOfTabs);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(adapter);
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTankId = getIntent().getStringExtra(DatabaseHelper.TANK_ID_COLUMN);
        SQLiteDatabase sdb = DatabaseHelper.createDatabaseHelper(this).getWritableDatabase();
        Cursor cursor = null;
        try {
            if (getResources().getConfiguration().locale.getCountry().equals("RU")) {
                cursor = sdb.rawQuery("select " + DatabaseHelper.TANK_NAME_I18N + " from " + DatabaseHelper.DATABASE_TABLE_TANKS + " where " + DatabaseHelper.TANK_ID_COLUMN + " = '" + mTankId + "'", null);
            } else {
                cursor = sdb.rawQuery("select " + DatabaseHelper.TANK_NAME_I18N + " from " + DatabaseHelper.DATABASE_TABLE_TANKS_EN + " where " + DatabaseHelper.TANK_ID_COLUMN + " = '" + mTankId + "'", null);
            }
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                setTitle(cursor.getString(cursor.getColumnIndex(DatabaseHelper.TANK_NAME_I18N)));
            }
            mTabs.setViewPager(mPager);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tanks_detal, menu);
        return true;
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
