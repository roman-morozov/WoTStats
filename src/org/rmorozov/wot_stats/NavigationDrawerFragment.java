package org.rmorozov.wot_stats;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("Convert2Lambda")
public class NavigationDrawerFragment extends Fragment {
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private ArrayList<String> listValueName;
    private NavigationDrawerCallbacks mCallbacks;
    private int mCurrentSelectedPosition;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mFragmentContainerView;

    public class MyArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private ImageView imageMenuItemName;
        private TextView tipoTextItemName;

        public MyArrayAdapter(Context context, List<String> values) {
            super(context, R.layout.simple_list_menu, values);
            this.tipoTextItemName = null;
            this.imageMenuItemName = null;
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View view = inflater.inflate(R.layout.simple_list_menu, parent, false);
            tipoTextItemName = (TextView) view.findViewById(R.id.textViewMenu);
            imageMenuItemName = (ImageView) view.findViewById(R.id.imageViewMenu);
            tipoTextItemName.setText(listValueName.get(position));
            switch (position) {
                case 0:
                    imageMenuItemName.setImageResource(R.drawable.menu_stat);
                    break;
                case 1:
                    imageMenuItemName.setImageResource(R.drawable.menu_dymamic);
                    break;
                case 2:
                    imageMenuItemName.setImageResource(R.drawable.menu_session);
                    break;
                case 3:
                    imageMenuItemName.setImageResource(R.drawable.menu_history);
                    break;
                case 4:
                    imageMenuItemName.setImageResource(R.drawable.menu_infogr);
                    break;
                case 5:
                    imageMenuItemName.setImageResource(R.drawable.menu_graph_tanks);
                    break;
            }
            return view;
        }
    }

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int i);
    }

    public NavigationDrawerFragment() {
        mCurrentSelectedPosition = 0;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }
        selectItem(mCurrentSelectedPosition);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);

            }
        });
        listValueName = new ArrayList<>();
        listValueName.add(getString(R.string.title_section1));
        listValueName.add(getString(R.string.title_section2));
        listValueName.add(getString(R.string.title_section3));
        listValueName.add(getString(R.string.title_section4));
        listValueName.add(getString(R.string.title_section5));
        listValueName.add(getString(R.string.title_section6));
        mDrawerListView.setAdapter(new MyArrayAdapter(mDrawerListView.getContext(), listValueName));
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    @SuppressWarnings("SameParameterValue")
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.drawable.ic_drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (isAdded()) {
                    getActivity().invalidateOptionsMenu();
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (isAdded()) {
                    getActivity().invalidateOptionsMenu();
                }
            }
        };
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }
}
