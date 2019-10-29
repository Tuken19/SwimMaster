package com.example.swimmaster.TrainingSetBuilder;

import com.example.swimmaster.SingleWorkout.SingleWorkout;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.util.Calendar;
import java.util.HashSet;

class TrainingSet implements TrainningSetBuilder{

    private static final TrainingSet ourInstance = new TrainingSet();
    private boolean mDays[] = new boolean[7];
    private int mWeeks;
    private int mStartingdistance;
    private HashSet<SingleWorkout> mTraining = null;
    private HashSet<CalendarDay> mTrainingDates = null;

    static TrainingSet getInstance() {
        return ourInstance;
    }

    private TrainingSet() {

    }

    public TrainingSet getTrainingSet() {
        return ourInstance;
    }

    public HashSet<CalendarDay> getTrainingDates (){
        return mTrainingDates;
    }

    @Override
    public void resetTraining() {
        mDays = null;
        mWeeks = 0;
        mStartingdistance = 0;
        mTraining = null;
        mTrainingDates = null;
    }

    @Override
    public void setDays(boolean[] days) {
        mDays = days;
    }

    @Override
    public void setWeeks(int weeks) {
        mWeeks = weeks;
    }

    @Override
    public void setStartingDistance(int startingDistance) {
        mStartingdistance = startingDistance;
    }

    @Override
    public TrainingSet generateTraining() {
        resetTraining();

        CalendarDay today = CalendarDay.today();
        int day = today.getDay();
        int month = today.getMonth();
        int year = today.getYear();
        int daysCount = mWeeks * 7;

        Calendar calendar = Calendar.getInstance();

        int move;
        switch (today.getDay()){
            case Calendar.MONDAY:
                move = 7;
            case Calendar.TUESDAY:
                move = 6;
            case Calendar.WEDNESDAY:
                move = 5;
            case Calendar.THURSDAY:
                move = 4;
            case Calendar.FRIDAY:
                move = 3;
            case Calendar.SATURDAY:
                move = 2;
            case Calendar.SUNDAY:
                move = 1;
        }

        System.out.println("Today1: " + day);
        System.out.println("Today2: " + Calendar.DAY_OF_MONTH);

        for (int i = 0; i < daysCount; i++) {
            calendar.roll(Calendar.DAY_OF_MONTH, true);
            System.out.println("Roll " + i + ":" + Calendar.DAY_OF_MONTH);
            //CalendarDay day = CalendarDay.from();
            //mTrainingDates.add(day);
        }

        return getInstance();
    }
}
