package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class BannerDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public BannerDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addBanner(Banner banner) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_BANNER_IMAGE, banner.getImageUrl());
        long id = db.insert(DatabaseHelper.TABLE_BANNERS, null, values);
        close();
        return id;
    }

    public void updateBanner(Banner banner) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_BANNER_IMAGE, banner.getImageUrl());
        db.update(DatabaseHelper.TABLE_BANNERS, values, DatabaseHelper.KEY_BANNER_ID + " = ?", new String[]{banner.getId()});
        close();
    }

    public void deleteBanner(String id) {
        open();
        db.delete(DatabaseHelper.TABLE_BANNERS, DatabaseHelper.KEY_BANNER_ID + " = ?", new String[]{id});
        close();
    }

    public List<Banner> getAllBanners() {
        open();
        List<Banner> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_BANNERS, null);
        if (cursor.moveToFirst()) {
            do {
                Banner b = new Banner();
                b.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BANNER_ID))));
                b.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BANNER_IMAGE)));
                list.add(b);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return list;
    }
}
