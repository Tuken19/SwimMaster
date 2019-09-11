package com.example.swimmaster;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.swimmaster.MainMenuActivity.mWorkoutsList;

public class TrainingLogActivity extends AppCompatActivity {

    private final static String TAG = "TrainingLogActivity";
    private DatabaseReference mDatabase;
    private long maxSWId = 0;
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;

    ListView listViewSingleWorkouts;
    ArrayList<SingleWorkout> arraySingleWorkout;
    SingleWorkoutAdapter arrayAdapterSingleWorkouts;
    public static final String W_KEY = "Position of Workout";

    ActionBar actionBar;
    ImageButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_log);

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
                    Intent profileIntent = new Intent(TrainingLogActivity.this, ProfileActivity.class);
                    startActivity(profileIntent);
                }
            });
        }
        // ===================

        // ========== List view with workouts ==========
        {
            arraySingleWorkout = new ArrayList<SingleWorkout>();
            arrayAdapterSingleWorkouts = new SingleWorkoutAdapter(this, arraySingleWorkout);
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

        // =========== Workout Info Activity ===========
        {
            listViewSingleWorkouts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    SingleWorkout selectedSW = arraySingleWorkout.get(position);
                    String creationDate = selectedSW.getCreationdate();

                    String transitionName = getString(R.string.name_anim);
                    TextView nameField = view.findViewById(R.id.name_field);

                    String transitionDate = getString(R.string.date_anim);
                    TextView dateField = view.findViewById(R.id.date_field);

                    Pair[] pair = new Pair[2];
                    pair[0] = new Pair<View, String>(nameField, transitionName);
                    pair[1] = new Pair<View, String>(dateField, transitionDate);

                    Intent info = new Intent(TrainingLogActivity.this, SingleWorkoutInfoActivity.class);
                    info.putExtra(W_KEY, creationDate);
                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(TrainingLogActivity.this, pair);
                    startActivity(info, transitionActivityOptions.toBundle());
                }
            });
        }
        // ==================================================

        // ================ Deleting Workouts ===============
        {
        listViewSingleWorkouts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TrainingLogActivity.this);
                alertDialogBuilder
                        .setTitle("Do you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                SingleWorkout workout = arraySingleWorkout.get(position);
                                for (SingleWorkout w : mWorkoutsList) {
                                    if (w.getCreationdate().equals(workout.getCreationdate())) {
                                        workout = w;
                                    }
                                }
                                mWorkoutsList.remove(workout);
                                String msg = "Name: " + workout.getName() + "Creation date: " + workout.getCreationdate();
                                Log.e(TAG, msg);

                                mDatabase.setValue(mWorkoutsList)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                arrayAdapterSingleWorkouts.notifyDataSetChanged();
                                                Toast.makeText(TrainingLogActivity.this, "You have successfully deleted an item.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(TrainingLogActivity.this, "Deletion failed. Try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
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
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("TrainingLog");

        listViewSingleWorkouts = findViewById(R.id.list_view_single_workouts);
    }
}
