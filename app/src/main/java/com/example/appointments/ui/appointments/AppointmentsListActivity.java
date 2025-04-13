package com.example.appointments.ui.appointments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.widget.SearchView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appointments.CalendarActivity;
import com.example.appointments.R;
import com.example.appointments.model.Appointment;
import com.example.appointments.repository.AppointmentRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AppointmentsListActivity extends AppCompatActivity implements AppointmentAdapter.OnItemActionListener {

    private RecyclerView recyclerView;
    private AppointmentAdapter adapter;
    private AppointmentRepository appointmentRepository;
    private FirebaseAuth mAuth;
    private Button addButton, buttonShowAppointment;
    private Spinner filterSpinner;
    private SearchView searchView;
    private ToggleButton toggleSortOrder;

    private List<Appointment> allAppointments = new ArrayList<>();
    private boolean sortAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointments_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        appointmentRepository = new AppointmentRepository();

        // Init views
        filterSpinner = findViewById(R.id.spinnerFilter);
        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerViewAppointments);
        toggleSortOrder = findViewById(R.id.toggleSortOrder);
        addButton = findViewById(R.id.buttonAddAppointment);
        buttonShowAppointment = findViewById(R.id.buttonShowAppointment);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter();
        adapter.setOnItemActionListener(this);
        recyclerView.setAdapter(adapter);

        // Spinner filter options
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        // Toggle sort order
        toggleSortOrder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sortAscending = isChecked;
            filterAppointments();
        });

        // Search listener
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

        // Spinner listener
        filterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                filterAppointments();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Load appointments
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

        addButton.setOnClickListener(view -> {
            startActivity(new Intent(AppointmentsListActivity.this, AddAppointmentActivity.class));
        });

        buttonShowAppointment.setOnClickListener(view -> {
            startActivity(new Intent(AppointmentsListActivity.this, CalendarActivity.class));
        });
    }

    private void filterAppointments() {
        String query = searchView.getQuery().toString().toLowerCase().trim();
        String filterOption = filterSpinner.getSelectedItem().toString();
        List<Appointment> filtered = new ArrayList<>();
        long currentTime = System.currentTimeMillis();

        for (Appointment appointment : allAppointments) {
            if (!appointment.getTitle().toLowerCase().contains(query)) continue;
            if (filterOption.equals("Upcoming") && appointment.getDateTimeMillis() < currentTime) continue;
            if (filterOption.equals("Past") && appointment.getDateTimeMillis() >= currentTime) continue;
            filtered.add(appointment);
        }

        if (sortAscending) {
            filtered.sort(Comparator.comparingLong(Appointment::getDateTimeMillis));
        } else {
            filtered.sort((a, b) -> Long.compare(b.getDateTimeMillis(), a.getDateTimeMillis()));
        }

        adapter.setAppointments(filtered);
    }

    @Override
    public void onDelete(Appointment appointment, int position) {
        appointmentRepository.deleteAppointment(appointment.getId(), new AppointmentRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AppointmentsListActivity.this, "Appointment deleted", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(AppointmentsListActivity.this, AppointmentDetailsActivity.class);
        intent.putExtra("appointment", appointment);
        startActivity(intent);
    }
}
