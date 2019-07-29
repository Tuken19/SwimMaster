package com.example.swimmaster;

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
import com.google.firebase.auth.UserProfileChangeRequest;


public class SignUpFormActivity extends AppCompatActivity {

    private static final String TAG = "SignUpFormActivity";
    private FirebaseAuth mAuth;

    EditText mNameField;
    EditText mEmailField;
    EditText mPasswordField;
    TextView mStatusTextView;

    Button signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);

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

        signUp = findViewById(R.id.sign_up);
    }

    // ========== Functions for Firebase ==========
    private void createAccount(final String name, String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        // showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Updating profile with name.
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User profile updated.");
                                            }
                                        }
                                    });

                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            mStatusTextView.setText(task.getException().getMessage());
                            Toast.makeText(SignUpFormActivity.this, "Authentication failed!",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        // hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void sendEmailVerification() {
        // Disable button
        findViewById(R.id.sign_up).setEnabled(false);

        // Send verification email
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // [START_EXCLUDE]
                        // Re-enable button
                        findViewById(R.id.sign_up).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpFormActivity.this,
                                    "Verification email sent to " + user.getEmail(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e(TAG, "sendEmailVerification", task.getException());
                            Toast.makeText(SignUpFormActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // [END_EXCLUDE]
                    }
                });
        // [END send_email_verification]
    }

    private boolean validateForm() {
        boolean valid = true;

        String name = mNameField.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mNameField.setError("Required.");
            valid = false;
        } else {
            mNameField.setError(null);
        }

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

    private void updateUI(FirebaseUser user) {
        // hideProgressDialog();
        if (user != null) {
            mStatusTextView.setText(user.getEmail() + " Verified: " + user.isEmailVerified());

            findViewById(R.id.sign_up).setVisibility(View.GONE);
            findViewById(R.id.verify).setVisibility(View.VISIBLE);

            findViewById(R.id.verify).setEnabled(!user.isEmailVerified());
        }
    }

    public void signUp(View v) {
        if (v.getId() == R.id.sign_up) {
            createAccount(mNameField.getText().toString(), mEmailField.getText().toString(), mPasswordField.getText().toString());
        }
        else if (v.getId() == R.id.verify){
            sendEmailVerification();
        }
    }
    // ============================================
}
