package com.example.appointments;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.HashSet;
import java.util.Set;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private final Set<Long> appointmentDays = new HashSet<>();
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        mAuth = FirebaseAuth.getInstance();

        loadAppointments();

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Get selected date in millis
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            long selectedDate = cal.getTimeInMillis();

            if (appointmentDays.contains(trimTime(selectedDate))) {

                Toast.makeText(this, "You have an appointment on this day!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No appointment on this day.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAppointments() {
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointments");

        ref.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot appt : snapshot.getChildren()) {
                            Long millis = appt.child("dateTimeMillis").getValue(Long.class);
                            if (millis != null) {
                                appointmentDays.add(trimTime(millis)); // Only keep date part
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CalendarActivity.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Normalize time to start of day
    private long trimTime(long millis) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
