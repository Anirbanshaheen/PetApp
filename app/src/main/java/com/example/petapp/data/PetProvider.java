package com.example.petapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.widget.Toast;

public class PetProvider extends ContentProvider {

    private static final int PETS = 100;
    private static final int PETS_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PETS_ID);
    }

    // Database Helper Object
    private PetDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // For query we use Writable Database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // cursor will hold the result of the query
        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case PETS:
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }

        // Set notification URI on the cursor
        // thus we can know what content URI the cursor was created for
        // If the data at this URI changes, then we know we need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(),uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri" + uri + "with match" + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {

        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires a valid gender");
        }

        Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Toast.makeText(getContext(), "Failed to insert" + uri, Toast.LENGTH_SHORT).show();
            return null;
        }
        // Notify all listener that data has changed for the pet content uri
        getContext().getContentResolver().notifyChange(uri,null);

        return ContentUris.withAppendedId(uri, id);

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME,selection,selectionArgs);
                break;
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not support for " + uri);
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // First check name is present or not then get the name and check
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires a valid gender");
            }
        }

        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        // If there are no value for update
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(PetContract.PetEntry.TABLE_NAME,values,selection,selectionArgs);

        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }

        return rowsUpdated;
    }
}
