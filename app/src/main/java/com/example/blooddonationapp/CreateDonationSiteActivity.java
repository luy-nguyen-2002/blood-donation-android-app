package com.example.blooddonationapp;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.blooddonationapp.databinding.ActivityCreateDonationSiteBinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class CreateDonationSiteActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final Logger log = LoggerFactory.getLogger(CreateDonationSiteActivity.class);
    private GoogleMap mMap;
    private FusedLocationProviderClient client;

    private EditText siteNameField, addressField, openingTimeField, closingTimeField;
    private Button getPositionButton, submitButton;
    private TextView backToMenuTextView,logoutTextView;

    private String latitude = null, longitude = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_donation_site);
        siteNameField = findViewById(R.id.site_name);
        addressField = findViewById(R.id.address);
        openingTimeField = findViewById(R.id.opening_time);
        closingTimeField = findViewById(R.id.closing_time);
        getPositionButton = findViewById(R.id.get_position_on_map);
        submitButton = findViewById(R.id.submit_button);
        backToMenuTextView = findViewById(R.id.backToMenuTextView);
        logoutTextView = findViewById(R.id.logoutTextView);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        client = LocationServices.getFusedLocationProviderClient(this);
        getPositionButton.setOnClickListener(this::getPosition);
        submitButton.setOnClickListener(this::submitDonationSite);
        backToMenuTextView.setOnClickListener(this::backToMenu);
        logoutTextView.setOnClickListener(this::logout);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        requestPermission();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double currentLatitude = location.getLatitude();
                    double currentLongitude = location.getLongitude();
                    LatLng currentLocation = new LatLng(currentLatitude, currentLongitude);
                    mMap.addMarker(new MarkerOptions().position(currentLocation).title("Your Current Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                } else {
                    // Fallback to a default location if current location is unavailable
                    LatLng defaultLocation = new LatLng(10.7881401, 106.6954451);
                    mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Marker"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
                    Toast.makeText(this, "Unable to fetch current location. Showing default location.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                LatLng defaultLocation = new LatLng(10.7881401, 106.6954451);
                mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Default Marker"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
                Toast.makeText(this, "Error fetching location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    public void getPosition(View view) {
        String address = addressField.getText().toString().trim();

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                latitude = String.valueOf(location.getLatitude());
                longitude = String.valueOf(location.getLongitude());

                Toast.makeText(this, "Address Location: Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_SHORT).show();
                LatLng addressLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(addressLocation).title("Address Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(addressLocation, 15));
            } else {
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error finding address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void submitDonationSite(View view) {
        String userId = getIntent().getStringExtra("userId");
        String siteName = siteNameField.getText().toString();
        String address = addressField.getText().toString();
        String openingTime = openingTimeField.getText().toString();
        String closingTime = closingTimeField.getText().toString();
        //check the opening time and closing time must be in format HH:mm
        // Regular expression for HH:mm format
        String timePattern = "([0-9]|[01][0-9]|2[0-3]):[0-5][0-9]";
        if (!openingTime.matches(timePattern)) {
            Toast.makeText(this, "Opening time must be in HH:mm format", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!closingTime.matches(timePattern)) {
            Toast.makeText(this, "Closing time must be in HH:mm format", Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude == null || longitude == null) {
            Toast.makeText(this, "Please get the location first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (siteName.isEmpty() || address.isEmpty() || openingTime.isEmpty() || closingTime.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }
        DatabaseManager dbManager = new DatabaseManager(this);
        dbManager.open();

        boolean success = dbManager.createDonationSite(siteName, address, longitude, latitude, openingTime, closingTime, userId);

        if (success) {
            Toast.makeText(this, "Donation Site Created Successfully", Toast.LENGTH_SHORT).show();
            finish();
            siteNameField.setText("");
            addressField.setText("");
            openingTimeField.setText("");
            closingTimeField.setText("");
        } else {
            Toast.makeText(this, "Failed to Create Donation Site", Toast.LENGTH_SHORT).show();
        }

        dbManager.close();
    }
    public void backToMenu(View view){
        finish();
        siteNameField.setText("");
        addressField.setText("");
        openingTimeField.setText("");
        closingTimeField.setText("");
    }
    public void logout(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear all activities above SignInActivity in the stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        siteNameField.setText("");
        addressField.setText("");
        openingTimeField.setText("");
        closingTimeField.setText("");
    }

}
