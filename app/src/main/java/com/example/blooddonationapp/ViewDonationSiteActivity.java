package com.example.blooddonationapp;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.blooddonationapp.databinding.ActivityViewDonationSiteBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class ViewDonationSiteActivity extends FragmentActivity implements OnMapReadyCallback {
    private FusedLocationProviderClient client;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private GoogleMap mMap;
    private ActivityViewDonationSiteBinding binding;
    private DatabaseManager databaseManager;
    private TextView siteNameTextView;
    private TextView addressTextView;
    private TextView openingTimeTextView;
    private TextView closingTimeTextView;
    private TextView managerUsernameTextView;
    private TextView managerEmailTextView;
    private TextView managerIdTextView;
    private TextView siteIdTextView,backToMenuTextView,logoutTextView;
    private Button registerVolunteerButton,registerDonationButton,findRouteButton,stopVolunteeringButton,editDonationSite;
    private static final String apiKey = "AIzaSyCZXhgcfB4A3Nw0eqaQuy9M7bEeeaLmThI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewDonationSiteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setVisible(R.id.editDonationSiteLayout, false);
        setVisible(R.id.registerDonationLayout, false);
        setVisible(R.id.registerVolunteerLayout, false);
        databaseManager = new DatabaseManager(this);
        databaseManager.open();
        siteNameTextView = findViewById(R.id.site_name);
        addressTextView = findViewById(R.id.address);
        openingTimeTextView = findViewById(R.id.opening_time);
        closingTimeTextView = findViewById(R.id.closing_time);
        managerUsernameTextView = findViewById(R.id.manager_username);
        managerEmailTextView = findViewById(R.id.manager_email);
        managerIdTextView = findViewById(R.id.manager_id);
        siteIdTextView = findViewById(R.id.site_id);
        registerVolunteerButton = findViewById(R.id.registerVolunteerButton);
        registerDonationButton = findViewById(R.id.registerDonationButton);
        findRouteButton = findViewById(R.id.findRouteButton);
        backToMenuTextView = findViewById(R.id.backToMenuTextView);
        logoutTextView = findViewById(R.id.logoutTextView);
        stopVolunteeringButton = findViewById(R.id.stopVolunteeringButton);
        editDonationSite = findViewById(R.id.editDonationSite);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            client = LocationServices.getFusedLocationProviderClient(this);
        }
        backToMenuTextView.setOnClickListener(this::backToMenu);
        logoutTextView.setOnClickListener(this::logout);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        Cursor cursorSite = databaseManager.getAllDonationSites();
        if (cursorSite != null && cursorSite.moveToFirst()) {
            do {
                @SuppressLint("Range") String siteId = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.SITE_ID));
                @SuppressLint("Range") String siteName = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.SITE_NAME));
                @SuppressLint("Range") String address = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.ADDRESS));
                @SuppressLint("Range") double latitude = Double.parseDouble(cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.LATITUDE)));
                @SuppressLint("Range") double longitude = Double.parseDouble(cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.LONGITUDE)));
                @SuppressLint("Range") String openingTime = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.OPENING_TIME));
                @SuppressLint("Range") String closingTime = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.CLOSING_TIME));
                @SuppressLint("Range") String managerId = cursorSite.getString(cursorSite.getColumnIndex(DatabaseHelper.MANAGER_ID));

                Cursor cursorUser = databaseManager.getUserById(managerId);
                if (cursorUser != null && cursorUser.moveToFirst()) {
                    @SuppressLint("Range") String managerUsername = cursorUser.getString(cursorUser.getColumnIndex(DatabaseHelper.USER_NAME));
                    @SuppressLint("Range") String managerEmail = cursorUser.getString(cursorUser.getColumnIndex(DatabaseHelper.EMAIL));

                    // Add a marker for each donation site
                    LatLng location = new LatLng(latitude, longitude);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(siteName));
                    if (marker != null) {
                        marker.setTag(new String[]{address, openingTime, closingTime, managerId, managerUsername, managerEmail, siteId, String.valueOf(latitude), String.valueOf(longitude),siteName});
                    }

                    // Include the location in the bounds
                    boundsBuilder.include(location);
                }
                if (cursorUser != null) {
                    cursorUser.close();
                }
            } while (cursorSite.moveToNext());
            cursorSite.close();

            LatLngBounds bounds = boundsBuilder.build();
            int padding = 100;
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }

        mMap.setOnMarkerClickListener(marker -> {
            if (marker.getTag() != null) {
                String[] details = (String[]) marker.getTag();
                siteNameTextView.setText(marker.getTitle());
                addressTextView.setText(details[0]);
                openingTimeTextView.setText(details[1]);
                closingTimeTextView.setText(details[2]);
                managerIdTextView.setText(details[3]);
                managerUsernameTextView.setText(details[4]);
                managerEmailTextView.setText(details[5]);
                siteIdTextView.setText(details[6]);

                String role = getIntent().getStringExtra("role");
                String userId = getIntent().getStringExtra("userId");
                String donationSiteId = details[6];
                findRouteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LatLng currentLatLng = new LatLng(Double.parseDouble(details[7]), Double.parseDouble(details[8]));
                        String destination = getAddressFromLatLng(currentLatLng);
                        findRouteFromCurrentLocation(details[0]);
//                        getDirections("594 Ba Thang Hai District 10 Ho Chi Minh city", details[0]);
//                        findRouteFromCurrentLocation("702 Nguyen Van Linh District 7 Ho Chi Minh city");
                    }
                });

                if (role != null && role.equals("DONOR")) {
                    setVisible(R.id.registerDonationLayout, true);
                    registerDonationButton.setOnClickListener(v -> {
                        int registrationStatus = databaseManager.checkDonationRegistrationStatus(userId, donationSiteId);

                        if (registrationStatus == 0) {
                            showAlert("Registration Pending", "Your previous registration at this site is still pending approval. You cannot register again until it is processed.");
                        } else if (registrationStatus == 1) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Number of Donors");

                            // Create an EditText input for the dialog
                            final EditText input = new EditText(this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER); // Input type for numeric values
                            builder.setView(input);

                            // Set up the dialog buttons
                            builder.setPositiveButton("Confirm", (dialog, which) -> {
                                String numberOfDonors = input.getText().toString();

                                if (numberOfDonors.isEmpty()) {
                                    showAlert("Invalid Input", "Please enter a valid number of donors.");
                                    return;
                                }

                                // Attempt to create a new donation registration
                                boolean isCreated = databaseManager.createDonationRegistration(numberOfDonors, userId, donationSiteId);
                                if (isCreated) {
                                    showAlert("Registration Successful", "You have successfully registered for donation at this site.");
                                } else {
                                    showAlert("Registration Failed", "An error occurred while registering. Please try again later.");
                                }
                            });

                            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                            builder.show();
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Number of Donors");
                            final EditText input = new EditText(this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER); // Input type for numeric values
                            builder.setView(input);
                            builder.setPositiveButton("Confirm", (dialog, which) -> {
                                String numberOfDonors = input.getText().toString();
                                if (numberOfDonors.isEmpty()) {
                                    showAlert("Invalid Input", "Please enter a valid number of donors.");
                                    return;
                                }
                                boolean isCreated = databaseManager.createDonationRegistration(numberOfDonors, userId, donationSiteId);
                                if (isCreated) {
                                    showAlert("Registration Successful", "You have successfully registered for donation at this site.");
                                } else {
                                    showAlert("Registration Failed", "An error occurred while registering. Please try again later.");
                                }
                            });

                            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                            builder.show();
                        }
                    });
                } else if(role != null && role.equals("SITE_MANAGER")) {
                    if (userId != null && userId.equals(details[3])){
                        setVisible(R.id.editDonationSiteLayout, true);
                        setVisible(R.id.registerVolunteerLayout, false);
                        editDonationSite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                builder.setTitle("Edit Donation Site Details");

                                LinearLayout layout = new LinearLayout(v.getContext());
                                layout.setOrientation(LinearLayout.VERTICAL);
                                layout.setPadding(20, 20, 20, 20);

                                final EditText siteNameInput = new EditText(v.getContext());
                                siteNameInput.setHint("Enter new site name (optional)");
                                layout.addView(siteNameInput);

                                final EditText openingTimeInput = new EditText(v.getContext());
                                openingTimeInput.setHint("Enter new opening time (HH:mm) (optional)");
                                layout.addView(openingTimeInput);

                                final EditText closingTimeInput = new EditText(v.getContext());
                                closingTimeInput.setHint("Enter new closing time (HH:mm) (optional)");
                                layout.addView(closingTimeInput);

                                builder.setView(layout);

                                builder.setPositiveButton("Update", (dialog, which) -> {
                                    String newSiteName = siteNameInput.getText().toString().trim();
                                    String newOpeningTime = openingTimeInput.getText().toString().trim();
                                    String newClosingTime = closingTimeInput.getText().toString().trim();
                                    String timePattern = "([0-9]|[01][0-9]|2[0-3]):[0-5][0-9]";
                                    // Validate opening and closing times if provided
                                    if (!newOpeningTime.isEmpty() && !newOpeningTime.matches(timePattern)) {
                                        showAlert("Invalid Input", "Opening time must be in HH:mm format.");
                                        return;
                                    }
                                    if (!newClosingTime.isEmpty() && !newClosingTime.matches(timePattern)) {
                                        showAlert("Invalid Input", "Closing time must be in HH:mm format.");
                                        return;
                                    }
                                    if (newSiteName.isEmpty() && newOpeningTime.isEmpty() && newClosingTime.isEmpty()) {
                                        showAlert("Invalid Input", "Please provide at least one field to update.");
                                        return;
                                    }
                                    boolean isUpdated = databaseManager.updateDonationSiteById(
                                            donationSiteId,
                                            newSiteName.isEmpty() ? siteNameTextView.getText().toString() : newSiteName,
                                            newOpeningTime.isEmpty() ? openingTimeTextView.getText().toString() : newOpeningTime,
                                            newClosingTime.isEmpty() ? closingTimeTextView.getText().toString() : newClosingTime
                                    );

                                    if (isUpdated) {
                                        showAlert("Update Successful", "The donation site details have been successfully updated.");
                                        siteNameTextView.setText(newSiteName.isEmpty() ? siteNameTextView.getText().toString() : newSiteName);
                                        openingTimeTextView.setText(newOpeningTime.isEmpty() ? openingTimeTextView.getText().toString() : newOpeningTime);
                                        closingTimeTextView.setText(newClosingTime.isEmpty() ? closingTimeTextView.getText().toString() : newClosingTime);
                                    } else {
                                        showAlert("Update Failed", "An error occurred while updating the donation site. Please try again later.");
                                    }
                                });

                                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                                builder.show();
                            }
                        });
                    } else {
                        setVisible(R.id.registerVolunteerLayout, true);
                        setVisible(R.id.editDonationSiteLayout, false);
                        setVisible(R.id.stopVolunteeringButton,false);
                        if(!databaseManager.checkVolunteeringState(userId,donationSiteId)){
                            setVisible(R.id.registerVolunteerButton,true);
                            setVisible(R.id.stopVolunteeringButton,false);
                            registerVolunteerButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Register as a volunteer
                                    boolean success = databaseManager.createVolunteerRegistration(userId, siteIdTextView.getText().toString());
                                    if (success) {
                                        Toast.makeText(
                                                ViewDonationSiteActivity.this,
                                                "Registered as volunteer successfully in Donation Site: " + siteNameTextView.getText().toString(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        setVisible(R.id.stopVolunteeringButton,true);
                                        setVisible(R.id.registerVolunteerButton,false);
                                    }else {
                                        Toast.makeText(
                                                ViewDonationSiteActivity.this,
                                                "Failed to Register volunteering at Donation Site: " + siteNameTextView.getText().toString(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                }
                            });
                        }else {
                            setVisible(R.id.registerVolunteerButton,false);
                            setVisible(R.id.stopVolunteeringButton,true);
                            stopVolunteeringButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(ViewDonationSiteActivity.this);
                                builder.setTitle("Stop Volunteering");
                                builder.setMessage("You do not want to volunteer for " + siteNameTextView.getText().toString() + "?");

                                builder.setPositiveButton("Yes", (dialog, which) -> {
                                    // Delete volunteer registration
                                    boolean success = databaseManager.deleteVolunteerRegistration(userId,siteIdTextView.getText().toString());
                                    if (success) {
                                        Toast.makeText(
                                                ViewDonationSiteActivity.this,
                                                "Stopped volunteering at Donation Site: " + siteNameTextView.getText().toString(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        setVisible(R.id.stopVolunteeringButton,false);
                                        setVisible(R.id.registerVolunteerButton,true);
                                    } else {
                                        Toast.makeText(
                                                ViewDonationSiteActivity.this,
                                                "Failed to stop volunteering at Donation Site: " + siteNameTextView.getText().toString(),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });

                                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                                builder.create().show();
                            }
                        });
                        }

                    }
                }
            }
            return false;
        });
    }
    private void findRouteFromCurrentLocation(String destination) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        }
        client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        android.location.Location location = task.getResult();
                        if (location != null) {
                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(currentLatLng).title("You are here"));

                            // Get the address using Geocoder
                            String address = getAddressFromLatLng(currentLatLng);
                            if (address != null) {
                                Log.d("Address", "Current location address: " + address);
                                getDirections(address, destination);
                                Toast.makeText(this, "Current address: " + address, Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("Address Error", "Unable to retrieve address for current location.");
                            }
                        }
                    } else {
                        showAlert("Location Error", "Unable to get the current location. Please try again later.");
                    }
                });
    }
    private String getAddressFromLatLng(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0); // Full address as a string
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void getDirections(String origin, String destination) {
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin + "&destination=" + destination + "&key=" + apiKey;
        new Thread(() -> {
            try {
                URL directionsUrl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) directionsUrl.openConnection();
                Log.d("Route Debug", "direction URL " + urlConnection.toString());
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                JSONObject responseJson = new JSONObject(stringBuilder.toString());
                Log.d("Route Debug", "responseJson " + responseJson.toString());
                JSONArray routes = responseJson.getJSONArray("routes");
                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject legs = route.getJSONArray("legs").getJSONObject(0);
                    JSONArray steps = legs.getJSONArray("steps");

                    for (int i = 0; i < steps.length(); i++) {
                        JSONObject step = steps.getJSONObject(i);
                        JSONObject startLocation = step.getJSONObject("start_location");
                        JSONObject endLocation = step.getJSONObject("end_location");

                        double startLat = startLocation.getDouble("lat");
                        double startLng = startLocation.getDouble("lng");
                        double endLat = endLocation.getDouble("lat");
                        double endLng = endLocation.getDouble("lng");

                        LatLng startPoint = new LatLng(startLat, startLng);
                        LatLng endPoint = new LatLng(endLat, endLng);

                        runOnUiThread(() -> {
//                            mMap.addMarker(new MarkerOptions().position(startPoint).title("Step Start"));
                            Log.d("Route Debug", "Added marker for start point: " + startPoint.toString());
//                            mMap.addMarker(new MarkerOptions().position(endPoint).title("Step End"));
                            Log.d("Route Debug", "Added marker for end point: " + endPoint.toString());
                            mMap.addPolyline(new PolylineOptions().add(startPoint, endPoint).color(Color.BLUE));
                            Log.d("Route Debug", "Added polyline between: " + startPoint.toString() + " and " + endPoint.toString());
                        });

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Directions API Error", e.getMessage());
                runOnUiThread(() -> showAlert("Error", "Failed to retrieve directions. Please try again later."));
            }

        }).start();
    }
    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    public void backToMenu(View view){
        finish();
    }
    public void logout(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear all activities above SignInActivity in the stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.DONATION_SITE_UPDATE");
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, filter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
    }
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String siteId = intent.getStringExtra("siteId");
            if (siteId == null || siteId.isEmpty()) {
                return; // Exit if siteId is not provided
            }

            Cursor cursor = databaseManager.getDonationSiteById(siteId);
            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String siteName = cursor.getString(cursor.getColumnIndex("site_name"));
                @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex("address"));
                cursor.close();
                notifyUsers(siteName,address);
                Log.d("DonationUpdate", "Broadcasting donation site update with siteId: " + siteId);
            } else {
                Log.e("BroadcastReceiver", "Donation site with ID " + siteId + " not found.");
            }
        }
    };
    private void notifyUsers(String siteName, String siteAddress) {
        Log.d("notifyUsers", "Starting notification process...");

        // Get the NotificationManager system service
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d("notifyUsers", "NotificationManager obtained");

        // Check and create NotificationChannel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("notifyUsers", "Creating NotificationChannel for Android O and above...");
            NotificationChannel channel = new NotificationChannel(
                    "DONATION_UPDATES",
                    "Donation Updates",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
            Log.d("notifyUsers", "NotificationChannel created successfully");
        }

        // Build the notification
        Log.d("notifyUsers", "Building notification...");
        Notification notification = new NotificationCompat.Builder(this, "DONATION_UPDATES")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Donation Site Update")
                .setContentText("New updates on " + siteName + "\nAddress: " + siteAddress)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        Log.d("notifyUsers", "Notification built successfully");

        // Display the notification
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notification);
        Log.d("notifyUsers", "Notification sent with ID: " + notificationId);
    }

    private void setVisible(int id, boolean isVisible){
        View view = findViewById(id);
        if(isVisible){
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.INVISIBLE);
        }
    }

}
