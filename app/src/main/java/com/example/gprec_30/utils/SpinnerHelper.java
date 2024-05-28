package com.example.gprec_30.utils;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.example.gprec_30.fragment_classes.AssignClassFragment;

import java.util.List;

public class SpinnerHelper {
    public static void populateSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(spinner.getContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }



    // Custom Spinner item selected listener
    public static abstract class SpinnerItemSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Handle case when nothing is selected
        }
    }

    public static void populateEmployees(AutoCompleteTextView autotvEmp, List<String> employees){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(autotvEmp.getContext(), android.R.layout.simple_dropdown_item_1line, employees);
        autotvEmp.setAdapter(adapter);
        autotvEmp.setThreshold(1);
    }
}
