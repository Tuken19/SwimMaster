package com.example.swimmaster;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swimmaster.calendarDecorators.EventDecorator;
import com.example.swimmaster.calendarDecorators.TodaysDecorator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.HashSet;

public class TrainingPlanActivity extends AppCompatActivity {

    private final static String TAG = "TrainingPlanActivity";
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;

    ActionBar actionBar;
    ImageButton profileButton;

    MaterialCalendarView calendarView;
    Button generateTraining;

    View background;
    View popupView;
    PopupWindow popupWindow;
    CheckBox monCheckBox;
    CheckBox tueCheckBox;
    CheckBox wedCheckBox;
    CheckBox thuCheckBox;
    CheckBox friCheckBox;
    CheckBox satCheckBox;
    CheckBox sunCheckBox;
    EditText editWeeks;
    EditText editDistance;

    Button generate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_plan);

        initializeFields();

        // ===== Profile =====
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(TrainingPlanActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
        // ===================

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    generateTraining.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // ========== Calendar initialising ==========
        CalendarDay today = CalendarDay.today();
        HashSet<CalendarDay> days = new HashSet<>();
        days.add(today);
        Drawable drawable = getDrawable(R.drawable.calendar_today_drawable);
        TodaysDecorator decorator = new TodaysDecorator(drawable, days);

        calendarView.addDecorator(decorator);
        // ===========================================

        // ========== Training generating ==========
        generateTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.setFocusable(true);
                popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                popupWindow.showAtLocation(background, Gravity.BOTTOM, 0, 0);

                generate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (true) { // ToDo: zamienić na validate()
                            popupWindow.dismiss();
                        }
                        else {
                            Toast.makeText(TrainingPlanActivity.this, "Please provide all information.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                CalendarDay calendarDay = CalendarDay.today();
                HashSet<CalendarDay> calendarDays = new HashSet<>();
                calendarDays.add(calendarDay);
                EventDecorator eventDecorator = new EventDecorator(getResources().getColor(R.color.secondary), calendarDays);

                calendarView.addDecorator(eventDecorator);
            }
        });
        // =========================================


        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Toast.makeText(TrainingPlanActivity.this, "" + date + " " + selected, Toast.LENGTH_SHORT).show();
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

        profileButton = findViewById(R.id.profile);

        mAuth = FirebaseAuth.getInstance();
        mFBUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("TrainingPlan");

        generateTraining = findViewById(R.id.generate_training);

        calendarView = findViewById(R.id.calendar_view);

        // ====== Add Generate Training popup =======
        background = findViewById(R.id.background);
        popupView = layoutInflater.inflate(R.layout.generate_training_popup, null);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        monCheckBox = popupView.findViewById(R.id.monday);
        tueCheckBox = popupView.findViewById(R.id.tuesday);
        wedCheckBox = popupView.findViewById(R.id.wednesday);
        thuCheckBox = popupView.findViewById(R.id.thursday);
        friCheckBox = popupView.findViewById(R.id.friday);
        satCheckBox = popupView.findViewById(R.id.saturday);
        sunCheckBox = popupView.findViewById(R.id.sunday);

        editWeeks = popupView.findViewById(R.id.edit_weeks);
        editDistance = popupView.findViewById(R.id.edit_distance);

        generate = popupView.findViewById(R.id.generate);
        // =======================================
    }

    private boolean validate() {
        return (monCheckBox.isChecked() |
                tueCheckBox.isChecked() |
                wedCheckBox.isChecked() |
                thuCheckBox.isChecked() |
                friCheckBox.isChecked() |
                satCheckBox.isChecked() |
                sunCheckBox.isChecked()) &&
                !editWeeks.getText().toString().equals("") &&
                !editDistance.getText().toString().equals("");
    }

}
