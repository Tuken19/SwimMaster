package com.example.swimmaster;

public class SingleWorkout {
    private long mPosition;
    private String mName;
    private String mDate;


    // ========== Getters ==========
    public long getPosition() {
        return mPosition;
    }

    public String getName() {
        return mName;
    }

    public String getDate() {
        return mDate;
    }
    // =============================

    // ========== Setters ==========
    public void setPosition(long position) {
        this.mPosition = position;
    }

    public void setName(String Name) {
        this.mName = Name;
    }

    public void setDate(String Date) {
        this.mDate = Date;
    }
    // =============================
}
