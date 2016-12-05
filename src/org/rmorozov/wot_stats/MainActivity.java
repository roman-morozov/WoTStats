package org.rmorozov.wot_stats;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import com.github.mikephil.charting.utils.Utils;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final int CHOOSE_PLAYER = 0;
    private static String mainPlayer;
    private DatabaseHelper dbHelper;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.init(this);
        mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        dbHelper = DatabaseHelper.createDatabaseHelper(this);
        SQLiteDatabase sdb = dbHelper.getWritableDatabase();
        mainPlayer = getString(R.string.ttl_search_name);
        Cursor cursor = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER + " WHERE active = 1", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            mainPlayer = cursor.getString(1);
        }
        cursor.close();
        this.mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    public void onNavigationDrawerItemSelected(int position) {
        getFragmentManager().beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1)).commit();
    }

    public void onSectionAttached(int number) {
        Fragment tmp;
        switch (number) {
            case 1:
                tmp = new MainStatFragment();
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                tmp = new StatGraphRoot();
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                tmp = new SessionHistoryRoot();
                break;
            case 4:
                tmp = new StatHistory();
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                tmp = new Infographics();
                mTitle = getString(R.string.title_section5);
                break;
            case 6:
                tmp = TanksGraph.newInstance("1", "2");
                mTitle = getString(R.string.title_section6);
                break;
            default:
                return;
        }
        getFragmentManager().beginTransaction().replace(R.id.container, tmp).commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            return super.onCreateOptionsMenu(menu);
        }
        getMenuInflater().inflate(R.menu.main, menu);
        restoreActionBar();
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id != R.id.active_player) {
            return super.onOptionsItemSelected(item);
        }
        startActivityForResult(new Intent(this, SelectPlayer.class), CHOOSE_PLAYER);
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_PLAYER && resultCode == Activity.RESULT_OK) {
            String[] param_array = data.getStringExtra(SelectPlayer.THIEF).split("\\$");
            mainPlayer = param_array[0];
            String mainPlayerId = param_array[1];
            String mainPlayerServer = param_array[2];
            SQLiteDatabase sdb = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.ACTIVE, false);
            sdb.update(DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER, values, null, null);
            Cursor cursor = sdb.rawQuery("SELECT * FROM " + DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER
                    +" WHERE " + DatabaseHelper.PLAYER_ID_COLUMN + " = \'" + mainPlayerId + "\'", null);
            if (cursor.getCount() > 0) {
                values = new ContentValues();
                values.put(DatabaseHelper.ACTIVE, true);
                String str = DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER;
                StringBuilder stringBuilder = new StringBuilder();
                sdb.update(str, values, stringBuilder.append(DatabaseHelper.PLAYER_ID_COLUMN).append(" = ?").toString(), new String[]{mainPlayerId});
            } else {
                ContentValues newValues = new ContentValues();
                newValues.put(DatabaseHelper.PLAYER_NAME_COLUMN, mainPlayer);
                newValues.put(DatabaseHelper.PLAYER_ID_COLUMN, mainPlayerId);
                newValues.put(DatabaseHelper.ACTIVE, true);
                newValues.put(DatabaseHelper.SERVER, mainPlayerServer);
                sdb.insert(DatabaseHelper.DATABASE_TABLE_MAIN_PLAYER, null, newValues);
            }
            cursor.close();
            invalidateOptionsMenu();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, new MainStatFragment()).commit();
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
            onSectionAttached(CHOOSE_PLAYER);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.findItem(R.id.active_player) != null) {
            menu.findItem(R.id.active_player).setTitle(mainPlayer);
        }
        return super.onPrepareOptionsMenu(menu);
    }
}
