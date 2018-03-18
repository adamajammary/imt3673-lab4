package com.ntnu.imt3673.imt3673_lab4;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Background Data Fetch Service
 */
public class BackgroundDataFetchService extends JobService {

    private DatabaseReference          dbRef;
    private ChildEventListener         dbRefListener;
    private final static AtomicInteger notificationID = new AtomicInteger(0);

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        PersistableBundle bundle      = jobParameters.getExtras();
        String            lastMessage = bundle.getString(Constants.FETCH_LAST_MSG);

        this.dbRefListener = new ChildEventListener() {

            private boolean initData;

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // Skip already viewed messages
                if (this.initData)
                    notifyUser(dataSnapshot);

                if (((previousChildName == null) && TextUtils.isEmpty(lastMessage)) ||
                    ((previousChildName != null) && (previousChildName.equals(lastMessage))))
                {
                    this.initData = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("LAB4", databaseError.toException());
            }

            @Override public void onChildChanged(DataSnapshot dataSnapshot, String p) {}
            @Override public void onChildMoved(DataSnapshot   dataSnapshot, String p) {}
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}

        };

        this.dbRef = FirebaseDatabase.getInstance().getReference().child(Constants.DB_MESSAGES);
        this.dbRef.addChildEventListener(this.dbRefListener);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        this.dbRef.removeEventListener(this.dbRefListener);
        return false;
    }

    /**
     * Notifies the user about new messages.
     */
    private void notifyUser(DataSnapshot dataSnapshot) {
        Map    dbValue  = (Map<String, String>)dataSnapshot.getValue(Object.class);
        Date   date     = new Date(Long.parseLong((String)dbValue.get("d")));
        String date2    = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(date);
        String msgShort = (dbValue.get("u") + "\t(" + date2 + ")");
        String msgLong  = (msgShort + "\n" + dbValue.get("m"));

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(msgShort)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(msgLong))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(contentIntent)
            .setAutoCancel(true);

        NotificationManager manager = getSystemService(NotificationManager.class);

        // Register a NotificationChannel on API 26+ (Android 8.0 and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String       desc = getString(R.string.notification_channel_desc);
            int          prio = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID, name, prio);
            channel.setDescription(desc);

            manager.createNotificationChannel(channel);
        }

        // Assign the notification a new incremented ID
        manager.notify(this.notificationID.incrementAndGet(), notification.build());
    }

}
