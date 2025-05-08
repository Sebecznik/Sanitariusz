package com.example.sanitariuszapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/***
 * SQLiteOpenHelper for managing patient database:
 * create, upgrade, CRUD operations, archiving, and afternoon procedures.
 ***/
public class PatientDatabaseHelper extends SQLiteOpenHelper {

    /***
     * Database constants: name, version, table and column identifiers.
     ***/
    private static final String DATABASE_NAME = "patientsPUBLIC.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "patients";
    public static final String ARCHIVE_TABLE_NAME = "patients_archive";
    public static final String AFTERNOON_TABLE_NAME = "patients_afternoon";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ROOM = "room";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PROCEDURES = "procedures";
    public static final String COLUMN_NOTE = "note";
    public static final String CURRENT_DATE = "date";
    public static final String TYPE_ID = "type";

    /***
     * Constructor to instantiate helper with context, DB name, and version.
     ***/
    public PatientDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /***
     * Creates main, archive, and afternoon tables on first run.
     ***/
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ROOM + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PROCEDURES + " TEXT, " +
                COLUMN_NOTE + " TEXT)";

        String createArchiveTable = "CREATE TABLE " + ARCHIVE_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ROOM + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PROCEDURES + " TEXT, " +
                COLUMN_NOTE + " TEXT, " +
                CURRENT_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

        String createAfternoonTable = "CREATE TABLE " + AFTERNOON_TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ROOM + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PROCEDURES + " TEXT, " +
                COLUMN_NOTE + " TEXT, " +
                TYPE_ID + " INTEGER)";

        db.execSQL(createTable);
        db.execSQL(createArchiveTable);
        db.execSQL(createAfternoonTable);
    }

    /***
     * Upgrades the database by dropping existing main table and recreating all.
     ***/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /***
     * Inserts new patient record into main table, returns new row ID.
     ***/
    public long insertPatient(Patient patient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM, patient.getRoomNumber());
        values.put(COLUMN_NAME, patient.getName());
        String proceduresJson = new Gson().toJson(patient.getProcedures());
        values.put(COLUMN_PROCEDURES, proceduresJson);
        values.put(COLUMN_NOTE, patient.getNote());
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    /***
     * Retrieves all patients from main table ordered by ID ascending.
     ***/
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_ID + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Procedure>>() {}.getType();
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String procString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROCEDURES));
                String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));
                List<Procedure> procedures = new ArrayList<>();
                if (procString != null && procString.trim().startsWith("[")) {
                    try {
                        procedures = gson.fromJson(procString, listType);
                    } catch (Exception e) {
                        procedures = new ArrayList<>();
                    }
                }
                patients.add(new Patient(id, room, name, procedures, note));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return patients;
    }

    /***
     * Retrieves a single patient by database ID.
     ***/
    public Patient getPatientById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Patient patient = null;
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Procedure>>() {}.getType();
            String room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            String procString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROCEDURES));
            String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));
            List<Procedure> procedures = new ArrayList<>();
            if (procString != null && procString.trim().startsWith("[")) {
                try {
                    procedures = gson.fromJson(procString, listType);
                } catch (Exception e) {
                    procedures = new ArrayList<>();
                }
            }
            patient = new Patient(id, room, name, procedures, note);
            cursor.close();
        }
        db.close();
        return patient;
    }

    /***
     * Updates the procedures JSON for a given patient ID, returns rows affected.
     ***/
    public int updatePatientProcedures(int id, List<Procedure> procedures) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String json = new Gson().toJson(procedures);
        values.put(COLUMN_PROCEDURES, json);
        int count = db.update(TABLE_NAME, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return count;
    }

    /***
     * Updates the note for a given patient ID, returns rows affected.
     ***/
    public int updatePatientNote(int id, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE, note);
        int count = db.update(TABLE_NAME, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return count;
    }

    /***
     * Deletes a patient by ID from the main table.
     ***/
    public void deletePatient(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    /***
     * Returns today's date as a string in yyyy-MM-dd format.
     ***/
    public static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
    }

    /***
     * Retrieves archive records matching a specific date.
     ***/
    public Cursor getArchivedPatientsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + ARCHIVE_TABLE_NAME +
                        " WHERE DATE(" + CURRENT_DATE + ") = ?",
                new String[]{date});
    }

    /***
     * Retrieves all entries in the afternoon procedures table.
     ***/
    public Cursor getAllAfternoonProcedures() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + AFTERNOON_TABLE_NAME, null);
    }

    /***
     * Archives a patient's data for today if not already archived.
     ***/
    public void archivePatient(Patient patient) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ARCHIVE_TABLE_NAME +
                        " WHERE " + COLUMN_NAME + " = ? AND " + CURRENT_DATE + " = ?",
                new String[]{patient.getName(), getTodayDate()}
        );
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM, patient.getRoomNumber());
        values.put(COLUMN_NAME, patient.getName());
        values.put(COLUMN_PROCEDURES, new Gson().toJson(patient.getProcedures()));
        values.put(COLUMN_NOTE, patient.getNote());
        values.put(CURRENT_DATE, getTodayDate());
        db.insert(ARCHIVE_TABLE_NAME, null, values);
        db.close();
    }

    /***
     * Deletes archive entries for today's date.
     ***/
    public void deleteOldArchive() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(ARCHIVE_TABLE_NAME, CURRENT_DATE + " = ?",
                new String[]{getTodayDate()});
        db.close();
    }

    /***
     * Moves a specific procedure to the afternoon table if not already present.
     ***/
    public void addToAfternoonTable(Patient patient, Procedure proc, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + AFTERNOON_TABLE_NAME +
                        " WHERE " + COLUMN_NAME + " = ? AND " + COLUMN_ROOM + " = ? AND " +
                        COLUMN_PROCEDURES + " = ?",
                new String[]{patient.getName(), patient.getRoomNumber(),
                        proc.getText() + " " + proc.getTime()}
        );
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        if (count == 0) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ROOM, patient.getRoomNumber());
            values.put(COLUMN_NAME, patient.getName());
            values.put(COLUMN_PROCEDURES, proc.getText() + " " + proc.getTime());
            values.put(COLUMN_NOTE, note);
            values.put(TYPE_ID, 1);
            db.insert(AFTERNOON_TABLE_NAME, null, values);
        }
        db.close();
    }

    /***
     * Inserts a custom record into the afternoon table.
     ***/
    public void insertAfternoonData(String room, String name, String procedures, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM, room);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PROCEDURES, procedures);
        values.put(COLUMN_NOTE, note);
        values.put(TYPE_ID, 1);
        db.insert(AFTERNOON_TABLE_NAME, null, values);
        db.close();
    }

    /***
     * Deletes an afternoon procedure by room and patient name.
     ***/
    public void deleteAfternoonProcedure(String room, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AFTERNOON_TABLE_NAME, COLUMN_ROOM + "=? AND " +
                COLUMN_NAME + "=?", new String[]{room, name});
        db.close();
    }

    /***
     * Clears all procedures in main table by resetting procedures to an empty list.
     ***/
    public void deleteAllProcedures() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROCEDURES, "[]");
        db.update(TABLE_NAME, values, null, null);
        db.close();
    }
}
