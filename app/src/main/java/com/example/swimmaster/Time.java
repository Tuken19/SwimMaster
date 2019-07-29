package com.example.swimmaster;

import java.util.Comparator;

public class Time implements Comparable<Time> {

    private long position;
    private String style;
    private String distance;
    private String time;
    private String date;

    Time() {

    }


    // ========== Getters ==========
    public long getPosition() {
        return position;
    }

    public String getStyle() {
        return style;
    }

    public String getDistance() {
        return distance;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    // =============================
    // ========== Setters ==========

    public void setPosition(long position) {
        this.position = position;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }
    // =============================

    // ========== Methods ==========
    public boolean equals(Time time) {
        if (this.getDate().equals(time.getDate())
                && this.getStyle().equals(time.getStyle())
                && this.getDistance().equals(time.getDistance())
                && this.getTime().equals(time.getTime())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Time time) {
        int compareDistance = Integer.parseInt(time.getDistance().substring(0, time.getDistance().length() - 2));
        int thisDistance = Integer.parseInt(this.getDistance().substring(0, this.getDistance().length() - 2));

        //ascending order
        return thisDistance - compareDistance;

        //descending order
        //return compareDistance - thisDistance;
    }

    public static Comparator<Time> timeComparator = new Comparator<Time>() {

        public int compare(Time t1, Time t2) {

            //ascending order
            return t1.compareTo(t2);

            //descending order
            //return fruitName2.compareTo(fruitName1);
        }

    };
    // =============================
}
