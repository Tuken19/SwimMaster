package com.example.swimmaster;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SingleWorkoutActivity extends AppCompatActivity {

    private final static String TAG = "SingleWorkoutActivity";
    private DatabaseReference mDatabase;
    private long maxSWId = 0;
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;

    ListView listViewSingleWorkouts;
    ArrayList<SingleWorkout> arraySingleWorkout;
    SingleWorkoutAdapter arrayAdapterSingleWorkouts;
    public static final String SW_KEY = "Position of Single Workout";

    ActionBar actionBar;
    ImageButton profileButton;

    FloatingActionButton addSW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_workout);

        initializeFields();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    maxSWId = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // ===== Profile =====
        {
            profileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent = new Intent(SingleWorkoutActivity.this, ProfileActivity.class);
                    startActivity(profileIntent);
                }
            });
        }
        // ===================

        // ========== List view with workouts ==========
        {
            arraySingleWorkout = new ArrayList<SingleWorkout>();
            arrayAdapterSingleWorkouts = new SingleWorkoutAdapter(SingleWorkoutActivity.this, arraySingleWorkout);
            listViewSingleWorkouts.setAdapter(arrayAdapterSingleWorkouts);

            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    arraySingleWorkout.clear();
                    for (DataSnapshot element : dataSnapshot.getChildren()) {
                        SingleWorkout singleWorkout = element.getValue(SingleWorkout.class);
                        arraySingleWorkout.add(singleWorkout);
                        arrayAdapterSingleWorkouts.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        // =============================================

        // ========== Add new Single Workout ==========
        {
            addSW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final EditText editName = new EditText(SingleWorkoutActivity.this);
                    editName.setHint(R.string.SW_name);

                    LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nameParams.weight = 1;
                    nameParams.setMargins(50, 10, 50, 10);

                    LinearLayout LL = new LinearLayout(SingleWorkoutActivity.this);
                    LL.setOrientation(LinearLayout.VERTICAL);
                    LL.addView(editName, nameParams);

                    final SingleWorkout singleWorkout = new SingleWorkout();

                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SingleWorkoutActivity.this);
                    alertDialogBuilder
                            .setTitle("Add new single workout:")
                            .setView(LL)
                            .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    String name = editName.getText().toString();

                                    if (TextUtils.isEmpty(name)) {
                                        editName.setError("Required.");
                                    } else {
                                        singleWorkout.setPosition(maxSWId);
                                        singleWorkout.setName(name);

                                        String editDate;
                                        String myFormat = "EEEE, dd/MM/yyyy";
                                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
                                        editDate = sdf.format(Calendar.getInstance().getTime());
                                        singleWorkout.setDate(editDate);
                                    }

                                    mDatabase.child(String.valueOf(maxSWId)).setValue(singleWorkout)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    arrayAdapterSingleWorkouts.notifyDataSetChanged();
                                                    String msg = "Success: New single workout: " + singleWorkout.getName() + "Time: " + singleWorkout.getDate();
                                                    Log.e(TAG, msg);
                                                    Toast.makeText(SingleWorkoutActivity.this, "You have successfully created new workout.", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    String msg = "Failure: New single workout: " + singleWorkout.getName() + "Time: " + singleWorkout.getDate()
                                                            + "Caused by: " + e.getCause();
                                                    Log.e(TAG, msg);
                                                    Toast.makeText(SingleWorkoutActivity.this, "Creation failed. Try again.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });
        }
        // ============================================

        // ========== Single Workout Info Activity ==========
        {
            listViewSingleWorkouts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    SingleWorkout selectedSW = arraySingleWorkout.get(position);
                    long pos = selectedSW.getPosition();

                    String transitionName = getString(R.string.name_anim);
                    TextView nameField = view.findViewById(R.id.name_field);

                    String transitionDate = getString(R.string.date_anim);
                    TextView dateField = view.findViewById(R.id.date_field);

                    Pair[] pair = new Pair[2];
                    pair[0] = new Pair<View, String>(nameField, transitionName);
                    pair[1] = new Pair<View, String>(dateField, transitionDate);

                    Intent info = new Intent(SingleWorkoutActivity.this, SingleWorkoutInfoActivity.class);
                    info.putExtra(SW_KEY, pos);
                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(SingleWorkoutActivity.this, pair);
                    startActivity(info, transitionActivityOptions.toBundle());
                }
            });
        }
        // ==================================================
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


        listViewSingleWorkouts = findViewById(R.id.list_view_single_workout);

        addSW = findViewById(R.id.add_S_W);
    }
}
