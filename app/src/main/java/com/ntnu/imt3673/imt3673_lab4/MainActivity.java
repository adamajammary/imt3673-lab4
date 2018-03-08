package com.ntnu.imt3673.imt3673_lab4;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity {

    private TabsAdapter tabsAdapter;
    private ViewPager   viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initTabbedUI();
    }

    /**
     * Creates the Settings menu in the top toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles clicks on the Settings menu item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // TODO: Open the Settings menu
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the adapter and view pager so they can manage the tabbed UI layout.
     */
    private void initTabbedUI() {
        // Set the top toolbar (contains the back button and settings etc.)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will manage the tabbed fragments
        this.tabsAdapter = new TabsAdapter(getSupportFragmentManager());

        // Link the adapter to9 the view pager which handles navigating between tabs
        this.viewPager = findViewById(R.id.container);
        this.viewPager.setAdapter(this.tabsAdapter);

        // Register listeners for the tabs so the view pager can manage them
        TabLayout tabLayout = findViewById(R.id.tabs);
        this.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(this.viewPager));
    }

}
