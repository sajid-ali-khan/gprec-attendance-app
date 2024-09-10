package com.example.gprec_30.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class HintArrayAdapter extends ArrayAdapter<String> {

    private String hint;

    public HintArrayAdapter(Context context, int resource, List<String> objects, String hint) {
        // Add the hint dynamically inside the constructor
        super(context, resource, objects);
        this.hint = hint;
        // Add the hint at the first position
        if (!objects.contains(hint)) {
            objects.add(0, hint);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(getItem(position));

        if (position == 0) {
            textView.setTextColor(android.graphics.Color.GRAY); // Hint item color
        } else {
            textView.setTextColor(android.graphics.Color.BLACK); // Normal item color
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(getItem(position));

        if (position == 0) {
            textView.setTextColor(android.graphics.Color.GRAY); // Hint item color
        } else {
            textView.setTextColor(android.graphics.Color.BLACK); // Normal item color
        }

        return convertView;
    }
}
