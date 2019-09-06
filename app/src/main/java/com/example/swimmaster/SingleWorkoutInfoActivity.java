package com.example.swimmaster;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.swimmaster.TrainingLogActivity.W_KEY;

public class SingleWorkoutInfoActivity extends AppCompatActivity {

    private final static String TAG = "SingleWorkoutInfoActivity";
    private DatabaseReference mDatabase;
    private long maxSWId = 0;
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;

    ActionBar actionBar;
    ImageButton profileButton;

    SingleWorkout currentSingleWorkout;

    TextView nameField;
    TextView dateField;

    FloatingActionButton addTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_workout_info);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeFields();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long pos = -1;
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    pos = extras.getLong(W_KEY);
                }

                for (DataSnapshot element : dataSnapshot.getChildren()) {
                    SingleWorkout singleWorkout = element.getValue(SingleWorkout.class);
                    if(singleWorkout.getPosition() == pos){
                        currentSingleWorkout = singleWorkout;
                        nameField.setText(currentSingleWorkout.getName());
                        dateField.setText(currentSingleWorkout.getDate());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("SingleWorkouts");

        nameField = findViewById(R.id.name_field);
        dateField = findViewById(R.id.date_field);

        addTasks = findViewById(R.id.add_tasks);
    }
}
