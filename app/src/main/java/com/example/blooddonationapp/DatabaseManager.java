package com.example.blooddonationapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.uuid.Generators;

import java.util.ArrayList;
import java.util.List;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;
    public DatabaseManager(Context c){
        context = c;
    }

    public DatabaseManager open() throws SQLException {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }


    // Close the database
    public void close() {
        dbHelper.close();
    }

    // READ: Retrieve role by ROLE_NAME
    public String getRoleIdByRoleName(String roleName) {
        String roleId = null;
        String[] columns = { DatabaseHelper.ROLE_ID };

        // Query the database
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_ROLE,
                columns,
                DatabaseHelper.ROLE_NAME + "=?",
                new String[]{roleName},
                null,
                null,
                null
        );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                roleId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ROLE_ID));
            }
            cursor.close();
        }
        return roleId;
    }

    public String getRoleNameById(String roleId) {
        String roleName = null;
        String[] columns = { DatabaseHelper.ROLE_NAME };
        Cursor cursor = database.query(DatabaseHelper.TABLE_ROLE, columns,
                DatabaseHelper.ROLE_ID + "=?", new String[]{roleId},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int roleNameIndex = cursor.getColumnIndex(DatabaseHelper.ROLE_NAME);
            if (roleNameIndex != -1) {
                roleName = cursor.getString(roleNameIndex);
            }
            cursor.close();
        }
        return roleName;
    }

    // CREATE: Add a new user to the User table
    public boolean addUser(String userName, String email, String password, String roleId) {
        String hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.USER_ID, Generators.randomBasedGenerator().generate().toString());
        values.put(DatabaseHelper.USER_NAME, userName);
        values.put(DatabaseHelper.EMAIL, email);
        values.put(DatabaseHelper.PASSWORD, hashedPassword);
        values.put(DatabaseHelper.ROLE_ID_IN_TABLE_USER, roleId);

        long result = database.insert(DatabaseHelper.TABLE_USER, null, values);
        if (result == -1) {
            Log.e("DatabaseManager", "Error inserting user: " + userName);
            return false;
        } else {
            Log.i("DatabaseManager", "User inserted successfully: " + userName);
            return true;
        }
    }
    // READ: Retrieve a user by USER_ID
    public Cursor getUserById(String userId) {
        String[] columns = {
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_NAME,
                DatabaseHelper.EMAIL,
                DatabaseHelper.PASSWORD,
                DatabaseHelper.ROLE_ID
        };
        return database.query(DatabaseHelper.TABLE_USER, columns,
                DatabaseHelper.USER_ID + "=?", new String[]{userId},
                null, null, null);
    }

    // READ: Retrieve a user by USER_NAME
    public Cursor getUserByUsername(String username) {
        String[] columns = {
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_NAME,
                DatabaseHelper.EMAIL,
                DatabaseHelper.PASSWORD,
                DatabaseHelper.ROLE_ID_IN_TABLE_USER
        };

        return database.query(DatabaseHelper.TABLE_USER, columns,
                DatabaseHelper.USER_NAME + "=?", new String[]{username},
                null, null, null);
    }
    public boolean verifyPassword(String enteredPassword, String storedHashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(enteredPassword.toCharArray(), storedHashedPassword);
        return result.verified;
    }

    public boolean login(String username, String enteredPassword) {
        Cursor cursor = getUserByUsername(username);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String storedHashedPassword = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PASSWORD));
            cursor.close();

            return verifyPassword(enteredPassword, storedHashedPassword);
        }

        return false;
    }

    //Donation Sites CRUD
    // Create a new DonationSite
    public boolean createDonationSite(String siteName, String address, String longitude, String latitude, String openingTime, String closingTime, String managerId) {
        String siteId = Generators.randomBasedGenerator().generate().toString();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SITE_ID, siteId);
        values.put(DatabaseHelper.SITE_NAME, siteName);
        values.put(DatabaseHelper.ADDRESS, address);
        values.put(DatabaseHelper.LONGITUDE, longitude);
        values.put(DatabaseHelper.LATITUDE, latitude);
        values.put(DatabaseHelper.OPENING_TIME, openingTime);
        values.put(DatabaseHelper.CLOSING_TIME, closingTime);
        values.put(DatabaseHelper.MANAGER_ID, managerId);

        long result = database.insert(DatabaseHelper.TABLE_DONATION_SITE, null, values);
        Log.i("DatabaseManager","Inserted Donation Site with ID: " + siteId);

        return result != -1;
    }

    // Retrieve a DonationSite by ID
    public Cursor getDonationSiteById(String siteId) {
        return database.query(
                DatabaseHelper.TABLE_DONATION_SITE,
                null,
                DatabaseHelper.SITE_ID + " = ?",
                new String[]{siteId},
                null,
                null,
                null
        );
    }

    // Retrieve all DonationSites
    public Cursor getAllDonationSites() {
        return database.query(
                DatabaseHelper.TABLE_DONATION_SITE,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    // Update a DonationSite
    public boolean updateDonationSiteById(String siteId, String siteName, String openingTime, String closingTime) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SITE_NAME, siteName);
        values.put(DatabaseHelper.OPENING_TIME, openingTime);
        values.put(DatabaseHelper.CLOSING_TIME, closingTime);
        values.put(DatabaseHelper.UPDATED_AT, System.currentTimeMillis());
        int rowsAffected = database.update(
                DatabaseHelper.TABLE_DONATION_SITE,
                values,
                DatabaseHelper.SITE_ID + " = ?",
                new String[]{siteId}
        );

        Log.i("DatabaseManager","Updated Donation Site with ID: " + siteId);
        Intent intent = new Intent("com.example.DONATION_SITE_UPDATE");
        intent.putExtra("siteId", siteId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        return rowsAffected > 0;
    }

    //check current volunteer and donation site id having in the database
    public boolean checkVolunteeringState(String managerId, String donationSiteId) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_VOLUNTEER_REGISTRATION,
                    new String[]{DatabaseHelper.VOLUNTEER_REGISTRATION_ID},
                    DatabaseHelper.MANAGER_ID + " = ? AND " + DatabaseHelper.DONATION_SITE_ID + " = ?",
                    new String[]{managerId, donationSiteId},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                Log.i("DatabaseManager", "Volunteer registration found for Manager ID: " + managerId + " and Site ID: " + donationSiteId);
                return true;
            } else {
                Log.i("DatabaseManager", "No volunteer registration found for Manager ID: " + managerId + " and Site ID: " + donationSiteId);
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error checking volunteer registration: ", e);
        }
        return false;
    }


    // Create a new volunteer registration
    public boolean createVolunteerRegistration(String managerId, String donationSiteId) {
        String registrationId = Generators.randomBasedGenerator().generate().toString();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.VOLUNTEER_REGISTRATION_ID, registrationId);
        values.put(DatabaseHelper.MANAGER_ID, managerId);
        values.put(DatabaseHelper.DONATION_SITE_ID, donationSiteId);

        try {
            long result = database.insert(DatabaseHelper.TABLE_VOLUNTEER_REGISTRATION, null, values);
            if (result == -1) {
                Log.e("DatabaseManager","Failed to insert volunteer registration with ID: " + registrationId);
                return false;
            } else {
                Log.i("DatabaseManager","Volunteer registration created successfully with ID: " + registrationId);
                return true;
            }
        } catch (Exception e) {
            Log.e("DatabaseManager","Error inserting volunteer registration: ", e);
            return false;
        }
    }

    // Delete a volunteer registration by managerId and siteId
    public boolean deleteVolunteerRegistration(String managerId, String siteId) {
        int rowsDeleted = 0;
        try {
            rowsDeleted = database.delete(
                    DatabaseHelper.TABLE_VOLUNTEER_REGISTRATION,
                    DatabaseHelper.MANAGER_ID + " = ? AND " + DatabaseHelper.DONATION_SITE_ID + " = ?",
                    new String[]{managerId, siteId}
            );
            if (rowsDeleted > 0) {
                Log.i("DatabaseManager", "Volunteer registration deleted successfully for Manager ID: " + managerId + " and Site ID: " + siteId);
                return true;
            } else {
                Log.w("DatabaseManager", "No volunteer registration found for Manager ID: " + managerId + " and Site ID: " + siteId);
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error deleting volunteer registration: ", e);
        }
        return false;
    }

    // Retrieve a cursor for managers registered as volunteers in the donation sites of the logged-in manager
    public Cursor getManagersRegisteredAsVolunteers(String loggedInManagerId) {
        Cursor cursor = null;

        try {
            String tables = DatabaseHelper.TABLE_VOLUNTEER_REGISTRATION + " vr " +
                    "JOIN " + DatabaseHelper.TABLE_DONATION_SITE + " ds ON vr." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID +
                    " JOIN " + DatabaseHelper.TABLE_USER + " u ON vr." + DatabaseHelper.MANAGER_ID + " = u." + DatabaseHelper.USER_ID;
            String[] columns = {
                    "u." + DatabaseHelper.USER_ID,
                    "u." + DatabaseHelper.USER_NAME + " || ', ' || u." + DatabaseHelper.EMAIL + " AS user_name",
                    "ds." + DatabaseHelper.SITE_NAME + " || ', ' || ds." + DatabaseHelper.ADDRESS + " AS site_info"
            };
            String selection = "ds." + DatabaseHelper.MANAGER_ID + " = ?";
            String[] selectionArgs = { loggedInManagerId };
            cursor = database.query(
                    tables,
                    columns,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error fetching volunteer managers: ", e);
        }

        return cursor;
    }

    // Create a new donation registration
    public boolean createDonationRegistration(String numberOfDonor, String registerId, String donationSiteId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.DONATION_REGISTRATION_ID, Generators.randomBasedGenerator().generate().toString()); // Generate random ID
        values.put(DatabaseHelper.NUMBER_OF_DONOR, numberOfDonor);
        values.put(DatabaseHelper.REGISTER_ID, registerId);
        values.put(DatabaseHelper.DONATION_SITE_ID, donationSiteId);
        values.put(DatabaseHelper.IS_CONFIRMED, 0); // Default value for IS_CONFIRMED is 0

        long result = database.insert(DatabaseHelper.TABLE_DONATION_REGISTRATION, null, values); // Insert into the DONATION_REGISTRATION table
        return result != -1;
    }

    // Update an existing donation registration
    public boolean updateDonationRegistrationStatus(String donationRegistrationId, boolean isConfirmed) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.IS_CONFIRMED, isConfirmed ? 1 : 0);

        int rowsUpdated = database.update(DatabaseHelper.TABLE_DONATION_REGISTRATION, values, DatabaseHelper.DONATION_REGISTRATION_ID+ " = ?", new String[]{donationRegistrationId});
        return rowsUpdated > 0;
    }

    // Update an existing donation registration
    public boolean updateDonationRegistrationNumber(String donationRegistrationId, int numberOfDonors) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NUMBER_OF_DONOR, numberOfDonors);

        int rowsUpdated = database.update(DatabaseHelper.TABLE_DONATION_REGISTRATION, values, DatabaseHelper.DONATION_REGISTRATION_ID+ " = ?", new String[]{donationRegistrationId});
        return rowsUpdated > 0;
    }

    public int checkDonationRegistrationStatus(String registerId, String donationSiteId) {
        int status = -1;//none
        Cursor cursor = null;

        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_DONATION_REGISTRATION,
                    new String[]{DatabaseHelper.IS_CONFIRMED},
                    DatabaseHelper.REGISTER_ID + " = ? AND  " + DatabaseHelper.DONATION_SITE_ID + " = ?",
                    new String[]{registerId, donationSiteId},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                status = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.IS_CONFIRMED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return status;
    }

    //retrieve registered donation sites of the current login donor
    public Cursor getRegisteredDonationSites(String userId) {
        String table = DatabaseHelper.TABLE_DONATION_REGISTRATION + " dr INNER JOIN " +
                DatabaseHelper.TABLE_DONATION_SITE + " ds ON dr." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID;
        String[] columns = {
                "ds." + DatabaseHelper.SITE_ID,
                "ds." + DatabaseHelper.SITE_NAME,
                "ds." + DatabaseHelper.ADDRESS,
                "ds." + DatabaseHelper.LONGITUDE,
                "ds." + DatabaseHelper.LATITUDE,
                "ds." + DatabaseHelper.OPENING_TIME,
                "ds." + DatabaseHelper.CLOSING_TIME
        };
        String selection = "dr." + DatabaseHelper.REGISTER_ID + " = ?";
        String[] selectionArgs = { userId };
        return database.query(
                table,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    public Cursor getDonorsRegisteredDonation(String loginManagerId) {
        // Define the columns to retrieve
        String[] columns = {
                "u." + DatabaseHelper.USER_ID + " AS _id", // Alias for _id
                "u." + DatabaseHelper.USER_ID + " AS UserId",
                "u." + DatabaseHelper.USER_NAME + " AS UserName",
                "u." + DatabaseHelper.EMAIL + " AS Email",
                "ds." + DatabaseHelper.SITE_NAME + " AS SiteName",
                "ds." + DatabaseHelper.SITE_ID + " AS SiteId",
                "ds." + DatabaseHelper.ADDRESS + " AS Address",
                "dr." + DatabaseHelper.NUMBER_OF_DONOR + " AS NumberOfDonors",
                "dr." + DatabaseHelper.IS_CONFIRMED + " AS IsConfirmed",
                "dr." + DatabaseHelper.DONATION_REGISTRATION_ID + " AS DonationRegistrationId"
        };
        String tables = DatabaseHelper.TABLE_DONATION_REGISTRATION + " dr " +
                "INNER JOIN " + DatabaseHelper.TABLE_USER + " u " +
                "ON dr." + DatabaseHelper.REGISTER_ID + " = u." + DatabaseHelper.USER_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_DONATION_SITE + " ds " +
                "ON dr." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID;
        String selection = "ds." + DatabaseHelper.MANAGER_ID + " = ?";
        String[] selectionArgs = { loginManagerId };
        return database.query(tables, columns, selection, selectionArgs, null, null, null);
    }

    //api create number of reports depends on the number of donors
    public boolean generateReport(String donorId, String donationSiteId) {
        try {
            String reportId = Generators.randomBasedGenerator().generate().toString();
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.REPORT_ID, reportId);
            values.put(DatabaseHelper.REGISTER_ID, donorId);
            values.put(DatabaseHelper.DONATION_SITE_ID, donationSiteId);

            long result = database.insert(DatabaseHelper.TABLE_REPORT, null, values);
            if (result == -1) {
                Log.e("DatabaseManager", "Failed to insert empty report");
                return false;
            }
            Log.i("DatabaseManager", "Empty report created successfully with ID: " + reportId);
            return true;
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error creating empty report", e);
            return false;
        }
    }

    // Retrieve all reports of the current login manager by current login user id
    public Cursor getAllReportsByUserId(String loginManagerId) {
        String[] columns = {
                "r." + DatabaseHelper.REPORT_ID + " AS _id",
                "r." + DatabaseHelper.AMOUNT_OF_BLOOD + " AS AmountOfBlood",
                "r." + DatabaseHelper.BLOOD_TYPE + " AS BloodType",
                "r." + DatabaseHelper.REPORT_DATE + " AS ReportDate",
                "r." + DatabaseHelper.FULL_NAME + " AS FullName",
                "r." + DatabaseHelper.REGISTER_ID + " AS RegisterId",
                "r." + DatabaseHelper.DONATION_SITE_ID + " AS SiteId",
                "ds." + DatabaseHelper.SITE_NAME + " AS SiteName",
                "ds." + DatabaseHelper.ADDRESS + " AS Address",
                "ds." + DatabaseHelper.OPENING_TIME + " AS OpeningTime",
                "ds." + DatabaseHelper.CLOSING_TIME + " AS ClosingTime",
                "u." + DatabaseHelper.USER_NAME + " AS UserName",
                "u." + DatabaseHelper.EMAIL + " AS Email",
        };
        String tables = DatabaseHelper.TABLE_REPORT + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_DONATION_SITE + " ds " +
                "ON r." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_USER + " u " +
                "ON r." + DatabaseHelper.REGISTER_ID + " = u." + DatabaseHelper.USER_ID;
        String selection = "ds." + DatabaseHelper.MANAGER_ID + " = ?";
        String[] selectionArgs = { loginManagerId };
        return database.query(tables, columns, selection, selectionArgs, null, null, null);
    }
    //get all valid reports by user ID
    public Cursor getAllValidReportsByUserId(String loginManagerId) {
        String[] columns = {
                "r." + DatabaseHelper.REPORT_ID + " AS _id",
                "r." + DatabaseHelper.AMOUNT_OF_BLOOD + " AS AmountOfBlood",
                "r." + DatabaseHelper.BLOOD_TYPE + " AS BloodType",
                "r." + DatabaseHelper.REPORT_DATE + " AS ReportDate",
                "r." + DatabaseHelper.FULL_NAME + " AS FullName",
                "r." + DatabaseHelper.REGISTER_ID + " AS RegisterId",
                "r." + DatabaseHelper.DONATION_SITE_ID + " AS SiteId",
                "ds." + DatabaseHelper.SITE_NAME + " AS SiteName",
                "ds." + DatabaseHelper.ADDRESS + " AS Address",
                "ds." + DatabaseHelper.OPENING_TIME + " AS OpeningTime",
                "ds." + DatabaseHelper.CLOSING_TIME + " AS ClosingTime",
                "u." + DatabaseHelper.USER_NAME + " AS UserName",
                "u." + DatabaseHelper.EMAIL + " AS Email",
        };
        String tables = DatabaseHelper.TABLE_REPORT + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_DONATION_SITE + " ds " +
                "ON r." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_USER + " u " +
                "ON r." + DatabaseHelper.REGISTER_ID + " = u." + DatabaseHelper.USER_ID;
        String selection = "r." + DatabaseHelper.AMOUNT_OF_BLOOD + " IS NOT NULL AND " +
                "r." + DatabaseHelper.BLOOD_TYPE + " IS NOT NULL AND " +
                "r." + DatabaseHelper.REPORT_DATE + " IS NOT NULL AND " +
                "r." + DatabaseHelper.FULL_NAME + " IS NOT NULL AND " +
                "ds." + DatabaseHelper.MANAGER_ID + " = ?"; // Filter by loginManagerId
        return database.query(tables, columns, selection, new String[]{loginManagerId}, null, null, null);
    }

    // get all reports
    public Cursor getAllValidReports() {
        String[] columns = {
                "r." + DatabaseHelper.REPORT_ID + " AS _id",
                "r." + DatabaseHelper.AMOUNT_OF_BLOOD + " AS AmountOfBlood",
                "r." + DatabaseHelper.BLOOD_TYPE + " AS BloodType",
                "r." + DatabaseHelper.REPORT_DATE + " AS ReportDate",
                "r." + DatabaseHelper.FULL_NAME + " AS FullName",
                "r." + DatabaseHelper.REGISTER_ID + " AS RegisterId",
                "r." + DatabaseHelper.DONATION_SITE_ID + " AS SiteId",
                "ds." + DatabaseHelper.SITE_NAME + " AS SiteName",
                "ds." + DatabaseHelper.ADDRESS + " AS Address",
                "ds." + DatabaseHelper.OPENING_TIME + " AS OpeningTime",
                "ds." + DatabaseHelper.CLOSING_TIME + " AS ClosingTime",
                "u." + DatabaseHelper.USER_NAME + " AS UserName",
                "u." + DatabaseHelper.EMAIL + " AS Email",
        };
        String tables = DatabaseHelper.TABLE_REPORT + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_DONATION_SITE + " ds " +
                "ON r." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_USER + " u " +
                "ON r." + DatabaseHelper.REGISTER_ID + " = u." + DatabaseHelper.USER_ID;
        String selection = "r." + DatabaseHelper.AMOUNT_OF_BLOOD + " IS NOT NULL AND " +
                "r." + DatabaseHelper.BLOOD_TYPE + " IS NOT NULL AND " +
                "r." + DatabaseHelper.REPORT_DATE + " IS NOT NULL AND " +
                "r." + DatabaseHelper.FULL_NAME + " IS NOT NULL";
        return database.query(tables, columns, selection, null, null, null, null);
    }

    public Cursor getReportAggregates() {
        String[] columns = {
                "COUNT(r." + DatabaseHelper.REPORT_ID + ") AS TotalOfValidReports",
                "COUNT(DISTINCT r." + DatabaseHelper.FULL_NAME + ") AS TotalOfValidDonors",
                "COUNT(DISTINCT r." + DatabaseHelper.BLOOD_TYPE + ") AS TotalOfBloodTypes",
                "ROUND(SUM(r." + DatabaseHelper.AMOUNT_OF_BLOOD + "), 2) AS TotalAmountOfBlood"
        };

        String tables = DatabaseHelper.TABLE_REPORT + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_DONATION_SITE + " ds " +
                "ON r." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_USER + " u " +
                "ON r." + DatabaseHelper.REGISTER_ID + " = u." + DatabaseHelper.USER_ID;

        String selection = "r." + DatabaseHelper.AMOUNT_OF_BLOOD + " IS NOT NULL AND " +
                "r." + DatabaseHelper.BLOOD_TYPE + " IS NOT NULL AND " +
                "r." + DatabaseHelper.REPORT_DATE + " IS NOT NULL AND " +
                "r." + DatabaseHelper.FULL_NAME + " IS NOT NULL";

        return database.query(tables, columns, selection, null, null, null, null);
    }


    // Retrieve all reports of the current login manager by current login user id + filtering and search
    public Cursor getAllValidReportsSearch(String bloodType, String openingTime, String closingTime) {
        String[] columns = {
                "r." + DatabaseHelper.REPORT_ID + " AS _id",
                "r." + DatabaseHelper.AMOUNT_OF_BLOOD + " AS AmountOfBlood",
                "r." + DatabaseHelper.BLOOD_TYPE + " AS BloodType",
                "r." + DatabaseHelper.REPORT_DATE + " AS ReportDate",
                "r." + DatabaseHelper.FULL_NAME + " AS FullName",
                "r." + DatabaseHelper.REGISTER_ID + " AS RegisterId",
                "r." + DatabaseHelper.DONATION_SITE_ID + " AS SiteId",
                "ds." + DatabaseHelper.SITE_NAME + " AS SiteName",
                "ds." + DatabaseHelper.ADDRESS + " AS Address",
                "ds." + DatabaseHelper.OPENING_TIME + " AS OpeningTime",
                "ds." + DatabaseHelper.CLOSING_TIME + " AS ClosingTime",
                "u." + DatabaseHelper.USER_NAME + " AS UserName",
                "u." + DatabaseHelper.EMAIL + " AS Email",
        };
        String tables = DatabaseHelper.TABLE_REPORT + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_DONATION_SITE + " ds " +
                "ON r." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_USER + " u " +
                "ON r." + DatabaseHelper.REGISTER_ID + " = u." + DatabaseHelper.USER_ID;
        StringBuilder whereClause = new StringBuilder();
        List<String> whereArgs = new ArrayList<>();
        whereClause.append("r.").append(DatabaseHelper.AMOUNT_OF_BLOOD).append(" IS NOT NULL AND ")
                .append("r.").append(DatabaseHelper.BLOOD_TYPE).append(" IS NOT NULL AND ")
                .append("r.").append(DatabaseHelper.REPORT_DATE).append(" IS NOT NULL AND ")
                .append("r.").append(DatabaseHelper.FULL_NAME).append(" IS NOT NULL");
        if (bloodType != null && !bloodType.isEmpty()) {
            whereClause.append(" AND r.").append(DatabaseHelper.BLOOD_TYPE).append(" = ?");
            whereArgs.add(bloodType);
        }
        if (openingTime != null && !openingTime.isEmpty()) {
            whereClause.append(" AND ds.").append(DatabaseHelper.OPENING_TIME).append(" >= ?");
            whereArgs.add(openingTime);
        }
        if (closingTime != null && !closingTime.isEmpty()) {
            whereClause.append(" AND ds.").append(DatabaseHelper.CLOSING_TIME).append(" <= ?");
            whereArgs.add(closingTime);
        }
        return database.query(tables, columns,
                whereClause.toString(),
                whereArgs.toArray(new String[0]),
                null, null, null);
    }

    public Cursor getFilteredReportAggregates(String bloodType, String openingTime, String closingTime) {
        String[] columns = {
                "COUNT(r." + DatabaseHelper.REPORT_ID + ") AS TotalOfValidReports",
                "COUNT(DISTINCT r." + DatabaseHelper.FULL_NAME + ") AS TotalOfValidDonors",
                "COUNT(DISTINCT r." + DatabaseHelper.BLOOD_TYPE + ") AS TotalOfBloodTypes",
                "ROUND(SUM(r." + DatabaseHelper.AMOUNT_OF_BLOOD + "), 2) AS TotalAmountOfBlood"
        };

        String tables = DatabaseHelper.TABLE_REPORT + " r " +
                "INNER JOIN " + DatabaseHelper.TABLE_DONATION_SITE + " ds " +
                "ON r." + DatabaseHelper.DONATION_SITE_ID + " = ds." + DatabaseHelper.SITE_ID + " " +
                "INNER JOIN " + DatabaseHelper.TABLE_USER + " u " +
                "ON r." + DatabaseHelper.REGISTER_ID + " = u." + DatabaseHelper.USER_ID;

        StringBuilder whereClause = new StringBuilder();
        List<String> whereArgs = new ArrayList<>();
        whereClause.append("r.").append(DatabaseHelper.AMOUNT_OF_BLOOD).append(" IS NOT NULL AND ")
                .append("r.").append(DatabaseHelper.BLOOD_TYPE).append(" IS NOT NULL AND ")
                .append("r.").append(DatabaseHelper.REPORT_DATE).append(" IS NOT NULL AND ")
                .append("r.").append(DatabaseHelper.FULL_NAME).append(" IS NOT NULL");
        if (bloodType != null && !bloodType.isEmpty()) {
            whereClause.append(" AND r.").append(DatabaseHelper.BLOOD_TYPE).append(" = ?");
            whereArgs.add(bloodType);
        }
        if (openingTime != null && !openingTime.isEmpty()) {
            whereClause.append(" AND ds.").append(DatabaseHelper.OPENING_TIME).append(" >= ?");
            whereArgs.add(openingTime);
        }
        if (closingTime != null && !closingTime.isEmpty()) {
            whereClause.append(" AND ds.").append(DatabaseHelper.CLOSING_TIME).append(" <= ?");
            whereArgs.add(closingTime);
        }
        return database.query(tables, columns,
                whereClause.toString(),
                whereArgs.toArray(new String[0]),
                null, null, null);
    }



    //api create report
    public boolean createReport(String username, String donationSiteId, String fullNameOfDonor, String bloodType, String amountOfBlood, String dateOfDonation) {
        try {
            Cursor cursor = getUserByUsername(username);
            if (cursor != null && cursor.moveToFirst()) {
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_ID));
                cursor.close();
                String reportId = Generators.randomBasedGenerator().generate().toString();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.REPORT_ID, reportId);
                values.put(DatabaseHelper.AMOUNT_OF_BLOOD, amountOfBlood);
                values.put(DatabaseHelper.BLOOD_TYPE, bloodType);
                values.put(DatabaseHelper.REPORT_DATE, dateOfDonation);
                values.put(DatabaseHelper.FULL_NAME, fullNameOfDonor);
                values.put(DatabaseHelper.REGISTER_ID, userId);
                values.put(DatabaseHelper.DONATION_SITE_ID, donationSiteId);
                long result = database.insert(DatabaseHelper.TABLE_REPORT, null, values);
                return result != -1;
            } else {
                Log.e("DatabaseManager", "User not found for username: " + username);
            }
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error while creating report: ", e);
        }
        return false;
    }

    //api update report by report id
    public boolean updateReportById(String reportId, String fullNameOfDonor, String bloodType, String amountOfBlood, String dateOfDonation) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.FULL_NAME, fullNameOfDonor);
            values.put(DatabaseHelper.BLOOD_TYPE, bloodType);
            values.put(DatabaseHelper.AMOUNT_OF_BLOOD, amountOfBlood);
            values.put(DatabaseHelper.REPORT_DATE, dateOfDonation);

            int rowsUpdated = database.update(
                    DatabaseHelper.TABLE_REPORT,
                    values,
                    DatabaseHelper.REPORT_ID + "=?",
                    new String[]{reportId}
            );

            return rowsUpdated > 0;
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error while updating report: ", e);
        }
        return false;
    }

    //api delete report by id
    public boolean removeReportById(String reportId) {
        try {
            int rowsDeleted = database.delete(
                    DatabaseHelper.TABLE_REPORT,
                    DatabaseHelper.REPORT_ID + "=?",
                    new String[]{reportId}
            );

            return rowsDeleted > 0;
        } catch (Exception e) {
            Log.e("DatabaseManager", "Error while deleting report: ", e);
        }
        return false;
    }

}