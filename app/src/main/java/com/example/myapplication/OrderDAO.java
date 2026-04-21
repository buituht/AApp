package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private final Gson gson = new Gson();

    public OrderDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addOrder(Order order) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ORDER_ID, order.getOrderId());
        values.put(DatabaseHelper.KEY_ORDER_USERNAME, order.getUsername());
        values.put(DatabaseHelper.KEY_ORDER_RECEIVER_NAME, order.getReceiverName());
        values.put(DatabaseHelper.KEY_ORDER_RECEIVER_PHONE, order.getReceiverPhone());
        values.put(DatabaseHelper.KEY_ORDER_RECEIVER_ADDRESS, order.getReceiverAddress());
        values.put(DatabaseHelper.KEY_ORDER_ITEMS_JSON, gson.toJson(order.getItems()));
        values.put(DatabaseHelper.KEY_ORDER_TOTAL_PRICE, order.getTotalPrice());
        values.put(DatabaseHelper.KEY_ORDER_STATUS, order.getStatus());
        values.put(DatabaseHelper.KEY_ORDER_TIMESTAMP, order.getTimestamp());
        long result = db.insert(DatabaseHelper.TABLE_ORDERS, null, values);
        close();
        return result;
    }

    public void updateOrderStatus(String orderId, String status) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_ORDER_STATUS, status);
        db.update(DatabaseHelper.TABLE_ORDERS, values, DatabaseHelper.KEY_ORDER_ID + " = ?", new String[]{orderId});
        close();
    }

    public List<Order> getOrdersByUsername(String username) {
        open();
        List<Order> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ORDERS, null, DatabaseHelper.KEY_ORDER_USERNAME + "=?", new String[]{username}, null, null, DatabaseHelper.KEY_ORDER_TIMESTAMP + " DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return list;
    }

    public List<Order> getAllOrders() {
        open();
        List<Order> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_ORDERS, null, null, null, null, null, DatabaseHelper.KEY_ORDER_TIMESTAMP + " DESC");
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToOrder(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return list;
    }

    private Order cursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setOrderId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_ID)));
        order.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_USERNAME)));
        order.setReceiverName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_RECEIVER_NAME)));
        order.setReceiverPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_RECEIVER_PHONE)));
        order.setReceiverAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_RECEIVER_ADDRESS)));
        String itemsJson = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_ITEMS_JSON));
        List<Product> items = gson.fromJson(itemsJson, new TypeToken<List<Product>>() {}.getType());
        order.setItems(items);
        order.setTotalPrice(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_TOTAL_PRICE)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_STATUS)));
        order.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ORDER_TIMESTAMP)));
        return order;
    }
}
