package com.example.project2_sroszel;

// product object class
public class Product {
    private final String name;
    private final String description;
    private final double cost;
    private int quantity;

    public Product(String name, String description, double cost, int quantity) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getCost() {
        return cost;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
