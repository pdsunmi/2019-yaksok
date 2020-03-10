package com.example.yaksok.Models;

public class Promise {
    private int id;
    private long date;
    private String dateString;

    public Promise(int id, long date, String dateString) {
        this.id = id;
        this.date = date;
        this.dateString = dateString;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateString() {
        return dateString;
    }

    public long getDate() {
        return date;
    }
}
