package com.example.swimmaster;

import android.accounts.Account;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.swimmaster.SingleWorkout.SingleWorkout;
import com.example.swimmaster.SingleWorkout.SingleWorkoutAdapter;
import com.example.swimmaster.TrainingSetBuilder.TrainingSet;
import com.example.swimmaster.calendarDecorators.EventDecorator;
import com.example.swimmaster.calendarDecorators.TodaysDecorator;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.example.swimmaster.MainMenuActivity.CALENDAR_NAME;
import static com.example.swimmaster.MainMenuActivity.GetCalendarTask.GET_CALENDAR;
import static com.example.swimmaster.MainMenuActivity.HTTP_TRANSPORT;
import static com.example.swimmaster.MainMenuActivity.JSON_FACTORY;
import static com.example.swimmaster.MainMenuActivity.SCOPE;
import static com.example.swimmaster.MainMenuActivity.mDatabaseTrainingPlan;
import static com.example.swimmaster.MainMenuActivity.mTrainingSetWorkoutsList;
import static com.example.swimmaster.MainMenuActivity.mWorkoutsList;
import static com.example.swimmaster.MainMenuActivity.personPhoto;
import static com.example.swimmaster.TrainingLogActivity.W_KEY_CREATION_DATE;
import static com.example.swimmaster.TrainingLogActivity.W_KEY_LIST_NAME;
import static com.example.swimmaster.TrainingSetBuilder.TrainingSet.TRAINING_NAME;

public class TrainingPlanActivity extends AppCompatActivity {

    private final static String TAG = "TrainingPlanActivity";
    private static final int RC_AUTHORIZE_MAKE_CALENDAR_ENTRY = 11;
    private static List<String> mDatesOfTrainings = null;

    ActionBar actionBar;
    ImageButton profileButton;

    MaterialCalendarView calendarView;
    EventDecorator eventDecorator;

    Button generateTraining;

    View background;
    View popupView;
    PopupWindow popupWindow;
    CheckBox monCheckBox;
    CheckBox tueCheckBox;
    CheckBox wedCheckBox;
    CheckBox thuCheckBox;
    CheckBox friCheckBox;
    CheckBox satCheckBox;
    CheckBox sunCheckBox;
    EditText editWeeks;
    EditText editDistance;
    CheckBox butterflyCheckBox;
    CheckBox backstrokeCheckBox;
    CheckBox breaststrokeCheckBox;
    CheckBox freestyleCheckBox;
    CheckBox legsCheckBox;
    CheckBox kickCheckBox;
    CheckBox finsCheckBox;
    CheckBox armsCheckBox;
    CheckBox buoyCheckBox;
    CheckBox paddlesCheckBox;

    ProgressBar progressBar;

    ListView listViewOfWorkouts;
    ArrayList<SingleWorkout> arrayOfWorkouts;
    SingleWorkoutAdapter arrayAdapterOfWorkouts;

