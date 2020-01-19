package com.example.swimmaster;

import android.accounts.Account;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
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
import static com.example.swimmaster.MainMenuActivity.mWorkoutsList;
import static com.example.swimmaster.MainMenuActivity.personPhoto;

public class TrainingLogActivity extends AppCompatActivity {

    private final static String TAG = "TrainingLogActivity";
    private static final int RC_AUTHORIZE_DELETE_CALENDAR_ENTRY = 117;
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
                                SingleWorkout workout = mWorkoutsList.get(position);
//                                for (SingleWorkout w : mWorkoutsList) {
//                                    if (w.getName().equals(workout.getName()) && w.getCreationDate().equals(workout.getCreationDate())) {
//                                        workout = w;
//                                    }
//                                }
                                mWorkoutsList.remove(workout);
                                String msg = "Name: " + workout.getName() + "Creation date: " + workout.getCreationDate();
                                Log.e(TAG, msg);

                                mDatabaseTrainingLog.setValue(mWorkoutsList)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                arrayAdapterSingleWorkouts.notifyDataSetChanged();
                                                Log.i(TAG, "You have successfully deleted an item.");
                                                Toast.makeText(TrainingLogActivity.this, "You have successfully deleted an item.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i(TAG, "Deletion filed.");
                                                Toast.makeText(TrainingLogActivity.this, "Deletion failed. Try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                deleteCalendarEntry(workout.getName(), workout.getDate());

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

        MainMenuActivity.downloadPhoto(profileButton, personPhoto, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_AUTHORIZE_DELETE_CALENDAR_ENTRY || requestCode == GET_CALENDAR) {
            if (resultCode == RESULT_OK) {
                deleteCalendarEntry(data.getStringExtra("Name"), data.getStringExtra("Date"));
            }
        }
    }

    /**
     * Function which makes new calendar entry in async task.
     */
    private void deleteCalendarEntry(String nameOfTraining, String dateOfTraining) {
        int day = Integer.parseInt(dateOfTraining.substring(dateOfTraining.length() - 10, dateOfTraining.length() - 8));
        int month = Integer.parseInt(dateOfTraining.substring(dateOfTraining.length() - 7, dateOfTraining.length() - 5));
        int year = Integer.parseInt(dateOfTraining.substring(dateOfTraining.length() - 4, dateOfTraining.length()));
        String date = year + "-" + month + "-" + day;

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            TrainingLogActivity.DeleteCalendarEntryTask task = new TrainingLogActivity.DeleteCalendarEntryTask(account.getAccount(), SCOPE);
            task.setName(nameOfTraining);
            task.setDay(date);
            task.execute();
        }
    }

    /**
     * Class which is responsible for making a new calendar entry.
     */
    class DeleteCalendarEntryTask extends AsyncTask {
        private final String TAG = "MakeCalendarEntryTask";
        public static final int DELETE_CALENDAR_ENTRY = 102;

        private Account mAccount;
        private String mScope;
        private String mNameOfTraining;
        private String mDateOfTraining;

        String calendarId = "-1";
        String calendarTimeZone = "Europe/Warsaw";

        DeleteCalendarEntryTask(Account account, String scope) {
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
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(TrainingLogActivity.this, Collections.singleton(mScope));
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

                // Iterate over the events in the specified calendar and deletes them.
                pageToken = null;
                do {
                    Events events = service.events().list(calendarId).setPageToken(pageToken).execute();
                    List<Event> eventsList = events.getItems();
                    for (Event event : eventsList) {
                        if (event.getSummary().contains(mNameOfTraining)) {
                            service.events().delete(calendarId, event.getId()).execute();
                            Log.i(TAG, "Event deleted: %s\n" + event.getHtmlLink());
                        }
                    }
                    pageToken = events.getNextPageToken();
                } while (pageToken != null);


            } catch (UserRecoverableAuthIOException userRecoverableException) {
                // Explain to the user again why you need these OAuth permissions
                // And prompt the resolution to the user again:
                startActivityForResult(TrainingLogActivity.this, userRecoverableException.getIntent(), DELETE_CALENDAR_ENTRY, null);
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
            intent.putExtra("Name", mNameOfTraining);
            intent.putExtra("Day", mDateOfTraining);
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

        private void setName(String nameOfTraining){
            this.mNameOfTraining = nameOfTraining;
        }

        private void setDay(String dateOfTraining) {
            this.mDateOfTraining = dateOfTraining;
        }
    }
}
