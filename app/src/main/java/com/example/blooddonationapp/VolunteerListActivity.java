package com.example.blooddonationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class VolunteerListActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private ListView listView;
    private TextView backToMenuTextView,logoutTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_list); // Uses your provided XML
        listView = findViewById(R.id.listOfVolunteers);
        backToMenuTextView = findViewById(R.id.backToMenuTextView);
        logoutTextView = findViewById(R.id.logoutTextView);
        databaseManager = new DatabaseManager(this);
        databaseManager.open();
        String loggedInManagerId = getIntent().getStringExtra("userId");
        Cursor cursor = databaseManager.getManagersRegisteredAsVolunteers(loggedInManagerId);

        if (cursor != null && cursor.getCount() > 0) {
            try {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                        this,
                        android.R.layout.simple_list_item_2,
                        cursor,
                        new String[]{"user_name", "site_info"},
                        new int[]{android.R.id.text1, android.R.id.text2},
                        0
                ) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        String username = cursor.getString(cursor.getColumnIndexOrThrow("user_name"));
                        String siteInfo = cursor.getString(cursor.getColumnIndexOrThrow("site_info"));

                        text1.setText("Volunteer Information: " + username);
                        text2.setText("Site Information: " + siteInfo);
                    }
                };
                listView.setAdapter(adapter);
            } catch (Exception e) {
                Log.e("VolunteerListActivity", "Error setting up the adapter: ", e);
                Toast.makeText(this, "Error displaying volunteers.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No volunteers registered for your donation sites.", Toast.LENGTH_SHORT).show();
        }
        backToMenuTextView.setOnClickListener(this::backToMenu);
        logoutTextView.setOnClickListener(this::logout);
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
    protected void onDestroy() {
        super.onDestroy();
//        if (databaseManager != null) {
//            databaseManager.close();
//        }
    }
}
