package com.example.pb.reminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;

    public static final String APP_PREFERENCES = "preferences";
    public static final String APP_PREFERENCES_TITLE = "title";
    public static final String APP_PREFERENCES_TIME = "time";
    public static final String APP_PREFERENCES_DESCRIPTION = "description";


    public ReminderReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            setAlarm(context, settings.getLong(APP_PREFERENCES_TIME, 0));
            Log.d("myTAG", "Boot completed");
        } else {
            WakeLocker.acquire(context);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setTicker(settings.getString(APP_PREFERENCES_TITLE, ""))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(settings.getString(APP_PREFERENCES_TITLE, ""))
                    .setContentText(settings.getString(APP_PREFERENCES_DESCRIPTION, ""));

            Intent intentTL = new Intent(context, ReminderActivity.class);
            intentTL.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentTL, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            Notification notification = builder.build();
            notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
            nm.notify(NOTIFICATION_ID, notification);
            ReminderActivity.enableReceiver(false, context);
        }
    }

    public static void setAlarm(Context context, long time) {
        ReminderActivity.enableReceiver(true, context);
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pendingIntent);
        am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

}
