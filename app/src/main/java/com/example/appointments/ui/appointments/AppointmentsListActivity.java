package com.example.appointments.ui.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointments.R;
import com.example.appointments.model.Appointment;
import com.example.appointments.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsListActivity extends AppCompatActivity implements AppointmentAdapter.OnItemActionListener {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private AppointmentRepository appointmentRepository;
    private FirebaseAuth mAuth;
    private Button addButton;
    private Spinner filterSpinner;
    private SearchView searchView;

    // Hold full list of appointments loaded from Firebase
    private List<Appointment> allAppointments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointments_list);

        // Set window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        appointmentRepository = new AppointmentRepository();

        filterSpinner = findViewById(R.id.spinnerFilter);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerViewAppointments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter();
        adapter.setOnItemActionListener(this);
        recyclerView.setAdapter(adapter);
        addButton = findViewById(R.id.buttonAddAppointment);

        // Setup spinner filter options from array resource
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        // Listener for search text changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterAppointments();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterAppointments();
                return true;
            }
        });

        // Listener for spinner selection changes
        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                filterAppointments();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Load appointments from Firebase
        String userId = mAuth.getCurrentUser().getUid();
        appointmentRepository.getUserAppointments(userId, new AppointmentRepository.OnGetAppointmentsListener() {
            @Override
            public void onSuccess(List<Appointment> appointments) {
                allAppointments = appointments;
                filterAppointments();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AppointmentsListActivity.this, "Failed to load appointments: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Set click listener for the "Add Appointment" button
        addButton.setOnClickListener(view -> {
            // Navigate to AddAppointmentActivity
            startActivity(new Intent(AppointmentsListActivity.this, AddAppointmentActivity.class));
        });
    }

    // Filter the appointments based on search query and spinner selection
    private void filterAppointments() {
        String query = searchView.getQuery().toString().toLowerCase().trim();
        String filterOption = filterSpinner.getSelectedItem().toString();
        List<Appointment> filtered = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (Appointment appointment : allAppointments) {
            // Filter by search query: check if the title contains the query string
            if (!appointment.getTitle().toLowerCase().contains(query)) {
                continue;
            }
            // Filter by spinner option:
            // "Upcoming" means appointment time is in the future,
            // "Past" means appointment time is in the past,
            // "All" means no time filtering.
            if (filterOption.equals("Upcoming") && appointment.getDateTimeMillis() < currentTime) {
                continue;
            } else if (filterOption.equals("Past") && appointment.getDateTimeMillis() >= currentTime) {
                continue;
            }
            filtered.add(appointment);
        }
        adapter.setAppointments(filtered);
    }

    @Override
    public void onDelete(Appointment appointment, int position) {
        // Delete from Firebase
        appointmentRepository.deleteAppointment(appointment.getId(), new AppointmentRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AppointmentsListActivity.this, "Appointment deleted", Toast.LENGTH_SHORT).show();
                // Update the full list and filter list
                allAppointments.remove(appointment);
                filterAppointments();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AppointmentsListActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMoreDetails(Appointment appointment, int position) {
        // Launch AppointmentDetailsActivity, passing the appointment (make sure Appointment implements Serializable or Parcelable)
        Intent intent = new Intent(AppointmentsListActivity.this, AppointmentDetailsActivity.class);
        intent.putExtra("appointment", appointment);
        startActivity(intent);
    }
}
