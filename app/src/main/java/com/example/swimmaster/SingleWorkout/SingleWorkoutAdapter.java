package com.example.swimmaster.SingleWorkout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.swimmaster.R;
import com.example.swimmaster.SingleWorkout.SingleWorkout;

import java.util.ArrayList;
import java.util.List;

public class SingleWorkoutAdapter extends ArrayAdapter<SingleWorkout> {

    private Context mContext;
    private List<SingleWorkout> mSingleWorkoutList = new ArrayList<>();

    public SingleWorkoutAdapter(@NonNull Context context, @NonNull ArrayList<SingleWorkout> list) {
        super(context, 0, list);
        mContext = context;
        mSingleWorkoutList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.custom_single_workout_array_adapter, parent, false);
        }

        SingleWorkout currentSingleWorkout = mSingleWorkoutList.get(position);

        TextView numberField = listItem.findViewById(R.id.number_field);
        numberField.setText(position + 1 + ".");

        TextView nameField = listItem.findViewById(R.id.name_field);
        nameField.setText(currentSingleWorkout.getName());

        TextView dateField = listItem.findViewById(R.id.date_field);
        dateField.setText(currentSingleWorkout.getDate());

        return listItem;
    }

}
