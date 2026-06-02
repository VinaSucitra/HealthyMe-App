package com.example.healthyme.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.healthyme.model.History;
import com.example.healthyme.model.Workout;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HealthyMeDB";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_HISTORY = "workout_history";
    
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_MUSCLE = "muscle";
    private static final String COLUMN_DIFFICULTY = "difficulty";
    private static final String COLUMN_INSTRUCTIONS = "instructions";
    private static final String COLUMN_DATE = "completion_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_MUSCLE + " TEXT,"
                + COLUMN_DIFFICULTY + " TEXT,"
                + COLUMN_INSTRUCTIONS + " TEXT" + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);

        String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String CREATE_HISTORY_TABLE = "CREATE TABLE " + TABLE_HISTORY + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NAME + " TEXT,"
                    + COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP" + ")";
            db.execSQL(CREATE_HISTORY_TABLE);
        }
    }

    // --- FAVORITES ---
    public void addFavorite(Workout workout) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, workout.getName());
        values.put(COLUMN_TYPE, workout.getType());
        values.put(COLUMN_MUSCLE, workout.getMuscle());
        values.put(COLUMN_DIFFICULTY, workout.getDifficulty());
        values.put(COLUMN_INSTRUCTIONS, workout.getInstructions());

        db.insert(TABLE_FAVORITES, null, values);
        db.close();
    }

    public void deleteFavorite(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, COLUMN_NAME + " = ?", new String[]{name});
        db.close();
    }

    public boolean isFavorite(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_ID},
                COLUMN_NAME + " = ?", new String[]{name}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public List<Workout> getAllFavorites() {
        List<Workout> favoriteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FAVORITES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Workout workout = new Workout();
                workout.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                workout.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
                workout.setMuscle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MUSCLE)));
                workout.setDifficulty(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY)));
                workout.setInstructions(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS)));
                favoriteList.add(workout);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return favoriteList;
    }

    // --- HISTORY ---
    public void addToHistory(String workoutName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, workoutName);
        db.insert(TABLE_HISTORY, null, values);
        db.close();
    }

    public int getHistoryCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_HISTORY, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<History> getAllHistory() {
        List<History> historyList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + COLUMN_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                historyList.add(new History(name, date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return historyList;
    }
}
