package com.ntnu.imt3673.imt3673_lab4;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Creates the fragment needed for each tab.
 */
public final class TabFragment extends Fragment {

    /**
     * Returns a new instance of this fragment for the specified tab index.
     */
    public static TabFragment newInstance(int tabIndex) {
        TabFragment tabFragment  = new TabFragment();
        Bundle      arguments    = new Bundle();

        arguments.putInt(Constants.TAB_ARG_INDEX, tabIndex);
        tabFragment.setArguments(arguments);

        return tabFragment;
    }

    /**
     * Sets up the UI layout for the requested tab fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int tabIndex = getArguments().getInt(Constants.TAB_ARG_INDEX);

        switch (tabIndex) {
            case 0: return this.getTabMessages(inflater, container);
            case 1: return this.getTabFriends(inflater,  container);
        }

        return null;
    }

    /**
     * Sets up and returns the list of friends (users).
     */
    private View getTabFriends(LayoutInflater inflater, ViewGroup container) {
        MainActivity activity = ((MainActivity)getActivity());
        View         view     = inflater.inflate(R.layout.fragment_friends, container, false);
        ListView     list     = view.findViewById(R.id.lv_friends);

        // Set a custom adapter to handle the friends list
        FriendsAdapter friendsAdapter = new FriendsAdapter(this.getActivity(), R.layout.list_item_friend);
        list.setAdapter(friendsAdapter);

        // Update the users listener on the database
        activity.updateUserListenerDB(friendsAdapter);

        // Handle clicks/touches on the list view items
        list.setOnItemClickListener(
            (AdapterView<?> parent, View v, int pos, long id) -> {
                // TODO: Pressing on a given nickname, should provide a list view with all messages FROM that user only, in chronological order.
            }
        );

        return view;
    }

    /**
     * Sets up and returns the list of messages.
     */
    private View getTabMessages(LayoutInflater inflater, ViewGroup container) {
        MainActivity activity = ((MainActivity)getActivity());
        View         view     = inflater.inflate(R.layout.fragment_messages, container, false);
        Button       button   = view.findViewById(R.id.btn_message);
        ListView     list     = view.findViewById(R.id.lv_messages);
        EditText     message  = view.findViewById(R.id.et_message);

        // Set a custom adapter to handle the messages list
        MessagesAdapter messagesAdapter = new MessagesAdapter(this.getActivity(), R.layout.list_item_message);
        list.setAdapter(messagesAdapter);

        // Update the messages listener on the database
        activity.updateMessageListenerDB(messagesAdapter);

        // Handle clicks/touches on the list view items
        list.setOnItemClickListener(
            (AdapterView<?> parent, View v, int pos, long id) -> {
                hideKeyboard();
                parent.requestFocus();
            }
        );

        // Add the message to the database when the send button is clicked
        button.setOnClickListener((View v) -> {
            String msg = message.getText().toString().trim();

            if (!msg.isEmpty()) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
                String            nickname    = preferences.getString(Constants.SETTINGS_NICK, "");
                String            date        = String.valueOf(System.currentTimeMillis());

                activity.addMessageToDB(date, nickname, msg);
                message.setText("");
                message.clearFocus();

                hideKeyboard();
            }
        });

        return view;
    }

    /**
     * Hides the software keyboard.
     */
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)this.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getActivity().getCurrentFocus().getWindowToken(), 0);
    }

}
