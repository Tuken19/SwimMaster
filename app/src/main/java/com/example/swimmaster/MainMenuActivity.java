package com.example.swimmaster;

import android.Manifest;
import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.swimmaster.SingleWorkout.SingleWorkout;
import com.example.swimmaster.SingleWorkout.SingleWorkoutActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.swimmaster.MainMenuActivity.GetCalendarTask.GET_CALENDAR;
import static com.example.swimmaster.MainMenuActivity.MakeCalendarTask.MAKE_CALENDAR;
import static com.example.swimmaster.WelcomeActivity.mFBUser;

public class MainMenuActivity extends AppCompatActivity {
    private final static String TAG = "MainMenuActivity";

    public static DatabaseReference mDatabaseHome;
    public static DatabaseReference mDatabaseSingleWorkout;
    public static DatabaseReference mDatabaseTrainingPlan;
    public static DatabaseReference mDatabaseTrainingLog;
    public static DatabaseReference mDatabaseTimes;

    private static final int MY_PERMISSIONS_REQUEST_CALENDAR = 0;
    private static final int RC_AUTHORIZE_CALENDAR = 10;
    public final static String CALENDAR_NAME = "SwimMaster";
    public final static String SCOPE = "https://www.googleapis.com/auth/calendar";

    // Global instance of the HTTP transport.
    public final static HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    //Global instance of the JSON factory.
    public final static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    ActionBar actionBar;
    ImageButton profileButton;
    RelativeLayout singleWorkout;
    RelativeLayout trainingPlan;
    RelativeLayout trainingLog;
    RelativeLayout timesLog;
    ProgressBar progressBar;

    public static GoogleSignInClient mGoogleSignInClient;
    public static List<SingleWorkout> mWorkoutsList = new ArrayList<>();
    public static final String mWorkoutsListName = "WorkoutList";
    public static List<SingleWorkout> mTrainingSetWorkoutsList = new ArrayList<>();
    public static final String mTrainingSetWorkoutsListName = "TrainingSetWorkoutsList";

    public static String personName;
    public static String personEmail;
    public static String personId;
    public static Uri personPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // =========== Initialisation ============
        initializeFields();
        // =======================================

