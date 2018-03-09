package com.ntnu.imt3673.imt3673_lab4;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Creates the fragment needed for each tab.
 */
public final class TabsFragment extends Fragment {

    /**
     * Returns a new instance of this fragment for the specified tab index.
     */
    public static TabsFragment newInstance(int tabIndex) {
        TabsFragment fragment  = new TabsFragment();
        Bundle       arguments = new Bundle();

        arguments.putInt(Constants.TAB_ARG_INDEX, tabIndex);
        fragment.setArguments(arguments);

        return fragment;
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
        View     view = inflater.inflate(R.layout.fragment_friends, container, false);
        ListView list = view.findViewById(R.id.lv_friends);

        // Set a custom adapter to handle the friends list
        FriendsAdapter friendsAdapter = new FriendsAdapter(this.getActivity(), R.layout.list_item_friend);
        list.setAdapter(friendsAdapter);

        return view;
    }

    /**
     * Sets up and returns the list of messages.
     */
    private View getTabMessages(LayoutInflater inflater, ViewGroup container) {
        View     view    = inflater.inflate(R.layout.fragment_messages, container, false);
        Button   button  = view.findViewById(R.id.btn_message);
        ListView list    = view.findViewById(R.id.lv_messages);
        EditText message = view.findViewById(R.id.et_message);

        // Set a custom adapter to handle the messages list
        MessagesAdapter messagesAdapter = new MessagesAdapter(this.getActivity(), R.layout.list_item_message);
        list.setAdapter(messagesAdapter);

        // Add the message to the list view when the send button is clicked
        button.setOnClickListener((View v) -> {
            String msg = message.getText().toString().trim();

            if (!msg.isEmpty()) {
                messagesAdapter.add(msg);
                messagesAdapter.notifyDataSetChanged();
                message.setText("");
            }
        });

        return view;
    }

}
