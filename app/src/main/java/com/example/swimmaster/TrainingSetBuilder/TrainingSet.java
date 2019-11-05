package com.example.swimmaster.TrainingSetBuilder;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.swimmaster.SingleWorkout.SingleWorkout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.example.swimmaster.MainMenuActivity.mTrainingSetWorkoutsListName;

public class TrainingSet implements TrainningSetBuilder {

    private static final TrainingSet ourInstance = new TrainingSet();
    private boolean[] mDays = new boolean[7];
    private int mWeeks;
    private int mStartingDistance;
    private boolean[] mAdditions = new boolean[6];
    private List<String> mTrainingDatesString = null;
    private List<SingleWorkout> mTraining = null;
    private HashSet<CalendarDay> mTrainingDates = null;
    public static final String TRAINING_NAME = "SM Generated Training";

    public static TrainingSet getInstance() {
        return ourInstance;
    }

    private TrainingSet() {
    }

    public TrainingSet getTrainingSet() {
        return ourInstance;
    }

    public HashSet<CalendarDay> getTrainingDatesCalendarDay() {
        return mTrainingDates;
    }

    public List<String> getTrainingDatesString() {
        return mTrainingDatesString;
    }

    @Override
    public void resetTraining() {
        mDays = null;
        mWeeks = 0;
        mStartingDistance = 0;
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
    public void setAdditions(boolean[] additions) {
        mAdditions = additions;
    }

    @Override
    public void setStartingDistance(int startingDistance) {
        mStartingDistance = startingDistance;
    }

    @Override
    public TrainingSet generateTraining() {

        mTraining = new LinkedList<>();
        mTrainingDatesString = generateDates(mDays, mWeeks);
        mTrainingDates = toCalendarDay(mTrainingDatesString);

        // ToDo: Dopisać tworzenie treningu
        int i = 0;
        for (String date : mTrainingDatesString) {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8, 10));
            String name = nameOfDay(day, month, year);

            String myFormat = "dd MM yyyy hh:mm:ss ";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
            String creation_date = sdf.format(Calendar.getInstance().getTime());

            i++;
            SingleWorkout singleWorkorkout = new SingleWorkout();
            singleWorkorkout.setName(TRAINING_NAME + " " + i);

            String genDate = name + ", ";
            if (day < 10) {
                genDate += "0";
            }
            genDate += day + "/";
            if (month < 10) {
                genDate += "0";
            }
            genDate += month + "/" + year;
            singleWorkorkout.setDate(genDate);
            singleWorkorkout.setListName(mTrainingSetWorkoutsListName);
            singleWorkorkout.setCreationDate(creation_date);

            mTraining.add(singleWorkorkout);
        }


        return getInstance();
    }

    private List<String> generateDates(boolean[] days, int numberOfWeeks) {
        List dates = new LinkedList<String>();

        CalendarDay today = CalendarDay.today();
        int day = today.getDay();
        int month = today.getMonth();
        int year = today.getYear();

        /* adjust months so February is the last one */
        int m = month - 2;
        int y = year;
        if (m < 1) {
            m += 12;
            --y;
        }
        /* split by century */
        int cent = y / 100;
        y %= 100;
        int dayOfWeek = ((26 * m - 2) / 10 + day + y + y / 4 + cent / 4 + 5 * cent) % 7;

        int delta = 0;
        switch (dayOfWeek) {
            case 1: {
                delta = 0;
                break;
            }
            case 2: {
                delta = 6;
                break;
            }
            case 3: {
                delta = 5;
                break;
            }
            case 4: {
                delta = 4;
                break;
            }
            case 5: {
                delta = 3;
                break;
            }
            case 6: {
                delta = 2;
                break;
            }
            case 0: {
                delta = 1;
                break;
            }
            default: {
                delta = 0;
                break;
            }
        }

        int d = 0;
        int genDay = 0;
        int genMon = 0;
        int genYear = 0;


        for (boolean dzien : days) {
            if (dzien) {
                genYear = year;
                genMon = month;
                for (int i = 0; i < numberOfWeeks; i++) {

                    genDay = day + delta + 7 * i + d;
                    YearMonth ymObject = YearMonth.of(genYear, genMon);
                    int maxDayOfMonth = ymObject.lengthOfMonth();

                    if (genDay > maxDayOfMonth) {
                        genDay -= maxDayOfMonth;
                        genMon++;
                    }

                    if (genMon > 12) {
                        genMon -= 12;
                        genYear++;
                    }

                    String date = genYear + "-";
                    if (genMon < 10) {
                        date += "0";
                    }
                    date += genMon + "-";
                    if (genDay < 10) {
                        date += "0";
                    }
                    date += genDay;
                    System.out.println(date);
                    dates.add(date);
                }
            }
            d++;
        }
        Collections.sort(dates);
        return dates;
    }

    private HashSet<CalendarDay> toCalendarDay(List<String> datesOfTrainings) {
        HashSet datesOfTrainingHS = new HashSet<CalendarDay>();
        for (String date : datesOfTrainings) {
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(5, 7));
            int day = Integer.parseInt(date.substring(8, 10));
            CalendarDay dateCD = CalendarDay.from(year, month, day);
            datesOfTrainingHS.add(dateCD);
        }
        return datesOfTrainingHS;
    }

    public void uploadData(final Activity activity, DatabaseReference databaseReference) {
        databaseReference.setValue(mTraining)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(activity, "Training set was successfully created!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity, "Training set was not created!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String nameOfDay(int day, int month, int year) {
        String name = "";
        /* adjust months so February is the last one */
        int m = month - 2;
        int y = year;
        if (m < 1) {
            m += 12;
            --y;
        }
        /* split by century */
        int cent = y / 100;
        y %= 100;
        int dayOfWeek = ((26 * m - 2) / 10 + day + y + y / 4 + cent / 4 + 5 * cent) % 7;

        switch (dayOfWeek) {
            case 1: {
                name = "Monday";
                break;
            }
            case 2: {
                name = "Tuesday";
                break;
            }
            case 3: {
                name = "Wednesday";
                break;
            }
            case 4: {
                name = "Thursday";
                break;
            }
            case 5: {
                name = "Friday";
                break;
            }
            case 6: {
                name = "Saturday";
                break;
            }
            case 0: {
                name = "Sunday";
                break;
            }
            default: {
                name = "Sunday";
                break;
            }
        }
        return name;
    }
}
