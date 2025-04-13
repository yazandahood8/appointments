package com.example.appointments.ui.appointments;


import android.os.Bundle;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appointments.R;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        double lat = getIntent().getDoubleExtra("latitude", 0);
        double lng = getIntent().getDoubleExtra("longitude", 0);
        String name = getIntent().getStringExtra("name");

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        String mapUrl = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
        webView.loadUrl(mapUrl);
    }
}
