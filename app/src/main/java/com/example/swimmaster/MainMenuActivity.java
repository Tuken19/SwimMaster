package com.example.swimmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainMenuActivity extends AppCompatActivity {

    Button signOutButton;
    RelativeLayout singleWorkout;
    RelativeLayout trainingPlan;
    RelativeLayout trainingLog;
    RelativeLayout timesLog;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        signOutButton = findViewById(R.id.sign_out);
        singleWorkout = findViewById(R.id.SingleWorkout);
        trainingPlan = findViewById(R.id.TrainingPlan);
        trainingLog = findViewById(R.id.TrainingLog);
        timesLog = findViewById(R.id.TimesLog);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ========== Sign out ==========
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_out:
                        signOut();
                        break;
                }
            }
        });
        // ==============================

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
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
        // ===== Times Log =====
        timesLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent timesLogIntent = new Intent(MainMenuActivity.this, TimesLogActivity.class);
                startActivity(timesLogIntent);
            }
        });
        // =====================
        // ==============================================
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainMenuActivity.this, "Signed out!", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }
}
