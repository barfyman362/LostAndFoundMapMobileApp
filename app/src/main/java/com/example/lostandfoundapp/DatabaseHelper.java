package com.example.lostandfoundapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "lost_found.db";
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "adverts";

    public static final String COL_ID = "id";
    public static final String COL_TYPE = "type";
    public static final String COL_NAME = "name";
    public static final String COL_PHONE = "phone";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DATE = "date";
    public static final String COL_LOCATION = "location";
    public static final String COL_IMAGE = "image";
    public static final String COL_POSTED_TIME = "posted_time";

    // Latitude and longitude map fields added
    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // creates the db table when the app first needs it
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TYPE + " TEXT, " +
                COL_NAME + " TEXT, " +
                COL_PHONE + " TEXT, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_LOCATION + " TEXT, " +
                COL_IMAGE + " TEXT, " +
                COL_POSTED_TIME + " TEXT, " +
                COL_LATITUDE + " REAL, " +
                COL_LONGITUDE + " REAL)";

        db.execSQL(sql);
    }

    // adds the map columns if the old db already exists
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_LATITUDE + " REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_LONGITUDE + " REAL DEFAULT 0");
        }
    }

    // inserts a new lost/found advert into SQLite
    public boolean insertAdvert(String type, String name, String phone, String description,
                                String category, String date, String location,
                                String image, String postedTime,
                                double latitude, double longitude) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_TYPE, type);
        values.put(COL_NAME, name);
        values.put(COL_PHONE, phone);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_CATEGORY, category);
        values.put(COL_DATE, date);
        values.put(COL_LOCATION, location);
        values.put(COL_IMAGE, image);
        values.put(COL_POSTED_TIME, postedTime);
        values.put(COL_LATITUDE, latitude);
        values.put(COL_LONGITUDE, longitude);

        long result = db.insert(TABLE_NAME, null, values);

        return result != -1;
    }

    // gets every saved advert, newest first
    public ArrayList<Advert> getAllAdverts() {
        ArrayList<Advert> adverts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COL_ID + " DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                adverts.add(getAdvertFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return adverts;
    }

    // gets only the adverts that match the chosen category
    public ArrayList<Advert> getAdvertsByCategory(String category) {
        ArrayList<Advert> adverts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_CATEGORY + " = ? ORDER BY " + COL_ID + " DESC",
                new String[]{category}
        );

        if (cursor.moveToFirst()) {
            do {
                adverts.add(getAdvertFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return adverts;
    }

    // gets one advert using its db id
    public Advert getAdvert(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        Advert advert = null;

        if (cursor.moveToFirst()) {
            advert = getAdvertFromCursor(cursor);
        }

        cursor.close();
        return advert;
    }

    // deletes one advert from the db
    public boolean deleteAdvert(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int deletedRows = db.delete(
                TABLE_NAME,
                COL_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        return deletedRows > 0;
    }

    // turns the current db row into an Advert object
    private Advert getAdvertFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
        String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
        String image = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE));
        String postedTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_POSTED_TIME));

        double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LATITUDE));
        double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LONGITUDE));

        return new Advert(id, type, name, phone, description, category,
                date, location, image, postedTime, latitude, longitude);
    }
}