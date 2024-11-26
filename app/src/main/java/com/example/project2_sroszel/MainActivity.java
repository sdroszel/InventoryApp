package com.example.project2_sroszel;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView productsView;
    private Button addProductBtn, placeOrderBtn;

    private InventoryDatabaseHelper dbHelper;
    private List<Product> productList;
    private ArrayAdapter<String> productAdapter;
    private List<String> productDescriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize UI elements
        productsView = findViewById(R.id.productsView);
        addProductBtn = findViewById(R.id.addProductBtn);
        placeOrderBtn = findViewById(R.id.placeOrderBtn);

        // initialize database helper
        dbHelper = new InventoryDatabaseHelper(this);

        // initialize product lists
        productList = new ArrayList<>();
        productDescriptions = new ArrayList<>();

        // set up the ListView adapter
        productAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productDescriptions);
        productsView.setAdapter(productAdapter);

        // load products from the database
        loadProductsFromDatabase();

        // set up event listeners
        setListeners();
    }

    private void setListeners() {
        // handle press for snackbar
        productsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Product selectedProduct = productList.get(position);
                String message = "Name: " + selectedProduct.getName() + "\nDescription: " + selectedProduct.getDescription();
                Snackbar.make(productsView, message, Snackbar.LENGTH_LONG).show();
            }
        });

        // handle long press for editing a product
        productsView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Product selectedProduct = productList.get(position);
                openEditProductActivity(selectedProduct.getName());
                return true;
            }
        });

        // handle add product button press
        addProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddProductActivity();
            }
        });

        // handle place order button press
        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrderActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        productList.clear();
        productDescriptions.clear();

        Cursor cursor = dbHelper.getAllProducts();

        if (cursor != null && cursor.moveToFirst()) {
            // extract product information from each product
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_DESCRIPTION));
                double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_COST));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_QUANTITY));

                // add product to the list
                Product product = new Product(name, description, cost, quantity);
                productList.add(product);

                // format cost to two decimal places
                String formattedCost = String.format("%.2f", cost);

                // add description for the ListView
                productDescriptions.add(product.getName() + "\nCost per unit: $" + formattedCost + "\nQty: " + quantity);
            } while (cursor.moveToNext());
            cursor.close();
        }

        productAdapter.notifyDataSetChanged();
    }

    //  Open activity to add a new product
    private void openAddProductActivity() {
        Intent intent = new Intent(this, ProductDetail.class);
        intent.putExtra("isNewProduct", true);
        startActivity(intent);
    }

    // Open activity to place a new order
    private void openOrderActivity() {
        Intent intent = new Intent(this, OrderScreen.class);
        startActivity(intent);
    }

    // Open activity to update existing product
    private void openEditProductActivity(String productName) {
        Intent intent = new Intent(this, ProductDetail.class);
        intent.putExtra("isNewProduct", false);
        intent.putExtra("productName", productName);
        startActivity(intent);
    }
}
