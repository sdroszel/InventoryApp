package com.example.project2_sroszel;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class OrderScreen extends AppCompatActivity {

    private EditText itemQuantityText;
    private TextView cartTotal;

    private List<Product> availableProducts;
    private List<Product> orderItems;
    private ArrayAdapter<String> orderAdapter;
    private List<String> orderDescriptions;

    private Product selectedProduct;
    private Product selectedOrderItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_screen);

        Spinner availableProductsSpinner = findViewById(R.id.availableProducts);
        ListView orderCart = findViewById(R.id.orderCart); // ListView

        cartTotal = findViewById(R.id.cartTotal);
        itemQuantityText = findViewById(R.id.itemQuantityText);

        Button addToOrderBtn = findViewById(R.id.addToOrderBtn);
        Button removeFromOrderBtn = findViewById(R.id.removeFromOrderBtn);
        Button finishOrderBtn = findViewById(R.id.finishOrderBtn);

        // get all available products
        availableProducts = loadAvailableProducts();

        List<String> productNames = new ArrayList<>();

        // extract the name from product to add to spinner
        for (Product product : availableProducts) {
            productNames.add(product.getName());
        }

        // Set up the spinner with product names
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        availableProductsSpinner.setAdapter(adapter);

        // handles spinner selection
        availableProductsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProduct = availableProducts.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProduct = null;
            }
        });

        // initialize order items and descriptions lists
        orderItems = new ArrayList<>();
        orderDescriptions = new ArrayList<>();

        // Set up the ListView adapter
        orderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, orderDescriptions);
        orderCart.setAdapter(orderAdapter);

        // set click listener for the order cart
        orderCart.setOnItemClickListener((parent, view, position, id) -> {
            selectedOrderItem = orderItems.get(position);
            Toast.makeText(this, "Selected for removal: " + selectedOrderItem.getName(), Toast.LENGTH_SHORT).show();
        });

        setButtonClickListeners(addToOrderBtn, removeFromOrderBtn, finishOrderBtn);

        updateTotalCost();
    }

    // Handles setting up the click listeners for the buttons
    private void setButtonClickListeners(Button addToOrderBtn, Button removeFromOrderBtn, Button finishOrderBtn) {
        addToOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToOrder();
            }
        });

        removeFromOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromOrder();
            }
        });

        finishOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOrder();
            }
        });
    }

    // Method to update total cost of cart
    private void updateTotalCost() {
        double totalCost = 0;
        for (Product product : orderItems) {
            totalCost += product.getCost() * product.getQuantity();
        }
        String total = String.format("Total Cost: $%.2f", totalCost);
        cartTotal.setText(total);
    }

    // Gets the available products for the database
    private List<Product> loadAvailableProducts() {
        InventoryDatabaseHelper dbHelper = new InventoryDatabaseHelper(this);
        List<Product> products = new ArrayList<>();
        Cursor cursor = dbHelper.getAllProducts();
        if (cursor != null && cursor.moveToFirst()) {
            // extract data from each row of the database
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_DESCRIPTION));
                double cost = cursor.getDouble(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_COST));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_QUANTITY));

                // add the product data to the list
                products.add(new Product(name, description, cost, quantity));
            } while (cursor.moveToNext());
            cursor.close();
        }
        // returns a list of products including description, cost, and quantity
        return products;
    }

    private void addToOrder() {
        // handles if no product is selected
        if (selectedProduct == null) {
            Toast.makeText(this, "No product selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String quantityText = itemQuantityText.getText().toString();
        // Handles if user did not enter a quantity to add to order
        if (quantityText.isEmpty()) {
            Toast.makeText(this, "Enter a quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int requestedQuantity = Integer.parseInt(quantityText);
        int availableQuantity = selectedProduct.getQuantity();

        if (availableQuantity == 0) {
            Toast.makeText(this, "Can't add " + selectedProduct.getName() + ", there are none in stock.", Toast.LENGTH_SHORT).show();
            return;
        }

        // handles if user enters quantity less than or equal to zero
        if (requestedQuantity <= 0) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handles if requested quantity is greater than available quantity
        if (requestedQuantity > availableQuantity) {
            requestedQuantity = availableQuantity;
            Toast.makeText(this, "Only " + availableQuantity + " available. Added maximum to order.", Toast.LENGTH_SHORT).show();
        }

        // check if the product already exists in the order
        boolean productExists = false;
        for (Product orderItem : orderItems) {
            if (orderItem.getName().equals(selectedProduct.getName())) {
                // update quantity
                orderItem.setQuantity(orderItem.getQuantity() + requestedQuantity);
                selectedProduct.setQuantity(availableQuantity - requestedQuantity);

                // update the description
                int index = orderItems.indexOf(orderItem);
                orderDescriptions.set(index, orderItem.getName() + " | Quantity: " + orderItem.getQuantity() +
                        " | Total Cost: $" + String.format("%.2f", orderItem.getCost() * orderItem.getQuantity()));

                productExists = true;
                break;
            }
        }

        if (!productExists) {
            // add new product to cart
            Product orderItem = new Product(selectedProduct.getName(), selectedProduct.getDescription(), selectedProduct.getCost(), requestedQuantity);
            orderItems.add(orderItem);

            // adjust quantity
            selectedProduct.setQuantity(availableQuantity - requestedQuantity);

            // add item details to cart
            orderDescriptions.add(orderItem.getName() + " | Quantity: " + orderItem.getQuantity() +
                    " | Total Cost: $" + String.format("%.2f", orderItem.getCost() * orderItem.getQuantity()));
        }

        orderAdapter.notifyDataSetChanged();
        updateTotalCost();
    }

    private void removeFromOrder() {
        // Handles if user does not select product to be deleted
        if (selectedOrderItem == null) {
            Toast.makeText(this, "No order item selected for removal", Toast.LENGTH_SHORT).show();
            return;
        }

        // get product details for selected product
        Product availableProduct = getProductByName(selectedOrderItem.getName());

        if (availableProduct != null) {
            // adds the ordered quantity back into available quantity
            availableProduct.setQuantity(availableProduct.getQuantity() + selectedOrderItem.getQuantity());
        }

        // remove the ordered product from lists
        int index = orderItems.indexOf(selectedOrderItem);
        orderItems.remove(selectedOrderItem);
        orderDescriptions.remove(index);

        // reset order cart selection to null
        selectedOrderItem = null;

        orderAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Item removed from order", Toast.LENGTH_SHORT).show();

        updateTotalCost();
    }

    // updates the database and finishes the intent
    private void finishOrder() {
        for (Product item : orderItems) {
            updateInventory(item);
        }
        Toast.makeText(this, "Order completed", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Method to update the inventory when finishing the order
    private void updateInventory(Product product) {
        InventoryDatabaseHelper dbHelper = new InventoryDatabaseHelper(this);

        // points to product row in database
        Cursor cursor = dbHelper.getProductByName(product.getName());
        if (cursor != null && cursor.moveToFirst()) {
            // gets the current quantity in database
            int currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COLUMN_QUANTITY));

            cursor.close();

            int orderedQuantity = product.getQuantity();
            // set new quantity
            int newQuantity = currentQuantity - orderedQuantity;

            // update the new quantity to the database
            dbHelper.updateProductQuantity(product.getName(), newQuantity);
        }
    }

    // helper function to get product by name
    private Product getProductByName(String name) {
        for (Product product : availableProducts) {
            if (product.getName().equals(name)) return product;
        }
        return null;
    }
}
