package com.example.user.menutoday;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by user on 2016-09-16.
 */
public class CafeteriaAdapter extends ArrayAdapter<String> {
    public CafeteriaAdapter(Context context, ArrayList<String> names) {
        super(context, 0, names);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String name = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.meal, parent, false);
            /*convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        System.out.println("ListItem clicked!");
                    }
                }
            );*/
        }

        TextView cafeteriaName = (TextView) convertView.findViewById(R.id.mealName);

        cafeteriaName.setText(name);
        return convertView;
    }
}
