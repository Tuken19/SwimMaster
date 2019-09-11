package com.example.swimmaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context mContext;
    private List<Task> mTaskList = new ArrayList<>();

    public TaskAdapter(@NonNull Context context, @NonNull ArrayList<Task> list) {
        super(context, 0, list);
        mContext = context;
        mTaskList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.custom_task_array_adapter, parent, false);
        }

        Task currentTask = mTaskList.get(position);

        TextView numberField = listItem.findViewById(R.id.number_field);
        numberField.setText(position + 1 + ".");

        TextView distanceField = listItem.findViewById(R.id.distance_field);
        distanceField.setText(currentTask.getDistance());

        TextView styleField = listItem.findViewById(R.id.style_field);
        styleField.setText(currentTask.getStyle());

        TextView typeField = listItem.findViewById(R.id.type_field);
        typeField.setText(currentTask.getType());

        TextView repetitionsField = listItem.findViewById(R.id.repetitions_field);
        repetitionsField.setText(String.valueOf(currentTask.getRepetitions()));

        TextView paceField = listItem.findViewById(R.id.pace_field);
        paceField.setText(currentTask.getPace());

        TextView restField = listItem.findViewById(R.id.rest_field);
        restField.setText(String.valueOf(currentTask.getRest()));

        TextView additionsField = listItem.findViewById(R.id.additions_field);
        if(!currentTask.isAdditionsEmpty()){
            additionsField.setVisibility(View.VISIBLE);
            List<String> additions = currentTask.getAdditions();
            String additionsText = "";
            for (String a : additions){
                additionsText += "\u2022 "+a;
                additionsText += "\t\t\t";
            }
            additionsField.setText(additionsText);
        }

        return listItem;
    }
}
