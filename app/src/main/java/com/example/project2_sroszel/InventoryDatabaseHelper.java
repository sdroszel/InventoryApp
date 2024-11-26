package com.example.project2_sroszel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Database class to store products
public class InventoryDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 6;

    // Sets column names
    public static final String TABLE_NAME = "products";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_COST = "cost";
    public static final String COLUMN_QUANTITY = "quantity";

    // Creating the table
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL UNIQUE, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_COST + " REAL, "
            + COLUMN_QUANTITY + " INTEGER DEFAULT 0"
            + ");";

    // Constructor to use in activity_main
    public InventoryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);

        // Put starting data into database
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Used to put initial data into database
    private void insertInitialData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_NAME + ", " + COLUMN_DESCRIPTION + ", " + COLUMN_COST + ", " + COLUMN_QUANTITY + ") VALUES ('APPLE', 'Honey Crisp Apple', 1.99, 50);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_NAME + ", " + COLUMN_DESCRIPTION + ", " + COLUMN_COST + ", " + COLUMN_QUANTITY + ") VALUES ('BANANA', 'Chiquita Banana', 0.40, 75);");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_NAME + ", " + COLUMN_DESCRIPTION + ", " + COLUMN_COST + ", " + COLUMN_QUANTITY + ") VALUES ('CARROT', 'Fresh Garden Carrot', 0.55, 40);");
    }

    // Inserts new product into database
    public long insertProduct(String name, String description, double cost, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.toUpperCase());
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_COST, cost);
        values.put(COLUMN_QUANTITY, quantity);

        return db.insert(TABLE_NAME, null, values);
    }

    // Method to retrieve all products from the database
    public Cursor getAllProducts() {
        SQLiteDatabase db = this.getReadableDatabase();
        // get all the columns in the table
        return db.query(TABLE_NAME, null, null, null, null, null, COLUMN_NAME);
    }

    // Method to update a product (does not update name)
    public void updateProduct(String name, String description, double cost, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_COST, cost);
        values.put(COLUMN_QUANTITY, quantity);

        db.update(TABLE_NAME, values, COLUMN_NAME + "=?", new String[]{name});
    }

    // Method to delete a product from database
    public void deleteProduct(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_NAME, COLUMN_NAME + "=?", new String[]{name});
    }

    // Method used to get a cursor pointing to product
    public Cursor getProductByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        // return cursor pointing to row matching passed in name
        return db.query(TABLE_NAME, null, COLUMN_NAME + "=?", new String[]{name}, null, null, null);
    }

    // Method to update the quantity of a product
    public void updateProductQuantity(String productName, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUANTITY, newQuantity);

        // Update the quantity where the product name matches
        db.update(TABLE_NAME, values, COLUMN_NAME + "=?", new String[]{productName});
    }
}
