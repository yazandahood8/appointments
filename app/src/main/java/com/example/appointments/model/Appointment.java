package com.example.appointments.model;

import java.io.Serializable;

public class Appointment implements Serializable {
    private String id;
    private String userId;
    private String title;
    private String description;
    private long dateTimeMillis; // Appointment time in milliseconds
    private Hospital hospital;

    // Required empty constructor for Firebase
    public Appointment() {}

    public Appointment(String id, String userId, String title, String description, long dateTimeMillis, Hospital hospital) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.dateTimeMillis = dateTimeMillis;
        this.hospital = hospital;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDateTimeMillis() { return dateTimeMillis; }
    public void setDateTimeMillis(long dateTimeMillis) { this.dateTimeMillis = dateTimeMillis; }

    public Hospital getHospital() { return hospital; }
    public void setHospital(Hospital hospital) { this.hospital = hospital; }
}
