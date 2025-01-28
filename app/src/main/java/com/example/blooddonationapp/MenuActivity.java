package com.example.blooddonationapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MenuActivity extends AppCompatActivity {
    private TextView createDonationSite, viewDonationSites,viewVolunteers,registerForDonation,viewRegisteredDonationSites, viewRegisteredDonors,viewReports,viewAllDonationSites,manageReports,logoutTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        createDonationSite = findViewById(R.id.createDonationSite);
        viewDonationSites = findViewById(R.id.viewDonationSites);
        viewVolunteers = findViewById(R.id.viewVolunteers);
        registerForDonation = findViewById(R.id.registerForDonation);
        viewRegisteredDonationSites = findViewById(R.id.viewRegisteredDonationSites);
        viewRegisteredDonors = findViewById(R.id.viewRegisteredDonors);
        viewReports = findViewById(R.id.viewReports);
        viewAllDonationSites = findViewById(R.id.viewAllDonationSites);
        manageReports = findViewById(R.id.manageReports);
        logoutTextView = findViewById(R.id.logoutTextView);
        String role = getIntent().getStringExtra("role");
        String userId = getIntent().getStringExtra("userId");
        Log.e("Menu Activity", "Role: " + role);

        setVisible(R.id.superUserMenu, false);
        setVisible(R.id.managerMenu, false);
        setVisible(R.id.donorMenu, false);

        // Display the appropriate menu based on the role
        if ("SUPER_USER".equals(role)) {
            setVisible(R.id.superUserMenu, true);
            onClickSuperUserMenuOptions(userId);
        } else if ("SITE_MANAGER".equals(role)) {
            setVisible(R.id.managerMenu, true);
            onClickManagerMenuOptions(userId);
        } else if ("DONOR".equals(role)) {
            setVisible(R.id.donorMenu, true);
            onClickDonorMenuOptions(userId);
        }
        logoutTextView.setOnClickListener(this::logout);
    }

    private void onClickSuperUserMenuOptions(String userId){
        viewAllDonationSites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ViewDonationSiteActivity.class);
                intent.putExtra("userId", userId);
                String role = getIntent().getStringExtra("role");
                intent.putExtra("role", role);
                startActivity(intent);
            }
        });
        manageReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ReportsActivity.class);
                intent.putExtra("userId", userId);
                String role = getIntent().getStringExtra("role");
                intent.putExtra("role", role);
                startActivity(intent);
            }
        });
    }

    private void onClickDonorMenuOptions(String userId){
        registerForDonation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ViewDonationSiteActivity.class);
                intent.putExtra("userId", userId);
                String role = getIntent().getStringExtra("role");
                intent.putExtra("role", role);
                startActivity(intent);
            }
        });
        viewRegisteredDonationSites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, RegisteredDonationSiteActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
    }

    private void onClickManagerMenuOptions(String userId){
        createDonationSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, CreateDonationSiteActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
        viewDonationSites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ViewDonationSiteActivity.class);
                intent.putExtra("userId", userId);
                String role = getIntent().getStringExtra("role");
                intent.putExtra("role", role);
                startActivity(intent);
            }
        });
        viewVolunteers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, VolunteerListActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
        viewRegisteredDonors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, RegisteredDonorListActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });
        viewReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, ReportsActivity.class);
                intent.putExtra("userId", userId);
                String role = getIntent().getStringExtra("role");
                intent.putExtra("role", role);
                startActivity(intent);
            }
        });
    }

    private void logout(View view){
        finish();
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