package com.example.swimmaster;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swimmaster.SingleWorkout.SingleWorkout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.swimmaster.MainMenuActivity.mDatabaseTrainingLog;
import static com.example.swimmaster.MainMenuActivity.mDatabaseTrainingPlan;
import static com.example.swimmaster.MainMenuActivity.mTrainingSetWorkoutsListName;
import static com.example.swimmaster.MainMenuActivity.mWorkoutsListName;
import static com.example.swimmaster.TrainingLogActivity.W_KEY_CREATION_DATE;
import static com.example.swimmaster.TrainingLogActivity.W_KEY_LIST_NAME;

public class SingleWorkoutInfoActivity extends AppCompatActivity {

    private final static String TAG = "SingleWorkoutInfoActivity";
    private long maxSWId = 0;

    ActionBar actionBar;
    ImageButton profileButton;

    SingleWorkout currentSingleWorkout;

    TextView nameField;
    TextView dateField;

    ListView listViewWarmUpTask;
    ListView listViewMainSetTask;
    ListView listViewCooldownTask;
    ArrayList<Task> warmUpTaskArray;
    ArrayList<Task> mainSetTaskArray;
    ArrayList<Task> cooldownTaskArray;
    TaskAdapter arrayAdapterWarmUpTask;
    TaskAdapter arrayAdapterMainSetTask;
    TaskAdapter arrayAdapterCooldownTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_workout_info);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeFields();

        warmUpTaskArray = new ArrayList<Task>();
        arrayAdapterWarmUpTask = new TaskAdapter(SingleWorkoutInfoActivity.this, warmUpTaskArray);
        listViewWarmUpTask.setAdapter(arrayAdapterWarmUpTask);

        mainSetTaskArray = new ArrayList<Task>();
        arrayAdapterMainSetTask = new TaskAdapter(SingleWorkoutInfoActivity.this, mainSetTaskArray);
        listViewMainSetTask.setAdapter(arrayAdapterMainSetTask);

        cooldownTaskArray = new ArrayList<Task>();
        arrayAdapterCooldownTask = new TaskAdapter(SingleWorkoutInfoActivity.this, cooldownTaskArray);
        listViewCooldownTask.setAdapter(arrayAdapterCooldownTask);

        final String[] creationDate = {""};
        final Bundle extras = getIntent().getExtras();

        if (extras.getString(W_KEY_LIST_NAME).equals(mTrainingSetWorkoutsListName)) {
            mDatabaseTrainingPlan.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (extras != null) {
                        creationDate[0] = extras.getString(W_KEY_CREATION_DATE);
                    }

                    for (DataSnapshot element : dataSnapshot.getChildren()) {
                        SingleWorkout singleWorkout = element.getValue(SingleWorkout.class);
                        if (singleWorkout.getCreationDate().equals(creationDate[0])) {
                            currentSingleWorkout = singleWorkout;
                            nameField.setText(currentSingleWorkout.getName());
                            dateField.setText(currentSingleWorkout.getDate());

                            warmUpTaskArray.clear();
                            for (Task task : currentSingleWorkout.getWarmUp()) {
                                warmUpTaskArray.add(task);
                                arrayAdapterWarmUpTask.notifyDataSetChanged();
                            }
                            mainSetTaskArray.clear();
                            for (Task task : currentSingleWorkout.getMainSet()) {
                                mainSetTaskArray.add(task);
                                arrayAdapterMainSetTask.notifyDataSetChanged();
                            }
                            cooldownTaskArray.clear();
                            for (Task task : currentSingleWorkout.getCooldown()) {
                                cooldownTaskArray.add(task);
                                arrayAdapterCooldownTask.notifyDataSetChanged();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else if (extras.getString(W_KEY_LIST_NAME).equals(mWorkoutsListName)) {
            mDatabaseTrainingLog.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (extras != null) {
                        creationDate[0] = extras.getString(W_KEY_CREATION_DATE);
                    }

                    for (DataSnapshot element : dataSnapshot.getChildren()) {
                        SingleWorkout singleWorkout = element.getValue(SingleWorkout.class);
                        if (singleWorkout.getCreationDate().equals(creationDate[0])) {
                            currentSingleWorkout = singleWorkout;
                            nameField.setText(currentSingleWorkout.getName());
                            dateField.setText(currentSingleWorkout.getDate());

                            warmUpTaskArray.clear();
                            for (Task task : currentSingleWorkout.getWarmUp()) {
                                warmUpTaskArray.add(task);
                                arrayAdapterWarmUpTask.notifyDataSetChanged();
                            }
                            mainSetTaskArray.clear();
                            for (Task task : currentSingleWorkout.getMainSet()) {
                                mainSetTaskArray.add(task);
                                arrayAdapterMainSetTask.notifyDataSetChanged();
                            }
                            cooldownTaskArray.clear();
                            for (Task task : currentSingleWorkout.getCooldown()) {
                                cooldownTaskArray.add(task);
                                arrayAdapterCooldownTask.notifyDataSetChanged();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void initializeFields() {
        // ========== Custom Action Bar ==========
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(actionBarView);

        profileButton = findViewById(R.id.profile);

        nameField = findViewById(R.id.name_field);
        dateField = findViewById(R.id.date_field);

        listViewWarmUpTask = findViewById(R.id.list_view_warm_up);
        listViewMainSetTask = findViewById(R.id.list_view_main_set);
        listViewCooldownTask = findViewById(R.id.list_view_cooldown);
    }
}
