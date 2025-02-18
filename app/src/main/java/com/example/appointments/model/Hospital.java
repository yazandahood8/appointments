package com.example.appointments.model;

import java.io.Serializable;

public class Hospital implements Serializable {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private String image; // URL or resource reference

    // Required empty constructor for Firebase
    public Hospital() {}

    public Hospital(String id, String name, double latitude, double longitude, String description, String image) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.image = image;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }
}