    Button generate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_plan);

        initializeFields();

        // ===== Profile =====
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(TrainingPlanActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
            }
        });
        // ===================

        // ========== Training generating ==========
        generateTraining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupWindow.setFocusable(true);
                popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                popupWindow.showAtLocation(background, Gravity.BOTTOM, 0, 0);

                generate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (validate()) {
                            boolean[] days = getDays();
                            int numberOfWeeks = getNumberOfWeeks();
                            int startingDistance = getStartingDistance();
                            boolean[] strokes = getStrokes();
                            boolean[] additions = getAdditions();

                            // Creating a training set.
                            TrainingSet trainingSet = TrainingSet.getInstance();
                            trainingSet.resetTraining();
                            trainingSet.setDays(days);
                            trainingSet.setWeeks(numberOfWeeks);
                            trainingSet.setStartingDistance(startingDistance);
                            trainingSet.setStrokes(strokes);
                            trainingSet.setAdditions(additions);
                            trainingSet.generateTraining();
                            trainingSet.uploadData(TrainingPlanActivity.this, mDatabaseTrainingPlan);


                            mDatesOfTrainings = trainingSet.getTrainingDatesString();

                            // Making google calendar events.
                            makeCalendarEntry(mDatesOfTrainings);

                            // Close popup
                            popupWindow.dismiss();

                            // Showing generated stuff on calendar view.
                            calendarView.removeDecorator(eventDecorator);

                            calendarInit();

                            calendarView.addDecorator(eventDecorator);
                        } else {
                            Toast.makeText(TrainingPlanActivity.this, "Please provide all information.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        // =========================================

        // ========== List View uppdating ==========
        arrayAdapterOfWorkouts = new SingleWorkoutAdapter(TrainingPlanActivity.this, arrayOfWorkouts);
        listViewOfWorkouts.setAdapter(arrayAdapterOfWorkouts);

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                updateListView(date);
            }
        });

        listViewOfWorkouts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SingleWorkout selectedSW = arrayOfWorkouts.get(position);
                String selectedListName = selectedSW.getListName();
                String creationDate = selectedSW.getCreationDate();

                String transitionName = getString(R.string.name_anim);
                TextView nameField = view.findViewById(R.id.name_field);

                String transitionDate = getString(R.string.date_anim);
                TextView dateField = view.findViewById(R.id.date_field);

                Pair[] pair = new Pair[2];
                pair[0] = new Pair<View, String>(nameField, transitionName);
                pair[1] = new Pair<View, String>(dateField, transitionDate);

                Intent info = new Intent(TrainingPlanActivity.this, SingleWorkoutInfoActivity.class);
                info.putExtra(W_KEY_LIST_NAME, selectedListName);
                info.putExtra(W_KEY_CREATION_DATE, creationDate);
                ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(TrainingPlanActivity.this, pair);
                startActivity(info, transitionActivityOptions.toBundle());
            }
        });
        // =========================================

        // ========= Marking events on calendar view ==========
        calendarInit();
        CalendarDay today = CalendarDay.today();
        updateListView(today);
        // ====================================================
    }

    @Override
    protected void onResume() {
        super.onResume();
        calendarInit();
        arrayAdapterOfWorkouts.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if(calendarView.getCalendarMode().equals(CalendarMode.WEEKS)){
            calendarView.state().edit()
                    .setCalendarDisplayMode(CalendarMode.MONTHS)
                    .commit();
        }
        else {
            super.onBackPressed();
        }
    }


    /**
     * Function for initialising particular fields.
     */
    private void initializeFields() {
        // ========== Custom Action Bar ==========
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_action_bar, null);
        actionBar.setCustomView(actionBarView);

        profileButton = findViewById(R.id.profile);

        generateTraining = findViewById(R.id.generate_training);

        calendarView = findViewById(R.id.calendar_view);

        // ====== Add Generate Training popup =======
        background = findViewById(R.id.background);
        popupView = layoutInflater.inflate(R.layout.generate_training_popup, null);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        monCheckBox = popupView.findViewById(R.id.monday);
        tueCheckBox = popupView.findViewById(R.id.tuesday);
        wedCheckBox = popupView.findViewById(R.id.wednesday);
        thuCheckBox = popupView.findViewById(R.id.thursday);
        friCheckBox = popupView.findViewById(R.id.friday);
        satCheckBox = popupView.findViewById(R.id.saturday);
        sunCheckBox = popupView.findViewById(R.id.sunday);

        editWeeks = popupView.findViewById(R.id.edit_weeks);
        editDistance = popupView.findViewById(R.id.edit_distance);

        butterflyCheckBox = popupView.findViewById(R.id.butterfly);
        backstrokeCheckBox = popupView.findViewById(R.id.backstroke);
        breaststrokeCheckBox = popupView.findViewById(R.id.breaststroke);
        freestyleCheckBox = popupView.findViewById(R.id.freestyle);

        //legsCheckBox = popupView.findViewById(R.id.legs);
        kickCheckBox = popupView.findViewById(R.id.kick);
        finsCheckBox = popupView.findViewById(R.id.fins);
        //armsCheckBox = popupView.findViewById(R.id.arms);
        buoyCheckBox = popupView.findViewById(R.id.buoy);
        paddlesCheckBox = popupView.findViewById(R.id.paddles);

        progressBar = findViewById(R.id.progress_bar);

        listViewOfWorkouts = findViewById(R.id.list_of_trainings_in_a_day);
        arrayOfWorkouts = new ArrayList<>();

        generate = popupView.findViewById(R.id.generate);

        MainMenuActivity.downloadPhoto(profileButton, personPhoto, this);
        // =======================================
    }

    /**
     * Function which check if all needed fields oof popup are filled.
     *
     * @return true if validation is successful
     */
    private boolean validate() {
        return (monCheckBox.isChecked() |
                tueCheckBox.isChecked() |
                wedCheckBox.isChecked() |
                thuCheckBox.isChecked() |
                friCheckBox.isChecked() |
                satCheckBox.isChecked() |
                sunCheckBox.isChecked()) &&
                !editWeeks.getText().toString().equals("") &&
                !editDistance.getText().toString().equals("") &&
                (butterflyCheckBox.isChecked() |
                backstrokeCheckBox.isChecked() |
                breaststrokeCheckBox.isChecked() |
                freestyleCheckBox.isChecked());
    }

    /**
     * @return
     */
    private boolean[] getDays() {
        boolean[] days = {false, false, false, false, false, false, false};
        days[0] = monCheckBox.isChecked();
        days[1] = tueCheckBox.isChecked();
        days[2] = wedCheckBox.isChecked();
        days[3] = thuCheckBox.isChecked();
        days[4] = friCheckBox.isChecked();
        days[5] = satCheckBox.isChecked();
        days[6] = sunCheckBox.isChecked();
        return days;
    }

    /**
     * @return
     */
    private int getNumberOfWeeks() {
        return Integer.parseInt(editWeeks.getText().toString());
    }

    /**
     * @return
     */
    private int getStartingDistance() {
        return Integer.parseInt(editDistance.getText().toString());
    }

    /**
     *
     * @return
     */
    private boolean[] getStrokes() {
       boolean[] strokes = {false, false, false, false};
       strokes[0] = butterflyCheckBox.isChecked();
       strokes[1] = backstrokeCheckBox.isChecked();
       strokes[2] = breaststrokeCheckBox.isChecked();
       strokes[3] = freestyleCheckBox.isChecked();
       return strokes;
    }

    /**
     *
     * @return
     */
    public boolean[] getAdditions() {
        boolean[] additions = {false, false, false, false, false, false};
        additions[0] = false; //legsCheckBox.isChecked();
        additions[1] = kickCheckBox.isChecked();
        additions[2] = finsCheckBox.isChecked();
        additions[3] = false; //armsCheckBox.isChecked();
        additions[4] = buoyCheckBox.isChecked();
        additions[5] = paddlesCheckBox.isChecked();
        return additions;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_AUTHORIZE_MAKE_CALENDAR_ENTRY || requestCode == GET_CALENDAR) {
            if (resultCode == RESULT_OK) {
                makeCalendarEntry(data.getStringArrayListExtra("Days"));
            }
        }
    }

    /**
     * Function which makes new calendar entry in async task.
     */
    private void makeCalendarEntry(List<String> datesOfTrainings) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            MakeCalendarEntryTask task = new MakeCalendarEntryTask(account.getAccount(), SCOPE);
            task.setDays(datesOfTrainings);
            task.execute();
        }
    }

    /**
     * Class which is responsible for making a new calendar entry.
     */
    class MakeCalendarEntryTask extends AsyncTask {
        private final String TAG = "MakeCalendarEntryTask";
        public static final int MAKE_CALENDAR_ENTRY = 102;

        private Account mAccount;
        private String mScope;
        private List<String> mDatesOfTraining;

        String trainingName = TRAINING_NAME;
        String calendarId = "-1";
        String calendarTimeZone = "Europe/Warsaw";

        MakeCalendarEntryTask(Account account, String scope) {
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
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(TrainingPlanActivity.this, Collections.singleton(mScope));
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
                        if (event.getSummary().contains(trainingName)) {
                            service.events().delete(calendarId, event.getId()).execute();
                        }
                    }
                    pageToken = events.getNextPageToken();
                } while (pageToken != null);

                // Generating dates of following trainings.
                int i = 0;
                for (String date : mDatesOfTraining) {
                    i++;
                    Event event = new Event()
                            .setSummary(trainingName + " " + i)
                            .setDescription("A SwimMaster generated training.");

                    DateTime startDateTime = new DateTime(date + "T20:00:00");
                    EventDateTime start = new EventDateTime()
                            .setDateTime(startDateTime)
                            .setTimeZone(calendarTimeZone);
                    event.setStart(start);

                    DateTime endDateTime = new DateTime(date + "T21:00:00");
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone(calendarTimeZone);
                    event.setEnd(end);

                    // Setting up reminder
                    EventReminder[] reminderOverrides = new EventReminder[]{
                            new EventReminder().setMethod("popup").setMinutes(60),
                    };

                    Event.Reminders reminders = new Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(Arrays.asList(reminderOverrides));
                    event.setReminders(reminders);

                    event = service.events().insert(calendarId, event).execute();
                    Log.i(TAG, "Event created: %s\n" + event.getHtmlLink());
                }

            } catch (UserRecoverableAuthIOException userRecoverableException) {
                // Explain to the user again why you need these OAuth permissions
                // And prompt the resolution to the user again:
                startActivityForResult(TrainingPlanActivity.this, userRecoverableException.getIntent(), MAKE_CALENDAR_ENTRY, null);
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
            intent.putExtra("Days", (Parcelable) mDatesOfTraining);
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
            calendarInit();
        }

        private void setDays(List<String> datesOfTraining) {
            this.mDatesOfTraining = datesOfTraining;
        }
    }

    /**
     * Converts String date in format "EEEE, dd/mm/yyyy" to CalendarDay format.
     * @param date
     * @return CalendarDate date
     */
    private CalendarDay swDateToCalendarDay(String date){
        int day = Integer.parseInt(date.substring(date.length()-10, date.length()-8));
        int month = Integer.parseInt(date.substring(date.length()-7, date.length()-5));
        int year = Integer.parseInt(date.substring(date.length()-4, date.length()));
        return CalendarDay.from(year, month, day);
    }

    /**
     *
     * @param date
     */
    private void updateListView(CalendarDay date){
        arrayOfWorkouts.clear();

        for(SingleWorkout w : mWorkoutsList){
            CalendarDay d = swDateToCalendarDay(w.getDate());
            if(date.equals(d)){
                arrayOfWorkouts.add(w);
            }
        }

        for(SingleWorkout w : mTrainingSetWorkoutsList){
            CalendarDay d = swDateToCalendarDay(w.getDate());
            if(date.equals(d)){
                arrayOfWorkouts.add(w);
            }
        }
        arrayAdapterOfWorkouts.notifyDataSetChanged();
    }

    /**
     *
     */
    private void calendarInit(){
        // ========== Calendar view initialising ==========
        calendarView.removeDecorators();
        // Today stamp
        {
            final CalendarDay today = CalendarDay.today();
            HashSet<CalendarDay> days = new HashSet<>();
            days.add(today);
            Drawable drawable = getDrawable(R.drawable.calendar_today_drawable);
            TodaysDecorator decorator = new TodaysDecorator(drawable, days);
            calendarView.addDecorator(decorator);
        }

        // TrainingSet Workouts
        if (!(mTrainingSetWorkoutsList.isEmpty())) {
            final HashSet<CalendarDay> calendarDays;
            calendarDays = new HashSet<>();

            for(SingleWorkout workout : mTrainingSetWorkoutsList){
                String date = workout.getDate();
                CalendarDay swDate = swDateToCalendarDay(date);
                calendarDays.add(swDate);
            }

            eventDecorator = new EventDecorator(12, getResources().getColor(R.color.generated), calendarDays);
            calendarView.addDecorator(eventDecorator);
        }
        // ================================================

        // Logged Workouts
        if (!(mWorkoutsList.isEmpty())) {
            final HashSet<CalendarDay> calendarDays;
            calendarDays = new HashSet<>();

            for(SingleWorkout workout : mWorkoutsList){
                String date = workout.getDate();
                CalendarDay swDate = swDateToCalendarDay(date);
                calendarDays.add(swDate);
            }

            eventDecorator = new EventDecorator(7, getResources().getColor(R.color.logged), calendarDays);
            calendarView.addDecorator(eventDecorator);
        }
    }
}
