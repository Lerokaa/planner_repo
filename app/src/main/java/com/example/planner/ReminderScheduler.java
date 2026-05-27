package com.example.planner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

public class ReminderScheduler {

    public static void scheduleReminder(
            Context context,
            long reminderId,
            long triggerTime,
            String title
    ) {

        Intent intent = new Intent(context, ReminderReceiver.class);

        intent.putExtra("title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {

            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                    if (alarmManager.canScheduleExactAlarms()) {

                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                        );

                    } else {

                        alarmManager.setAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                triggerTime,
                                pendingIntent
                        );
                    }

                } else {

                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                    );
                }

            } catch (SecurityException e) {

                e.printStackTrace();

                Toast.makeText(
                        context,
                        "Нет разрешения на точные уведомления",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}