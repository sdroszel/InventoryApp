package com.example.project2_sroszel;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

public class ProductDetail extends AppCompatActivity {

    private EditText productNameText, productDescriptionText, productCostText, itemQuantityText;
    private InventoryDatabaseHelper dbHelper;

    private boolean isNewProduct = true;
    private String productName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // database helper
        dbHelper = new InventoryDatabaseHelper(this);

        // UI elements
        productNameText = findViewById(R.id.productNameText);
        productDescriptionText = findViewById(R.id.productDescriptionText);
        productCostText = findViewById(R.id.productCostText);
        itemQuantityText = findViewById(R.id.itemQuantityText);

        Button buttonSave = findViewById(R.id.saveBtn);
        Button buttonCancel = findViewById(R.id.cancelBtn);
        Button buttonRemove = findViewById(R.id.removeBtn);

        Intent intent = getIntent();
        isNewProduct = intent.getBooleanExtra("isNewProduct", true);

        // check if adding a new product
        if (!isNewProduct) {
            // if updating product
            // get product name
            productName = intent.getStringExtra("productName");
            // set product name
            productNameText.setText(productName);
            // disable editing product name
            productNameText.setEnabled(false);
            // load product details
            loadProductDetails(productName);
        } else {
            // remove button hidden if adding new product
            buttonRemove.setVisibility(View.GONE);
        }

        setButtonClickListener(buttonSave, buttonCancel, buttonRemove);
    }

    //Set click listeners for buttons
    private void setButtonClickListener(Button buttonSave, Button buttonCancel, Button buttonRemove) {
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeProduct();
            }
        });
    }

    // Load product details from the database into the text fields
    private void loadProductDetails(String name) {
        // points to table row to update
        Cursor cursor = dbHelper.getProductByName(name);
        if (cursor != null && cursor.moveToFirst()) {
            // gets description, cost, and quantity columns from database
            String description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_DESCRIPTION));
            double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_COST));
            int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_QUANTITY));

            // set extracted data to text fields
            productDescriptionText.setText(description);
            productCostText.setText(String.valueOf(cost));
            itemQuantityText.setText(String.valueOf(quantity));

            cursor.close();
        }
    }

    // Save the product details to the database
    private void saveProduct() {
        // Handles product name for adding new products
        if (isNewProduct) {
            productName = productNameText.getText().toString();
            if (productName.isEmpty()) {
                Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // get product details from text fields
        String description = productDescriptionText.getText().toString();
        String costStr = productCostText.getText().toString();
        String quantityStr = itemQuantityText.getText().toString();

        // Checks if all fields are filled out
        if (description.isEmpty() || costStr.isEmpty() || quantityStr.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // parse to correct types for database
        double cost = Double.parseDouble(costStr);
        int quantity = Integer.parseInt(quantityStr);

        if (isNewProduct) {
            // add new product to database
            long result = dbHelper.insertProduct(productName, description, cost, quantity);

            if (result == -1) {
                Toast.makeText(this, "Product already exists in database", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Product added to database", Toast.LENGTH_SHORT).show();
            }
        } else {
            // update existing product to database
            dbHelper.updateProduct(productName, description, cost, quantity);
        }

        finish();
    }

    // Remove the product from the database
    private void removeProduct() {
        if (!isNewProduct && productName != null) {
            // double check if user wants to remove product for database
            new AlertDialog.Builder(this)
                    .setTitle("Confirm Removal")
                    .setMessage("Are you sure you want to remove this product from the inventory?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper.deleteProduct(productName);
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }
}
