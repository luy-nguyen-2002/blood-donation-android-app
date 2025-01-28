package com.example.blooddonationapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.fasterxml.uuid.Generators;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "BloodDonationApp.db";
    private static final int DB_VERSION = 1;

    //Table Role
    public static final String TABLE_ROLE = "Role";
    public static final String ROLE_ID = "_id";
    public static final String ROLE_NAME = "role_name";

    //Table User
    public static final String TABLE_USER = "User";
    public static final String USER_ID = "_id";
    public static final String USER_NAME = "user_name";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String ROLE_ID_IN_TABLE_USER = "role_id";


    //Table DonationSite
    public static final String TABLE_DONATION_SITE = "DonationSite";
    public static final String SITE_ID = "_id";
    public static final String SITE_NAME = "site_name";
    public static final String ADDRESS = "address";
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String MANAGER_ID = "manager_id";
    public static final String OPENING_TIME = "opening_time";
    public static final String CLOSING_TIME = "closing_time";
    public static final String UPDATED_AT = "updated_at";

    //Table Report
    public static final String  TABLE_REPORT = "Report";
    public static final String REPORT_ID = "_id";
    public static final String AMOUNT_OF_BLOOD = "amount_of_blood";
    public static final String BLOOD_TYPE = "blood_type";
    public static final String REPORT_DATE = "report_date";//using the same created_at
    //    public static final String MANAGER_ID = "manager_id";
    public static final String REGISTER_ID = "register_id";//donor_id
    public static final String DONATION_SITE_ID = "site_id";
    public static final String FULL_NAME = "full_name";

    //Table DonationRegistration
    public static final String  TABLE_DONATION_REGISTRATION = "DonationRegistration";
    public static final String  DONATION_REGISTRATION_ID = "_id";
