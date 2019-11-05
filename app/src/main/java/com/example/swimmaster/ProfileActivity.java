package com.example.swimmaster;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static android.widget.Toast.LENGTH_SHORT;
import static com.example.swimmaster.MainMenuActivity.mAuth;
import static com.example.swimmaster.MainMenuActivity.mDatabaseHome;
import static com.example.swimmaster.MainMenuActivity.mFBUser;
import static com.example.swimmaster.MainMenuActivity.mGoogleSignInClient;

public class ProfileActivity extends AppCompatActivity {

    private final static String TAG = "ProfileActivity";
    private User mSMUser;

    ActionBar actionBar;
    ImageButton profileButton;

    ImageView mPicture;
    TextView mAccountNameField;
    EditText mEditHeightField;
    EditText mEditWeightField;
    EditText mEditAgeField;
    RadioButton mMale;
    RadioButton mFemale;
    Button submitButton;
    Button editButton;

    Button signOutButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // ========== Initialize fields ==========
        initializeFields();
        // =======================================

        mAccountNameField.setText(mFBUser.getDisplayName());


        // ========== Add or edit user in database ==========
        mSMUser = new User(mFBUser.getDisplayName(), mFBUser.getEmail());

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get User object and use the values to update the UI

                User user = dataSnapshot.getValue(User.class);

                Integer height = 0;
                Integer weight = 0;
                Integer age = 0;

                if (user.getHeight() != null) {
                    height = user.getHeight();
                }
                if (user.getWeight() != null) {
                    weight = user.getWeight();
                }
                if (user.getAge() != null) {
                    age = user.getAge();
                }

                mEditHeightField.setHint(height.toString());
                mEditWeightField.setHint(weight.toString());
                mEditAgeField.setHint(age.toString());


                if (user.getGender()) {
                    RadioButton male = findViewById(R.id.male);
                    male.toggle();
                } else {
                    RadioButton female = findViewById(R.id.female);
                    female.toggle();
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadUser:onCancelled", databaseError.toException());
                // ...
            }
        };

        mDatabaseHome.child("users").child(mFBUser.getUid()).addValueEventListener(userListener);

        // ====================================================

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

        mPicture = findViewById(R.id.picture);
        mAccountNameField = findViewById(R.id.account_name);
        mEditHeightField = findViewById(R.id.edit_height);
        mEditWeightField = findViewById(R.id.edit_weight);
        mEditAgeField = findViewById(R.id.edit_age);
        mMale = findViewById(R.id.male);
        mFemale = findViewById(R.id.female);

        submitButton = findViewById(R.id.submit);
        editButton = findViewById(R.id.edit);

        signOutButton = findViewById(R.id.sign_out);
    }

    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(ProfileActivity.this, "Signed out!", LENGTH_SHORT).show();
                        // Signed in successfully, show authenticated UI.
                        Intent signUpIntent = new Intent(ProfileActivity.this, SignUpActivity.class);
                        signUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(signUpIntent);
                        finish();
                    }
                });
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.male:
                if (checked)
                    mSMUser.setGender(true);
                Toast.makeText(ProfileActivity.this, "Male", LENGTH_SHORT).show();
                break;
            case R.id.female:
                if (checked)
                    mSMUser.setGender(false);
                Toast.makeText(ProfileActivity.this, "Female", LENGTH_SHORT).show();
                break;
        }
    }

    public void editSubmit(View view) {
        switch (view.getId()) {
            case R.id.submit: {
                try {
                    mSMUser.setHeight(Integer.parseUnsignedInt(mEditHeightField.getText().toString()));
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Please enter correct height.", LENGTH_SHORT).show();
                    break;
                }
                try {
                    mSMUser.setWeight(Integer.parseUnsignedInt(mEditWeightField.getText().toString()));
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Please enter correct weight.", LENGTH_SHORT).show();
                    break;
                }
                try {
                    mSMUser.setAge(Integer.parseUnsignedInt(mEditAgeField.getText().toString()));
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Please enter correct age.", LENGTH_SHORT).show();
                    break;
                }
                try {
                    if (mMale.isChecked()) {
                        mSMUser.setGender(true);
                    } else if (mFemale.isChecked()) {
                        mSMUser.setGender(false);
                    } else {
                        String exception = "Choose a gender";
                        throw new Exception(exception);
                    }
                } catch (Exception e) {
                    Toast.makeText(ProfileActivity.this, "Please choose a gender", LENGTH_SHORT).show();
                    break;
                }

                // Edit existing one
                mDatabaseHome.child("users").child(mFBUser.getUid()).setValue(mSMUser)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(ProfileActivity.this, "You have successfully submitted your data.", LENGTH_SHORT).show();

                                mEditHeightField.setEnabled(false);
                                mEditWeightField.setEnabled(false);
                                mEditAgeField.setEnabled(false);
                                findViewById(R.id.male).setEnabled(false);
                                findViewById(R.id.female).setEnabled(false);

                                editButton.setEnabled(true);
                                editButton.setVisibility(View.VISIBLE);
                                submitButton.setEnabled(false);
                                submitButton.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ProfileActivity.this, "Data submition failed...", LENGTH_SHORT).show();
                            }
                        });
                break;
            }
            case R.id.edit: {
                mEditHeightField.setEnabled(true);
                mEditWeightField.setEnabled(true);
                mEditAgeField.setEnabled(true);
                findViewById(R.id.male).setEnabled(true);
                findViewById(R.id.female).setEnabled(true);

                editButton.setEnabled(false);
                editButton.setVisibility(View.GONE);
                submitButton.setEnabled(true);
                submitButton.setVisibility(View.VISIBLE);

                Toast.makeText(ProfileActivity.this, "Clicked", LENGTH_SHORT).show();

                break;
            }
        }
    }
}
