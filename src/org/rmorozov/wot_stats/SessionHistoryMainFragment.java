package org.rmorozov.wot_stats;

import android.app.FragmentManager;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

/**
 * Created by 21990 on 07.10.2016.
 */
public class SessionHistoryMainFragment extends FragmentStatePagerAdapter {

    int numOfTabs;
    CharSequence[] titles;

    public SessionHistoryMainFragment(FragmentManager fm, CharSequence[] mTitles, int mNumOfTabs) {
        super(fm);
        this.titles = mTitles;
        this.numOfTabs = mNumOfTabs;
    }

    @Override
    public Fragment getItem(int i) {
        SessionHistoryItem fragment = new SessionHistoryItem();
        Bundle args = new Bundle();
        args.putInt("section_number", i);
        fragment.setArguments(args);
        return  fragment;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
