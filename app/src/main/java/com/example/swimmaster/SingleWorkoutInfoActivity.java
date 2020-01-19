package com.example.swimmaster;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swimmaster.SingleWorkout.SingleWorkout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.swimmaster.MainMenuActivity.CALENDAR_NAME;
import static com.example.swimmaster.MainMenuActivity.GetCalendarTask.GET_CALENDAR;
import static com.example.swimmaster.MainMenuActivity.HTTP_TRANSPORT;
import static com.example.swimmaster.MainMenuActivity.JSON_FACTORY;
import static com.example.swimmaster.MainMenuActivity.SCOPE;
import static com.example.swimmaster.MainMenuActivity.mDatabaseTrainingLog;
import static com.example.swimmaster.MainMenuActivity.mDatabaseTrainingPlan;
import static com.example.swimmaster.MainMenuActivity.mTrainingSetWorkoutsList;
import static com.example.swimmaster.MainMenuActivity.mTrainingSetWorkoutsListName;
import static com.example.swimmaster.MainMenuActivity.mWorkoutsList;
import static com.example.swimmaster.MainMenuActivity.mWorkoutsListName;
import static com.example.swimmaster.MainMenuActivity.personPhoto;
import static com.example.swimmaster.TrainingLogActivity.W_KEY_CREATION_DATE;
import static com.example.swimmaster.TrainingLogActivity.W_KEY_LIST_NAME;

public class SingleWorkoutInfoActivity extends AppCompatActivity {

    private final static String TAG = "SingleWorkoutInfoActivity";
    private static final int RC_AUTHORIZE_UPDATE_CALENDAR_ENTRY = 12;
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

    Button logButton;

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

