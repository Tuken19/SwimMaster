package com.example.swimmaster.SingleWorkout;

import com.example.swimmaster.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SingleWorkout implements SingleWorkoutBuilder, Comparable<SingleWorkout> {
    private String mName;
    private String mDate;
    private List<Task> mWarmUp = new ArrayList<>();
    private List<Task> mMainSet = new ArrayList<>();
    private List<Task> mCooldown = new ArrayList<>();

    private String mCreationDate;

    SingleWorkout() {

    }

    SingleWorkout(String name, String date, List<Task> warmUp, List<Task> mainSet, List<Task> cooldown, String creationDate) {
        mName = name;
        mDate = date;
        mWarmUp = warmUp;
        mMainSet = mainSet;
        mCooldown = cooldown;
        mCreationDate = creationDate;
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
        return mCreationDate;
    }
    // =============================

    // ========== Setters ==========
    @Override
    public void resetSingleWorkout() {

    }

    @Override
    public void setName(String name) {
        this.mName = name;
    }

    @Override
    public void setDate(String date) {
        this.mDate = date;
    }

    @Override
    public void setWarmUp(List<Task> warmUp) {
        mWarmUp = warmUp;
    }

    @Override
    public void setMainSet(List<Task> mainSet) {
        mMainSet = mainSet;
    }

    @Override
    public void setCooldown(List<Task> cooldown) {
        mCooldown = cooldown;
    }

    @Override
    public void setCreationdate(String creationDate) {
        this.mCreationDate = creationDate;
    }

    @Override
    public SingleWorkout build() {
        return this;
    }
    // =============================

    // ========== Methods ==========
    public boolean equals(SingleWorkout singleWorkout) {
        if (this.getName().equals(singleWorkout.getName()) && this.getDate().equals(singleWorkout.getDate())) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public int compareTo(SingleWorkout singleWorkout) {

        String compDate = singleWorkout.getDate().substring(singleWorkout.getDate().length() - 4) +
                singleWorkout.getDate().substring(singleWorkout.getDate().length() - 7, singleWorkout.getDate().length() - 5) +
                singleWorkout.getDate().substring(singleWorkout.getDate().length() - 10, singleWorkout.getDate().length() - 8);

        String tDate = this.getDate().substring(this.getDate().length() - 4) +
                this.getDate().substring(this.getDate().length() - 7, this.getDate().length() - 5) +
                this.getDate().substring(this.getDate().length() - 10, this.getDate().length() - 8);

        int compareDate = Integer.parseInt(compDate);
        int thisDate = Integer.parseInt(tDate);

        //ascending order
        return thisDate - compareDate;

        //descending order
        //return compareDate - thisDate;
    }

    public static Comparator<SingleWorkout> singleWorkoutComparator = new Comparator<SingleWorkout>() {

        public int compare(SingleWorkout s1, SingleWorkout s2) {

            //ascending order
            return s1.compareTo(s2);

            //descending order
            //return s2.compareTo(s1);
        }

    };
    // =============================
}
