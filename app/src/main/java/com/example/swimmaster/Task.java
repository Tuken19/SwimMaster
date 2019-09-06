package com.example.swimmaster;

import java.util.List;

public class Task {
    private long mPosition;
    private String mStyle;
    private String mDistance;
    private String mType;
    private String mPace;
    private int mRepetitions;
    private int mRest;
    private List<String> mAdditions;

    Task(){

    }

    Task(long position, String style, String distance, String type, String pace, int repetitions, int rest, List<String> additions){
        mPosition = position;
        mStyle = style;
        mDistance = distance;
        mType = type;
        mPace = pace;
        mRepetitions = repetitions;
        mRest = rest;
        mAdditions = additions;
    }

    // ===== Getters =====
    public long getPosition() {
        return mPosition;
    }

    public String getStyle() {
        return mStyle;
    }

    public String getDistance() {
        return mDistance;
    }

    public String getType() {
        return mType;
    }

    public String getPace() {
        return mPace;
    }

    public int getRepetitions() {
        return mRepetitions;
    }

    public int getRest() {
        return mRest;
    }

    public List<String> getAdditions() {
        return mAdditions;
    }
    // ===================

    // ===== Setters =====
    public void setPosition(int position) {
        this.mPosition = position;
    }

    public void setStyle(String mStyle) {
        this.mStyle = mStyle;
    }

    public void setDistance(String distance) {
        this.mDistance = distance;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public void setPace(String pace) {
        this.mPace = pace;
    }

    public void setRepetitions(int repetitions) {
        this.mRepetitions = repetitions;
    }

    public void setRest(int rest) {
        this.mRest = rest;
    }

    public void setAdditions(List<String> additions) {
        this.mAdditions = additions;
    }
    // ===================
}
