package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class FaqDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public FaqDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addFaq(Faq faq) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_FAQ_QUESTION, faq.getQuestion());
        values.put(DatabaseHelper.KEY_FAQ_ANSWER, faq.getAnswer());
        values.put(DatabaseHelper.KEY_FAQ_IMAGE, faq.getImageUrl());
        long id = db.insert(DatabaseHelper.TABLE_FAQS, null, values);
        close();
        return id;
    }

    public void updateFaq(Faq faq) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_FAQ_QUESTION, faq.getQuestion());
        values.put(DatabaseHelper.KEY_FAQ_ANSWER, faq.getAnswer());
        values.put(DatabaseHelper.KEY_FAQ_IMAGE, faq.getImageUrl());
        db.update(DatabaseHelper.TABLE_FAQS, values, DatabaseHelper.KEY_FAQ_ID + " = ?", new String[]{faq.getId()});
        close();
    }

    public void deleteFaq(String id) {
        open();
        db.delete(DatabaseHelper.TABLE_FAQS, DatabaseHelper.KEY_FAQ_ID + " = ?", new String[]{id});
        close();
    }

    public List<Faq> getAllFaqs() {
        open();
        List<Faq> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_FAQS, null);
        if (cursor.moveToFirst()) {
            do {
                Faq f = new Faq();
                f.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FAQ_ID))));
                f.setQuestion(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FAQ_QUESTION)));
                f.setAnswer(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FAQ_ANSWER)));
                f.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FAQ_IMAGE)));
                list.add(f);
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return list;
    }
}
