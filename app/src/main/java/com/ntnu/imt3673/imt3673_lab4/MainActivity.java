package com.ntnu.imt3673.imt3673_lab4;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity {

    private Authentication authentication;

    /**
     * Initializes the authenticator and sends the user to the login screen.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.authentication = new Authentication(this);
    }

    /**
     * Creates the options menu in the top toolbar.
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles clicks on the Settings menu item.
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        // TODO: Open the Settings menu
        if (id == R.id.action_settings) {
            //

            return true;
        // Log the user out
        } else if (id == R.id.action_logout) {
            this.authentication.logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Starts the authentication intent with Firebase Auth UI, and return with results.
     */
    public void onClickLogin(final View view) {
        this.authentication.login();
    }

    /**
     * Sends the user to the main screen if authentication was successful,
     * otherwise sends the user to the login screen.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.authentication.authenticate(requestCode, resultCode, data);
    }

    /**
     * Tries to log in an anonymous user using Firebase Auth.
     */
    public void onClickLoginAnonymous(final View view) {
        this.authentication.loginAnonymously();
    }

    /**
     * Tab fragments are destroyed when pausing/stopping the app or logging out etc.
     * So we need to re-initialize the UI after a resume by calling updateUI.
     */
    @Override
    protected void onResume(){
        super.onResume();
        this.updateUI(this.authentication.getUser());
    }

    /**
     * Shows the main UI with the two tabs for messages and friends.
     */
    private void initUI() {
        // Set the top toolbar (contains the back button and settings etc.)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will manage the tabbed fragments
        TabsAdapter tabsAdapter = new TabsAdapter(getSupportFragmentManager());

        // Link the adapter to the view pager which handles navigating between tabs
        ViewPager viewPager = findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(tabsAdapter);

        // Register listeners for the tabs so the view pager can manage them
        TabLayout tabLayout = findViewById(R.id.tabs);

        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    /**
     * Show the login screen if the user has not been successfully authenticated,
     * otherwise re-initialize and show the main UI with the two tabs for messages and friends.
     */
    public void updateUI(final FirebaseUser user) {
        if (user == null) {
            setContentView(R.layout.activity_login);
        } else {
            setContentView(R.layout.activity_main);
            this.initUI();
        }
    }

}
