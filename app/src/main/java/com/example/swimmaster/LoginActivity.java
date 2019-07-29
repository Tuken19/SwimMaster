package com.example.swimmaster;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    EditText mNameField;
    EditText mEmailField;
    EditText mPasswordField;
    TextView mStatusTextView;

    Button signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeFields();
    }

    private void initializeFields() {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /// Fields and Buttons
        mNameField = findViewById(R.id.name);
        mEmailField = findViewById(R.id.email);
        mPasswordField = findViewById(R.id.password);

        mStatusTextView = findViewById(R.id.status_text_view);

        signIn = findViewById(R.id.sign_in);
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        // showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(user.isEmailVerified()) {
                                Intent mainMenuIntent = new Intent(LoginActivity.this, MainMenuActivity.class);
                                startActivity(mainMenuIntent);
                            }
                            else {
                                mStatusTextView.setText(R.string.not_verified);
                            }
                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            mStatusTextView.setText(task.getException().getMessage());
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    public void login(View v){
        if(v.getId() == R.id.sign_in){
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
    }
}
