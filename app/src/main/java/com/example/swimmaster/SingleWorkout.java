package com.example.swimmaster;

import java.util.ArrayList;
import java.util.List;

public class SingleWorkout {
    private String mName;
    private String mDate;
    private List<Task> mWarmUp = new ArrayList<>();
    private List<Task> mMainSet = new ArrayList<>();
    private List<Task> mCooldown = new ArrayList<>();

    private String mCreationdate;

    SingleWorkout() {

    }

    SingleWorkout(String name, String date, List<Task> warmUp, List<Task> mainSet, List<Task> cooldown, String creationdate) {
        mName = name;
        mDate = date;
        mWarmUp = warmUp;
        mMainSet = mainSet;
        mCooldown = cooldown;
        mCreationdate = creationdate;
    }

    // ========== Getters ==========
    public String getName() {
        return mName;
    }

    public String getDate() {
        return mDate;
    }

    public List<Task> getWarmUp() {
        return mWarmUp;
    }

    public List<Task> getMainSet() {
        return mMainSet;
    }

    public List<Task> getCooldown() {
        return mCooldown;
    }

    public String getCreationdate() {
        return mCreationdate;
    }
    // =============================

    // ========== Setters ==========
    public void setName(String name) {
        this.mName = name;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public void setWarmUp(List<Task> warmUp) {
        mWarmUp = warmUp;
    }

    public void setMainSet(List<Task> mainSet) {
        mMainSet = mainSet;
    }

    public void setCooldown(List<Task> cooldown) {
        mCooldown = cooldown;
    }

    public void setCreationdate(String creationdate) {
        this.mCreationdate = creationdate;
    }
    // =============================
}
