package com.example.appointments.repository;

import androidx.annotation.NonNull;
import com.example.appointments.model.Appointment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AppointmentRepository {
    private DatabaseReference databaseRef;

    public AppointmentRepository() {
        // "appointments" is the root node for all appointment entries
        databaseRef = FirebaseDatabase.getInstance().getReference("appointments");
    }

    // Add a new appointment
    public void addAppointment(Appointment appointment, final OnOperationCompleteListener listener) {
        // Using the appointment ID as the key
        databaseRef.child(appointment.getId()).setValue(appointment)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    // Retrieve appointments for a given user
    public void getUserAppointments(String userId, final OnGetAppointmentsListener listener) {
        // Here we assume each appointment has a userId field to filter by
        databaseRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Appointment> appointments = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Appointment appointment = snapshot.getValue(Appointment.class);
                            appointments.add(appointment);
                        }
                        listener.onSuccess(appointments);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listener.onFailure(databaseError.toException());
                    }
                });
    }

    // Delete an appointment with the given appointment ID
    public void deleteAppointment(String appointmentId, final OnOperationCompleteListener listener) {
        databaseRef.child(appointmentId).removeValue()
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    // Callback interfaces
    public interface OnOperationCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnGetAppointmentsListener {
        void onSuccess(List<Appointment> appointments);
        void onFailure(Exception e);
    }
}
