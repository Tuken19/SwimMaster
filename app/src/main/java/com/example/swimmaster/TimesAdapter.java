package com.example.swimmaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TimesAdapter extends ArrayAdapter<Time> {

    private Context mContext;
    private List<Time> timesList = new ArrayList<>();

    public TimesAdapter(@NonNull Context context, @NonNull ArrayList<Time> list) {
        super(context, 0 , list);
        mContext = context;
        timesList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.custom_times_array_adapter, parent, false);

        Time currentTime = timesList.get(position);

        TextView numberField = listItem.findViewById(R.id.number_field);
        numberField.setText(position + 1 + ".");

        TextView timeField = listItem.findViewById(R.id.time_field);
        timeField.setText(currentTime.getTime());

        TextView distanceField = listItem.findViewById(R.id.distance_field);
        distanceField.setText(currentTime.getDistance());

        TextView dateField = listItem.findViewById(R.id.date_field);
        dateField.setText(currentTime.getDate());

        return listItem;
    }

}
