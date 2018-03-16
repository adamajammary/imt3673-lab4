package com.ntnu.imt3673.imt3673_lab4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity {

    private Authentication authentication;
    private Database       database;

    /**
     * Initializes the authenticator and sends the user to the login screen.
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.authentication = new Authentication(this);
        this.database       = new Database(this);
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
     * Handles clicks on the Options menu in the top right corner.
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();

        // Open the Settings menu
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, Constants.SETTINGS_RETURN);

            return true;
        // Log the user out
        } else if (id == R.id.action_logout) {
            this.authentication.logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle results when returning from other activities.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Applies user preferences when returning from the Settings menu.
        if ((requestCode == Constants.SETTINGS_RETURN) && (resultCode == RESULT_OK)) {
            String frequency = data.getStringExtra(Constants.SETTINGS_FREQ);

            // TODO:
        // Sends the user to the main screen if authentication was successful, otherwise to the login screen.
        } else {
            this.authentication.authenticate(requestCode, resultCode, data);
        }
    }

    /**
     * Starts the authentication intent with Firebase Auth UI, and return with results.
     */
    public void onClickLogin(final View view) {
        this.authentication.login();
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
     * Adds the message to the database.
     */
    public void addMessageToDB(final String date, final String user, final String message) {
        this.database.addMessage(date, user, message);
    }

    /**
     * Adds the user to the database.
     */
    public void addUserToDB(final String user, final String uuid) {
        this.database.addUser(user, uuid);
    }

    /**
     * Updates the messages listener on the database.
     */
    public void updateMessageListenerDB(MessagesAdapter messagesAdapter) {
        this.database.updateMessageListener(messagesAdapter);
    }

    /**
     * Updates the messages listener on the database.
     */
    public void updateUserListenerDB(FriendsAdapter friendsAdapter) {
        this.database.updateUserListener(friendsAdapter);
    }

    /**
     * Initializes the user preferences.
     */
    private void initPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String nickname    = preferences.getString(Constants.SETTINGS_NICK,         "");
        String nickChanged = preferences.getString(Constants.SETTINGS_NICK_CHANGED, "");

        // Initialize nick with a default random nickname
        if (TextUtils.isEmpty(nickname)) {
            String uuid        = UUID.randomUUID().toString();
            String defaultNick = ("default_" + uuid.substring(0, 8));

            SharedPreferences.Editor prefsEditor = preferences.edit();

            prefsEditor.putString(Constants.SETTINGS_NICK_CHANGED, Constants.FALSE).apply();
            prefsEditor.putString(Constants.SETTINGS_NICK_UUID,    uuid).apply();
            prefsEditor.putString(Constants.SETTINGS_NICK,         defaultNick).apply();

            // Save the default preferences if not already set
            PreferenceManager.setDefaultValues(this, R.xml.settings, false);

            this.addUserToDB(defaultNick, uuid);
        // Update the nick with the new name the user chose
        } else if (nickChanged.equals(Constants.TRUE)) {
            String uuid = preferences.getString(Constants.SETTINGS_NICK_UUID, "");
            this.database.updateUser(nickname, uuid);
        }
    }

    /**
     * Shows the main UI with the two tabs for messages and friends.
     */
    private void initUI() {
        this.initPreferences();

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
