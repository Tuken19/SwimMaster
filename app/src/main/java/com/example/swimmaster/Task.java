package com.example.swimmaster;

import java.util.ArrayList;
import java.util.List;

public class Task {
    private String mStyle;
    private String mDistance;
    private String mType;
    private String mPace;
    private int mRepetitions;
    private int mRest;
    private List<String> mAdditions = new ArrayList<String>();

    Task(){

    }

    Task(String style, String distance, String type, String pace, int repetitions, int rest, List<String> additions){
        mStyle = style;
        mDistance = distance;
        mType = type;
        mPace = pace;
        mRepetitions = repetitions;
        mRest = rest;
        mAdditions = additions;
    }

    // ===== Getters =====
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

    public boolean isAdditionsEmpty(){
        return mAdditions.isEmpty();
    }
}
