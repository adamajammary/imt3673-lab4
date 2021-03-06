package com.ntnu.imt3673.imt3673_lab4;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Database - Handles all management of the online database using Firebase Realtime Database.
 */
class Database {

    private final MainActivity                activity;
    private MessagesAdapter                   messagesAdapter;
    private final ArrayList<FirebaseListener> messagesEventListeners = new ArrayList<>();
    private FirebaseListener                  usersEventListener;
    private DatabaseReference                 dbMessagesRef;
    private DatabaseReference                 dbUsersRef;

    /**
     * Database - Sets up a reference to the Firebase database.
     * @param context Current activity context
     */
    public Database(final Context context) {
        this.activity = (MainActivity)context;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        if (dbRef != null) {
            this.dbMessagesRef = dbRef.child(Constants.DB_MESSAGES);
            this.dbUsersRef    = dbRef.child(Constants.DB_USERS);
        }

        if ((this.dbMessagesRef == null) || (this.dbUsersRef == null)) {
            Toast.makeText(this.activity, R.string.error_dbref, Toast.LENGTH_LONG).show();
            Log.e(Constants.LOG_TAG, activity.getString(R.string.error_dbref));
        }
    }

    /**
     * Adds the message to the list of messages in the database.
     */
    public void addMessage(final String date, final String user, final String message) {
        if (this.dbMessagesRef == null)
            return;

        String key = this.dbMessagesRef.push().getKey();

        Map<String, String> messageMap = new HashMap<>();
        messageMap.put(Constants.DB_MESSAGES_D, date);
        messageMap.put(Constants.DB_MESSAGES_U, user);
        messageMap.put(Constants.DB_MESSAGES_M, message);

        Map<String, Object> childMessage = new HashMap<>();
        childMessage.put(("/" + key), messageMap);

        this.dbMessagesRef.updateChildren(childMessage);
    }

    /**
     * Adds the user to the list of users in the database.
     */
    public void addUser(final String user, final String uuid) {
        if (this.dbUsersRef == null)
            return;

        Map<String, String> userMap = new HashMap<>();
        userMap.put(Constants.DB_USERS_NICK, user);

        Map<String, Object> childUser = new HashMap<>();
        childUser.put(("/" + uuid), userMap);

        this.dbUsersRef.updateChildren(childUser);
    }

    /**
     * Adds and registers a single message listener using the specified criteria.
     * @param orderBy The key (equivelant to column in database table), for example column 'u' in table 'messages'.
     * @param equalTo The value (equivelant to restricting rows by column value), for example value 'test_user' in column 'u'.
     */
    private void addMessageListener(final String orderBy, final String equalTo) {
        FirebaseListener listener = new FirebaseListener(this.messagesAdapter);
        this.messagesEventListeners.add(listener);

        if (!TextUtils.isEmpty(equalTo))
            this.dbMessagesRef.orderByChild(orderBy).equalTo(equalTo).addChildEventListener(listener);
        else
            this.dbMessagesRef.orderByChild(orderBy).addChildEventListener(listener);
    }

    /**
     * Removes all message listeners and clears all collections referring to them.
     */
    private void removeMessageListeners() {
        for (FirebaseListener listener : this.messagesEventListeners)
            this.dbMessagesRef.removeEventListener(listener);

        this.messagesEventListeners.clear();
        this.messagesAdapter.clear();
    }

    /**
     * Updates the user with the new nickname in the database.
     */
    public void updateUser(final String user, final String uuid) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put(Constants.DB_USERS_NICK, user);

        this.dbUsersRef.child(uuid).setValue(userMap);
    }

    /**
     * Updates the message listener(s) with the specified criteria.
     * @param orderBy The key (equivelant to column in database table), for example column 'u' in table 'messages'.
     * @param equalTo The value (equivelant to restricting rows by column value), for example value 'test_user' in column 'u'.
     */
    public void updateMessageListener(final String orderBy, final ArrayList<String> equalTo) {
        this.removeMessageListeners();

        if (!equalTo.isEmpty()) {
            for (String value : equalTo)
                this.addMessageListener(orderBy, value);
        } else {
            this.addMessageListener(orderBy, "");
        }
    }

    /**
     * Register a listener to notify us when a new message has been added to the Firebase server.
     */
    public void updateMessageListener(final MessagesAdapter messagesAdapter) {
        this.messagesAdapter = messagesAdapter;
        this.removeMessageListeners();
        this.addMessageListener(Constants.DB_MESSAGES_D, "");
    }

    /**
     * Register a listener to notify us when a new user has been added to the Firebase server.
     */
    public void updateUserListener(final FriendsAdapter friendsAdapter) {
        if (this.usersEventListener != null)
            this.dbUsersRef.removeEventListener(this.usersEventListener);

        friendsAdapter.clear();

        this.usersEventListener = new FirebaseListener(friendsAdapter);
        this.dbUsersRef.orderByChild(Constants.DB_USERS_NICK).addChildEventListener(this.usersEventListener);
    }

    /**
     * A listener which notifies us when a data has changed in the Firebase server.
     */
    private class FirebaseListener implements ChildEventListener {

        private final ArrayAdapter<String> adapter;

        FirebaseListener(final ArrayAdapter<String> adapter) {
            this.adapter = adapter;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onChildAdded(final DataSnapshot dataSnapshot, final String previousChildName) {
            String listEntry = "";
            Map    dbValue   = (Map<String, String>)dataSnapshot.getValue(Object.class);
            String dbName    = dataSnapshot.getRef().getParent().getKey();

            // Should never happen
            if (dbValue == null)
                throw new NullPointerException();

            // Messages (chat)
            if (dbName.equals(Constants.DB_MESSAGES)) {
                Date date = new Date(Long.parseLong((String) dbValue.get(Constants.DB_MESSAGES_D)));
                String date2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(date);

                listEntry = (date2 + "\t(" + dbValue.get(Constants.DB_MESSAGES_U) + ")\n" + dbValue.get(Constants.DB_MESSAGES_M));

                // Save the last message - used when app runs in the background
                activity.updateLastMessageFetched(previousChildName);

            // Users (friends)
            } else if (dbName.equals(Constants.DB_USERS)) {
                listEntry = dbValue.get(Constants.DB_USERS_NICK).toString();
            }

            // Sort the list on the client-side since filtered JSON data is returned unordered.
            // https://firebase.google.com/docs/database/rest/retrieve-data
            if (!TextUtils.isEmpty(listEntry)) {
                this.adapter.add(listEntry);
                this.adapter.sort(Comparator.naturalOrder());
                this.adapter.notifyDataSetChanged();
            }
        }

        @Override public void onChildChanged(final DataSnapshot dataSnapshot, final String p) {
            this.updateListener();
        }

        @Override public void onChildMoved(final DataSnapshot   dataSnapshot, final String p) {
            this.updateListener();
        }

        @Override public void onChildRemoved(final DataSnapshot dataSnapshot) {
            this.updateListener();
        }

        @Override
        public void onCancelled(final DatabaseError error) {
            Log.w(Constants.LOG_TAG, error.toException());
        }

        private void updateListener() {
            if (this.adapter instanceof FriendsAdapter)
                updateUserListener((FriendsAdapter)this.adapter);
            else if (this.adapter instanceof MessagesAdapter)
                updateMessageListener((MessagesAdapter)this.adapter);
        }
    }

}
