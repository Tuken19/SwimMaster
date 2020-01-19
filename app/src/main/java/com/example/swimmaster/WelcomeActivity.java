package com.example.swimmaster;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    public static FirebaseAuth mAuth;
    public static FirebaseUser mFBUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        mFBUser = mAuth.getCurrentUser();

        // After 1s go to proper activity
        CountDownTimer cdt = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                updateUI(mFBUser);
            }
        }.start();
    }

    private void updateUI(FirebaseUser currentUser){
        if(currentUser != null){
            Intent mainMenuIntent = new Intent(WelcomeActivity.this, MainMenuActivity.class);
            startActivity(mainMenuIntent);
        }
        else{
            Intent signUpIntent = new Intent(WelcomeActivity.this, SignUpActivity.class);
            startActivity(signUpIntent);
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
