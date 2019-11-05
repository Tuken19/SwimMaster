package com.example.swimmaster;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.swimmaster.MainMenuActivity.mDatabaseTimes;

public class TimerActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final static String TAG = "TimerActivity";
    private long maxId = 0;

    ActionBar actionBar;
    ImageButton profileButton;

    RadioButton shortPool;
    RadioButton longPool;

    Spinner spinnerStyles;
    ArrayAdapter<CharSequence> adapterStyles;

    Spinner spinnerDistances;
    ArrayAdapter<CharSequence> adapterDistances;

    TextView txtTimer;
    Button startButton;
    Button stopButton;
    Button lapButton;
    Button resetButton;
    Button saveButton;

    Handler customHandler = new Handler();
    Runnable updateTimerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updateTime = timeSwapBuff + timeInMilliseconds;
            int milliseconds = (int) (updateTime / 10);
            int secs = (int) (updateTime / 1000);
            int mins = (secs / 60);
            milliseconds %= 100;
            secs %= 60;
            txtTimer.setText("" + String.format("%02d", mins) + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%02d", milliseconds));
            customHandler.postDelayed(updateTimerThread, 0);
        }
    };
    private long startTime = 0L;
    private long timeInMilliseconds = 0L;
    private long timeSwapBuff = 0L;
    private long updateTime = 0L;

    private ListView list;
    ArrayAdapter<String> listAdapter;
    ArrayList<String> timesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        initializeFields();

        mDatabaseTimes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    maxId = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        timesList = new ArrayList<String>();

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, timesList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                text1.setTextAppearance(R.style.basic_text);
                text2.setTextAppearance(R.style.description);

                int lapCounter = position + 1;
                int distance;

                if (shortPool.isChecked()) {
                    distance = lapCounter * 25;
                } else if (longPool.isChecked()) {
                    distance = lapCounter * 50;
                } else {
                    distance = 0;
                }

                text1.setText(timesList.get(position));
                text2.setText("Lap " + lapCounter + "\t\tDistance: " + distance + " m");
                return view;
            }
        };
        list.setAdapter(listAdapter);


        // ===== Profile =====
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(TimerActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
        // ===================

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    startButton.setEnabled(false);
                    startButton.setVisibility(View.INVISIBLE);
                    stopButton.setEnabled(true);
                    stopButton.setVisibility(View.VISIBLE);
//                    lapButton.setEnabled(true);
//                    lapButton.setVisibility(View.VISIBLE);
                    resetButton.setEnabled(false);
//                    resetButton.setVisibility(View.INVISIBLE);
                    saveButton.setEnabled(false);

                    shortPool.setEnabled(false);
                    longPool.setEnabled(false);

                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                } else {
                    Toast.makeText(TimerActivity.this, "Please choose a pool length.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton.setEnabled(true);
                startButton.setVisibility(View.VISIBLE);
                stopButton.setEnabled(false);
                stopButton.setVisibility(View.INVISIBLE);
//                lapButton.setEnabled(false);
//                lapButton.setVisibility(View.INVISIBLE);
                resetButton.setEnabled(true);
                resetButton.setVisibility(View.VISIBLE);
                saveButton.setEnabled(true);

                timesList.add(txtTimer.getText().toString());
                listAdapter.notifyDataSetChanged();

                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
            }
        });

        lapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timesList.add(txtTimer.getText().toString());
                listAdapter.notifyDataSetChanged();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateForm()) {
                    final Time time = new Time();
                    time.setPosition(maxId);
                    time.setStyle(spinnerStyles.getSelectedItem().toString());
                    time.setDistance(spinnerDistances.getSelectedItem().toString());
                    time.setTime(timesList.get(timesList.size() - 1));

                    final Date date = Calendar.getInstance().getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                    time.setDate(sdf.format(date));

                    mDatabaseTimes.child(String.valueOf(maxId)).setValue(time)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.e(TAG, "Add timer success. Date: " + date.toString() + "Time: " + time.getTime());
                                    Toast.makeText(TimerActivity.this, "You have successfully added a time.", Toast.LENGTH_SHORT).show();
                                    reset();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Add timer failed. Date: " + date.toString() + "Time: " + time.getTime());
                                    Toast.makeText(TimerActivity.this, "Time was not added. Try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void initializeFields() {
        // ========== Custom Action Bar ==========
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(actionBarView);
        // =======================================

        profileButton = findViewById(R.id.profile);

        shortPool = findViewById(R.id.shortPool);
        longPool = findViewById(R.id.longPool);

        // ============ Spinner Styles ============
        spinnerStyles = findViewById(R.id.spinner_styles);
        adapterStyles = ArrayAdapter.createFromResource(this, R.array.swimming_styles, R.layout.custom_spinner);
        adapterStyles.setDropDownViewResource(R.layout.custom_spinner);
        spinnerStyles.setAdapter(adapterStyles);
        spinnerStyles.setOnItemSelectedListener(this);
        // =======================================

        // ========== Spinner Distances ==========
        spinnerDistances = findViewById(R.id.spinner_distance);
        adapterDistances = ArrayAdapter.createFromResource(this, R.array.distances, R.layout.custom_spinner);
        adapterDistances.setDropDownViewResource(R.layout.custom_spinner);
        spinnerDistances.setAdapter(adapterDistances);
        spinnerDistances.setOnItemSelectedListener(this);
        // =======================================

        txtTimer = findViewById(R.id.timer);
        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);
        lapButton = findViewById(R.id.lap);
        resetButton = findViewById(R.id.reset);
        saveButton = findViewById(R.id.save);

        list = findViewById(R.id.timeList);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private boolean validate() {
        return ((shortPool.isChecked() || longPool.isChecked()));
    }

    private boolean validateForm() {
        boolean valid = true;

        if (spinnerStyles.getSelectedItemPosition() == 0) {
            String err = "Please choose correct style from list.";
            Toast.makeText(TimerActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (spinnerDistances.getSelectedItemPosition() == 0) {
            String err = "Please choose correct distance from list.";
            Toast.makeText(TimerActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    private void reset(){
        startButton.setEnabled(true);
        startButton.setVisibility(View.VISIBLE);
        stopButton.setEnabled(false);
        stopButton.setVisibility(View.INVISIBLE);
//                lapButton.setEnabled(false);
//                lapButton.setVisibility(View.VISIBLE);
        resetButton.setEnabled(false);
//                resetButton.setVisibility(View.INVISIBLE);
        saveButton.setEnabled(false);

        shortPool.setEnabled(true);
        longPool.setEnabled(true);

        listAdapter.clear();

        updateTime = 0L;
        timeSwapBuff = 0L;
        timeInMilliseconds = 0L;
        txtTimer.setText("00:00:00");
    }
}
