package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public CategoryDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addCategory(Category category) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_CATEGORY_NAME, category.getName());
        values.put(DatabaseHelper.KEY_CATEGORY_IMAGE, category.getImageUrl());
        long id = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
        close();
        return id;
    }

    public void updateCategory(Category category) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_CATEGORY_NAME, category.getName());
        values.put(DatabaseHelper.KEY_CATEGORY_IMAGE, category.getImageUrl());
        db.update(DatabaseHelper.TABLE_CATEGORIES, values, DatabaseHelper.KEY_CATEGORY_ID + " = ?", new String[]{category.getId()});
        close();
    }

    public void deleteCategory(String id) {
        open();
        db.delete(DatabaseHelper.TABLE_CATEGORIES, DatabaseHelper.KEY_CATEGORY_ID + " = ?", new String[]{id});
        close();
    }

    public List<Category> getAllCategories() {
        open();
        List<Category> categoryList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_CATEGORIES, null);
        if (cursor.moveToFirst()) {
            do {
                Category cat = new Category();
                cat.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CATEGORY_ID))));
                cat.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CATEGORY_NAME)));
                cat.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CATEGORY_IMAGE)));
                categoryList.add(cat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return categoryList;
    }
}
