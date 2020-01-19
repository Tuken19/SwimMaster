package com.example.swimmaster.TrainingSetBuilder;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.swimmaster.SingleWorkout.SingleWorkout;
import com.example.swimmaster.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.example.swimmaster.MainMenuActivity.mTrainingSetWorkoutsListName;

public class TrainingSet implements TrainningSetBuilder {

    private static final String TAG = "TrainingSet";
    private static final TrainingSet ourInstance = new TrainingSet();
    private boolean[] mDays = new boolean[7];
    private int mWeeks;
    private int mStartingDistance;
    private boolean[] mStrokes = new boolean[4];
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
    public void setStrokes(boolean[] strokes){
        mStrokes = strokes;
    }

    @Override
    public void setAdditions(boolean[] additions) {
        mAdditions = additions;
    }

    @Override
    public void setStartingDistance(int startingDistance) {
        if (startingDistance < 500){
            mStartingDistance = 500;
        }
        else {
            mStartingDistance = startingDistance - startingDistance % 500;
        }
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
            String creation_date = sdf.format(Calendar.getInstance().getTime()) + " " + i;

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

            int fullDistance = mStartingDistance + i % mTrainingDates.size()/mWeeks * 200;
            fullDistance = setWarmUp(singleWorkorkout, fullDistance);
            fullDistance = setCooldown(singleWorkorkout, fullDistance);
            setMainSet(singleWorkorkout, fullDistance, i);
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
        int m = month-2;
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
            genYear = year;
            genMon = month;
            genDay = day - 7 + delta + d;
            if (dzien) {
                for (int i = 0; i < numberOfWeeks; i++) {

                    genDay = genDay + 7;
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
                        Log.i(TAG, "Training set was successfully uploaded!");
                        Toast.makeText(activity, "Training set was successfully uploaded!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Training set was not uploaded!");
                        Toast.makeText(activity, "Training set was not uploaded!", Toast.LENGTH_SHORT).show();
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

    private int setWarmUp(SingleWorkout singleWorkout, int fullDistance){
        List<Task> warmUp = new ArrayList<>();

        List<String> additions = new ArrayList<>();
        int warmUpDistance = 0;

        if (fullDistance >= 0 && fullDistance <= 1000){
            warmUpDistance = 100;

            if(mStrokes[1]){
                Task t1 = new Task("Freestyle", "50", "Swim", "Moderate", 1, 10, additions);
                Task t2 = new Task("Backstroke", "50", "Swim", "Moderate", 1, 10, additions);
                warmUp.add(t1);
                warmUp.add(t2);
            }
            else if (mStrokes[2]){
                Task t1 = new Task("Freestyle", "50", "Swim", "Moderate", 1, 10, additions);
                Task t2 = new Task("Breaststroke", "50", "Swim", "Moderate", 1, 10, additions);
                warmUp.add(t1);
                warmUp.add(t2);
            }
            else {
                Task t1 = new Task("Freestyle", "50", "Swim", "Moderate", 2, 10, additions);
                warmUp.add(t1);
            }
        }
        else {
            warmUpDistance = 300;

            if(mStrokes[1]){
                Task t1 = new Task("Freestyle", "100", "Swim", "Moderate", 1, 10, additions);
                Task t2 = new Task("Backstroke", "100", "Swim", "Moderate", 1, 10, additions);
                warmUp.add(t1);
                warmUp.add(t2);
                warmUp.add(t1);
            }
            else if (mStrokes[2]){
                Task t1 = new Task("Freestyle", "100", "Swim", "Moderate", 1, 10, additions);
                Task t2 = new Task("Breaststroke", "100", "Swim", "Moderate", 1, 10, additions);
                warmUp.add(t1);
                warmUp.add(t2);
                warmUp.add(t1);
            }
            else {
                Task t1 = new Task("Freestyle", "100", "Swim", "Moderate", 3, 10, additions);
                warmUp.add(t1);
            }
        }

        singleWorkout.setWarmUp(warmUp);
        return fullDistance - warmUpDistance;
    }

    private int setCooldown(SingleWorkout singleWorkout, int fullDistance){
        List<Task> cooldown = new ArrayList<>();

        List<String> additions = new ArrayList<>();
        int cooldownDistance = 0;

        if (fullDistance >= 0 && fullDistance <= 1000){
            cooldownDistance = 100;

            if(mStrokes[1]){
                Task t1 = new Task("Freestyle", "50", "Swim", "Moderate", 1, 10, additions);
                Task t2 = new Task("Backstroke", "50", "Swim", "Moderate", 1, 10, additions);
                cooldown.add(t1);
                cooldown.add(t2);
            }
            else if (mStrokes[2]){
                Task t1 = new Task("Freestyle", "50", "Swim", "Moderate", 1, 10, additions);
                Task t2 = new Task("Breaststroke", "50", "Swim", "Moderate", 1, 10, additions);
                cooldown.add(t1);
                cooldown.add(t2);
            }
            else {
                Task t1 = new Task("Freestyle", "50", "Swim", "Moderate", 2, 10, additions);
                cooldown.add(t1);
            }
        }
        else {
            cooldownDistance = 200;

            if(mStrokes[1]){
                Task t1 = new Task("Freestyle", "100", "Swim", "Slow", 1, 10, additions);
                Task t2 = new Task("Backstroke", "100", "Swim", "Slow", 1, 10, additions);
                cooldown.add(t1);
                cooldown.add(t2);
            }
            else if (mStrokes[2]){
                Task t1 = new Task("Freestyle", "100", "Swim", "Slow", 1, 10, additions);
                Task t2 = new Task("Breaststroke", "100", "Swim", "Slow", 1, 10, additions);
                cooldown.add(t1);
                cooldown.add(t2);
            }
            else {
                Task t1 = new Task("Freestyle", "100", "Swim", "Slow", 2, 10, additions);
                cooldown.add(t1);
            }
        }

        singleWorkout.setCooldown(cooldown);
        return fullDistance - cooldownDistance;
    }

    private void setMainSet(SingleWorkout singleWorkout, int fullDistance, int trainingNumber){
        List<Task> mainSet = new ArrayList<>();
        List<String> additions = new ArrayList<>();
        List<String> legAdditions = new ArrayList<>();
        List<String> armAdditions = new ArrayList<>();

        legAdditions.add("Legs only");
        if(mAdditions[1]) {
            legAdditions.add("Kick board");
        }
        if(mAdditions[2]){
            legAdditions.add("Fins");
        }

        armAdditions.add("Arms only");
        if(mAdditions[4]) {
            legAdditions.add("Pull buoy");
        }
        if(mAdditions[5]){
            legAdditions.add("Paddles");
        }

        // ========== Training Dictionary ==========
        // Medley task
        Task tm0 = new Task("Medley", "100", "Swim", "Moderate", 1, 10, additions);

        // Pull Fl
        Task tp0 = new Task("Butterfly", "50", "Pull", "Moderate", 1, 20, additions);
        // Pull Bk
        Task tp1 = new Task("Backstroke", "50", "Pull", "Moderate", 1, 20, additions);
        // Pull Br
        Task tp2 = new Task("Breaststroke", "50", "Pull", "Moderate", 1, 20, additions);
        // Pull Fr
        Task tp3 = new Task("Freestyle", "50", "Pull", "Moderate", 1, 20, additions);

        // Kick Fl
        Task tk0 = new Task("Butterfly", "50", "Kick", "Moderate", 1, 20, additions);
        // Kick Bk
        Task tk1 = new Task("Backstroke", "50", "Kick", "Moderate", 1, 20, additions);
        // Kick Br
        Task tk2 = new Task("Breaststroke", "50", "Kick", "Moderate", 1, 20, additions);
        // Kick Fr
        Task tk3 = new Task("Freestyle", "50", "Kick", "Moderate", 1, 20, additions);

        // Swim Fl
        Task tf0 = new Task("Butterfly", "50", "Swim", "Fast", 1, 20, additions);
        // Swim Bk
        Task tf1 = new Task("Backstroke", "50", "Swim", "Fast", 1, 20, additions);
        // Swim Br
        Task tf2 = new Task("Breaststroke", "50", "Swim", "Fast", 1, 20, additions);
        // Swim Fr
        Task tf3 = new Task("Freestyle", "50", "Swim", "Fast", 1, 20, additions);

        // Sprint Fl 25s
        Task ts0 = new Task("Butterfly", "25", "Swim", "Sprint", 1, 30, additions);
        // Sprint Bk 25s
        Task ts1 = new Task("Backstroke", "25", "Swim", "Sprint", 1, 30, additions);
        // Sprint Br 25s
        Task ts2 = new Task("Breaststroke", "25", "Swim", "Sprint", 1, 30, additions);
        // Sprint Fr 25s
        Task ts3 = new Task("Freestyle", "25", "Swim", "Sprint", 1, 30, additions);
        // =========================================

        if(trainingNumber < (5 * getTrainingDatesString().size()) / 10){
            // Distance training
            if(mStrokes[0] && mStrokes[1] && mStrokes[2]){
                if(fullDistance == 300){
                    Task t1 = tf1;
                    Task t2 = tf2;
                    t1.setDistance("100");
                    t2.setDistance("100");

                    mainSet.add(t1);
                    mainSet.add(t2);
                    mainSet.add(tm0);
                }
                else {
                    Task t1 = tf1;
                    Task t2 = tf2;
                    t1.setDistance("100");
                    t2.setDistance("100");
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t1.setRepetitions(rep);
                    t2.setRepetitions(rep);

                    mainSet.add(tm0);
                    mainSet.add(t1);
                    mainSet.add(tm0);
                    mainSet.add(t2);
                    mainSet.add(tm0);
                }
            }
            else if(mStrokes[1] && mStrokes[2]){
                if(fullDistance == 300){
                    Task t1 = tf1;
                    Task t2 = tf2;
                    Task t3 = tf3;
                    t1.setDistance("100");
                    t2.setDistance("100");
                    t3.setDistance("100");

                    mainSet.add(t1);
                    mainSet.add(t2);
                    mainSet.add(t3);
                }
                else {
                    Task t1 = tf1;
                    Task t2 = tf2;
                    Task t3 = tf3;
                    t2.setDistance("100");
                    t3.setDistance("100");
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t1.setRepetitions(rep);
                    t2.setRepetitions(rep);

                    mainSet.add(t3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(t2);
                    mainSet.add(t3);
                }
            }
            else if(mStrokes[1]){
                if(fullDistance == 300){
                    Task t1 = tf1;
                    Task t3 = tf3;
                    t1.setDistance("100");
                    t3.setDistance("100");

                    mainSet.add(t3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                }
                else {
                    Task t1 = tf1;
                    Task t3 = tf3;
                    t1.setDistance("100");
                    t3.setDistance("100");
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t1.setRepetitions(rep);
                    t3.setRepetitions(rep);

                    mainSet.add(t3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                }
            }
            else if(mStrokes[2]){
                if(fullDistance == 300){
                    Task t2 = tf2;
                    Task t3 = tf3;
                    t2.setDistance("100");
                    t3.setDistance("100");

                    mainSet.add(t3);
                    mainSet.add(t2);
                    mainSet.add(t3);
                }
                else {
                    Task t2 = tf2;
                    Task t3 = tf3;
                    t2.setDistance("100");
                    t3.setDistance("100");
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t2.setRepetitions(rep);
                    t3.setRepetitions(rep);

                    mainSet.add(t3);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(t2);
                    mainSet.add(t3);
                }
            }
        }
        else if (trainingNumber < (3 * getTrainingDatesString().size()) / 10) {
            // Power training
            if(mStrokes[0] && mStrokes[1] && mStrokes[2]){
                if(fullDistance == 300){
                    Task t1 = tk1;
                    Task t2 = tk2;
                    Task t3 = tp1;
                    Task t4 = tp2;
                    t1.setAdditions(legAdditions);
                    t2.setAdditions(legAdditions);
                    t3.setAdditions(armAdditions);
                    t4.setAdditions(armAdditions);

                    mainSet.add(t1);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(t4);
                    mainSet.add(tm0);
                }
                else {
                    Task t1 = tk1;
                    Task t2 = tk2;
                    Task t3 = tp1;
                    Task t4 = tp2;
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t1.setRepetitions(rep);
                    t2.setRepetitions(rep);
                    t3.setRepetitions(rep);
                    t4.setRepetitions(rep);
                    t1.setAdditions(legAdditions);
                    t2.setAdditions(legAdditions);
                    t3.setAdditions(armAdditions);
                    t4.setAdditions(armAdditions);

                    mainSet.add(tm0);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(tm0);
                    mainSet.add(t2);
                    mainSet.add(t4);
                    mainSet.add(tm0);
                }
            }
            else if(mStrokes[1] && mStrokes[2]){
                if(fullDistance == 300){
                    Task t1 = tk1;
                    Task t2 = tk2;
                    Task t3 = tp1;
                    Task t4 = tp2;
                    Task t5 = tf3;
                    t5.setDistance("100");
                    t1.setAdditions(legAdditions);
                    t2.setAdditions(legAdditions);
                    t3.setAdditions(armAdditions);
                    t4.setAdditions(armAdditions);

                    mainSet.add(t1);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(t4);
                    mainSet.add(t5);
                }
                else {
                    Task t1 = tk1;
                    Task t2 = tk2;
                    Task t3 = tp1;
                    Task t4 = tp2;
                    Task t5 = tf3;
                    t5.setDistance("100");
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t1.setRepetitions(rep);
                    t2.setRepetitions(rep);
                    t3.setRepetitions(rep);
                    t4.setRepetitions(rep);
                    t1.setAdditions(legAdditions);
                    t2.setAdditions(legAdditions);
                    t3.setAdditions(armAdditions);
                    t4.setAdditions(armAdditions);

                    mainSet.add(t5);
                    mainSet.add(t1);
                    mainSet.add(t2);
                    mainSet.add(t5);
                    mainSet.add(t3);
                    mainSet.add(t4);
                    mainSet.add(t5);
                }
            }
            else if(mStrokes[1]){
                if(fullDistance == 300){
                    Task t1 = tk1;
                    Task t3 = tp1;
                    Task t5 = tf3;
                    t5.setDistance("100");
                    t1.setAdditions(legAdditions);
                    t3.setAdditions(armAdditions);

                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(t5);
                }
                else {
                    Task t1 = tk1;
                    Task t3 = tp1;
                    Task t5 = tf3;
                    t5.setDistance("100");
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t1.setRepetitions(rep);
                    t3.setRepetitions(rep);
                    t1.setAdditions(legAdditions);
                    t3.setAdditions(armAdditions);

                    mainSet.add(t5);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(t5);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(t5);
                }
            }
            else if(mStrokes[2]){
                if(fullDistance == 300){
                    Task t2 = tk2;
                    Task t4 = tp2;
                    Task t5 = tf3;
                    t5.setDistance("100");
                    t2.setAdditions(legAdditions);
                    t4.setAdditions(armAdditions);

                    mainSet.add(t2);
                    mainSet.add(t4);
                    mainSet.add(t2);
                    mainSet.add(t4);
                    mainSet.add(t5);
                }
                else {
                    Task t2 = tk2;
                    Task t4 = tp2;
                    Task t5 = tf3;
                    t5.setDistance("100");
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t2.setRepetitions(rep);
                    t4.setRepetitions(rep);
                    t2.setAdditions(legAdditions);
                    t4.setAdditions(armAdditions);

                    mainSet.add(t5);
                    mainSet.add(t2);
                    mainSet.add(t4);
                    mainSet.add(t5);
                    mainSet.add(t2);
                    mainSet.add(t4);
                    mainSet.add(t5);
                }
            }
        }
        else {
            // Speed training
            if(mStrokes[0] && mStrokes[1] && mStrokes[2]){
                if(fullDistance == 300){
                    Task t0 = ts0;
                    Task t1 = ts1;
                    Task t2 = ts2;
                    Task t3 = ts3;
                    t0.setRepetitions(2);
                    t1.setRepetitions(2);
                    t2.setRepetitions(2);
                    t3.setRepetitions(2);
                    mainSet.add(t0);
                    mainSet.add(t1);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(tm0);
                }
                else {
                    Task t0 = ts0;
                    Task t1 = ts1;
                    Task t2 = ts2;
                    Task t3 = ts3;
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t0.setRepetitions(rep);
                    t1.setRepetitions(rep);
                    t2.setRepetitions(rep);
                    t3.setRepetitions(rep);

                    mainSet.add(tm0);
                    mainSet.add(t0);
                    mainSet.add(t1);
                    mainSet.add(tm0);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(tm0);
                }
            }
            else if(mStrokes[1] && mStrokes[2]){
                if(fullDistance == 300){
                    Task t1 = ts1;
                    Task t2 = ts2;
                    Task t3 = ts3;
                    t1.setRepetitions(2);
                    t2.setRepetitions(2);
                    t3.setRepetitions(2);
                    mainSet.add(t3);
                    mainSet.add(t1);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(tm0);
                }
                else {
                    Task t1 = ts1;
                    Task t2 = ts2;
                    Task t3 = ts3;
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t1.setRepetitions(rep);
                    t2.setRepetitions(rep);
                    t3.setRepetitions(rep);

                    mainSet.add(tf3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(tf3);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(tf3);
                }
            }
            else if(mStrokes[1]){
                if(fullDistance == 300){
                    Task t1 = ts1;
                    Task t3 = ts3;
                    t1.setRepetitions(2);
                    t3.setRepetitions(2);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(tm0);
                }
                else {
                    Task t1 = ts1;
                    Task t3 = ts3;
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t1.setRepetitions(rep);
                    t3.setRepetitions(rep);

                    mainSet.add(tf3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(tf3);
                    mainSet.add(t1);
                    mainSet.add(t3);
                    mainSet.add(tf3);
                }
            }
            else if(mStrokes[2]){
                if(fullDistance == 300){
                    Task t2 = ts2;
                    Task t3 = ts3;
                    t2.setRepetitions(2);
                    t3.setRepetitions(2);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(tm0);
                }
                else {
                    Task t2 = ts2;
                    Task t3 = ts3;
                    int rep = ((fullDistance - 300) / 100) / 2;
                    t2.setRepetitions(rep);
                    t3.setRepetitions(rep);

                    mainSet.add(tf3);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(tf3);
                    mainSet.add(t2);
                    mainSet.add(t3);
                    mainSet.add(tf3);
                }
            }
        }
        singleWorkout.setMainSet(mainSet);
    }
}
