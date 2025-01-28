package com.example.blooddonationapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.blooddonationapp.databinding.ActivityRegisteredDonationSiteBinding;

public class RegisteredDonationSiteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityRegisteredDonationSiteBinding binding;
    private DatabaseManager databaseManager;
    private TextView siteIdTextView;
    private TextView siteNameTextView;
    private TextView addressTextView;
    private TextView openingTimeTextView;
    private TextView closingTimeTextView;
    private TextView donorIdTextView;
    private TextView donorUsernameTextView;
    private TextView donorEmailTextView,backToMenu,logoutTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisteredDonationSiteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        databaseManager = new DatabaseManager(this);
        databaseManager.open();

         siteIdTextView = findViewById(R.id.site_id);
         siteNameTextView = findViewById(R.id.site_name);
         addressTextView = findViewById(R.id.address);
         openingTimeTextView = findViewById(R.id.opening_time);
         closingTimeTextView = findViewById(R.id.closing_time);
         donorIdTextView = findViewById(R.id.donor_id);
         donorUsernameTextView = findViewById(R.id.donor_username);
         donorEmailTextView = findViewById(R.id.donor_email);
         backToMenu = findViewById(R.id.backToMenu);
         logoutTextView = findViewById(R.id.logoutTextView);

         SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        backToMenu.setOnClickListener(this::backToMenu);
        logoutTextView.setOnClickListener(this::logout);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String userId = getIntent().getStringExtra("userId");
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        Cursor cursorSite = databaseManager.getRegisteredDonationSites(userId);
        if (cursorSite != null && cursorSite.moveToFirst()) {
            do {
                @SuppressLint("Range") String siteId = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.SITE_ID));
                @SuppressLint("Range") String siteName = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.SITE_NAME));
                @SuppressLint("Range") String address = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.ADDRESS));
                @SuppressLint("Range") double latitude = Double.parseDouble(cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.LATITUDE)));
                @SuppressLint("Range") double longitude = Double.parseDouble(cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.LONGITUDE)));
                @SuppressLint("Range") String openingTime = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.OPENING_TIME));
                @SuppressLint("Range") String closingTime = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.CLOSING_TIME));

                Cursor cursorUser = databaseManager.getUserById(userId);
                if (cursorUser != null && cursorUser.moveToFirst()) {
                    @SuppressLint("Range") String donorUsername = cursorUser.getString(cursorUser.getColumnIndex(DatabaseHelper.USER_NAME));
                    @SuppressLint("Range") String donorEmail = cursorUser.getString(cursorUser.getColumnIndex(DatabaseHelper.EMAIL));

                    LatLng location = new LatLng(latitude, longitude);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(siteName));
                    if (marker != null) {
                        marker.setTag(new String[]{siteName, address, openingTime, closingTime, userId, donorUsername, donorEmail, siteId});
                    }

                    boundsBuilder.include(location);
                }
                if (cursorUser != null) {
                    cursorUser.close();
                }
            } while (cursorSite.moveToNext());
            cursorSite.close();

            // Move the camera to fit all markers
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 100;
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null) {
                String[] siteDetails = (String[]) marker.getTag();
                siteIdTextView.setText("Site ID: " + siteDetails[7]);
                siteNameTextView.setText("Site Name: " + siteDetails[0]);
                addressTextView.setText("Address: " + siteDetails[1]);
                openingTimeTextView.setText("Opening Time: " + siteDetails[2]);
                closingTimeTextView.setText("Closing Time: " + siteDetails[3]);
                donorIdTextView.setText("Donor Id: " + siteDetails[4]);
                donorUsernameTextView.setText("Donor Username: " + siteDetails[5]);
                donorEmailTextView.setText("Donor Email: " + siteDetails[6]);
            }
            return false;
        });
    }

    public void backToMenu(View view){
        finish();
    };

    private void logout(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear all activities above SignInActivity in the stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
