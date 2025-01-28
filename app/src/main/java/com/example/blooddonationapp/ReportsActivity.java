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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
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


public class ReportsActivity extends AppCompatActivity {
    private DatabaseManager databaseManager;
    private LinearLayout listViewReportsLayout, addNewReportLayout,searchAndFilterLayout,reportOutcomeLayout;
    private ScrollView reportDetailsLayout;
    private ListView listViewReports;
    private String roleName, managerId;
    private Button newReportButton, cancelButton,removeButton,updateButton,addButton,cancelAddButton,searchButton,downloadButton;
    private TextView reportId, registerUsername,registerEmail,siteName,siteAddress,siteOpeningTime,siteClosingTime,totalOfValidReports,totalOfValidDonors,totalOfBloodTypes,totalAmountOfBlood,backToMenuTextView,logoutTextView;
    private EditText fullNameOfDonor,bloodType,amountOfBlood,dateOfDonation,registerUsernameInput,siteIdInput,fullNameOfDonorInput,bloodTypeInput,amountOfBloodInput,dateOfDonationInput,searchBar;
    private Spinner openingTimeSpinner, closingTimeSpinner;
    private String registerIdData,siteIdData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        roleName = getIntent().getStringExtra("role");
        managerId = getIntent().getStringExtra("userId");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        databaseManager = new DatabaseManager(this);
        databaseManager.open();
        initializeUI();
        backToMenuTextView.setOnClickListener(this::backToMenu);
        logoutTextView.setOnClickListener(this::logout);
    }

    private void initializeUI() {
        listViewReportsLayout = findViewById(R.id.listViewReportsLayout);
        reportDetailsLayout = findViewById(R.id.reportDetailsLayout);
        addNewReportLayout = findViewById(R.id.addNewReportLayout);
        searchAndFilterLayout = findViewById(R.id.searchAndFilterLayout);
        reportOutcomeLayout = findViewById(R.id.reportOutcomeLayout);

        // Initially, show the listViewReportsLayout and hide the reportDetailsLayout
        setVisible(R.id.reportDetailsLayout, false);
        setVisible(R.id.listViewReportsLayout,true);
        setVisible(R.id.addNewReportLayout,false);
        if(roleName.equals("SUPER_USER")){
            setVisible(R.id.searchAndFilterLayout,true);
            setVisible(R.id.reportOutcomeLayout, true);
            setVisible(R.id.downloadButton, true);
        } else if(roleName.equals("SITE_MANAGER")){
            searchAndFilterLayout.setVisibility(View.GONE);
            reportOutcomeLayout.setVisibility(View.GONE);
            setVisible(R.id.downloadButton, true);
        }
        else{
            searchAndFilterLayout.setVisibility(View.GONE);
            reportOutcomeLayout.setVisibility(View.GONE);
            setVisible(R.id.downloadButton, false);
        }
        // Initialize the ListView
        listViewReports = findViewById(R.id.listViewReports);

        // Initialize buttons in listViewReportsLayout
        newReportButton = findViewById(R.id.newReportButton);

        // Initialize TextViews and EditTexts in reportDetailsLayout
         reportId = findViewById(R.id.reportId);
         registerUsername = findViewById(R.id.registerUsername);
         registerEmail = findViewById(R.id.registerEmail);
         siteName = findViewById(R.id.siteName);
         siteAddress = findViewById(R.id.siteAddress);
         siteOpeningTime = findViewById(R.id.siteOpeningTime);
         siteClosingTime = findViewById(R.id.siteClosingTime);
         fullNameOfDonor = findViewById(R.id.fullNameOfDonor);
         bloodType = findViewById(R.id.bloodType);
         amountOfBlood = findViewById(R.id.amountOfBlood);
         dateOfDonation = findViewById(R.id.dateOfDonation);
        registerUsernameInput = findViewById(R.id.registerUsernameInput);
        siteIdInput = findViewById(R.id.siteIdInput);
        fullNameOfDonorInput = findViewById(R.id.fullNameOfDonorInput);
        bloodTypeInput = findViewById(R.id.bloodTypeInput);
        amountOfBloodInput = findViewById(R.id.amountOfBloodInput);
        dateOfDonationInput = findViewById(R.id.dateOfDonationInput);
        searchBar = findViewById(R.id.searchBar);
        totalOfValidReports = findViewById(R.id.totalOfValidReports);
        totalOfValidDonors = findViewById(R.id.totalOfValidDonors);
        totalOfBloodTypes = findViewById(R.id.totalOfBloodTypes);
        totalAmountOfBlood = findViewById(R.id.totalAmountOfBlood);
        backToMenuTextView = findViewById(R.id.backToMenuTextView);
        logoutTextView = findViewById(R.id.logoutTextView);
        // Initialize buttons in buttonLayout
        cancelButton = findViewById(R.id.cancelButton);
        removeButton = findViewById(R.id.removeButton);
        updateButton = findViewById(R.id.updateButton);
        addButton = findViewById(R.id.addButton);
        cancelAddButton = findViewById(R.id.cancelAddButton);
        searchButton = findViewById(R.id.searchButton);
        downloadButton = findViewById(R.id.downloadButton);

        //Spinner initialize
        openingTimeSpinner = findViewById(R.id.openingTimeSpinner);
        closingTimeSpinner = findViewById(R.id.closingTimeSpinner);

        // Set up the New Report button click listener
        newReportButton.setOnClickListener(view -> {
            setVisible(R.id.reportDetailsLayout, false);
            setVisible(R.id.listViewReportsLayout,false);
            setVisible(R.id.addNewReportLayout,true);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String registerUsername = registerUsernameInput.getText().toString().trim();
                    String siteId = siteIdInput.getText().toString().trim();
                    String fullNameOfDonor = fullNameOfDonorInput.getText().toString().trim();
                    String bloodType = bloodTypeInput.getText().toString().trim();
                    String amountOfBlood = amountOfBloodInput.getText().toString().trim();
                    String dateOfDonation = dateOfDonationInput.getText().toString().trim();
                    // Validate amountOfBloodInput
                    try {
                        double bloodAmount = Double.parseDouble(amountOfBlood);
                        if (bloodAmount <= 0) {
                            Toast.makeText(ReportsActivity.this, "Amount of blood must be a positive number", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(ReportsActivity.this, "Amount of blood must be a valid number", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String datePattern = "^([0-2][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$";
                    if (!dateOfDonation.matches(datePattern)) {
                        Toast.makeText(ReportsActivity.this, "Date of donation must be in dd-MM-yyyy format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean success = databaseManager.createReport(
                            registerUsername,
                            siteId,
                            fullNameOfDonor,
                            bloodType,
                            amountOfBlood,
                            dateOfDonation);

                    if (success) {
                        registerUsernameInput.setText("");
                        siteIdInput.setText("");
                        fullNameOfDonorInput.setText("");
                        bloodTypeInput.setText("");
                        amountOfBloodInput.setText("");
                        dateOfDonationInput.setText("");

                        Toast.makeText(ReportsActivity.this, "Add new report successfully", Toast.LENGTH_SHORT).show();
                        setVisible(R.id.reportDetailsLayout, false);
                        setVisible(R.id.listViewReportsLayout, true);
                        setVisible(R.id.addNewReportLayout, false);
                        loadReports();
                    } else {
                        Toast.makeText(ReportsActivity.this, "Failed to Add new report", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            cancelAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setVisible(R.id.reportDetailsLayout, false);
                    setVisible(R.id.listViewReportsLayout,true);
                    setVisible(R.id.addNewReportLayout,false);
                    registerUsernameInput.setText("");
                    siteIdInput.setText("");
                    fullNameOfDonorInput.setText("");
                    bloodTypeInput.setText("");
                    amountOfBloodInput.setText("");
                    dateOfDonationInput.setText("");
                }
            });
        });

        // Set up the Cancel button click listener
        cancelButton.setOnClickListener(view -> {
            setVisible(R.id.reportDetailsLayout, false);
            setVisible(R.id.listViewReportsLayout,true);
            setVisible(R.id.addNewReportLayout,false);
        });

        removeButton.setOnClickListener(view -> {
            boolean success = databaseManager.removeReportById(reportId.getText().toString());
            if(success){
                setVisible(R.id.reportDetailsLayout, false);
                setVisible(R.id.listViewReportsLayout,true);
                setVisible(R.id.addNewReportLayout,false);
                loadReports();
                fullNameOfDonor.setText("");
                bloodType.setText("");
                amountOfBlood.setText("");
                dateOfDonation.setText("");
                Toast.makeText(ReportsActivity.this, "Remove successfully report with Id: " + reportId.getText().toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ReportsActivity.this, "Failed to remove report with Id: " + reportId.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        updateButton.setOnClickListener(view -> {
            String fullNameOfDonorText = fullNameOfDonor.getText().toString().trim();
            String bloodTypeText = bloodType.getText().toString().trim();
            String amountOfBloodText = amountOfBlood.getText().toString().trim();
            String dateOfDonationText = dateOfDonation.getText().toString().trim();
            try {
                double bloodAmount = Double.parseDouble(amountOfBloodText);
                if (bloodAmount <= 0) {
                    Toast.makeText(ReportsActivity.this, "Amount of blood must be a positive number", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(ReportsActivity.this, "Amount of blood must be a valid number", Toast.LENGTH_SHORT).show();
                return;
            }
            String datePattern = "^([0-2][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$";
            if (!dateOfDonationText.matches(datePattern)) {
                Toast.makeText(ReportsActivity.this, "Date of donation must be in dd-MM-yyyy format", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean success = databaseManager.updateReportById(
                    reportId.getText().toString(),
                    fullNameOfDonorText,
                    bloodTypeText,
                    amountOfBloodText,
                    dateOfDonationText
            );

            if (success) {
                setVisible(R.id.reportDetailsLayout, false);
                setVisible(R.id.listViewReportsLayout, true);
                setVisible(R.id.addNewReportLayout, false);
                loadReports();
                fullNameOfDonor.setText("");
                bloodType.setText("");
                amountOfBlood.setText("");
                dateOfDonation.setText("");
                Toast.makeText(ReportsActivity.this, "Updated successfully report with Id: " + reportId.getText().toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ReportsActivity.this, "Failed to update report with Id: " + reportId.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        searchButton.setOnClickListener(v -> {
            String bloodTypeSearch = searchBar.getText().toString();
            String openingTimeFiltering = openingTimeSpinner.getSelectedItem().toString();
            String closingTimeFiltering = closingTimeSpinner.getSelectedItem().toString();
                Cursor cursor = databaseManager.getAllValidReportsSearch(bloodTypeSearch, openingTimeFiltering, closingTimeFiltering);
                Cursor cursorAggregates = databaseManager.getFilteredReportAggregates(bloodTypeSearch, openingTimeFiltering, closingTimeFiltering);
                if (cursor != null && cursor.getCount() > 0) {
                    // Update the ListView with filtered results
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                            this,
                            android.R.layout.simple_list_item_2,
                            cursor,
                            new String[]{"SiteName", "FullName"},
                            new int[]{android.R.id.text1, android.R.id.text2},
                            0
                    ) {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void bindView(View view, Context context, Cursor cursor) {
                            TextView text1 = view.findViewById(android.R.id.text1);
                            TextView text2 = view.findViewById(android.R.id.text2);

                            String siteName = cursor.getString(cursor.getColumnIndexOrThrow("SiteName"));
                            String userName = cursor.getString(cursor.getColumnIndexOrThrow("FullName"));

                            text1.setText("Donation Site Name: " + siteName);
                            text2.setText("Donor Full Name: " + userName);
                        }
                    };

                    listViewReports.setAdapter(adapter);
                    listViewReports.setOnItemClickListener((parent, view, position, id) -> {
                        cursor.moveToPosition(position);
                        showReportDetailsPopup(cursor);
                    });

                    // Extract and display aggregate information
                    if (cursorAggregates != null && cursorAggregates.moveToFirst()) {
                        @SuppressLint("Range") int totalReports = cursorAggregates.getInt(cursorAggregates.getColumnIndex("TotalOfValidReports"));
                        @SuppressLint("Range") int totalDonors = cursorAggregates.getInt(cursorAggregates.getColumnIndex("TotalOfValidDonors"));
                        @SuppressLint("Range") int totalBloodTypes = cursorAggregates.getInt(cursorAggregates.getColumnIndex("TotalOfBloodTypes"));
                        @SuppressLint("Range") double totalBloodAmount = cursorAggregates.getDouble(cursorAggregates.getColumnIndex("TotalAmountOfBlood"));

                        totalOfValidReports.setText(String.valueOf(totalReports));
                        totalOfValidDonors.setText(String.valueOf(totalDonors));
                        totalOfBloodTypes.setText(String.valueOf(totalBloodTypes));
                        totalAmountOfBlood.setText(String.valueOf(totalBloodAmount));
                    }
            }
            else {
                // Handle the case where no detailed results are found
                Toast.makeText(this, "No results found.", Toast.LENGTH_SHORT).show();
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(roleName.equals("SUPER_USER")){
                    String bloodTypeSearch = searchBar.getText().toString();
                    String openingTimeFiltering = openingTimeSpinner.getSelectedItem().toString();
                    String closingTimeFiltering = closingTimeSpinner.getSelectedItem().toString();
                    if(bloodTypeSearch.isEmpty() && openingTimeFiltering.isEmpty() && closingTimeFiltering.isEmpty()) {
                        downloadReportsAsPDF(totalOfValidReports.getText().toString(), totalOfValidDonors.getText().toString(),totalOfBloodTypes.getText().toString(),totalAmountOfBlood.getText().toString());
                    } else {
                        downloadReportsSearchAsPDF(bloodTypeSearch,openingTimeFiltering,closingTimeFiltering,
                                totalOfValidReports.getText().toString(), totalOfValidDonors.getText().toString(),totalOfBloodTypes.getText().toString(),totalAmountOfBlood.getText().toString());
                    }
                } else {
                    downloadReportsOfManagerId(managerId);
                }
            }
        });
        loadReports();
    }

    // Method to load reports into the ListView
    private void loadReports() {
        if(roleName.equals("SITE_MANAGER")){
            Cursor cursor = databaseManager.getAllReportsByUserId(managerId);

            if (cursor != null && cursor.getCount() > 0) {
//                String[] fromColumns = {"SiteName", "FullName"};
//                int[] toViews = {android.R.id.text1, android.R.id.text2};

                SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                        this,
                        android.R.layout.simple_list_item_2,
                        cursor,
                        new String[]{"SiteName", "FullName"},
                        new int[]{android.R.id.text1, android.R.id.text2},
                        0
                ) {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        String siteName = cursor.getString(cursor.getColumnIndexOrThrow("SiteName"));
                        String userName = cursor.getString(cursor.getColumnIndexOrThrow("FullName"));

                        text1.setText("Donation Site Name: " + siteName);
                        text2.setText("Donor Full Name: " + userName);
                    }
                };

                listViewReports.setAdapter(adapter);
                listViewReports.setOnItemClickListener((parent, view, position, id) -> {
                    cursor.moveToPosition(position);
                    showReportDetailsPopup(cursor);
                });
            } else {
                Toast.makeText(this, "No registered donors found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Cursor cursor = databaseManager.getAllValidReports();
            Cursor aggregateCursor = databaseManager.getReportAggregates();
                if (cursor != null && cursor.getCount() > 0) {
                    if (aggregateCursor != null && aggregateCursor.moveToFirst()) {
                        @SuppressLint("Range") int totalOfValidReportsData = aggregateCursor.getInt(aggregateCursor.getColumnIndex("TotalOfValidReports"));
                        @SuppressLint("Range") int totalOfValidDonorsData = aggregateCursor.getInt(aggregateCursor.getColumnIndex("TotalOfValidDonors"));
                        @SuppressLint("Range") int totalOfBloodTypesData = aggregateCursor.getInt(aggregateCursor.getColumnIndex("TotalOfBloodTypes"));
                        @SuppressLint("Range") double totalAmountOfBloodData = aggregateCursor.getDouble(aggregateCursor.getColumnIndex("TotalAmountOfBlood"));

                        Log.d("AggregateData", "Reports: " + totalOfValidReportsData +
                                ", Donors: " + totalOfValidDonorsData +
                                ", Blood Types: " + totalOfBloodTypesData +
                                ", Total Blood: " + totalAmountOfBloodData);

                        totalOfValidReports.setText(String.valueOf(totalOfValidReportsData));
                        totalOfValidDonors.setText(String.valueOf(totalOfValidDonorsData));
                        totalOfBloodTypes.setText(String.valueOf(totalOfBloodTypesData));
                        totalAmountOfBlood.setText(String.valueOf(totalAmountOfBloodData));

                        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                                this,
                                android.R.layout.simple_list_item_2,
                                cursor,
                                new String[]{"SiteName", "FullName"},
                                new int[]{android.R.id.text1, android.R.id.text2},
                                0
                        ) {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void bindView(View view, Context context, Cursor cursor) {
                                TextView text1 = view.findViewById(android.R.id.text1);
                                TextView text2 = view.findViewById(android.R.id.text2);

                                String siteName = cursor.getString(cursor.getColumnIndexOrThrow("SiteName"));
                                String userName = cursor.getString(cursor.getColumnIndexOrThrow("FullName"));

                                text1.setText("Donation Site Name: " + siteName);
                                text2.setText("Donor Full Name: " + userName);
                            }
                        };

                        listViewReports.setAdapter(adapter);
                        listViewReports.setOnItemClickListener((parent, view, position, id) -> {
                            cursor.moveToPosition(position);
                            showReportDetailsPopup(cursor);
                        });

                } else {
                    Toast.makeText(this, "No registered donors found.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showReportDetailsPopup(Cursor cursor) {
        setVisible(R.id.reportDetailsLayout,true);
        setVisible(R.id.listViewReportsLayout,false);
        setVisible(R.id.addNewReportLayout,false);
        String reportIdData = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
        String amountOfBloodData = cursor.getString(cursor.getColumnIndexOrThrow("AmountOfBlood"));
        String bloodTypeData = cursor.getString(cursor.getColumnIndexOrThrow("BloodType"));
        String reportDateData = cursor.getString(cursor.getColumnIndexOrThrow("ReportDate"));
        String fullNameData = cursor.getString(cursor.getColumnIndexOrThrow("FullName"));
        registerIdData = cursor.getString(cursor.getColumnIndexOrThrow("RegisterId"));
        siteIdData = cursor.getString(cursor.getColumnIndexOrThrow("SiteId"));
        String siteNameData = cursor.getString(cursor.getColumnIndexOrThrow("SiteName"));
        String addressData = cursor.getString(cursor.getColumnIndexOrThrow("Address"));
        String openingTimeData = cursor.getString(cursor.getColumnIndexOrThrow("OpeningTime"));
        String closingTimeData = cursor.getString(cursor.getColumnIndexOrThrow("ClosingTime"));
        String usernameData = cursor.getString(cursor.getColumnIndexOrThrow("UserName"));
        String emailData = cursor.getString(cursor.getColumnIndexOrThrow("Email"));

        reportId.setText(reportIdData);
        registerUsername.setText(usernameData);
        registerEmail.setText(emailData);
        siteName.setText(siteNameData);
        siteAddress.setText(addressData);
        siteOpeningTime.setText(openingTimeData);
        siteClosingTime.setText(closingTimeData);
        fullNameOfDonor.setText(fullNameData);
        bloodType.setText(bloodTypeData);
        amountOfBlood.setText(amountOfBloodData);
        dateOfDonation.setText(reportDateData);
    }

    private void setVisible(int id, boolean isVisible){
        View view = findViewById(id);
        if(isVisible){
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.INVISIBLE);
        }
    }


    @SuppressLint("Range")
    private void downloadReportsAsPDF(String totalOfValidReports, String totalOfValidDonors, String totalOfBloodTypes, String totalAmountOfBlood) {
        Cursor cursor = databaseManager.getAllValidReports(); // Fetch all reports or filter based on query
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No reports available to download", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Reports");
            if (!pdfFolder.exists()) {
                pdfFolder.mkdir();
            }
            File pdfFile = new File(pdfFolder, "BloodDonationReportsSuperUser.pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Blood Donation Reports").setBold().setFontSize(18).setMarginBottom(20));
            document.add(new Paragraph("Total Number Of Reports: " + totalOfValidReports).setBold().setFontSize(14).setMarginBottom(10));
            document.add(new Paragraph("Total Number Of Donors: " + totalOfValidDonors).setBold().setFontSize(14).setMarginBottom(10));
            document.add(new Paragraph("Total Number Of Blood Types: " + totalOfBloodTypes).setBold().setFontSize(14).setMarginBottom(10));
            document.add(new Paragraph("Total Amount Of Blood: " + totalAmountOfBlood).setBold().setFontSize(14).setMarginBottom(10));
            Table table = new Table(new float[]{2, 4, 3, 3, 3, 4, 4});
            table.setMaxWidth(100);
            table.addHeaderCell("REPORT ID");
            table.addHeaderCell("FULL NAME");
            table.addHeaderCell("BLOOD TYPE");
            table.addHeaderCell("BLOOD AMOUNT");
            table.addHeaderCell("REPORT_DATE");
            table.addHeaderCell("SITE_NAME");
            table.addHeaderCell("SITE_ADDRESS");
            while (cursor.moveToNext()) {
                table.addCell(cursor.getString(cursor.getColumnIndex("_id")));
                table.addCell(cursor.getString(cursor.getColumnIndex("FullName")));
                table.addCell(cursor.getString(cursor.getColumnIndex("BloodType")));
                table.addCell(cursor.getString(cursor.getColumnIndex("AmountOfBlood")));
                table.addCell(cursor.getString(cursor.getColumnIndex("ReportDate")));
                table.addCell(cursor.getString(cursor.getColumnIndex("SiteName")));
                table.addCell(cursor.getString(cursor.getColumnIndex("Address")));
            }
            document.add(table);
            document.close();
            Toast.makeText(this, "PDF downloaded successfully: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @SuppressLint("Range")
    private void downloadReportsSearchAsPDF(String bloodTypeSearch, String openingTimeFiltering, String closingTimeFiltering,String totalOfValidReports, String totalOfValidDonors, String totalOfBloodTypes, String totalAmountOfBlood) {
        Cursor cursor = databaseManager.getAllValidReportsSearch(bloodTypeSearch,openingTimeFiltering,closingTimeFiltering); // Fetch all reports or filter based on query
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No reports available to download", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Reports");
            if (!pdfFolder.exists()) {
                pdfFolder.mkdir();
            }
            File pdfFile = new File(pdfFolder, "BloodDonationReportsSuperUser.pdf");

            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Blood Donation Reports").setBold().setFontSize(18).setMarginBottom(20));
            document.add(new Paragraph("Total Number Of Reports: " + totalOfValidReports).setBold().setFontSize(14).setMarginBottom(10));
            document.add(new Paragraph("Total Number Of Donors: " + totalOfValidDonors).setBold().setFontSize(14).setMarginBottom(10));
            document.add(new Paragraph("Total Number Of Blood Types: " + totalOfBloodTypes).setBold().setFontSize(14).setMarginBottom(10));
            document.add(new Paragraph("Total Amount Of Blood: " + totalAmountOfBlood).setBold().setFontSize(14).setMarginBottom(10));
            Table table = new Table(new float[]{2, 4, 3, 3, 3, 4, 4});
            table.setMaxWidth(100);
            table.addHeaderCell("REPORT ID");
            table.addHeaderCell("FULL NAME");
            table.addHeaderCell("BLOOD TYPE");
            table.addHeaderCell("BLOOD AMOUNT");
            table.addHeaderCell("REPORT_DATE");
            table.addHeaderCell("SITE_NAME");
            table.addHeaderCell("SITE_ADDRESS");
            while (cursor.moveToNext()) {
                table.addCell(cursor.getString(cursor.getColumnIndex("_id")));
                table.addCell(cursor.getString(cursor.getColumnIndex("FullName")));
                table.addCell(cursor.getString(cursor.getColumnIndex("BloodType")));
                table.addCell(cursor.getString(cursor.getColumnIndex("AmountOfBlood")));
                table.addCell(cursor.getString(cursor.getColumnIndex("ReportDate")));
                table.addCell(cursor.getString(cursor.getColumnIndex("SiteName")));
                table.addCell(cursor.getString(cursor.getColumnIndex("Address")));
            }

            // Add table to the document
            document.add(table);

            // Close the document
            document.close();

            Toast.makeText(this, "PDF downloaded successfully: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @SuppressLint("Range")
    private void downloadReportsOfManagerId(String managerId) {
        Cursor cursor = databaseManager.getAllValidReportsByUserId(managerId); // Fetch all reports or filter based on query
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "No reports available to download", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Reports");
            if (!pdfFolder.exists()) {
                pdfFolder.mkdir();
            }
            File pdfFile = new File(pdfFolder, "BloodDonationReportsManager.pdf");
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Blood Donation Reports").setBold().setFontSize(18).setMarginBottom(20));
            Table table = new Table(new float[]{2, 4, 3, 3, 3, 4, 4});
            table.setMaxWidth(100);
            table.addHeaderCell("REPORT ID");
            table.addHeaderCell("FULL NAME");
            table.addHeaderCell("BLOOD TYPE");
            table.addHeaderCell("BLOOD AMOUNT");
            table.addHeaderCell("REPORT_DATE");
            table.addHeaderCell("SITE_NAME");
            table.addHeaderCell("SITE_ADDRESS");
            while (cursor.moveToNext()) {
                table.addCell(cursor.getString(cursor.getColumnIndex("_id")));
                table.addCell(cursor.getString(cursor.getColumnIndex("FullName")));
                table.addCell(cursor.getString(cursor.getColumnIndex("BloodType")));
                table.addCell(cursor.getString(cursor.getColumnIndex("AmountOfBlood")));
                table.addCell(cursor.getString(cursor.getColumnIndex("ReportDate")));
                table.addCell(cursor.getString(cursor.getColumnIndex("SiteName")));
                table.addCell(cursor.getString(cursor.getColumnIndex("Address")));
            }

            document.add(table);
            document.close();

            Toast.makeText(this, "PDF downloaded successfully: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error downloading PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    public void backToMenu(View view){
        finish();
        searchBar.setText("");
        openingTimeSpinner.getSelectedItem().toString();
        openingTimeSpinner.getSelectedItem().toString();
    }
    public void logout(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        // Clear all activities above SignInActivity in the stack
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        searchBar.setText("");
        openingTimeSpinner.getSelectedItem().toString();
        openingTimeSpinner.getSelectedItem().toString();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (databaseManager != null) {
//            databaseManager.close();
//        }
    }

}