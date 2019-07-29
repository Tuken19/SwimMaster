package com.example.swimmaster;


import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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
import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 */
public class FreestyleFragment extends Fragment {

    private final static String TAG = "ButterflyFragment";
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mFBUser;

    ListView listViewTimes;
    TimesAdapter arrayAdapterTimes;
    ArrayList<Time> arrayTimes;

    public FreestyleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_freestyle, container, false);

        TextView titleForFragments = view.findViewById(R.id.titleForFragments);
        String text = getResources().getString(R.string.title_for_fragments);
        String styleName = getResources().getString(R.string.freestyle);
        titleForFragments.setText(String.format(text, styleName));

        mAuth = FirebaseAuth.getInstance();
        mFBUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mFBUser.getUid()).child("Times");


        listViewTimes = view.findViewById(R.id.list_view_freestyle);
        arrayTimes = new ArrayList<>();
        arrayAdapterTimes = new TimesAdapter(getContext(), arrayTimes);
        listViewTimes.setAdapter(arrayAdapterTimes);


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayTimes.clear();
                for (DataSnapshot element : dataSnapshot.getChildren()) {
                    Time time = element.getValue(Time.class);
                    if (time.getStyle().equals("Freestyle")) {
                        arrayTimes.add(time);
                        arrayAdapterTimes.notifyDataSetChanged();
                    }
                }

                Collections.sort(arrayTimes, Time.timeComparator);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        listViewTimes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int position, long l) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder
                        .setTitle("Do you want to delete this item?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final Time time = arrayTimes.get(position);
                                long key = time.getPosition();
                                String msg = "Distance: " + time.getDistance() + "Time: " + time.getTime();
                                Log.e(TAG, msg);

                                mDatabase.child(String.valueOf(key)).child("style").setValue("none")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                arrayAdapterTimes.notifyDataSetChanged();
                                                Toast.makeText(getContext(), "You have successfully deleted an item.", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), "Deletion failed. Try again.", Toast.LENGTH_SHORT).show();
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

        return view;
    }

}
