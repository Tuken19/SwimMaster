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

import com.example.swimmaster.SingleWorkout.SingleWorkout;
import com.example.swimmaster.SingleWorkout.SingleWorkoutAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.swimmaster.MainMenuActivity.mDatabaseTrainingLog;
import static com.example.swimmaster.MainMenuActivity.mWorkoutsList;

public class TrainingLogActivity extends AppCompatActivity {

    private final static String TAG = "TrainingLogActivity";
    private long maxSWId = 0;

    ListView listViewSingleWorkouts;
    ArrayList<SingleWorkout> arraySingleWorkout;
    SingleWorkoutAdapter arrayAdapterSingleWorkouts;
    public static final String W_KEY_CREATION_DATE = "Creation Date of workout";
    public static final String W_KEY_LIST_NAME = "Name of list to which workout belongs";

    ActionBar actionBar;
    ImageButton profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_log);

        initializeFields();

        mDatabaseTrainingLog.addValueEventListener(new ValueEventListener() {
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
            arrayAdapterSingleWorkouts = new SingleWorkoutAdapter(this, (ArrayList<SingleWorkout>) mWorkoutsList);
            listViewSingleWorkouts.setAdapter(arrayAdapterSingleWorkouts);

            mDatabaseTrainingLog.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mWorkoutsList.clear();
                    for (DataSnapshot element : dataSnapshot.getChildren()) {
                        SingleWorkout singleWorkout = element.getValue(SingleWorkout.class);
                        mWorkoutsList.add(singleWorkout);
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
                    SingleWorkout selectedSW = mWorkoutsList.get(position);
                    String selectedListName = selectedSW.getListName();
                    String creationDate = selectedSW.getCreationDate();

                    String transitionName = getString(R.string.name_anim);
                    TextView nameField = view.findViewById(R.id.name_field);

                    String transitionDate = getString(R.string.date_anim);
                    TextView dateField = view.findViewById(R.id.date_field);

                    Pair[] pair = new Pair[2];
                    pair[0] = new Pair<View, String>(nameField, transitionName);
                    pair[1] = new Pair<View, String>(dateField, transitionDate);

                    Intent info = new Intent(TrainingLogActivity.this, SingleWorkoutInfoActivity.class);
                    info.putExtra(W_KEY_LIST_NAME, selectedListName);
                    info.putExtra(W_KEY_CREATION_DATE, creationDate);
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
                                    if (w.getName().equals(workout.getName()) && w.getCreationDate().equals(workout.getCreationDate())) {
                                        workout = w;
                                    }
                                }
                                mWorkoutsList.remove(workout);
                                String msg = "Name: " + workout.getName() + "Creation date: " + workout.getCreationDate();
                                Log.e(TAG, msg);

                                mDatabaseTrainingLog.setValue(mWorkoutsList)
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

        listViewSingleWorkouts = findViewById(R.id.list_view_single_workouts);
    }
}