        if (extras.getString(W_KEY_LIST_NAME).equals(mWorkoutsListName)) {
            logButton.setVisibility(View.GONE);
        }

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

        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logTraining(currentSingleWorkout);
                finish();
            }
        });
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

        logButton = findViewById(R.id.log_button);

        MainMenuActivity.downloadPhoto(profileButton, personPhoto, this);
    }

    private void logTraining(SingleWorkout singleWorkout) {
        SingleWorkout workout = null;
        for (SingleWorkout w : mTrainingSetWorkoutsList) {
            if (w.getName().equals(singleWorkout.getName()) && w.getCreationDate().equals(singleWorkout.getCreationDate())) {
                workout = w;
            }
        }
        mTrainingSetWorkoutsList.remove(workout);
        mDatabaseTrainingPlan.setValue(mTrainingSetWorkoutsList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "DatabaseTrainingPlan was successfully updated!");
                        Toast.makeText(SingleWorkoutInfoActivity.this, "Database was successfully updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "DatabaseTrainingPlan was not updated!");
                        Toast.makeText(SingleWorkoutInfoActivity.this, "Database was not updated!", Toast.LENGTH_SHORT).show();
                    }
                });

        updateCalendarEntry(singleWorkout.getName(), singleWorkout.getDate());

        String name = singleWorkout.getName().substring(3) + " - logged";
        singleWorkout.setName(name);
        singleWorkout.setListName(mWorkoutsListName);
        mWorkoutsList.add(singleWorkout);
        mDatabaseTrainingLog.setValue(mWorkoutsList)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "DatabaseTrainingLog was successfully updated!");
                        Toast.makeText(SingleWorkoutInfoActivity.this, "Database was successfully updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "DatabaseTrainingLog was not updated!");
                        Toast.makeText(SingleWorkoutInfoActivity.this, "Database was not updated!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_AUTHORIZE_UPDATE_CALENDAR_ENTRY || requestCode == GET_CALENDAR) {
            if (resultCode == RESULT_OK) {
                updateCalendarEntry(data.getStringExtra("Name"), data.getStringExtra("Date"));
            }
        }
    }

    /**
     * Function which makes new calendar entry in async task.
     */
    private void updateCalendarEntry(String trainingName, String dateOfTraining) {
        int day = Integer.parseInt(dateOfTraining.substring(dateOfTraining.length() - 10, dateOfTraining.length() - 8));
        int month = Integer.parseInt(dateOfTraining.substring(dateOfTraining.length() - 7, dateOfTraining.length() - 5));
        int year = Integer.parseInt(dateOfTraining.substring(dateOfTraining.length() - 4, dateOfTraining.length()));
        String date = year + "-" + month + "-" + day;

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            SingleWorkoutInfoActivity.UpdateCalendarEntryTask task = new SingleWorkoutInfoActivity.UpdateCalendarEntryTask(account.getAccount(), SCOPE);
            task.setTrainingName(trainingName);
            task.setDateOfTraining(date);
            task.execute();
        }
    }

    /**
     * Class which is responsible for making a new calendar entry.
     */
    class UpdateCalendarEntryTask extends AsyncTask {
        private final String TAG = "MakeCalendarEntryTask";
        public static final int UPDATE_CALENDAR_ENTRY = 106;

        private Account mAccount;
        private String mScope;

        String trainingName;
        String dateOfTraining;
        String calendarId = "-1";
        String calendarTimeZone = "Europe/Warsaw";

        UpdateCalendarEntryTask(Account account, String scope) {
            this.mScope = scope;
            this.mAccount = account;
        }

        /**
         * Executes the asynchronous job. This runs when you call execute()
         * on the AsyncTask instance.
         */
        @Override
        protected String doInBackground(Object[] objects) {
            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(SingleWorkoutInfoActivity.this, Collections.singleton(mScope));
                credential.setSelectedAccount(mAccount);
                Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName("Make Calendar Entry")
                        .build();

                // Looking for calendar with name: CALENDAR_NAME.
                String pageToken = null;
                CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items) {
                    System.out.println(calendarListEntry.getSummary());
                    if (calendarListEntry.getSummary().equals(CALENDAR_NAME)) {
                        calendarId = calendarListEntry.getId();
                        calendarTimeZone = calendarListEntry.getTimeZone();
                    }
                }
                if (calendarId.equals("-1")) {
                    throw new Exception();
                }

                Event e = null;
                // Iterate over the events in the specified calendar.
                pageToken = null;
                do {
                    Events events = service.events().list(calendarId).setPageToken(pageToken).execute();
                    List<Event> eventsList = events.getItems();
                    for (Event event : eventsList) {
                        if (event.getSummary().contains(trainingName)) {
                            // Retrieve the event from the API
                            e = service.events().get(calendarId, event.getId()).execute();
                        }
                    }
                    pageToken = events.getNextPageToken();
                } while (pageToken != null);

                // Make a change
                e.setSummary(trainingName.substring(3) + " - logged");

                // Update the event
                Event updatedEvent = service.events().update(calendarId, e.getId(), e).execute();
                Log.i(TAG, "Event updated: %s\n" + e.getHtmlLink());

            } catch (UserRecoverableAuthIOException userRecoverableException) {
                // Explain to the user again why you need these OAuth permissions
                // And prompt the resolution to the user again:
                startActivityForResult(SingleWorkoutInfoActivity.this, userRecoverableException.getIntent(), UPDATE_CALENDAR_ENTRY, null);
            } catch (IOException e) {
                // Other non-recoverable exceptions.
                Log.i(TAG, "IOException - MakeCalendarEntryTask " + e.getMessage());
            } catch (Exception e) {
                Log.i(TAG, "Unable to create events due to calendar missing.");
                e.printStackTrace();
            }
            return calendarId;
        }

        private void startActivityForResult(Activity mActivity, Intent intent, int rcReauthorize, Bundle bundle) {
            intent.putExtra("Name", trainingName);
            intent.putExtra("Date", dateOfTraining);
            mActivity.startActivityForResult(intent, rcReauthorize, bundle);
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            //progressBar.setVisibility(View.GONE);
        }

        private void setDateOfTraining(String dateOfTraining) {
            this.dateOfTraining = dateOfTraining;
        }

        private void setTrainingName(String trainingName) {
            this.trainingName = trainingName;
        }
    }
}
