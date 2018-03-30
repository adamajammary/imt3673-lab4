package com.ntnu.imt3673.imt3673_lab4;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Creates the fragment needed for each tab.
 */
public final class TabFragment extends Fragment {

    /**
     * Returns a new instance of this fragment for the specified tab index.
     */
    public static TabFragment newInstance(int tabIndex) {
        TabFragment tabFragment = new TabFragment();
        Bundle      arguments   = new Bundle();

        arguments.putInt(Constants.TAB_ARG_INDEX, tabIndex);
        tabFragment.setArguments(arguments);

        return tabFragment;
    }

    /**
     * Sets up the UI layout for the requested tab fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();

        // TODO: Should not happen
        if (arguments == null)
            throw new NullPointerException();

        int tabIndex = arguments.getInt(Constants.TAB_ARG_INDEX);

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

        // TODO: Should not happen
        if (activity == null)
            throw new NullPointerException();

        View     view = inflater.inflate(R.layout.fragment_friends, container, false);
        ListView list = view.findViewById(R.id.lv_friends);

        // Set a custom adapter to handle the friends list
        FriendsAdapter friendsAdapter = new FriendsAdapter(this.getActivity(), R.layout.list_item_friend);
        list.setAdapter(friendsAdapter);

        // Update the users listener on the database
        activity.updateUserListenerDB(friendsAdapter);

        // Clicking on a user will only show messages from that user
        list.setOnItemClickListener(
            (AdapterView<?> parent, View v, int pos, long id) -> {
                //SharedPreferences prefs    = PreferenceManager.getDefaultSharedPreferences(getActivity());
                //String            nickname = prefs.getString(Constants.SETTINGS_NICK, "");
                String            friend  = friendsAdapter.getItem(pos);
                ArrayList<String> equalTo = new ArrayList<>();

                // Show only messages from the selected user and us
                equalTo.add(friend);

                // TODO: Can add more users to the equalTo array if we want messages from multiple users.
                //if (!nickname.equals(user))
                //    equalTo.add(nickname);

                activity.updateMessageListenerDB(Constants.DB_MESSAGES_U, equalTo);

                // Switch over to the messages tab
                ViewPager viewPager = activity.findViewById(R.id.container);
                viewPager.setCurrentItem(0);
            }
        );

        return view;
    }

    /**
     * Sets up and returns the list of messages.
     */
    private View getTabMessages(LayoutInflater inflater, ViewGroup container) {
        MainActivity activity = ((MainActivity)getActivity());

        // TODO: Should not happen
        if (activity == null)
            throw new NullPointerException();

        View     view    = inflater.inflate(R.layout.fragment_messages, container, false);
        Button   button  = view.findViewById(R.id.btn_message);
        ListView list    = view.findViewById(R.id.lv_messages);
        EditText message = view.findViewById(R.id.et_message);

        // Set a custom adapter to handle the messages list
        MessagesAdapter messagesAdapter = new MessagesAdapter(getActivity(), R.layout.list_item_message);
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
                SharedPreferences prefs    = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String            nickname = prefs.getString(Constants.SETTINGS_NICK, "");
                String            date     = String.valueOf(System.currentTimeMillis());

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
        Activity activity = getActivity();

        // TODO: Should not happen
        if (activity == null)
            throw new NullPointerException();

        InputMethodManager inputManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View               view         = activity.getCurrentFocus();

        // TODO: Should not happen
        if ((inputManager == null) || (view == null))
            throw new NullPointerException();

        inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
