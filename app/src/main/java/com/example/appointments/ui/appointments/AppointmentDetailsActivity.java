package com.example.appointments.ui.appointments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.appointments.R;
import com.example.appointments.model.Appointment;
import com.example.appointments.model.Hospital;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppointmentDetailsActivity extends AppCompatActivity {

    private TextView tvTitle, tvDescription, tvDateTime, tvHospitalName, tvHospitalDesc, tvHospitalLocation;
    private Button btnGoToMap;
    private Appointment appointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvHospitalName = findViewById(R.id.tvHospitalName);
        tvHospitalDesc = findViewById(R.id.tvHospitalDesc);
        tvHospitalLocation = findViewById(R.id.tvHospitalLocation);
        btnGoToMap = findViewById(R.id.btnGoToMap);

        // Retrieve the Appointment object passed via Intent
        appointment = (Appointment) getIntent().getSerializableExtra("appointment");
        if (appointment == null) {
            finish();
            return;
        }

        // Display appointment details
        tvTitle.setText(appointment.getTitle());
        tvDescription.setText(appointment.getDescription());
        tvDateTime.setText(formatDateTime(appointment.getDateTimeMillis()));

        // Display hospital details if available
        Hospital hospital = appointment.getHospital();
        if (hospital != null) {
            tvHospitalName.setText(hospital.getName());
            tvHospitalDesc.setText(hospital.getDescription());
            tvHospitalLocation.setText("Lat: " + hospital.getLatitude() + ", Lng: " + hospital.getLongitude());
        } else {
            tvHospitalName.setText("No hospital selected");
            tvHospitalDesc.setText("");
            tvHospitalLocation.setText("");
        }

        // Set button to open hospital location in map
        btnGoToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hospital != null) {
                    // Create a geo URI with latitude and longitude
                    String uri = "geo:" + hospital.getLatitude() + "," + hospital.getLongitude() +
                            "?q=" + hospital.getLatitude() + "," + hospital.getLongitude() + "(" + hospital.getName() + ")";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    // Check if there's an app to handle the map intent
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Toast.makeText(AppointmentDetailsActivity.this, "No map application found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AppointmentDetailsActivity.this, "No hospital information available", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Helper method to format the date/time
    private String formatDateTime(long dateTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(dateTimeMillis));
    }
}
