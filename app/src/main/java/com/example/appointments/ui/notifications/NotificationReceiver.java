package com.example.appointments.ui.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.appointments.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "appointment_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String appointmentId = intent.getStringExtra("APPOINTMENT_ID");

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background) // Ensure you have an icon
                .setContentTitle("Appointment Reminder")
                .setContentText("You have an upcoming appointment.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(appointmentId.hashCode(), builder.build());
    }
}