//    public static final String DONOR_ID = "donor_id";
    public static final String IS_CONFIRMED = "is_confirmed";
    public static final String NUMBER_OF_DONOR = "number_of_donor";

    //Table VolunteerRegistration
    public static final String  TABLE_VOLUNTEER_REGISTRATION = "VolunteerRegistration";
    public static final String  VOLUNTEER_REGISTRATION_ID = "_id";

    //creating tables
    // Role Table
    public static final String CREATE_TABLE_ROLE = "CREATE TABLE " + TABLE_ROLE + " ("
            + ROLE_ID + " TEXT PRIMARY KEY, "
            + ROLE_NAME + " TEXT NOT NULL);";

    // User Table
    public static final String CREATE_TABLE_USER = "CREATE TABLE " + TABLE_USER + " ("
            + USER_ID + " TEXT PRIMARY KEY, "
            + USER_NAME + " TEXT NOT NULL, "
            + EMAIL + " TEXT NOT NULL UNIQUE, "
            + PASSWORD + " TEXT NOT NULL, "
            + ROLE_ID_IN_TABLE_USER + " TEXT, "
            + "FOREIGN KEY (" + ROLE_ID_IN_TABLE_USER + ") REFERENCES " + TABLE_ROLE + "(" + ROLE_ID + "));";

    // DonationSite Table
    public static final String CREATE_TABLE_DONATION_SITE = "CREATE TABLE " +  TABLE_DONATION_SITE + " ("
            + SITE_ID + " TEXT PRIMARY KEY, "
            + SITE_NAME + " TEXT NOT NULL, "
            + ADDRESS + " TEXT NOT NULL, "
            + LONGITUDE + " TEXT NOT NULL, "
            + LATITUDE + " TEXT NOT NULL, "
            + OPENING_TIME + " TEXT NOT NULL, "
            + CLOSING_TIME + " TEXT NOT NULL, "
            + MANAGER_ID + " TEXT, "
            + UPDATED_AT + " TEXT, "
            + "FOREIGN KEY (" + MANAGER_ID + ") REFERENCES " + TABLE_USER + "(" + USER_ID + "));";

    // Report Table
    public static final String CREATE_TABLE_REPORT = "CREATE TABLE " +  TABLE_REPORT + " ("
            + REPORT_ID + " TEXT PRIMARY KEY, "
            + AMOUNT_OF_BLOOD + " TEXT, "
            + BLOOD_TYPE + " TEXT, "
            + REPORT_DATE + " TEXT, "
            + FULL_NAME + " TEXT, "
            + REGISTER_ID + " TEXT, "
            + DONATION_SITE_ID + " TEXT, "
            + "FOREIGN KEY (" + REGISTER_ID + ") REFERENCES " + TABLE_USER + "(" + USER_ID + "), "
            + "FOREIGN KEY (" + DONATION_SITE_ID + ") REFERENCES " +  TABLE_DONATION_SITE + "(" + SITE_ID + "));";


    // HistoryRegistration Table
    public static final String CREATE_TABLE_DONATION_REGISTRATION = "CREATE TABLE " +  TABLE_DONATION_REGISTRATION + " ("
            + DONATION_REGISTRATION_ID + " TEXT PRIMARY KEY, "
            + NUMBER_OF_DONOR + " TEXT NOT NULL, "
            + REGISTER_ID + " TEXT, "
            + DONATION_SITE_ID + " TEXT, "
            + IS_CONFIRMED + " INTEGER DEFAULT 0, "
            + "FOREIGN KEY (" + REGISTER_ID + ") REFERENCES " + TABLE_USER + "(" + USER_ID + "), "
            + "FOREIGN KEY (" + DONATION_SITE_ID + ") REFERENCES " +  TABLE_DONATION_SITE + "(" + SITE_ID + "));";

    //VolunteerRegistration Table
    public static final String CREATE_TABLE_VOLUNTEER_REGISTRATION = "CREATE TABLE " +  TABLE_VOLUNTEER_REGISTRATION + " ("
            + VOLUNTEER_REGISTRATION_ID + " TEXT PRIMARY KEY, "
            + MANAGER_ID + " TEXT, "//the manager who register for volunteering, which is the current login userId (manager role)
            + DONATION_SITE_ID + " TEXT, "
            + "FOREIGN KEY (" + MANAGER_ID + ") REFERENCES " + TABLE_USER + "(" + USER_ID + "), "
            + "FOREIGN KEY (" + DONATION_SITE_ID + ") REFERENCES " +  TABLE_DONATION_SITE + "(" + SITE_ID + "));";


    public DatabaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign key support
        db.execSQL("PRAGMA foreign_keys=ON;");

        db.execSQL(CREATE_TABLE_ROLE);
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_DONATION_SITE);
        db.execSQL(CREATE_TABLE_REPORT);
        db.execSQL(CREATE_TABLE_DONATION_REGISTRATION);
        db.execSQL(CREATE_TABLE_VOLUNTEER_REGISTRATION);

        String roleDonorId = Generators.randomBasedGenerator().generate().toString();
        String insertRoleDonor = "INSERT INTO " + TABLE_ROLE + " (" + ROLE_ID + ", " + ROLE_NAME + ") VALUES ('" + roleDonorId + "', 'DONOR');";

        String roleSiteManagerId = Generators.randomBasedGenerator().generate().toString();
        String insertRoleSiteManager = "INSERT INTO " + TABLE_ROLE + " (" + ROLE_ID + ", " + ROLE_NAME + ") VALUES ('" + roleSiteManagerId + "', 'SITE_MANAGER');";

        String roleSuperUserId = Generators.randomBasedGenerator().generate().toString();
        String insertRoleSuperUser = "INSERT INTO " + TABLE_ROLE + " (" + ROLE_ID + ", " + ROLE_NAME + ") VALUES ('" + roleSuperUserId + "', 'SUPER_USER');";

        db.execSQL(insertRoleDonor);
        db.execSQL(insertRoleSiteManager);
        db.execSQL(insertRoleSuperUser);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VOLUNTEER_REGISTRATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DONATION_REGISTRATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DONATION_SITE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ROLE);

        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }
}