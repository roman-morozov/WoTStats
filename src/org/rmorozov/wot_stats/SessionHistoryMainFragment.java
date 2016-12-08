package org.rmorozov.wot_stats;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;

public class SessionHistoryMainFragment extends FragmentStatePagerAdapter {

    final int numOfTabs;
    final CharSequence[] titles;

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
