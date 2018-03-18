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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Database - Handles all management of the online database using Firebase Realtime Database.
 */
public class Database {

    private MainActivity       activity;
    private MessagesAdapter    messagesAdapter;
    private ChildEventListener messagesEventListener;
    private ChildEventListener usersEventListener;
    private DatabaseReference  dbMessagesRef;
    private DatabaseReference  dbUsersRef;

    /**
     * Database - Sets up a reference to the Firebase database.
     * @param context Current activity context
     */
    public Database(Context context) {
        this.activity = (MainActivity)context;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        if (dbRef != null) {
            this.dbMessagesRef = dbRef.child(Constants.DB_MESSAGES);
            this.dbUsersRef    = dbRef.child(Constants.DB_USERS);
        }

        if ((this.dbMessagesRef == null) || (this.dbUsersRef == null)) {
            Toast.makeText(this.activity, R.string.error_dbref, Toast.LENGTH_LONG).show();
            Log.e("LAB4", activity.getString(R.string.error_dbref));
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
        messageMap.put("d", date);
        messageMap.put("u", user);
        messageMap.put("m", message);

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
        userMap.put("nickname", user);

        Map<String, Object> childUser = new HashMap<>();
        childUser.put(("/" + uuid), userMap);

        this.dbUsersRef.updateChildren(childUser);
    }

    /**
     * Updates the user with the new nickname in the database.
     */
    public void updateUser(final String user, final String uuid) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("nickname", user);

        this.dbUsersRef.child(uuid).setValue(userMap);
    }

    /**
     * Updates the message listener with the specified query/search criteria.
     */
    public void updateMessageListener(String orderBy, String equalTo) {
        if (this.messagesEventListener != null)
            this.dbMessagesRef.removeEventListener(this.messagesEventListener);

        this.messagesAdapter.clear();

        if (!TextUtils.isEmpty(equalTo))
            this.dbMessagesRef.orderByChild(orderBy).equalTo(equalTo).addChildEventListener(this.messagesEventListener);
        else
            this.dbMessagesRef.orderByChild(orderBy).addChildEventListener(this.messagesEventListener);
    }

    /**
     * Register a listener to notify us when a new message has been added to the Firebase server.
     */
    public void updateMessageListener(MessagesAdapter messagesAdapter) {
        if (this.messagesEventListener != null)
            this.dbMessagesRef.removeEventListener(this.messagesEventListener);

        this.messagesAdapter = messagesAdapter;
        this.messagesAdapter.clear();

        this.messagesEventListener = new FirebaseListener(this.messagesAdapter);
        this.dbMessagesRef.orderByChild("d").addChildEventListener(this.messagesEventListener);
    }

    /**
     * Register a listener to notify us when a new user has been added to the Firebase server.
     */
    public void updateUserListener(FriendsAdapter friendsAdapter) {
        if (this.usersEventListener != null)
            this.dbUsersRef.removeEventListener(this.usersEventListener);

        friendsAdapter.clear();

        this.usersEventListener = new FirebaseListener(friendsAdapter);
        this.dbUsersRef.orderByChild("nickname").addChildEventListener(this.usersEventListener);
    }

    /**
     * A listener which notifies us when a data has changed in the Firebase server.
     */
    private class FirebaseListener implements ChildEventListener {

        ArrayAdapter<String> adapter;

        public FirebaseListener(ArrayAdapter<String> adapter) {
            this.adapter = adapter;
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            String listEntry = "";
            Map    dbValue   = (Map<String, String>)dataSnapshot.getValue(Object.class);
            String dbName    = dataSnapshot.getRef().getParent().getKey();

            // Messages (chat)
            if (dbName.equals(Constants.DB_MESSAGES)) {
                Date   date    = new Date(Long.parseLong((String)dbValue.get("d")));
                String date2   = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);

                listEntry = (dbValue.get("u") + "\t\t(" + date2 + ")\n\t" + dbValue.get("m"));
            // Users (friends)
            } else if (dbName.equals(Constants.DB_USERS)) {
                listEntry = dbValue.get("nickname").toString();
            }

            if (!TextUtils.isEmpty(listEntry)) {
                this.adapter.add(listEntry);
                this.adapter.notifyDataSetChanged();
            }

            // TODO: When a new message is available, the Notification should be used, to communicate that to the user. The user can start the foreground activity from the Notification.

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {}

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

        @Override
        public void onCancelled(DatabaseError error) {
            Log.w("LAB4", error.toException());
        }

    }

}
