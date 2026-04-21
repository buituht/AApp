package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private final Gson gson = new Gson();

    public FavoriteDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void addFavorite(String email, long productId) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_FAV_USER_EMAIL, email);
        values.put(DatabaseHelper.KEY_FAV_PRODUCT_ID, productId);
        db.insertWithOnConflict(DatabaseHelper.TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        close();
    }

    public void removeFavorite(String email, long productId) {
        open();
        db.delete(DatabaseHelper.TABLE_FAVORITES, DatabaseHelper.KEY_FAV_USER_EMAIL + " = ? AND " + DatabaseHelper.KEY_FAV_PRODUCT_ID + " = ?", new String[]{email, String.valueOf(productId)});
        close();
    }

    public List<Product> getFavorites(String email) {
        open();
        List<Product> list = new ArrayList<>();
        String query = "SELECT p.* FROM " + DatabaseHelper.TABLE_PRODUCTS + " p INNER JOIN " + DatabaseHelper.TABLE_FAVORITES + " f ON p." + DatabaseHelper.KEY_PRODUCT_ID + " = f." + DatabaseHelper.KEY_FAV_PRODUCT_ID + " WHERE f." + DatabaseHelper.KEY_FAV_USER_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return list;
    }

    public boolean isFavorite(String email, long productId) {
        open();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FAVORITES, null, DatabaseHelper.KEY_FAV_USER_EMAIL + "=? AND " + DatabaseHelper.KEY_FAV_PRODUCT_ID + "=?", new String[]{email, String.valueOf(productId)}, null, null, null);
        boolean fav = cursor.getCount() > 0;
        cursor.close();
        close();
        return fav;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product p = new Product();
        p.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_ID)));
        p.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_NAME)));
        p.setPrice(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_PRICE)));
        p.setDiscountPrice(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_DISCOUNT_PRICE)));
        p.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_DESCRIPTION)));

        // Convert JSON string back to List<String>
        String imagesJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_IMAGES));
        Type listType = new TypeToken<ArrayList<String>>(){}.getType();
        List<String> images = gson.fromJson(imagesJson, listType);
        if (images == null) images = new ArrayList<>();
        p.setImages(images);

        p.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_CATEGORY)));
        p.setRating(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_RATING)));
        p.setSoldQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_SOLD_QUANTITY)));
        p.setHotDiscount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_IS_HOT)) == 1);
        p.setNewArrival(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_IS_NEW)) == 1);
        p.setBestSeller(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_IS_BEST_SELLER)) == 1);
        p.setScreen(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_SCREEN)));
        p.setCpu(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_CPU)));
        p.setRam(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_RAM)));
        p.setRom(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_ROM)));
        p.setCamera(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_CAMERA)));
        p.setBattery(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRODUCT_BATTERY)));
        return p;
    }
}
