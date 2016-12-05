package org.rmorozov.wot_stats;

import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

public class StatGraphMainFragment extends FragmentStatePagerAdapter {
    private static final String ARG_SECTION_NUMBER = "section_number";
    int mNumOfTabs;
    CharSequence[] mTitles;

    public StatGraphMainFragment(FragmentManager fm, CharSequence[] mTitles, int mNumbOfTabsNumb) {
        super(fm);
        this.mTitles = mTitles;
        mNumOfTabs = mNumbOfTabsNumb;
    }

    public Fragment getItem(int position) {
        Fragment fragment;
        Bundle args;
        switch (position) {
            case 0:
                fragment = new TanksGraphItem();
                args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, 1);
                fragment.setArguments(args);
                return fragment;
            case 1:
                fragment = new TanksGraphItem();
                args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, 2);
                fragment.setArguments(args);
                return fragment;
            case 2:
                fragment = new TanksGraphItem();
                args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, 3);
                fragment.setArguments(args);
                return fragment;
            case 3:
                fragment = new TanksGraphItem();
                args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, 4);
                fragment.setArguments(args);
                return fragment;
            case 4:
                fragment = new TanksGraphItem();
                args = new Bundle();
                args.putInt(ARG_SECTION_NUMBER, 5);
                fragment.setArguments(args);
                return fragment;
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
