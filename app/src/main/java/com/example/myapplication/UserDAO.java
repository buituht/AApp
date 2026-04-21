package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addUser(User user) {
        open();
        ContentValues values = userToValues(user);
        long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        close();
        return id;
    }

    public User getUserByUsername(String username) {
        open();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.KEY_USER_USERNAME + "=?", new String[]{username}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        close();
        return user;
    }

    public User getUserByEmail(String email) {
        open();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, DatabaseHelper.KEY_USER_EMAIL + "=?", new String[]{email}, null, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        close();
        return user;
    }

    public void updateUser(User user) {
        open();
        ContentValues values = userToValues(user);
        db.update(DatabaseHelper.TABLE_USERS, values, DatabaseHelper.KEY_USER_EMAIL + " = ?", new String[]{user.getEmail()});
        close();
    }

    private ContentValues userToValues(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.KEY_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.KEY_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.KEY_USER_FULLNAME, user.getFullName());
        values.put(DatabaseHelper.KEY_USER_PHONE, user.getPhoneNumber());
        values.put(DatabaseHelper.KEY_USER_DOB, user.getDob());
        values.put(DatabaseHelper.KEY_USER_GENDER, user.getGender());
        values.put(DatabaseHelper.KEY_USER_ADDRESS, user.getAddress());
        values.put(DatabaseHelper.KEY_USER_HOME_ADDRESS, user.getHomeAddress());
        values.put(DatabaseHelper.KEY_USER_COMPANY_ADDRESS, user.getCompanyAddress());
        values.put(DatabaseHelper.KEY_USER_AVATAR_URL, user.getAvatarUrl());
        values.put(DatabaseHelper.KEY_USER_IS_ADMIN, user.isAdmin() ? 1 : 0);
        values.put(DatabaseHelper.KEY_USER_POINTS, user.getPoints());
        values.put(DatabaseHelper.KEY_USER_TOTAL_SPENT, user.getTotalSpent());
        return values;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_EMAIL)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_USERNAME)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_PASSWORD)));
        user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_FULLNAME)));
        user.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_PHONE)));
        user.setDob(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_DOB)));
        user.setGender(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_GENDER)));
        user.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ADDRESS)));
        user.setHomeAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_HOME_ADDRESS)));
        user.setCompanyAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_COMPANY_ADDRESS)));
        user.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_AVATAR_URL)));
        user.setAdmin(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_IS_ADMIN)) == 1);
        user.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_POINTS)));
        user.setTotalSpent(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_TOTAL_SPENT)));
        return user;
    }
}
