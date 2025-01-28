package com.example.blooddonationapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private EditText usernameInput;
    private EditText passwordInput;
    private TextView loginFailedText;
    private Button loginButton;
    private TextView signUpText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        databaseManager = new DatabaseManager(this);
        databaseManager.open();

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginFailedText = findViewById(R.id.loginFailed);
        loginButton = findViewById(R.id.loginButton);
        signUpText = findViewById(R.id.signUpText);

        onLoginAction();
        onClickSignUpText();

    }

    private void onLoginAction(){
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isLoginSuccessful = databaseManager.login(username, password);
                if (isLoginSuccessful) {
                    Cursor cursor = databaseManager.getUserByUsername(username);
                    if (cursor != null && cursor.moveToFirst()) {
                        int roleIdIndex = cursor.getColumnIndex(DatabaseHelper.ROLE_ID_IN_TABLE_USER);
                        int userIdIndex = cursor.getColumnIndex(DatabaseHelper.USER_ID);
                        if (roleIdIndex != -1) {
                            String roleId = cursor.getString(roleIdIndex);
                            String userId = cursor.getString(userIdIndex);
                            Log.e("Login Activity", "RoleId: " + roleId);
                            Log.e("Login Activity", "userId: " + userId);
                            String userRole = databaseManager.getRoleNameById(roleId);
                            Log.e("Login Activity", "User Role Name: " + userRole);
                            Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                            intent.putExtra("role", userRole);
                            intent.putExtra("role", userRole);
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            usernameInput.setText("");
                            passwordInput.setText("");
                            loginFailedText.setVisibility(View.INVISIBLE);
                        } else {
                            Log.e("Login Activity", "Role ID column not found in cursor.");
                            Toast.makeText(LoginActivity.this, "Error retrieving user role.", Toast.LENGTH_SHORT).show();
                        }
                        cursor.close();
                    }
                } else {
                    loginFailedText.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void onClickSignUpText(){
        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                usernameInput.setText("");
                passwordInput.setText("");
                loginFailedText.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseManager.close();
    }
}