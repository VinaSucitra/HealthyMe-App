package com.example.healthyme.model;

public class History {
    private String name;
    private String date;

    public History(String name, String date) {
        this.name = name;
        this.date = date;
    }

    public String getName() { return name; }
    public String getDate() { return date; }
}
