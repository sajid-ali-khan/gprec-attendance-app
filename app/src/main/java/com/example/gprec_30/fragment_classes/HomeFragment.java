package com.example.gprec_30.fragment_classes;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.gprec_30.R;
import com.example.gprec_30.utils.DataFetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    TableLayout tbl_classProfile;
    TextView profileStatus;
    String empId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.home_fragment, container, false);

        tbl_classProfile = rootView.findViewById(R.id.tbl_classProfile);
        profileStatus = rootView.findViewById(R.id.profileStatus);

        if (getArguments() != null) {
            empId = getArguments().getString("emp_id");
            Log.d("HomeFragment", "Emp ID from arguments: " + empId);
        }

        DataFetcher dataFetcher = new DataFetcher();

        //fetch the class assigned to this employee
        List<String> classes = dataFetcher.getEmployeeAssignmentsSimple(empId);

        if(classes.isEmpty()){
            //display a text that no classes are assigned for him
            profileStatus.setText("No classes are assigned to you.");
        }else{
            tbl_classProfile.setPadding(16, 16, 16, 16);
            //fetch the number of rows in each of this class registers
            HashMap<String, Integer> classCounts = dataFetcher.getNumPeriodsMarkedForEachClass(classes);

            populateTable(classCounts);
        }


        return rootView;
    }

    private void populateTable(HashMap<String, Integer> classCounts){
        TableRow headerRow = new TableRow(requireContext());
        String[] headers = {"Class", "No. of Periods Marked"};
        for (String header : headers) {
            TextView headerCell = new TextView(requireContext());
            headerCell.setText(header);
            headerCell.setPadding(16, 16, 16, 16);
            headerCell.setTypeface(null, Typeface.BOLD);
            headerCell.setGravity(Gravity.CENTER);
            headerCell.setBackgroundResource(R.drawable.border_cell);
            headerRow.addView(headerCell);
        }
        tbl_classProfile.addView(headerRow);

        for (Map.Entry<String, Integer> entry : classCounts.entrySet()){
            TableRow row = getTableRow(entry.getKey(), entry.getValue());
            tbl_classProfile.addView(row);
        }

    }

    private TableRow getTableRow(String className, int numClassesMarked) {
        TableRow row = new TableRow(requireContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        String[] rowData = {className, String.valueOf(numClassesMarked)};
        for (String cellData : rowData) {
            TextView cell = new TextView(requireContext());
            cell.setText(cellData);
            cell.setGravity(Gravity.CENTER);
            cell.setBackgroundResource(R.drawable.border_cell);
            cell.setPadding(16, 16, 16, 16);
            row.addView(cell);
        }
        return row;
    }

}

