package org.rmorozov.wot_stats;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlidingTabLayout extends HorizontalScrollView {
    private static final int TAB_VIEW_PADDING_DIPS = 16;
    private static final int TAB_VIEW_TEXT_SIZE_SP = 12;
    private static final int TITLE_OFFSET_DIPS = 24;
    private final SparseArray<String> mContentDescriptions;
    private boolean mDistributeEvenly;
    private final SlidingTabStrip mTabStrip;
    private final int mTitleOffset;
    private ViewPager mViewPager;

    private class TabClickListener implements OnClickListener {
        private TabClickListener() {
        }

        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
    }

    public interface TabColorizer {
        int getIndicatorColor(int i);
    }

    private class InternalViewPagerListener implements OnPageChangeListener {
        private int mScrollState;

        private InternalViewPagerListener() {
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if (tabStripChildCount != 0 && position >= 0 && position < tabStripChildCount) {
                mTabStrip.onViewPagerPageChanged(position, positionOffset);
                View selectedTitle = mTabStrip.getChildAt(position);
                scrollToTab(position, selectedTitle != null ? (int) (((float) selectedTitle.getWidth()) * positionOffset) : 0);
            }
        }

        public void onPageScrollStateChanged(int state) {
            mScrollState = state;
        }

        public void onPageSelected(int position) {
            if (mScrollState == 0) {
                mTabStrip.onViewPagerPageChanged(position, 0.0f);
                scrollToTab(position, 0);
            }
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                boolean z;
                View childAt = mTabStrip.getChildAt(i);
                z = position == i;
                childAt.setSelected(z);
            }
        }
    }

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContentDescriptions = new SparseArray<>();
        setHorizontalScrollBarEnabled(false);
        setFillViewport(true);
        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);
        mTabStrip = new SlidingTabStrip(context);
        addView(mTabStrip, -1, -2);
    }

    public void setDistributeEvenly(boolean distributeEvenly) {
        mDistributeEvenly = distributeEvenly;
    }

    public void setViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();
        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    protected TextView createDefaultTabView(Context context) {
        TextView textView = new TextView(context);
        textView.setGravity(17);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, TAB_VIEW_TEXT_SIZE_SP);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        textView.setBackgroundResource(outValue.resourceId);
        int padding = (int) (TAB_VIEW_PADDING_DIPS * getResources().getDisplayMetrics().density);
        textView.setPadding(padding, padding, padding, padding);
        return textView;
    }

    private void populateTabStrip() {
        PagerAdapter adapter = mViewPager.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            View tabView;
            TextView tabTitleView = null;
            tabView = createDefaultTabView(getContext());
            if (TextView.class.isInstance(tabView)) {
                tabTitleView = (TextView) tabView;
            }
            if (mDistributeEvenly) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                lp.width = 0;
                lp.weight = 1.0f;
            }
            if (tabTitleView != null) {
                tabTitleView.setText(adapter.getPageTitle(i));
            }
            tabView.setOnClickListener(new TabClickListener());
            String desc = mContentDescriptions.get(i, null);
            if (desc != null) {
                tabView.setContentDescription(desc);
            }
            mTabStrip.addView(tabView);
            if (i == mViewPager.getCurrentItem()) {
                tabView.setSelected(true);
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount != 0 && tabIndex >= 0 && tabIndex < tabStripChildCount) {
            View selectedChild = mTabStrip.getChildAt(tabIndex);
            if (selectedChild != null) {
                int targetScrollX = selectedChild.getLeft() + positionOffset;
                if (tabIndex > 0 || positionOffset > 0) {
                    targetScrollX -= mTitleOffset;
                }
                scrollTo(targetScrollX, 0);
            }
        }
    }
}
