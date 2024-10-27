package com.example.gprec_30.fragment_classes;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.gprec_30.R;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Report extends Fragment {

    Spinner sp_branch;
    Spinner sp_sem;
    Spinner sp_section;

    EditText et_from, et_to;

    public Report() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        sp_branch = rootView.findViewById(R.id.sp_branch);
        sp_sem = rootView.findViewById(R.id.sp_sem);
        sp_section =rootView.findViewById(R.id.sp_section);
        et_from = rootView.findViewById(R.id.date_from);
        et_to = rootView.findViewById(R.id.date_to);

        et_from.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("FROM")
                    .build();

            // Show the date picker
            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date(selection);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                et_from.setText(dateFormat.format(date)); // Display in EditText
            });
        });

        et_to.setOnClickListener(v->{
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("TO")
                    .build();

            // Show the date picker
            datePicker.show(getChildFragmentManager(), "DATE_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Date date = new Date(selection);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                et_to.setText(dateFormat.format(date)); // Display in EditText
            });
        });

        return rootView;
    }
}