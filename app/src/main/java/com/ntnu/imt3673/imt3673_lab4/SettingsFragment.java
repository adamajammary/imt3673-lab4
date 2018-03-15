package com.ntnu.imt3673.imt3673_lab4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Intent intent = new Intent();
        String value  = sharedPreferences.getString(key, "");

        intent.putExtra(key, value);
        getActivity().setResult(RESULT_OK, intent);

        // Disable the nickname preference once the user has changed their nick
        if (key.equals(Constants.SETTINGS_NICK)) {
            SharedPreferences.Editor prefsEditor = getPreferenceScreen().getSharedPreferences().edit();
            prefsEditor.putString(Constants.SETTINGS_NICK_CHANGED, Constants.TRUE).apply();
        }

        this.updateUI();
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        preferences.registerOnSharedPreferenceChangeListener(this);

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
