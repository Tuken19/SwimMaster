package com.example.swimmaster;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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
import java.util.List;
import java.util.Locale;

import static android.app.AlertDialog.Builder;
import static com.example.swimmaster.MainMenuActivity.mWorkoutsList;

public class SingleWorkoutActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private final static String TAG = "SingleWorkoutActivity";
    private DatabaseReference mDatabase;
    private DatabaseReference logDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;

    ListView listViewWarmUpTask;
    ListView listViewMainSetTask;
    ListView listViewCooldownTask;
    ArrayList<Task> warmUpTaskArray;
    ArrayList<Task> mainSetTaskArray;
    ArrayList<Task> cooldownTaskArray;
    TaskAdapter arrayAdapterWarmUpTask;
    TaskAdapter arrayAdapterMainSetTask;
    TaskAdapter arrayAdapterCooldownTask;
    public static final String W_KEY = "Position of Task";

    ActionBar actionBar;
    ImageButton profileButton;

    Calendar dateCalendar;
    EditText editDate;
    EditText editName;
    DatePickerDialog.OnDateSetListener date;

    FloatingActionButton add;
    Button logButton;

    View background;
    View popupView;
    PopupWindow popupWindow;
    Spinner spinnerCategory;
    ArrayAdapter<CharSequence> adapterCategory;
    Spinner spinnerStyles;
    ArrayAdapter<CharSequence> adapterStyles;
    Spinner spinnerDistance;
    ArrayAdapter<CharSequence> adapterDistance;
    Spinner spinnerType;
    ArrayAdapter<CharSequence> adapterType;
    Spinner spinnerPace;
    ArrayAdapter<CharSequence> adapterPace;
    EditText editRepetitions;
    EditText editRest;
    CheckBox legs;
    CheckBox kick;
    CheckBox fins;
    CheckBox arms;
    CheckBox buoy;
    CheckBox paddles;

    Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_workout);

        initializeFields();

        logDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mWorkoutsList.clear();
                    for (DataSnapshot element : dataSnapshot.getChildren()) {
                        SingleWorkout singleWorkout = element.getValue(SingleWorkout.class);
                        mWorkoutsList.add(singleWorkout);
                    }
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

        // ========== List views with tasks ==========
            {
                warmUpTaskArray = new ArrayList<Task>();
                arrayAdapterWarmUpTask = new TaskAdapter(SingleWorkoutActivity.this, warmUpTaskArray);
                listViewWarmUpTask.setAdapter(arrayAdapterWarmUpTask);

                mainSetTaskArray = new ArrayList<Task>();
                arrayAdapterMainSetTask = new TaskAdapter(SingleWorkoutActivity.this, mainSetTaskArray);
                listViewMainSetTask.setAdapter(arrayAdapterMainSetTask);

                cooldownTaskArray = new ArrayList<Task>();
                arrayAdapterCooldownTask = new TaskAdapter(SingleWorkoutActivity.this, cooldownTaskArray);
                listViewCooldownTask.setAdapter(arrayAdapterCooldownTask);

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            warmUpTaskArray.clear();
                            for (DataSnapshot element : dataSnapshot.child("WarmUp").getChildren()) {
                                Task task = element.getValue(Task.class);
                                warmUpTaskArray.add(task);
                                arrayAdapterWarmUpTask.notifyDataSetChanged();
                            }
                            mainSetTaskArray.clear();
                            for (DataSnapshot element : dataSnapshot.child("MainSet").getChildren()) {
                                Task task = element.getValue(Task.class);
                                mainSetTaskArray.add(task);
                                arrayAdapterMainSetTask.notifyDataSetChanged();
                            }
                            cooldownTaskArray.clear();
                            for (DataSnapshot element : dataSnapshot.child("Cooldown").getChildren()) {
                                Task task = element.getValue(Task.class);
                                cooldownTaskArray.add(task);
                                arrayAdapterCooldownTask.notifyDataSetChanged();
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            // =============================================

        // ============= Date Picker =============
        {
            dateCalendar = Calendar.getInstance();
            date = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    dateCalendar.set(Calendar.YEAR, year);
                    dateCalendar.set(Calendar.MONTH, month);
                    dateCalendar.set(Calendar.DAY_OF_MONTH, day);

                    String myFormat = "EEEE, dd/MM/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

                    editDate.setText(sdf.format(dateCalendar.getTime()));

                    editDate.setError(null);
                    if(!TextUtils.isEmpty(editName.getText())){
                        logButton.setEnabled(true);
                    }
                    else{
                        editName.setError("Required.");
                    }
                }
            };

            editDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new DatePickerDialog(SingleWorkoutActivity.this, date,
                            dateCalendar.get(Calendar.YEAR),
                            dateCalendar.get(Calendar.MONTH),
                            dateCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });
        }
        // =======================================

        // ============= Name Picker =============
        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editName.setError(null);
                if(!TextUtils.isEmpty(editDate.getText())){
                    logButton.setEnabled(true);
                }
                else{
                    editDate.setError("Required.");
                }
            }
        });
        // =======================================

        // ================ Add new task ===============
        {
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    popupView = layoutInflater.inflate(R.layout.add_task_popup, null);
                    popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    popupWindow.setFocusable(true);
                    popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    addButton = popupView.findViewById(R.id.addButton);

                    // =========== Spinner category ===========
                    spinnerCategory = popupView.findViewById(R.id.spinner_category);
                    adapterCategory = ArrayAdapter.createFromResource(popupView.getContext(), R.array.category_array, R.layout.custom_spinner);
                    adapterCategory.setDropDownViewResource(R.layout.custom_spinner);
                    spinnerCategory.setAdapter(adapterCategory);
                    spinnerCategory.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) popupView.getContext());
                    // ========================================

                    // =========== Spinner styles ===========
                    spinnerStyles = popupView.findViewById(R.id.spinner_styles);
                    adapterStyles = ArrayAdapter.createFromResource(popupView.getContext(), R.array.swimming_styles, R.layout.custom_spinner);
                    adapterStyles.setDropDownViewResource(R.layout.custom_spinner);
                    spinnerStyles.setAdapter(adapterStyles);
                    spinnerStyles.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) popupView.getContext());
                    // ========================================

                    // =========== Spinner distance ===========
                    spinnerDistance = popupView.findViewById(R.id.spinner_distance);
                    adapterDistance = ArrayAdapter.createFromResource(popupView.getContext(), R.array.distances, R.layout.custom_spinner);
                    adapterDistance.setDropDownViewResource(R.layout.custom_spinner);
                    spinnerDistance.setAdapter(adapterDistance);
                    spinnerDistance.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) popupView.getContext());
                    // =======================================

                    // =========== Spinner type ===========
                    spinnerType = popupView.findViewById(R.id.spinner_type);
                    adapterType = ArrayAdapter.createFromResource(popupView.getContext(), R.array.type_array, R.layout.custom_spinner);
                    adapterType.setDropDownViewResource(R.layout.custom_spinner);
                    spinnerType.setAdapter(adapterType);
                    spinnerType.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) popupView.getContext());
                    // =======================================

                    // =========== Spinner pace ===========
                    spinnerPace = popupView.findViewById(R.id.spinner_pace);
                    adapterPace = ArrayAdapter.createFromResource(popupView.getContext(), R.array.pace_array, R.layout.custom_spinner);
                    adapterPace.setDropDownViewResource(R.layout.custom_spinner);
                    spinnerPace.setAdapter(adapterPace);
                    spinnerPace.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) popupView.getContext());
                    // =======================================

                    // =========== Edit repetitions ==========
                    editRepetitions = popupView.findViewById(R.id.edit_repetitions);
                    // =======================================

                    // ============== Edit rest ==============
                    editRest = popupView.findViewById(R.id.edit_rest);
                    // =======================================

                    // ============= CheckBoxes ==============
                    legs = popupView.findViewById(R.id.legs);
                    kick = popupView.findViewById(R.id.kick);
                    fins = popupView.findViewById(R.id.fins);
                    arms = popupView.findViewById(R.id.arms);
                    buoy = popupView.findViewById(R.id.buoy);
                    paddles = popupView.findViewById(R.id.paddles);
                    // =======================================

                    addButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(validateForm()){
                                String category = spinnerCategory.getSelectedItem().toString();
                                String style = spinnerStyles.getSelectedItem().toString();
                                String distance = spinnerDistance.getSelectedItem().toString();
                                String type = spinnerType.getSelectedItem().toString();
                                String pace = spinnerPace.getSelectedItem().toString();
                                int repetitions = Integer.parseInt(editRepetitions.getText().toString());
                                int rest = Integer.parseInt(editRest.getText().toString());
                                List<String> additions = new ArrayList<>();

                                if(legs.isChecked()){
                                    additions.add(legs.getText().toString());
                                }
                                if(kick.isChecked()){
                                    additions.add(kick.getText().toString());
                                }
                                if(fins.isChecked()){
                                    additions.add(fins.getText().toString());
                                }
                                if(arms.isChecked()){
                                    additions.add(arms.getText().toString());
                                }
                                if(buoy.isChecked()){
                                    additions.add(buoy.getText().toString());
                                }
                                if(paddles.isChecked()){
                                    additions.add(paddles.getText().toString());
                                }

                                Task task;

                                if(category.equals("Warm-up")) {
                                    task = new Task(style, distance, type, pace, repetitions, rest, additions);
                                    warmUpTaskArray.add(task);
                                    mDatabase.child("WarmUp").setValue(warmUpTaskArray);
                                    arrayAdapterWarmUpTask.notifyDataSetChanged();
                                }
                                else if(category.equals("Main set")) {
                                    task = new Task(style, distance, type, pace, repetitions, rest, additions);
                                    mainSetTaskArray.add(task);
                                    mDatabase.child("MainSet").setValue(mainSetTaskArray);
                                    arrayAdapterMainSetTask.notifyDataSetChanged();
                                }
                                else if(category.equals("Cooldown")) {
                                    task = new Task(style, distance, type, pace, repetitions, rest, additions);
                                    cooldownTaskArray.add(task);
                                    mDatabase.child("Cooldown").setValue(cooldownTaskArray);
                                    arrayAdapterCooldownTask.notifyDataSetChanged();
                                }

                                add.setEnabled(true);
                                popupWindow.dismiss();
                            }
                        }
                    });

                    popupWindow.showAtLocation(background, Gravity.BOTTOM, 0, 0);
                }
            });
        }
        // =============================================

        // =============== Deleting tasks ==============
        // ===== WarmUp =====
        listViewWarmUpTask.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final Builder alertDialogBuilder = new Builder(SingleWorkoutActivity.this);
                alertDialogBuilder
                        .setTitle("Do you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Task task = warmUpTaskArray.get(position);
                                for (Task t : warmUpTaskArray) {
                                    if (t.getStyle().equals(task.getStyle()) && t.getDistance().equals(task.getDistance())
                                        && t.getRepetitions() == t.getRepetitions() && t.getPace().equals(task.getPace())
                                        && t.getRest() == task.getRest() && t.getAdditions().equals(task.getAdditions())) {
                                        task = t;
                                    }
                                }
                                warmUpTaskArray.remove(task);
                                String msg = "Style: " + task.getStyle() + " Distance: " + task.getDistance();
                                Log.e(TAG, msg);

                                mDatabase.child("WarmUp").setValue(warmUpTaskArray)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                arrayAdapterWarmUpTask.notifyDataSetChanged();
                                                Toast.makeText(SingleWorkoutActivity.this, "You have successfully deleted an item.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SingleWorkoutActivity.this, "Deletion failed. Try again.", Toast.LENGTH_SHORT).show();
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

        // ===== Main Set =====
        listViewMainSetTask.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final Builder alertDialogBuilder = new Builder(SingleWorkoutActivity.this);
                alertDialogBuilder
                        .setTitle("Do you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Task task = mainSetTaskArray.get(position);
                                for (Task t : mainSetTaskArray) {
                                    if (t.getStyle().equals(task.getStyle()) && t.getDistance().equals(task.getDistance())
                                            && t.getRepetitions() == t.getRepetitions() && t.getPace().equals(task.getPace())
                                            && t.getRest() == task.getRest() && t.getAdditions().equals(task.getAdditions())) {
                                        task = t;
                                    }
                                }
                                mainSetTaskArray.remove(task);
                                String msg = "Style: " + task.getStyle() + " Distance: " + task.getDistance();
                                Log.e(TAG, msg);

                                mDatabase.child("MainSet").setValue(mainSetTaskArray)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                arrayAdapterMainSetTask.notifyDataSetChanged();
                                                Toast.makeText(SingleWorkoutActivity.this, "You have successfully deleted an item.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SingleWorkoutActivity.this, "Deletion failed. Try again.", Toast.LENGTH_SHORT).show();
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

        // ===== Cooldown =====
        listViewCooldownTask.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final Builder alertDialogBuilder = new Builder(SingleWorkoutActivity.this);
                alertDialogBuilder
                        .setTitle("Do you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Task task = cooldownTaskArray.get(position);
                                for (Task t : cooldownTaskArray) {
                                    if (t.getStyle().equals(task.getStyle()) && t.getDistance().equals(task.getDistance())
                                            && t.getRepetitions() == t.getRepetitions() && t.getPace().equals(task.getPace())
                                            && t.getRest() == task.getRest() && t.getAdditions().equals(task.getAdditions())) {
                                        task = t;
                                    }
                                }
                                cooldownTaskArray.remove(task);
                                String msg = "Style: " + task.getStyle() + " Distance: " + task.getDistance();
                                Log.e(TAG, msg);

                                mDatabase.child("Cooldown").setValue(cooldownTaskArray)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                arrayAdapterCooldownTask.notifyDataSetChanged();
                                                Toast.makeText(SingleWorkoutActivity.this, "You have successfully deleted an item.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SingleWorkoutActivity.this, "Deletion failed. Try again.", Toast.LENGTH_SHORT).show();
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
        // =============================================

        // ========= Log Single Workout ================
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editName.getText().toString();
                String myFormat = "dd MM yyyy hh:mm:ss ";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
                String creation_date = sdf.format(Calendar.getInstance().getTime());

                SingleWorkout customSingleWorkout = new SingleWorkout(name, editDate.getText().toString(), warmUpTaskArray, mainSetTaskArray, cooldownTaskArray, creation_date);
                mWorkoutsList.add(customSingleWorkout);
                logDatabase.setValue(mWorkoutsList);
                mDatabase.removeValue();

                editName.clearFocus();
                editDate.clearFocus();

                arrayAdapterWarmUpTask.clear();
                arrayAdapterMainSetTask.clear();
                arrayAdapterCooldownTask.clear();
                arrayAdapterWarmUpTask.notifyDataSetChanged();
                arrayAdapterMainSetTask.notifyDataSetChanged();
                arrayAdapterCooldownTask.notifyDataSetChanged();
                finish();
            }
        });
        // =============================================

        {
            // ========== Add new Single Workout ==========
//        {
//            add.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    final EditText editName = new EditText(SingleWorkoutActivity.this);
//                    editName.setHint(R.string.SW_name);
//
//                    LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                    nameParams.weight = 1;
//                    nameParams.setMargins(50, 10, 50, 10);
//
//                    LinearLayout LL = new LinearLayout(SingleWorkoutActivity.this);
//                    LL.setOrientation(LinearLayout.VERTICAL);
//                    LL.addView(editName, nameParams);
//
//                    final SingleWorkout singleWorkout = new SingleWorkout();
//
//                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SingleWorkoutActivity.this);
//                    alertDialogBuilder
//                            .setTitle("Add new single workout:")
//                            .setView(LL)
//                            .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//
//                                    String name = editName.getText().toString();
//
//                                    if (TextUtils.isEmpty(name)) {
//                                        editName.setError("Required.");
//                                    } else {
//                                        singleWorkout.setPosition(maxSWId);
//                                        singleWorkout.setName(name);
//
//                                        String editDate;
//                                        String myFormat = "EEEE, dd/MM/yyyy";
//                                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);
//                                        editDate = sdf.format(Calendar.getInstance().getTime());
//                                        singleWorkout.setDate(editDate);
//                                    }
//
//                                    mDatabase.child(String.valueOf(maxSWId)).setValue(singleWorkout)
//                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                @Override
//                                                public void onSuccess(Void aVoid) {
//                                                    arrayAdapterSingleWorkouts.notifyDataSetChanged();
//                                                    String msg = "Success: New single workout: " + singleWorkout.getName() + "Time: " + singleWorkout.getDate();
//                                                    Log.e(TAG, msg);
//                                                    Toast.makeText(SingleWorkoutActivity.this, "You have successfully created new workout.", Toast.LENGTH_SHORT).show();
//                                                }
//                                            })
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    String msg = "Failure: New single workout: " + singleWorkout.getName() + "Time: " + singleWorkout.getDate()
//                                                            + "Caused by: " + e.getCause();
//                                                    Log.e(TAG, msg);
//                                                    Toast.makeText(SingleWorkoutActivity.this, "Creation failed. Try again.", Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//                                }
//                            })
//                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    dialogInterface.cancel();
//                                }
//                            });
//                    AlertDialog alertDialog = alertDialogBuilder.create();
//                    alertDialog.show();
//                }
//            });
//        }
            // ============================================
        }

        // ========== Single Workout Info Activity ==========
        {
//            listViewSingleWorkouts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                    SingleWorkout selectedSW = arraySingleWorkout.get(position);
//                    long pos = selectedSW.getPosition();
//
//                    String transitionName = getString(R.string.name_anim);
//                    TextView nameField = view.findViewById(R.id.name_field);
//
//                    String transitionDate = getString(R.string.date_anim);
//                    TextView dateField = view.findViewById(R.id.date_field);
//
//                    Pair[] pair = new Pair[2];
//                    pair[0] = new Pair<View, String>(nameField, transitionName);
//                    pair[1] = new Pair<View, String>(dateField, transitionDate);
//
//                    Intent info = new Intent(SingleWorkoutActivity.this, SingleWorkoutInfoActivity.class);
//                    info.putExtra(W_KEY, pos);
//                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(SingleWorkoutActivity.this, pair);
//                    startActivity(info, transitionActivityOptions.toBundle());
//                }
//            });
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
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("SingleWorkout");
        logDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("TrainingLog");

        listViewWarmUpTask = findViewById(R.id.list_view_warm_up);
        listViewMainSetTask = findViewById(R.id.list_view_main_set);
        listViewCooldownTask = findViewById(R.id.list_view_cooldown);

        editDate = findViewById(R.id.edit_date);
        editName = findViewById(R.id.edit_name);

        // =============== Buttons ===============
        add = findViewById(R.id.add);
        logButton = findViewById(R.id.log);
        // =======================================

        // ====== Add Single Workout popup =======
        background = findViewById(R.id.background);
        // =======================================
    }

    private boolean validateForm() {
        boolean valid = true;

        if (spinnerCategory.getSelectedItemPosition() == 0) {
            String err = "Please choose correct category from list.";
            Toast.makeText(SingleWorkoutActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (spinnerStyles.getSelectedItemPosition() == 0) {
            String err = "Please choose correct style from list.";
            Toast.makeText(SingleWorkoutActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (spinnerDistance.getSelectedItemPosition() == 0) {
            String err = "Please choose correct distance from list.";
            Toast.makeText(SingleWorkoutActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (spinnerType.getSelectedItemPosition() == 0) {
            String err = "Please choose correct type from list.";
            Toast.makeText(SingleWorkoutActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (spinnerPace.getSelectedItemPosition() == 0) {
            String err = "Please choose correct pace from list.";
            Toast.makeText(SingleWorkoutActivity.this, err, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        String repetitions = editRepetitions.getText().toString();
        if (TextUtils.isEmpty(repetitions)) {
            editRepetitions.setError("Required.");
            valid = false;
        } else {
            editRepetitions.setError(null);
        }

        String rest = editRest.getText().toString();
        if (TextUtils.isEmpty(rest)) {
            editRest.setError("Required.");
            valid = false;
        } else {
            editRest.setError(null);
        }

        return valid;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
