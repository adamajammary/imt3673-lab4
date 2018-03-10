package com.ntnu.imt3673.imt3673_lab4;

import android.content.Context;
import android.util.Log;
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
    private ChildEventListener childEventListener;
    private DatabaseReference  dbMessagesRef;

    /**
     * Database - Sets up a reference to the Firebase database.
     * @param context Current activity context
     */
    public Database(Context context) {
        this.activity = (MainActivity)context;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        if (dbRef != null)
            this.dbMessagesRef = dbRef.child("messages");

        if (this.dbMessagesRef == null) {
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
     * Register a listener to notify us when data is updated on the Firebase server.
     */
    public void updateMessageListener(MessagesAdapter messagesAdapter) {
        if (this.childEventListener != null)
            this.dbMessagesRef.removeEventListener(this.childEventListener);

        messagesAdapter.clear();

        this.childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Map    value   = (Map<String, String>)dataSnapshot.getValue(Object.class);
                Date   date    = new Date(Long.parseLong((String)value.get("d")));
                String date2   = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);
                String message = (value.get("u") + "\t\t(" + date2 + ")\n\t" + value.get("m"));

                messagesAdapter.add(message);
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        };

        this.dbMessagesRef.addChildEventListener(this.childEventListener);
    }

}
