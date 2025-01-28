package com.example.blooddonationapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.File;
import java.io.FileOutputStream;

public class RegisteredDonorListActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseManager databaseManager;
    private String managerId;
    private String siteId;
    private ScrollView listViewLayout,itemRegisteredDonorDetails;

    private TextView registerId, registerUsername, registerEmail, donationSiteName, siteAddress, registrationId, isConfirmedStatus,backToMenuTextView,logoutTextView;
    private EditText numberOfDonorsEditText;
    private Button cancelButton, confirmStatusButton, updateButton,downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registered_donor_list);

        initializeUI();
        setVisible(R.id.itemRegisteredDonorDetails, false);
        databaseManager = new DatabaseManager(this);
        databaseManager.open();

        managerId = getIntent().getStringExtra("userId");

        loadDonorList();
        backToMenuTextView.setOnClickListener(this::backToMenu);
        logoutTextView.setOnClickListener(this::logout);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadDonorListAsPDF(managerId);
            }
        });
    }

    private void initializeUI() {
        listView = findViewById(R.id.listOfRegisterDonors);
        itemRegisteredDonorDetails = findViewById(R.id.itemRegisteredDonorDetails);

        registerId = findViewById(R.id.registerId);
        registerUsername = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        donationSiteName = findViewById(R.id.donationSiteName);
        siteAddress = findViewById(R.id.siteAddress);
        registrationId = findViewById(R.id.registrationId);
        isConfirmedStatus = findViewById(R.id.isConfirmedStatus);
        numberOfDonorsEditText = findViewById(R.id.numberOfDonors);
        cancelButton = findViewById(R.id.cancelButton);
        confirmStatusButton = findViewById(R.id.confirmStatus);
        updateButton = findViewById(R.id.updateButton);
        backToMenuTextView = findViewById(R.id.backToMenuTextView);
        logoutTextView = findViewById(R.id.logoutTextView);
        downloadButton = findViewById(R.id.downloadButton);
        cancelButton.setOnClickListener(view -> setVisible(R.id.itemRegisteredDonorDetails, false));
        confirmStatusButton.setOnClickListener(view -> confirmDonorStatus());
        updateButton.setOnClickListener(view -> updateNumberOfDonors());
    }

    @SuppressLint("Range")
    private void downloadDonorListAsPDF(String managerId) {
        Cursor cursor = databaseManager.getDonorsRegisteredDonation(managerId);
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No donors available to download", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Define the file location
            File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Reports");
            if (!pdfFolder.exists()) {
                pdfFolder.mkdir();
            }
            File pdfFile = new File(pdfFolder, "DonorList.pdf");

            // Initialize PDF Writer
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title to the PDF
            document.add(new Paragraph("Donor List").setBold().setFontSize(18).setMarginBottom(20));

            // Define table structure
            Table table = new Table(new float[]{2, 4, 4, 3, 3});
            table.setMaxWidth(100);

            // Add table headers
            table.addHeaderCell("DONOR ID");
            table.addHeaderCell("DONOR NAME");
            table.addHeaderCell("EMAIL");
            table.addHeaderCell("NUMBER OF DONORS");
            table.addHeaderCell("CONFIRM STATUS");

            // Populate table rows with data from the cursor
            while (cursor.moveToNext()) {
                table.addCell(cursor.getString(cursor.getColumnIndex("_id")));
                table.addCell(cursor.getString(cursor.getColumnIndex("UserName")));
                table.addCell(cursor.getString(cursor.getColumnIndex("Email")));
                table.addCell(cursor.getString(cursor.getColumnIndex("NumberOfDonors")));
                String isConfirmed = cursor.getString(cursor.getColumnIndex("IsConfirmed"));
                String confirmStatus = null;
                if(isConfirmed.equalsIgnoreCase("1")){
                    confirmStatus = "Confirmed";
                }else {
                    confirmStatus = "Processing";
                }
                table.addCell(confirmStatus);
            }
            cursor.close();
            document.add(table);
            document.close();

            Toast.makeText(this, "PDF downloaded successfully: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void loadDonorList() {
        Cursor cursor = databaseManager.getDonorsRegisteredDonation(managerId);

        if (cursor != null && cursor.getCount() > 0) {
//            String[] fromColumns = {"UserName", "Email"};
//            int[] toViews = {android.R.id.text1, android.R.id.text2};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_2,
                    cursor,
                    new String[]{"UserName", "Email"},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    0
            ) {
                @SuppressLint("SetTextI18n")
                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);

                    String username = cursor.getString(cursor.getColumnIndexOrThrow("UserName"));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
                    String donorNumber = cursor.getString(cursor.getColumnIndexOrThrow("NumberOfDonors"));

                    text1.setText("Donor Username: " + username + "\nDonor Number: " + donorNumber);
                    text2.setText("Donor Email: " + email);
                }
            };
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                cursor.moveToPosition(position);
                showDonorDetailsPopup(cursor);
            });
        } else {
            Toast.makeText(this, "No registered donors found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDonorDetailsPopup(Cursor cursor) {
        setVisible(R.id.itemRegisteredDonorDetails, true);

        String username = cursor.getString(cursor.getColumnIndexOrThrow("UserName"));
        String email = cursor.getString(cursor.getColumnIndexOrThrow("Email"));
        String userId = cursor.getString(cursor.getColumnIndexOrThrow("UserId"));
        String siteName = cursor.getString(cursor.getColumnIndexOrThrow("SiteName"));
        String address = cursor.getString(cursor.getColumnIndexOrThrow("Address"));
        String donationRegistrationId = cursor.getString(cursor.getColumnIndexOrThrow("DonationRegistrationId"));
        String isConfirmed = cursor.getString(cursor.getColumnIndexOrThrow("IsConfirmed"));
        String numberOfDonors = cursor.getString(cursor.getColumnIndexOrThrow("NumberOfDonors"));
        siteId = cursor.getString(cursor.getColumnIndexOrThrow("SiteId"));

        registerId.setText(userId);
        registerUsername.setText(username);
        registerEmail.setText(email);
        donationSiteName.setText(siteName);
        siteAddress.setText(address);
        registrationId.setText(donationRegistrationId);
        isConfirmedStatus.setText(isConfirmed.equals("0") ? "False" : "True");
        numberOfDonorsEditText.setText(numberOfDonors);

        confirmStatusButton.setVisibility(isConfirmed.equals("0") ? View.VISIBLE : View.INVISIBLE);
    }

    private void confirmDonorStatus() {
        String donationRegistrationId = registrationId.getText().toString();
        String userId = registerId.getText().toString();
        int numberOfDonors = Integer.parseInt(numberOfDonorsEditText.getText().toString());

        boolean success = databaseManager.updateDonationRegistrationStatus(donationRegistrationId, true);
        if(success){
        for (int i = 0; i < numberOfDonors; i++) {
            boolean isReportGenerated = databaseManager.generateReport(userId, siteId);
            if (!isReportGenerated) {
                Log.e("Confirmation", "Failed to generate report for donor #" + (i + 1));
            } else {
                Log.i("Confirmation", "Report generated successfully for donor #" + (i + 1));
            }
        }

        Toast.makeText(this, "Donor status confirmed and reports generated.", Toast.LENGTH_SHORT).show();
        loadDonorList();
        setVisible(R.id.itemRegisteredDonorDetails, false);
        }
        else{
            Toast.makeText(this, "Donor status wrong", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNumberOfDonors() {
        try {
            String donationRegistrationId = registrationId.getText().toString();
            int updatedNumber = Integer.parseInt(numberOfDonorsEditText.getText().toString());
            boolean success = databaseManager.updateDonationRegistrationNumber(donationRegistrationId, updatedNumber);
            if(success){
                numberOfDonorsEditText.setText(String.valueOf(updatedNumber));
                Toast.makeText(this, "Number of donors updated: " + updatedNumber, Toast.LENGTH_SHORT).show();
                loadDonorList();
                setVisible(R.id.itemRegisteredDonorDetails, false);
            } else {
                Toast.makeText(this, "Cannot Update Number of donors into: " + updatedNumber, Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format.", Toast.LENGTH_SHORT).show();
        }
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

    private void setVisible(int id, boolean isVisible){
        View view = findViewById(id);
        if(isVisible){
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.INVISIBLE);
        }
    }
}
