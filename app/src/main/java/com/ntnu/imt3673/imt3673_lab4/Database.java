package com.ntnu.imt3673.imt3673_lab4;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Database - Handles all management of the online database using Firebase Realtime Database.
 */
public class Database {

    private MainActivity      activity;
    private DatabaseReference databaseRef;

    /**
     * Database
     * @param context Current activity context
     */
    public Database(Context context) {
        this.activity    = (MainActivity)context;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    /**
     *
     */
    public void Read() {

        if (this.databaseRef != null) {

            ValueEventListener event = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //String value = dataSnapshot.getValue(String.class);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    //Log.w(TAG, "Failed to read value.", error.toException());
                }
            };

            //this.databaseRef.addValueEventListener(event);
            //this.databaseRef.addListenerForSingleValueEvent(event);

            //this.databaseRef.removeEventListener(event);
        }

    }

    /**
     *
     * @param key
     * @param value
     */
    public void Write(final String key, final String value) {
        //DatabaseReference db = this.database.getReference(key);

        if (this.databaseRef != null) {

            DatabaseReference dbMessagesRef = this.databaseRef.child("messages");
            //dbMessagesRef.setValue(value);

            /*String key = mDatabase.child("posts").push().getKey();
            Post post = new Post(userId, username, title, body);
            Map<String, Object> postValues = post.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/posts/" + key, postValues);
            childUpdates.put("/user-posts/" + userId + "/" + key, postValues);

            this.databaseRef.updateChildren(childUpdates);*/

        }
    }

}
