package com.example.petapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for Pet app. Manages database creation and version management.
 */
public class PetDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version. If we change the database schema, we must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_PETS_TABLE =   "CREATE TABLE " + PetContract.PetEntry.TABLE_NAME + " ("
                + PetContract.PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetContract.PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetContract.PetEntry.COLUMN_PET_BREED + " TEXT, "
                + PetContract.PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL, "
                + PetContract.PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
}