        // ===== Permissions for reading and writing to calendar =====
        permissionsForCalendar();
        // ===========================================================

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/calendar"))
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            personName = acct.getDisplayName();
            personEmail = acct.getEmail();
            personId = acct.getId();
            personPhoto = acct.getPhotoUrl();
            downloadPhoto(profileButton, personPhoto, this);
        }
        // ================================================================================

        // ========== Jump to other activities ==========
        // ===== Single Workout =====
        singleWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent singleWorkoutIntent = new Intent(MainMenuActivity.this, SingleWorkoutActivity.class);
                startActivity(singleWorkoutIntent);
            }
        });
        // ==========================
        // ===== Training Plan =====
        trainingPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trainingPlanIntent = new Intent(MainMenuActivity.this, TrainingPlanActivity.class);
                startActivity(trainingPlanIntent);
            }
        });
        // =========================
        // ===== Training Log =====
        trainingLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent trainingLogIntent = new Intent(MainMenuActivity.this, TrainingLogActivity.class);
                startActivity(trainingLogIntent);
            }
        });
        // ========================
        // ===== TimesLogActivity Log =====
        timesLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent timesLogIntent = new Intent(MainMenuActivity.this, TimesLogActivity.class);
                startActivity(timesLogIntent);
            }
        });
        // =====================
        // ===== Profile =====
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(MainMenuActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
        // ===================
        // ==============================================

        // =========== Training Set Workouts ===========
        {
            mDatabaseTrainingPlan.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mTrainingSetWorkoutsList.clear();
                    for (DataSnapshot element : dataSnapshot.getChildren()) {
                        SingleWorkout singleWorkout = element.getValue(SingleWorkout.class);
                        mTrainingSetWorkoutsList.add(singleWorkout);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        // =============================================

        // ============== Logged Workouts ==============
        {
            mDatabaseTrainingLog.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mWorkoutsList.clear();
                    for (DataSnapshot element : dataSnapshot.getChildren()) {
                        SingleWorkout singleWorkout = element.getValue(SingleWorkout.class);
                        mWorkoutsList.add(singleWorkout);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        // =============================================
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ===== Checks if calendar with particular name exists if not create new one =====
        if (isDeviceOnline()) {
            getCalendar();
        } else {
            Toast.makeText(this, "Disconnected from network.", Toast.LENGTH_SHORT).show();
        }
        // ================================================================================
    }

    /**
     * Function for initialising particular fields.
     */
    private void initializeFields() {
        singleWorkout = findViewById(R.id.SingleWorkout);
        trainingPlan = findViewById(R.id.TrainingPlan);
        trainingLog = findViewById(R.id.TrainingLog);
        timesLog = findViewById(R.id.TimesLog);

        // ============== Firebase ===============
        mDatabaseHome = FirebaseDatabase.getInstance().getReference();
        mDatabaseSingleWorkout = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("SingleWorkout");
        mDatabaseTrainingPlan = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("TrainingPlan");
        mDatabaseTrainingLog = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("TrainingLog");
        mDatabaseTimes = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("Times");
        // =======================================

        // ========== Custom Action Bar ==========
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(actionBarView);

        profileButton = findViewById(R.id.profile);

        progressBar = findViewById(R.id.progress_bar);
    }

    /**
     * Function for asking user for giving permissions of READ and WRITE to Google calendar.
     */
    private void permissionsForCalendar() {
        if (ContextCompat.checkSelfPermission(MainMenuActivity.this,
                Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(MainMenuActivity.this,
                    new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR},
                    MY_PERMISSIONS_REQUEST_CALENDAR);
            Log.i(TAG, "Request permission for a calendar.");
        } else {
            // Permission has already been granted
            Log.i(TAG, "Permission for a calendar already granted!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // Disable all buttons
                    singleWorkout.setEnabled(false);
                    trainingLog.setEnabled(false);
                    trainingPlan.setEnabled(false);
                    timesLog.setEnabled(false);
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_AUTHORIZE_CALENDAR || requestCode == GET_CALENDAR) {
            if (resultCode == RESULT_OK) {
                getCalendar();
            }
        } else if (requestCode == MAKE_CALENDAR) {
            if (resultCode == RESULT_OK) {
                makeCalendar();
            }
        }
    }

    //TODO: Dopisać isDeviceOnline
    /**
     * Function which checks if device is connected to the network
     * @return true if connected
     */
    private boolean isDeviceOnline() {
        return true;
    }

    /**
     * Function which checks if a calendar, with name CALENDAR_NAME, already exists in async task.
     */
    private void getCalendar() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            GetCalendarTask task = new GetCalendarTask(account.getAccount(), SCOPE);
            task.execute();
        }
    }

    /**
     * Function which makes new calendar, with name CALENDAR_NAME, in async task.
     */
    private void makeCalendar() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            MakeCalendarTask task = new MakeCalendarTask(account.getAccount(), SCOPE);
            task.execute();
        }
    }

    /**
     * Class which is responsible for checking if calendar, with name CALENDAR_NAME, exists.
     */
    protected class GetCalendarTask extends AsyncTask {
        private final String TAG = "GetCalendarTask";
        public static final int GET_CALENDAR = 101;

        private String mScope;
        private Account mAccount;
        String result = "-1";

        GetCalendarTask(Account account, String scope) {
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
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(MainMenuActivity.this, Collections.singleton(mScope));
                credential.setSelectedAccount(mAccount);
                Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName("Get Calendar")
                        .build();

                String pageToken = null;
                CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
                List<CalendarListEntry> items = calendarList.getItems();

                for (CalendarListEntry calendarListEntry : items) {
                    System.out.println(calendarListEntry.getSummary());
                    if (calendarListEntry.getSummary().equals(CALENDAR_NAME)) {
                        result = calendarListEntry.getEtag();
                    }
                }

                Log.i(TAG, "Result: " + result);

            } catch (UserRecoverableAuthIOException userRecoverableException) {
                // Explain to the user again why you need these OAuth permissions
                // And prompt the resolution to the user again:
                startActivityForResult(MainMenuActivity.this, userRecoverableException.getIntent(), GET_CALENDAR, null);
            } catch (IOException e) {
                // Other non-recoverable exceptions.
                Log.i(TAG, "IOException - GetCalendarTask " + e.getMessage());
            }
            return result;
        }

        private void startActivityForResult(Activity mActivity, Intent intent, int rcReauthorize, Bundle bundle) {
            mActivity.startActivityForResult(intent, rcReauthorize, bundle);
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressBar.setVisibility(View.GONE);
            if (result.equals("-1")) {
                makeCalendar();
            }
        }
    }

    /**
     * Class which is responsible for making new calendar, with name CALENDAR_NAME.
     */
    protected class MakeCalendarTask extends AsyncTask {
        private final String TAG = "MakeCalendarTask";
        public static final int MAKE_CALENDAR = 100;

        private String mScope;
        private Account mAccount;

        MakeCalendarTask(Account account, String scope) {
            this.mScope = scope;
            this.mAccount = account;
        }

        /**
         * Executes the asynchronous job. This runs when you call execute()
         * on the AsyncTask instance.
         */
        @Override
        protected String doInBackground(Object[] objects) {

            String result = null;
            try {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(MainMenuActivity.this, Collections.singleton(mScope));
                credential.setSelectedAccount(mAccount);
                Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName("Make Calendar")
                        .build();

                com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
                calendar.setSummary(CALENDAR_NAME);

                com.google.api.services.calendar.model.Calendar createdCalendar = service
                        .calendars()
                        .insert(calendar)
                        .execute();
                result = createdCalendar.getId();

                Log.i(TAG, "Result: " + result);

            } catch (UserRecoverableAuthIOException userRecoverableException) {
                // Explain to the user again why you need these OAuth permissions
                // And prompt the resolution to the user again:
                startActivityForResult(MainMenuActivity.this, userRecoverableException.getIntent(), MAKE_CALENDAR, null);
            } catch (IOException e) {
                // Other non-recoverable exceptions.
                Log.i(TAG, "IOException - MakeCalendarTask " + e.getMessage());
            }
            return result;
        }

        private void startActivityForResult(Activity mActivity, Intent intent, int rcReauthorize, Bundle bundle) {
            mActivity.startActivityForResult(intent, rcReauthorize, bundle);
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            progressBar.setVisibility(View.GONE);

        }
    }


    public static void downloadPhoto(ImageView imageView, Uri uri, Context context){
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            DownloadImageTask downloadImageTask = new DownloadImageTask(imageView);
            downloadImageTask.setUri(uri);
            downloadImageTask.execute();
        }
    }

    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView bmImage;
        private Uri uri;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bmp = null;
            try {
                InputStream in = new java.net.URL(uri.toString()).openStream();
                bmp = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bmp;
        }

        public void setUri(Uri uri){
            this.uri = uri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bmImage.setImageResource(R.drawable.custom_account_icon);
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
