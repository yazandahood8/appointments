package com.example.appointments.ui.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.example.appointments.model.Appointment;

public class NotificationScheduler {
    public static void scheduleNotification(Context context, Appointment appointment) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("APPOINTMENT_ID", appointment.getId());
        // Add any additional appointment details as needed

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                appointment.getId().hashCode(), // unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule the notification at the specified appointment time.
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, appointment.getDateTimeMillis(), pendingIntent);
    }
}
