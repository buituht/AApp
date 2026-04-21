package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShopDatabase.db";
    private static final int DATABASE_VERSION = 17; 

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PRODUCTS = "products";
    public static final String TABLE_CATEGORIES = "categories";
    public static final String TABLE_BANNERS = "banners";
    public static final String TABLE_FAQS = "faqs";
    public static final String TABLE_ORDERS = "orders";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_VOUCHERS = "vouchers";

    // User Table Columns
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_USERNAME = "username";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_FULLNAME = "fullname";
    public static final String KEY_USER_PHONE = "phoneNumber";
    public static final String KEY_USER_DOB = "dob";
    public static final String KEY_USER_GENDER = "gender";
    public static final String KEY_USER_ADDRESS = "address";
    public static final String KEY_USER_HOME_ADDRESS = "homeAddress";
    public static final String KEY_USER_COMPANY_ADDRESS = "companyAddress";
    public static final String KEY_USER_TERMS_ACCEPTED = "termsAccepted";
    public static final String KEY_USER_AVATAR_URL = "avatarUrl";
    public static final String KEY_USER_IS_ADMIN = "isAdmin";
    public static final String KEY_USER_POINTS = "points"; 
    public static final String KEY_USER_TOTAL_SPENT = "totalSpent";

    // Product Table Columns
    public static final String KEY_PRODUCT_ID = "id";
    public static final String KEY_PRODUCT_NAME = "name";
    public static final String KEY_PRODUCT_PRICE = "price";
    public static final String KEY_PRODUCT_DISCOUNT_PRICE = "discountPrice";
    public static final String KEY_PRODUCT_DESCRIPTION = "description";
    public static final String KEY_PRODUCT_IMAGES = "images";
    public static final String KEY_PRODUCT_CATEGORY = "category";
    public static final String KEY_PRODUCT_RATING = "rating";
    public static final String KEY_PRODUCT_SOLD_QUANTITY = "soldQuantity";
    public static final String KEY_PRODUCT_IS_HOT = "isHot";
    public static final String KEY_PRODUCT_IS_NEW = "isNew";
    public static final String KEY_PRODUCT_IS_BEST_SELLER = "isBestSeller";
    public static final String KEY_PRODUCT_SCREEN = "screen";
    public static final String KEY_PRODUCT_CPU = "cpu";
    public static final String KEY_PRODUCT_RAM = "ram";
    public static final String KEY_PRODUCT_ROM = "rom";
    public static final String KEY_PRODUCT_CAMERA = "camera";
    public static final String KEY_PRODUCT_BATTERY = "battery";

    // Category Table Columns
    public static final String KEY_CATEGORY_ID = "categoryId";
    public static final String KEY_CATEGORY_NAME = "name";
    public static final String KEY_CATEGORY_IMAGE = "image";
    public static final String KEY_CATEGORY_PARENT_ID = "parentId"; 

    // Banner Table Columns
    public static final String KEY_BANNER_ID = "bannerId";
    public static final String KEY_BANNER_IMAGE = "image";

    // FAQ Table Columns
    public static final String KEY_FAQ_ID = "faqId";
    public static final String KEY_FAQ_QUESTION = "question";
    public static final String KEY_FAQ_ANSWER = "answer";
    public static final String KEY_FAQ_IMAGE = "imageUrl";

    // Order Table Columns
    public static final String KEY_ORDER_ID = "orderId";
    public static final String KEY_ORDER_USERNAME = "username";
    public static final String KEY_ORDER_RECEIVER_NAME = "receiverName";
    public static final String KEY_ORDER_RECEIVER_PHONE = "receiverPhone";
    public static final String KEY_ORDER_RECEIVER_ADDRESS = "receiverAddress";
    public static final String KEY_ORDER_ITEMS_JSON = "items_json";
    public static final String KEY_ORDER_TOTAL_PRICE = "totalPrice";
    public static final String KEY_ORDER_STATUS = "status";
    public static final String KEY_ORDER_TIMESTAMP = "timestamp";

    // Favorite Table Columns
    public static final String KEY_FAV_USER_EMAIL = "userEmail";
    public static final String KEY_FAV_PRODUCT_ID = "productId";

    // Voucher Table Columns
    public static final String KEY_VOUCHER_ID = "id";
    public static final String KEY_VOUCHER_CODE = "code";
    public static final String KEY_VOUCHER_DESCRIPTION = "description";
    public static final String KEY_VOUCHER_DISCOUNT_VALUE = "discountValue";
    public static final String KEY_VOUCHER_TYPE = "type";
    public static final String KEY_VOUCHER_MIN_ORDER = "minOrderAmount";
    public static final String KEY_VOUCHER_START_DATE = "startDate";
    public static final String KEY_VOUCHER_EXPIRY = "expiryDate";

    private final Gson gson = new Gson();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_EMAIL + " TEXT PRIMARY KEY,"
                + KEY_USER_USERNAME + " TEXT,"
                + KEY_USER_PASSWORD + " TEXT,"
                + KEY_USER_FULLNAME + " TEXT,"
                + KEY_USER_PHONE + " TEXT,"
                + KEY_USER_DOB + " TEXT,"
                + KEY_USER_GENDER + " TEXT,"
                + KEY_USER_ADDRESS + " TEXT,"
                + KEY_USER_HOME_ADDRESS + " TEXT,"
                + KEY_USER_COMPANY_ADDRESS + " TEXT,"
                + KEY_USER_TERMS_ACCEPTED + " INTEGER,"
                + KEY_USER_AVATAR_URL + " TEXT,"
                + KEY_USER_IS_ADMIN + " INTEGER DEFAULT 0,"
                + KEY_USER_POINTS + " INTEGER DEFAULT 0,"
                + KEY_USER_TOTAL_SPENT + " INTEGER DEFAULT 0" + ")";

        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + "("
                + KEY_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PRODUCT_NAME + " TEXT,"
                + KEY_PRODUCT_PRICE + " INTEGER,"
                + KEY_PRODUCT_DISCOUNT_PRICE + " INTEGER,"
                + KEY_PRODUCT_DESCRIPTION + " TEXT,"
                + KEY_PRODUCT_IMAGES + " TEXT,"
                + KEY_PRODUCT_CATEGORY + " TEXT,"
                + KEY_PRODUCT_RATING + " INTEGER,"
                + KEY_PRODUCT_SOLD_QUANTITY + " INTEGER,"
                + KEY_PRODUCT_IS_HOT + " INTEGER DEFAULT 0,"
                + KEY_PRODUCT_IS_NEW + " INTEGER DEFAULT 0,"
                + KEY_PRODUCT_IS_BEST_SELLER + " INTEGER DEFAULT 0,"
                + KEY_PRODUCT_SCREEN + " TEXT,"
                + KEY_PRODUCT_CPU + " TEXT,"
                + KEY_PRODUCT_RAM + " TEXT,"
                + KEY_PRODUCT_ROM + " TEXT,"
                + KEY_PRODUCT_CAMERA + " TEXT,"
                + KEY_PRODUCT_BATTERY + " TEXT" + ")";

        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + "("
                + KEY_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CATEGORY_NAME + " TEXT,"
                + KEY_CATEGORY_IMAGE + " TEXT,"
                + KEY_CATEGORY_PARENT_ID + " TEXT" + ")";

        String CREATE_BANNERS_TABLE = "CREATE TABLE " + TABLE_BANNERS + "("
                + KEY_BANNER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_BANNER_IMAGE + " TEXT" + ")";

        String CREATE_FAQS_TABLE = "CREATE TABLE " + TABLE_FAQS + "("
                + KEY_FAQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_FAQ_QUESTION + " TEXT,"
                + KEY_FAQ_ANSWER + " TEXT,"
                + KEY_FAQ_IMAGE + " TEXT" + ")";

        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                + KEY_ORDER_ID + " TEXT PRIMARY KEY,"
                + KEY_ORDER_USERNAME + " TEXT,"
                + KEY_ORDER_RECEIVER_NAME + " TEXT,"
                + KEY_ORDER_RECEIVER_PHONE + " TEXT,"
                + KEY_ORDER_RECEIVER_ADDRESS + " TEXT,"
                + KEY_ORDER_ITEMS_JSON + " TEXT,"
                + KEY_ORDER_TOTAL_PRICE + " INTEGER,"
                + KEY_ORDER_STATUS + " TEXT,"
                + KEY_ORDER_TIMESTAMP + " INTEGER" + ")";

        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + KEY_FAV_USER_EMAIL + " TEXT,"
                + KEY_FAV_PRODUCT_ID + " INTEGER,"
                + "PRIMARY KEY (" + KEY_FAV_USER_EMAIL + ", " + KEY_FAV_PRODUCT_ID + "))";

        String CREATE_VOUCHERS_TABLE = "CREATE TABLE " + TABLE_VOUCHERS + "("
                + KEY_VOUCHER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_VOUCHER_CODE + " TEXT UNIQUE,"
                + KEY_VOUCHER_DESCRIPTION + " TEXT,"
                + KEY_VOUCHER_DISCOUNT_VALUE + " INTEGER,"
                + KEY_VOUCHER_TYPE + " TEXT,"
                + KEY_VOUCHER_MIN_ORDER + " INTEGER,"
                + KEY_VOUCHER_START_DATE + " INTEGER,"
                + KEY_VOUCHER_EXPIRY + " INTEGER" + ")";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PRODUCTS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        db.execSQL(CREATE_BANNERS_TABLE);
        db.execSQL(CREATE_FAQS_TABLE);
        db.execSQL(CREATE_ORDERS_TABLE);
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_VOUCHERS_TABLE);

        insertDefaultData(db);
    }

    private void insertDefaultData(SQLiteDatabase db) {
        // Default Admin
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USERS + " (" + KEY_USER_EMAIL + ", " + KEY_USER_USERNAME + ", " + KEY_USER_PASSWORD + ", " + KEY_USER_FULLNAME + ", " + KEY_USER_IS_ADMIN + ") VALUES ('admin@ashop.com', 'admin', '123456', 'Administrator', 1)");

        // Initial Categories
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" + KEY_CATEGORY_NAME + ", " + KEY_CATEGORY_IMAGE + ") VALUES ('Điện thoại', 'https://png.pngtree.com/png-vector/20190130/ourmid/pngtree-hand-drawn-male-suit-clothing-can-be-commercial-elements-png-image_679450.jpg')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" + KEY_CATEGORY_NAME + ", " + KEY_CATEGORY_IMAGE + ") VALUES ('Máy tính', 'https://png.pngtree.com/png-vector/20220624/ourmid/pngtree-trousers-black-isolated-denim-clothes-png-image_5311266.png')");
        db.execSQL("INSERT INTO " + TABLE_CATEGORIES + " (" + KEY_CATEGORY_NAME + ", " + KEY_CATEGORY_IMAGE + ") VALUES ('Phụ kiện', 'https://png.pngtree.com/png-clipart/20230426/original/pngtree-modern-sports-sneaker-shoes-png-image_9100063.png')");

        // Initial Vouchers
        long now = System.currentTimeMillis();
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_VOUCHERS + " (" + KEY_VOUCHER_CODE + ", " + KEY_VOUCHER_DESCRIPTION + ", " + KEY_VOUCHER_DISCOUNT_VALUE + ", " + KEY_VOUCHER_TYPE + ", " + KEY_VOUCHER_MIN_ORDER + ", " + KEY_VOUCHER_START_DATE + ", " + KEY_VOUCHER_EXPIRY + ") VALUES ('WELCOME10', 'Giảm 10% cho đơn hàng đầu tiên', 10, 'PERCENT', 0, " + now + ", " + (now + 30L*24*60*60*1000) + ")");
        
        // Initial Products
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" + KEY_PRODUCT_NAME + ", " + KEY_PRODUCT_PRICE + ", " + KEY_PRODUCT_CATEGORY + ", " + KEY_PRODUCT_IMAGES + ", " + KEY_PRODUCT_RATING + ", " + KEY_PRODUCT_SOLD_QUANTITY + ") VALUES ('iPhone 15 Pro Max', 34990000, 'Điện thoại', '[\"https://cdn.tgdd.vn/Products/Images/42/305658/iphone-15-pro-max-blue-thumb-600x600.jpg\"]', 5, 150)");
        db.execSQL("INSERT INTO " + TABLE_PRODUCTS + " (" + KEY_PRODUCT_NAME + ", " + KEY_PRODUCT_PRICE + ", " + KEY_PRODUCT_CATEGORY + ", " + KEY_PRODUCT_IMAGES + ", " + KEY_PRODUCT_RATING + ", " + KEY_PRODUCT_SOLD_QUANTITY + ") VALUES ('MacBook Air M3', 27990000, 'Máy tính', '[\"https://cdn.tgdd.vn/Products/Images/44/322615/apple-macbook-air-m3-13-inch-midnight-600x600.jpg\"]', 5, 80)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 15) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_USER_POINTS + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + KEY_USER_TOTAL_SPENT + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 16) {
            db.execSQL("ALTER TABLE " + TABLE_CATEGORIES + " ADD COLUMN " + KEY_CATEGORY_PARENT_ID + " TEXT");
        }
        if (oldVersion < 17) {
            db.execSQL("ALTER TABLE " + TABLE_VOUCHERS + " ADD COLUMN " + KEY_VOUCHER_START_DATE + " INTEGER DEFAULT 0");
        }
    }

    // Category Methods
    public void addCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_NAME, category.getName());
        values.put(KEY_CATEGORY_IMAGE, category.getImageUrl());
        values.put(KEY_CATEGORY_PARENT_ID, category.getParentId()); 
        db.insert(TABLE_CATEGORIES, null, values);
        db.close();
    }

    public List<Category> getAllCategories() {
        return getCategoriesByParent(null);
    }

    public List<Category> getCategoriesByParent(String parentId) {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection;
        String[] selectionArgs;
        if (parentId == null) {
            selection = KEY_CATEGORY_PARENT_ID + " IS NULL";
            selectionArgs = null;
        } else {
            selection = KEY_CATEGORY_PARENT_ID + " = ?";
            selectionArgs = new String[]{parentId};
        }
        Cursor cursor = db.query(TABLE_CATEGORIES, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CATEGORY_ID))));
                category.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME)));
                category.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_IMAGE)));
                category.setParentId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_PARENT_ID)));
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public void updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CATEGORY_NAME, category.getName());
        values.put(KEY_CATEGORY_IMAGE, category.getImageUrl());
        values.put(KEY_CATEGORY_PARENT_ID, category.getParentId()); 
        db.update(TABLE_CATEGORIES, values, KEY_CATEGORY_ID + " = ?", new String[]{category.getId()});
        db.close();
    }

    public void deleteCategory(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORIES, KEY_CATEGORY_ID + " = ?", new String[]{id});
        db.close();
    }

    // FAQ Methods
    public void addFaq(Faq faq) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FAQ_QUESTION, faq.getQuestion());
        values.put(KEY_FAQ_ANSWER, faq.getAnswer());
        values.put(KEY_FAQ_IMAGE, faq.getImageUrl());
        db.insert(TABLE_FAQS, null, values);
        db.close();
    }

    public List<Faq> getAllFaqs() {
        List<Faq> faqs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAQS, null);
        if (cursor.moveToFirst()) {
            do {
                Faq faq = new Faq();
                faq.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_FAQ_ID))));
                faq.setQuestion(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FAQ_QUESTION)));
                faq.setAnswer(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FAQ_ANSWER)));
                faq.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FAQ_IMAGE)));
                faqs.add(faq);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return faqs;
    }

    public void updateFaq(Faq faq) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FAQ_QUESTION, faq.getQuestion());
        values.put(KEY_FAQ_ANSWER, faq.getAnswer());
        values.put(KEY_FAQ_IMAGE, faq.getImageUrl());
        db.update(TABLE_FAQS, values, KEY_FAQ_ID + " = ?", new String[]{faq.getId()});
        db.close();
    }

    public void deleteFaq(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAQS, KEY_FAQ_ID + " = ?", new String[]{id});
        db.close();
    }

    // Product Methods
    public long addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = productToValues(product);
        long id = db.insert(TABLE_PRODUCTS, null, values);
        db.close();
        return id;
    }

    public void updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = productToValues(product);
        db.update(TABLE_PRODUCTS, values, KEY_PRODUCT_ID + " = ?", new String[]{String.valueOf(product.getId())});
        db.close();
    }

    public void deleteProduct(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTS, KEY_PRODUCT_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);
        if (cursor.moveToFirst()) {
            do {
                products.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return products;
    }

    private ContentValues productToValues(Product product) {
        ContentValues values = new ContentValues();
        values.put(KEY_PRODUCT_NAME, product.getName());
        values.put(KEY_PRODUCT_PRICE, product.getPrice());
        values.put(KEY_PRODUCT_DISCOUNT_PRICE, product.getDiscountPrice());
        values.put(KEY_PRODUCT_DESCRIPTION, product.getDescription());
        values.put(KEY_PRODUCT_IMAGES, gson.toJson(product.getImages()));
        values.put(KEY_PRODUCT_CATEGORY, product.getCategory());
        values.put(KEY_PRODUCT_RATING, product.getRating());
        values.put(KEY_PRODUCT_SOLD_QUANTITY, product.getSoldQuantity());
        values.put(KEY_PRODUCT_IS_HOT, product.isHotDiscount() ? 1 : 0);
        values.put(KEY_PRODUCT_IS_NEW, product.isNewArrival() ? 1 : 0);
        values.put(KEY_PRODUCT_IS_BEST_SELLER, product.isBestSeller() ? 1 : 0);
        values.put(KEY_PRODUCT_SCREEN, product.getScreen());
        values.put(KEY_PRODUCT_CPU, product.getCpu());
        values.put(KEY_PRODUCT_RAM, product.getRam());
        values.put(KEY_PRODUCT_ROM, product.getRom());
        values.put(KEY_PRODUCT_CAMERA, product.getCamera());
        values.put(KEY_PRODUCT_BATTERY, product.getBattery());
        return values;
    }

    private Product cursorToProduct(Cursor cursor) {
        Product p = new Product();
        p.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PRODUCT_ID)));
        p.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_NAME)));
        p.setPrice(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PRODUCT_PRICE)));
        p.setDiscountPrice(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_PRODUCT_DISCOUNT_PRICE)));
        p.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_DESCRIPTION)));
        
        String imagesJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_IMAGES));
        Type listType = new TypeToken<ArrayList<String>>(){}.getType();
        List<String> images = gson.fromJson(imagesJson, listType);
        if (images == null) images = new ArrayList<>();
        p.setImages(images);

        p.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_CATEGORY)));
        p.setRating(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRODUCT_RATING)));
        p.setSoldQuantity(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRODUCT_SOLD_QUANTITY)));
        p.setHotDiscount(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRODUCT_IS_HOT)) == 1);
        p.setNewArrival(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRODUCT_IS_NEW)) == 1);
        p.setBestSeller(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRODUCT_IS_BEST_SELLER)) == 1);
        p.setScreen(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_SCREEN)));
        p.setCpu(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_CPU)));
        p.setRam(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_RAM)));
        p.setRom(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_ROM)));
        p.setCamera(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_CAMERA)));
        p.setBattery(cursor.getString(cursor.getColumnIndexOrThrow(KEY_PRODUCT_BATTERY)));
        return p;
    }

    // Banner Methods
    public void addBanner(Banner banner) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BANNER_IMAGE, banner.getImageUrl());
        db.insert(TABLE_BANNERS, null, values);
        db.close();
    }

    public List<Banner> getAllBanners() {
        List<Banner> banners = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BANNERS, null);
        if (cursor.moveToFirst()) {
            do {
                Banner b = new Banner();
                b.setId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BANNER_ID))));
                b.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(KEY_BANNER_IMAGE)));
                banners.add(b);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return banners;
    }

    public void updateBanner(Banner banner) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BANNER_IMAGE, banner.getImageUrl());
        db.update(TABLE_BANNERS, values, KEY_BANNER_ID + " = ?", new String[]{banner.getId()});
        db.close();
    }

    public void deleteBanner(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BANNERS, KEY_BANNER_ID + " = ?", new String[]{id});
        db.close();
    }

    // Favorite Methods
    public void addFavorite(String userEmail, long productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FAV_USER_EMAIL, userEmail);
        values.put(KEY_FAV_PRODUCT_ID, productId);
        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void removeFavorite(String userEmail, long productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, KEY_FAV_USER_EMAIL + " = ? AND " + KEY_FAV_PRODUCT_ID + " = ?", new String[]{userEmail, String.valueOf(productId)});
        db.close();
    }

    public boolean isFavorite(String userEmail, long productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null, KEY_FAV_USER_EMAIL + " = ? AND " + KEY_FAV_PRODUCT_ID + " = ?", new String[]{userEmail, String.valueOf(productId)}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public List<Product> getFavorites(String userEmail) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p.* FROM " + TABLE_PRODUCTS + " p INNER JOIN " + TABLE_FAVORITES + " f ON p." + KEY_PRODUCT_ID + " = f." + KEY_FAV_PRODUCT_ID + " WHERE f." + KEY_FAV_USER_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userEmail});
        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToProduct(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // Order Methods
    public long addOrder(Order order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ORDER_ID, order.getOrderId());
        values.put(KEY_ORDER_USERNAME, order.getUsername());
        values.put(KEY_ORDER_RECEIVER_NAME, order.getReceiverName());
        values.put(KEY_ORDER_RECEIVER_PHONE, order.getReceiverPhone());
        values.put(KEY_ORDER_RECEIVER_ADDRESS, order.getReceiverAddress());
        
        String itemsJson = gson.toJson(order.getItems());
        values.put(KEY_ORDER_ITEMS_JSON, itemsJson);
        
        values.put(KEY_ORDER_TOTAL_PRICE, order.getTotalPrice());
        values.put(KEY_ORDER_STATUS, order.getStatus());
        values.put(KEY_ORDER_TIMESTAMP, order.getTimestamp());
        long result = db.insert(TABLE_ORDERS, null, values);
        db.close();
        return result;
    }

    public List<Order> getOrdersByUsername(String username) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, KEY_ORDER_USERNAME + " = ?", new String[]{username}, null, null, KEY_ORDER_TIMESTAMP + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Order o = new Order();
                o.setOrderId(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_ID)));
                o.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_USERNAME)));
                o.setReceiverName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_RECEIVER_NAME)));
                o.setReceiverPhone(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_RECEIVER_PHONE)));
                o.setReceiverAddress(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_RECEIVER_ADDRESS)));
                
                String itemsJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_ITEMS_JSON));
                Type listType = new TypeToken<ArrayList<Product>>(){}.getType();
                List<Product> items = gson.fromJson(itemsJson, listType);
                o.setItems(items);

                o.setTotalPrice(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_TOTAL_PRICE)));
                o.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ORDER_STATUS)));
                o.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ORDER_TIMESTAMP)));
                list.add(o);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // Voucher Methods
    public long addVoucher(Voucher voucher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_VOUCHER_CODE, voucher.getCode());
        values.put(KEY_VOUCHER_DESCRIPTION, voucher.getDescription());
        values.put(KEY_VOUCHER_DISCOUNT_VALUE, voucher.getDiscountValue());
        values.put(KEY_VOUCHER_TYPE, voucher.getType());
        values.put(KEY_VOUCHER_MIN_ORDER, voucher.getMinOrderAmount());
        values.put(KEY_VOUCHER_START_DATE, voucher.getStartDate());
        values.put(KEY_VOUCHER_EXPIRY, voucher.getExpiryDate());
        long id = db.insert(TABLE_VOUCHERS, null, values);
        db.close();
        return id;
    }

    public void updateVoucher(Voucher voucher) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_VOUCHER_CODE, voucher.getCode());
        values.put(KEY_VOUCHER_DESCRIPTION, voucher.getDescription());
        values.put(KEY_VOUCHER_DISCOUNT_VALUE, voucher.getDiscountValue());
        values.put(KEY_VOUCHER_TYPE, voucher.getType());
        values.put(KEY_VOUCHER_MIN_ORDER, voucher.getMinOrderAmount());
        values.put(KEY_VOUCHER_START_DATE, voucher.getStartDate());
        values.put(KEY_VOUCHER_EXPIRY, voucher.getExpiryDate());
        db.update(TABLE_VOUCHERS, values, KEY_VOUCHER_ID + " = ?", new String[]{String.valueOf(voucher.getId())});
        close();
    }

    public void deleteVoucher(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_VOUCHERS, KEY_VOUCHER_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public List<Voucher> getAllVouchers() {
        List<Voucher> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_VOUCHERS, null);
        if (cursor.moveToFirst()) {
            do {
                Voucher v = new Voucher();
                v.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOUCHER_ID)));
                v.setCode(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOUCHER_CODE)));
                v.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOUCHER_DESCRIPTION)));
                v.setDiscountValue(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_VOUCHER_DISCOUNT_VALUE)));
                v.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOUCHER_TYPE)));
                v.setMinOrderAmount(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_VOUCHER_MIN_ORDER)));
                v.setStartDate(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_VOUCHER_START_DATE)));
                v.setExpiryDate(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_VOUCHER_EXPIRY)));
                list.add(v);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Voucher getVoucherByCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_VOUCHERS, null, KEY_VOUCHER_CODE + "=?", new String[]{code}, null, null, null);
        Voucher v = null;
        if (cursor != null && cursor.moveToFirst()) {
            v = new Voucher();
            v.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_VOUCHER_ID)));
            v.setCode(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOUCHER_CODE)));
            v.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOUCHER_DESCRIPTION)));
            v.setDiscountValue(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_VOUCHER_DISCOUNT_VALUE)));
            v.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_VOUCHER_TYPE)));
            v.setMinOrderAmount(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_VOUCHER_MIN_ORDER)));
            v.setStartDate(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_VOUCHER_START_DATE)));
            v.setExpiryDate(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_VOUCHER_EXPIRY)));
            cursor.close();
        }
        return v;
    }
}
