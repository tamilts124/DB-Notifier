package com.tamil.dbnotifier;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NotificationService extends Service {

    private String notificationCount ="0";
    private String latestNotificationCount ="0";
    private Infinitydatabase infdb =null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Fetch latest news articles from online source
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                SharedPreferences sp = getSharedPreferences("mydb", MODE_PRIVATE);
                String adminurl = sp.getString("adminurl", "");
                String tablename = sp.getString("tablename", "");
                if (infdb == null) {infdb = new Infinitydatabase(adminurl);}
                List<List> latestNotification = (List) fetchLatestNotification(infdb, tablename).get("row");

                // If there are new articles, create and send a notification
                if (!latestNotification.isEmpty() && !latestNotification.get(0).get(0).equals("0")) {
                    latestNotificationCount = (String) latestNotification.get(0).get(0);
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    ArrayList<StatusBarNotification[]> activeNotifications = new ArrayList<>();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        activeNotifications.add(notificationManager.getActiveNotifications());}
                    if (!(latestNotificationCount.equals(notificationCount)) || activeNotifications.isEmpty()) {
                        String channelId = "news_updates_channel";
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                                .setContentTitle("New Notification Available")
                                .setContentText("There are " + latestNotification.get(0).get(0) + " new Notifications available.")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL);

                        Intent nintent = new Intent(this, NotificationPage.class);
                        nintent.putExtra("adminurl", adminurl);
                        nintent.putExtra("tablename", tablename);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nintent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notificationBuilder.setContentIntent(pendingIntent);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            NotificationChannel channel = new NotificationChannel(channelId, "New Notification", NotificationManager.IMPORTANCE_HIGH);
                            channel.setDescription("Channel for new Notifications");
                            notificationManager.createNotificationChannel(channel);
                        }

                        int notificationId = 1;
                        notificationManager.notify(notificationId, notificationBuilder.build());
                    }
                }
            }
        } catch (Exception e) {e.printStackTrace();}
        // Schedule next update
        notificationCount = latestNotificationCount;
        scheduleNextUpdate();

        // Return START_STICKY to keep the service running even if the app is in the background
        return START_STICKY;
    }

    private HashMap fetchLatestNotification(Infinitydatabase infdb, String tablename) throws Exception {
        // Fetch latest news articles from online source using an API or RSS feed
        return infdb.query("select count(*) from "+tablename+" where notify=true");
    }

    public void scheduleNextUpdate() {
        // Schedule the next update for a specified interval
        Intent intent = new Intent(this, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set the interval for the next update (e.g. 6 hours)
        // long intervalMillis = 6 * 60 * 60 * 1000;
        long intervalMillis = 5 * 60 * 1000; // 5 mins

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // Schedule the next update using the AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + intervalMillis, pendingIntent);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}