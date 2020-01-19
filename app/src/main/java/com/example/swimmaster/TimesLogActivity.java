package com.example.swimmaster;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.swimmaster.ui.main.SectionsPagerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.example.swimmaster.MainMenuActivity.mDatabaseTimes;
import static com.example.swimmaster.MainMenuActivity.personPhoto;

public class TimesLogActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final static String TAG = "TimesLogActivity";
    private long maxId = 0;

    ActionBar actionBar;
    ImageButton profileButton;

    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;

    FloatingActionButton addTimer;
    FloatingActionButton addTime;

    View background;
    View popupView;
    PopupWindow popupWindow;

    Spinner spinnerStyles;
    ArrayAdapter<CharSequence> adapterStyles;
    Spinner spinnerDistance;
    ArrayAdapter<CharSequence> adapterDistance;

    EditText editTime;

    Calendar dateCalendar;
    EditText editDate;
    DatePickerDialog.OnDateSetListener date;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_times_log);

        initializeFields();

        mDatabaseTimes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    maxId = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // =========== Profile ===========
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(TimesLogActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
        // ===============================

        // ========== Add Timer ==========
        addTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent timerIntent = new Intent(TimesLogActivity.this, TimerActivity.class);
                startActivity(timerIntent);
            }
        });
        // ===============================

        // ========== Add Time ===========
        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                popupView = layoutInflater.inflate(R.layout.add_time_popup, null);
                popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                Button saveButton = popupView.findViewById(R.id.saveButton);

                // =========== Spinner styles ===========
                spinnerStyles = popupView.findViewById(R.id.spinner_styles);
                adapterStyles = ArrayAdapter.createFromResource(popupView.getContext(), R.array.swimming_styles, R.layout.custom_spinner);
                adapterStyles.setDropDownViewResource(R.layout.custom_spinner);
                spinnerStyles.setAdapter(adapterStyles);
                spinnerStyles.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) popupView.getContext());
                // ========================================

                // =========== Spinner distance ===========
                spinnerDistance = popupView.findViewById(R.id.spinner_distance);
                adapterDistance = ArrayAdapter.createFromResource(popupView.getContext(), R.array.distances, R.layout.custom_spinner);
                adapterDistance.setDropDownViewResource(R.layout.custom_spinner);
                spinnerDistance.setAdapter(adapterDistance);
                spinnerDistance.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) popupView.getContext());
                // =======================================

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        addTime.setEnabled(true);
                        addTimer.setEnabled(true);
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAtLocation(background, Gravity.BOTTOM, 0, 0);


                // ============= Date Picker =============
                dateCalendar = Calendar.getInstance();
                date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        dateCalendar.set(Calendar.YEAR, year);
                        dateCalendar.set(Calendar.MONTH, month);
                        dateCalendar.set(Calendar.DAY_OF_MONTH, day);

                        String myFormat = "dd/MM/yyyy";
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

                        editDate.setText(sdf.format(dateCalendar.getTime()));
                    }
                };

                editDate = popupView.findViewById(R.id.editDate);
                editDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DatePickerDialog(TimesLogActivity.this, date,
                                dateCalendar.get(Calendar.YEAR),
                                dateCalendar.get(Calendar.MONTH),
                                dateCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    }
                });
                // =======================================

                // ============= Time Picker =============
                editTime = popupView.findViewById(R.id.editTime);
                editTime.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        LinearLayout LL = new LinearLayout(TimesLogActivity.this);
                        LL.setOrientation(LinearLayout.HORIZONTAL);

                        final NumberPicker minuteNumberPicker = new NumberPicker(TimesLogActivity.this);
                        minuteNumberPicker.setMaxValue(59);
                        minuteNumberPicker.setMinValue(0);

                        final NumberPicker secondNumberPicker = new NumberPicker(TimesLogActivity.this);
                        secondNumberPicker.setMaxValue(59);
                        secondNumberPicker.setMinValue(0);

                        final NumberPicker millisecondNumberPicker = new NumberPicker(TimesLogActivity.this);
                        millisecondNumberPicker.setMaxValue(99);
                        millisecondNumberPicker.setMinValue(0);


                        LinearLayout.LayoutParams LLparams = new LinearLayout.LayoutParams(50, 50);
                        LLparams.gravity = Gravity.CENTER;

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.weight = 1;

                        LL.setLayoutParams(LLparams);
                        LL.addView(minuteNumberPicker, params);
                        LL.addView(secondNumberPicker, params);
                        LL.addView(millisecondNumberPicker, params);


                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TimesLogActivity.this);
                        alertDialogBuilder.setTitle("Select time:");
                        alertDialogBuilder.setView(LL);
                        alertDialogBuilder
                                .setCancelable(false)
                                .setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                String time = String.format("%02d", minuteNumberPicker.getValue()) +
                                                        ":" + String.format("%02d", secondNumberPicker.getValue()) +
                                                        ":" + String.format("%02d", millisecondNumberPicker.getValue());

                                                editTime.setText(time);

                                                Log.e(TAG, "Time: " + time);

                                            }
                                        })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
                // =======================================

                // ========== Save button ==========
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(validateForm()){
                            Time time = new Time();
                            time.setPosition(maxId);
                            time.setStyle(spinnerStyles.getSelectedItem().toString());
                            time.setDistance(spinnerDistance.getSelectedItem().toString());
                            time.setTime(editTime.getText().toString());
                            time.setDate(editDate.getText().toString());
                            mDatabaseTimes.child(String.valueOf(maxId)).setValue(time);

                            popupWindow.dismiss();
                        }
                    }
                });

                // =================================
            }
        });
        // ===============================================
    }

    private void initializeFields() {
        // ========== Custom Action Bar ==========
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setElevation(0);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(actionBarView);

        profileButton = findViewById(R.id.profile);
        // =======================================

        // ================ Tabs =================
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        // =======================================

        // =========== Add time popup ============
        background = findViewById(R.id.background);
        // =======================================

        addTimer = findViewById(R.id.addTimer);
        addTime = findViewById(R.id.addTime);

        MainMenuActivity.downloadPhoto(profileButton, personPhoto, this);
    }

    private boolean validateForm() {
        boolean valid = true;

        if (spinnerStyles.getSelectedItemPosition() == 0) {
            String err = "Please choose correct style from list.";
            Toast.makeText(TimesLogActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (spinnerDistance.getSelectedItemPosition() == 0) {
            String err = "Please choose correct distance from list.";
            Toast.makeText(TimesLogActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        String time = editTime.getText().toString();
        if (TextUtils.isEmpty(time)) {
            editTime.setError("Required.");
            valid = false;
        } else {
            editTime.setError(null);
        }

        String date = editDate.getText().toString();
        if (TextUtils.isEmpty(date)) {
            editDate.setError("Required.");
            valid = false;
        } else {
            editDate.setError(null);
        }

        return valid;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}