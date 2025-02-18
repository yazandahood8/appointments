package com.example.appointments.ui.appointments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.appointments.R;
import com.example.appointments.model.Appointment;
import com.example.appointments.model.Hospital;
import com.example.appointments.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class AddAppointmentActivity extends AppCompatActivity {

    private EditText titleEditText, descriptionEditText, dateTimeEditText;
    private Button saveButton, pickDateTimeButton;
    private Spinner hospitalSpinner;

    private AppointmentRepository appointmentRepository;
    private FirebaseAuth mAuth;
    private long appointmentDateTimeMillis = 0; // Store the selected date/time in milliseconds

    // List to hold hospitals from Firebase
    private List<Hospital> hospitalList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_appointment);

        // Set window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        titleEditText = findViewById(R.id.editTextTitle);
        descriptionEditText = findViewById(R.id.editTextDescription);
        dateTimeEditText = findViewById(R.id.editTextDateTime);
        saveButton = findViewById(R.id.buttonSave);
        pickDateTimeButton = findViewById(R.id.buttonPickDateTime);
        hospitalSpinner = findViewById(R.id.spinnerHospital);

        // Initialize Firebase Auth and Repository
        mAuth = FirebaseAuth.getInstance();
        appointmentRepository = new AppointmentRepository();

        // Initialize spinner adapter with an empty list
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hospitalSpinner.setAdapter(spinnerAdapter);

        // Load hospital list from Firebase
        loadHospitalsFromFirebase();

        // Set listener to pick date and time
        pickDateTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDateTime();
            }
        });

        // Set listener to save the appointment
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAppointment();
            }
        });
    }

    // Load hospitals from Firebase Realtime Database
    private void loadHospitalsFromFirebase() {
        DatabaseReference hospitalsRef = FirebaseDatabase.getInstance().getReference("hospitals");
        hospitalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                hospitalList.clear();
                List<String> hospitalNames = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Hospital hospital = ds.getValue(Hospital.class);
                    if (hospital != null) {
                        hospitalList.add(hospital);
                        hospitalNames.add(hospital.getName());
                    }
                }
                spinnerAdapter.clear();
                spinnerAdapter.addAll(hospitalNames);
                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AddAppointmentActivity.this, "Failed to load hospitals: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // Show date and time pickers to let the user choose an appointment date/time
    private void pickDateTime() {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar date = Calendar.getInstance();
        new DatePickerDialog(AddAppointmentActivity.this, (view, year, month, dayOfMonth) -> {
            date.set(year, month, dayOfMonth);
            new TimePickerDialog(AddAppointmentActivity.this, (timePicker, hourOfDay, minute) -> {
                date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                date.set(Calendar.MINUTE, minute);
                appointmentDateTimeMillis = date.getTimeInMillis();
                // Display chosen date/time in the EditText (format: DD/MM/YYYY HH:MM)
                dateTimeEditText.setText(dayOfMonth + "/" + (month + 1) + "/" + year + " "
                        + hourOfDay + ":" + (minute < 10 ? "0" + minute : minute));
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Validate inputs and save the appointment to Firebase using AppointmentRepository
    private void saveAppointment() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || appointmentDateTimeMillis == 0) {
            Toast.makeText(this, "Please fill in all fields and pick a date/time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected hospital from spinner
        int selectedPosition = hospitalSpinner.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= hospitalList.size()) {
            Toast.makeText(this, "Please select a hospital", Toast.LENGTH_SHORT).show();
            return;
        }
        Hospital selectedHospital = hospitalList.get(selectedPosition);

        // Generate a unique appointment ID and get the current user's UID
        String appointmentId = UUID.randomUUID().toString();
        String userId = mAuth.getCurrentUser().getUid();

        // Create a new Appointment object with hospital included
        Appointment appointment = new Appointment(appointmentId, userId, title, description, appointmentDateTimeMillis, selectedHospital);

        // Save the appointment using AppointmentRepository
        appointmentRepository.addAppointment(appointment, new AppointmentRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddAppointmentActivity.this, "Appointment added successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddAppointmentActivity.this, AppointmentsListActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AddAppointmentActivity.this, "Failed to add appointment: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
