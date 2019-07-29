package com.example.swimmaster;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class MainMenuActivity extends AppCompatActivity {

    ActionBar actionBar;
    ImageButton profileButton;
    RelativeLayout singleWorkout;
    RelativeLayout trainingPlan;
    RelativeLayout trainingLog;
    RelativeLayout timesLog;

    public static GoogleSignInClient mGoogleSignInClient;

    public static String personName;
    public static String personEmail;
    public static String personId;
    public static Uri personPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        initializeFields();

        // =======================================

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();
            personId = acct.getId();
            personPhoto = acct.getPhotoUrl();
        }

        // ========== Jump to other activities ==========
        // ===== Single Workout =====
        singleWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent singleWorkoutIntent = new Intent(MainMenuActivity.this, SingleWorkoutActivity.class);
                startActivity(singleWorkoutIntent);
            }
        });
        // ==========================
        // ===== Training Plan =====
        trainingPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trainingPlanIntent = new Intent(MainMenuActivity.this, TrainingPlanActivity.class);
                startActivity(trainingPlanIntent);
            }
        });
        // =========================
        // ===== Training Log =====
        trainingLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trainingLogIntent = new Intent(MainMenuActivity.this, TrainingLogActivity.class);
                startActivity(trainingLogIntent);
            }
        });
        // ========================
        // ===== TimesLogActivity Log =====
        timesLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent timesLogIntent = new Intent(MainMenuActivity.this, TimesLogActivity.class);
                startActivity(timesLogIntent);
            }
        });
        // =====================
        // ===== Profile =====
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(MainMenuActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
        // ===================
        // ==============================================
    }

    private void initializeFields(){
        singleWorkout = findViewById(R.id.SingleWorkout);
        trainingPlan = findViewById(R.id.TrainingPlan);
        trainingLog = findViewById(R.id.TrainingLog);
        timesLog = findViewById(R.id.TimesLog);

        // ========== Custom Action Bar ==========
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(actionBarView);

        profileButton = findViewById(R.id.profile);
    }
}
