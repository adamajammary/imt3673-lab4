package com.ntnu.imt3673.imt3673_lab4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        String value  = sharedPreferences.getString(key, "");
        Intent intent = new Intent();

        // Handle nickname changes - check for duplicates before accepting the change
        if (key.equals(Constants.SETTINGS_NICK)) {
            ValueEventListener listener = new ValueEventListener() {
                @Override
                @SuppressWarnings("unchecked")
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map                      dbValues    = (Map<String, Object>)dataSnapshot.getValue(Object.class);
                    SharedPreferences        prefs       = getPreferenceScreen().getSharedPreferences();
                    SharedPreferences.Editor prefsEditor = prefs.edit();

                    // Disable the nickname preference if the nick is available
                    if ((dbValues == null) || dbValues.isEmpty()) {
                        prefsEditor.putString(Constants.SETTINGS_NICK_CHANGED, Constants.TRUE).apply();

                        // Return intent results for the new valid nick
                        intent.putExtra(key, value);
                        getActivity().setResult(RESULT_OK, intent);
                    // Otherwise reset the nick back to the default value
                    } else {
                        String defaultNick = prefs.getString(Constants.SETTINGS_NICK_DEFAULT, "");
                        prefsEditor.putString(Constants.SETTINGS_NICK, defaultNick).apply();

                        EditTextPreference nickPref = (EditTextPreference)findPreference(Constants.SETTINGS_NICK);
                        nickPref.setText(defaultNick);

                        // Refresh the UI to reflect the changes
                        updateUI();

                        Toast.makeText(getContext(), R.string.error_nick, Toast.LENGTH_LONG).show();
                        Log.w(Constants.LOG_TAG, getString(R.string.error_nick));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(Constants.LOG_TAG, databaseError.toException());
                }
            };

            // Check if the nickname already exists in the database
            Query dbQuery = FirebaseDatabase.getInstance().getReference()
                .child(Constants.DB_USERS)
                .orderByChild(Constants.DB_USERS_NICK)
                .equalTo(value);

            dbQuery.addListenerForSingleValueEvent(listener);
            dbQuery.removeEventListener(listener);
        // Return intent results for all other preferences than nick
        } else {
            intent.putExtra(key, value);
            getActivity().setResult(RESULT_OK, intent);
        }

        // Refresh the UI to reflect the changes
        this.updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        this.updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Refreshes the UI components.
     */
    private void updateUI() {
        // Disable the nick pref if the user has changed their nick
        String nickChanged = getPreferenceScreen().getSharedPreferences().getString(
            Constants.SETTINGS_NICK_CHANGED, Constants.FALSE
        );

        if (nickChanged.equals(Constants.TRUE))
            findPreference(Constants.SETTINGS_NICK).setEnabled(false);

        // Update the preference to show the selected value
        ListPreference freqPref  = (ListPreference)findPreference(Constants.SETTINGS_FREQ);
        CharSequence   freqValue = freqPref.getEntry();

        EditTextPreference nickPref  = (EditTextPreference)findPreference(Constants.SETTINGS_NICK);
        String             nickValue = nickPref.getText();

        freqPref.setSummary(getString(R.string.settings_frequency_desc) + ": " + freqValue);
        nickPref.setSummary(getString(R.string.settings_nick_desc)      + ": " + nickValue);
    }
}
