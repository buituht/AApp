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

public class ProductDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private final Gson gson = new Gson();

    public ProductDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addProduct(Product product) {
        open();
        ContentValues values = productToValues(product);
        long id = db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);
        close();
        return id;
    }

    public void updateProduct(Product product) {
        open();
        ContentValues values = productToValues(product);
        db.update(DatabaseHelper.TABLE_PRODUCTS, values, DatabaseHelper.KEY_PRODUCT_ID + " = ?", new String[]{String.valueOf(product.getId())});
        close();
    }

    public void deleteProduct(long id) {
        open();
        db.delete(DatabaseHelper.TABLE_PRODUCTS, DatabaseHelper.KEY_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        close();
    }

    public List<Product> getAllProducts() {
        open();
        List<Product> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_PRODUCTS, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return list;
    }

    public Product getProductById(long id) {
        open();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PRODUCTS, null, DatabaseHelper.KEY_PRODUCT_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Product p = null;
        if (cursor != null && cursor.moveToFirst()) {
            p = cursorToProduct(cursor);
            cursor.close();
        }
        close();
        return p;
    }

    private ContentValues productToValues(Product product) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_PRODUCT_NAME, product.getName());
        values.put(DatabaseHelper.KEY_PRODUCT_PRICE, product.getPrice());
        values.put(DatabaseHelper.KEY_PRODUCT_DISCOUNT_PRICE, product.getDiscountPrice());
        values.put(DatabaseHelper.KEY_PRODUCT_DESCRIPTION, product.getDescription());
        
        // Convert List<String> to JSON string
        values.put(DatabaseHelper.KEY_PRODUCT_IMAGES, gson.toJson(product.getImages()));
        
        values.put(DatabaseHelper.KEY_PRODUCT_CATEGORY, product.getCategory());
        values.put(DatabaseHelper.KEY_PRODUCT_RATING, product.getRating());
        values.put(DatabaseHelper.KEY_PRODUCT_SOLD_QUANTITY, product.getSoldQuantity());
        values.put(DatabaseHelper.KEY_PRODUCT_IS_HOT, product.isHotDiscount() ? 1 : 0);
        values.put(DatabaseHelper.KEY_PRODUCT_IS_NEW, product.isNewArrival() ? 1 : 0);
        values.put(DatabaseHelper.KEY_PRODUCT_IS_BEST_SELLER, product.isBestSeller() ? 1 : 0);
        values.put(DatabaseHelper.KEY_PRODUCT_SCREEN, product.getScreen());
        values.put(DatabaseHelper.KEY_PRODUCT_CPU, product.getCpu());
        values.put(DatabaseHelper.KEY_PRODUCT_RAM, product.getRam());
        values.put(DatabaseHelper.KEY_PRODUCT_ROM, product.getRom());
        values.put(DatabaseHelper.KEY_PRODUCT_CAMERA, product.getCamera());
        values.put(DatabaseHelper.KEY_PRODUCT_BATTERY, product.getBattery());
        return values;
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
