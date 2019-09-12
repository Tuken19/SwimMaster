package com.example.swimmaster;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrainingPlanActivity extends AppCompatActivity {

    private final static String TAG = "TrainingPlanActivity";
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;

    ActionBar actionBar;
    ImageButton profileButton;

    Button generateTraining;

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
    }
}
