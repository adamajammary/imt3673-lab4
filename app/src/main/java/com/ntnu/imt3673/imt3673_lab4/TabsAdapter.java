package com.ntnu.imt3673.imt3673_lab4;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A custom adapter that manages and returns a specified tab.
 */
public class TabsAdapter extends FragmentPagerAdapter {

    /**
     * Tabs Adapter - Creates a new instance using the specified fragment manager.
     */
    public TabsAdapter(final FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /**
     * Creates and returns a new tab fragment for the specified tab index (0-based).
     */
    @Override
    public Fragment getItem(final int index) {
        return TabsFragment.newInstance(index);
    }

    /**
     * Returns the total number of tabs.
     */
    @Override
    public int getCount() {
        return 2;
    }

}
