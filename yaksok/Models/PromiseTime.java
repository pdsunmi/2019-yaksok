package com.example.yaksok.Models;

/**
 * 예비 리스트에 저장
 */
public class PromiseTime {
    private int id;
    private long date;

    public PromiseTime(int id, long date) {
        this.id = id;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
