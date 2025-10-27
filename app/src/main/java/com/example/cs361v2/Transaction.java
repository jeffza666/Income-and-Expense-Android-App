package com.example.cs361v2;

public class Transaction {
    private int id;
    private String date;
    private String type;
    private String category;
    private double amount;

    // Constructor
    public Transaction(int id, String date, String type, String category, double amount) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    // Optional: toString method for easier logging/debugging
    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", amount=" + amount +
                '}';
    }
}
