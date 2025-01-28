package com.example.blooddonationapp;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private EditText username, email, password, repeatedPassword;
    private TextView logInText,backToSignTextView;
    private Button signupButton;
    private ListView registerOptionsList;
    private String selectedRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setVisible(R.id.options,true);
        setVisible(R.id.createAccount, false);
        databaseManager = new DatabaseManager(this);
        databaseManager.open();

        registerOptionsList = findViewById(R.id.registerOptionsList);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        repeatedPassword = findViewById(R.id.passwordRepeated);
        logInText = findViewById(R.id.logInText);
        signupButton = findViewById(R.id.signupButton);
        backToSignTextView = findViewById(R.id.backToSignTextView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.user_roles, android.R.layout.simple_list_item_1);
        registerOptionsList.setAdapter(adapter);

        registerOptionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedRole = (String) parent.getItemAtPosition(position);
                setVisible(R.id.options,false);
                setVisible(R.id.createAccount, true);
                Toast.makeText(SignupActivity.this, "Selected Role: " + selectedRole, Toast.LENGTH_SHORT).show();
            }
        });

        signupButton.setOnClickListener(v -> {
            if (selectedRole == null) {
                Toast.makeText(this, "Please select a role first.", Toast.LENGTH_SHORT).show();
                return;
            }

            String roleName = selectedRole.toUpperCase().replace(" ", "_");
            String roleId = databaseManager.getRoleIdByRoleName(roleName);

            if (roleId == null) {
                Toast.makeText(this, "Role not found: " + selectedRole, Toast.LENGTH_SHORT).show();
                return;
            }

            String usernameText = username.getText().toString();
            String emailText = email.getText().toString();
            String passwordText = password.getText().toString();
            String repeatedPasswordText = repeatedPassword.getText().toString();
            if (usernameText.isEmpty() || emailText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!emailText.endsWith("@gmail.com")) {
                Toast.makeText(this, "Email must end with '@gmail.com'.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!repeatedPasswordText.equals(passwordText)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }
            Cursor cursor = databaseManager.getUserByUsername(usernameText);
            if(cursor != null){
                boolean isAdded = databaseManager.addUser(usernameText, emailText, passwordText, roleId);
                if (isAdded) {
                    Toast.makeText(this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    username.setText("");
                    email.setText("");
                    password.setText("");
                    repeatedPassword.setText("");
                    finish();
                } else {
                    Toast.makeText(this, "This user is already existed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        logInText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setText("");
                email.setText("");
                password.setText("");
                repeatedPassword.setText("");
                finish();
            }
        });
        backToSignTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username.setText("");
                email.setText("");
                password.setText("");
                repeatedPassword.setText("");
                finish();
            }
        });
    }

    private void setVisible(int id, boolean isVisible){
        View view = findViewById(id);
        if(isVisible){
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseManager.close();
    }
}