package com.example.gprec_30.fragment_classes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.gprec_30.R;
import com.example.gprec_30.utils.BranchYearExtractor;
import com.example.gprec_30.utils.ClassAssignment;
import com.example.gprec_30.utils.DataFetcher;
import com.example.gprec_30.utils.HintArrayAdapter;
import com.example.gprec_30.utils.RegesterCodeCreator;
import com.example.gprec_30.utils.SpinnerHelper;

import java.sql.SQLException;
import java.util.List;

public class SeeAssignmentsFragment extends Fragment {

    private Spinner spinnerScheme, spinnerBranch, spinnerYear, spinnerSemester, spinnerSection;
    Button buttonSearch;

    TableLayout tableLayout;

    TextView tableHeading;
    private String selectedScheme, selectedBranchName, selectedYear, selectedSemester, selectedSection, selectedBranchYear;

    public SeeAssignmentsFragment() {

    }

    DataFetcher dataFetcher = new DataFetcher();

    List<String> schemes, branches, years, sems, sections;
    List<ClassAssignment> assignmentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_see_assignments, container, false);

        spinnerScheme = rootView.findViewById(R.id.spinnerScheme);
        spinnerBranch = rootView.findViewById(R.id.spinnerBranch);
        spinnerYear = rootView.findViewById(R.id.spinnerYear);
        spinnerSemester = rootView.findViewById(R.id.spinnerSemester);
        spinnerSection = rootView.findViewById(R.id.spinnerSection);
        buttonSearch = rootView.findViewById(R.id.buttonSearch);
        tableLayout = rootView.findViewById(R.id.tableLayoutAssignments);
        tableHeading = rootView.findViewById(R.id.textViewHeading);


        spinnerListeners();

        buttonSearch.setOnClickListener(v -> {
            try {
                fetchClassAssignments();
            } catch (SQLException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        try {
            loadSchemeSpinner();
        } catch (SQLException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return rootView;
    }


    private void spinnerListeners() {
        spinnerScheme.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    //ignore
                    selectedScheme = "";
                }else{
                    selectedScheme = schemes.get(position);
                    try {
                        updateBranchSpinner();
                    } catch (SQLException e) {
                        logError(e.getMessage());
                    }
                }
            }
        });

        spinnerBranch.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedBranchName = "";
                }else{
                    selectedBranchName = branches.get(position);
                    try {
                        updateYearSpinner();
                    } catch (SQLException e) {
                        logError(e.getMessage());
                    }
                }
            }
        });

        spinnerYear.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedYear = "";
                }else{
                    selectedYear = years.get(position);
                    selectedBranchYear = BranchYearExtractor.generateBranchCode(selectedBranchName, selectedYear);
                    try {
                        updateSemSpinner();
                    } catch (SQLException e) {
                        logError(e.getMessage());
                    }
                }
            }
        });

        spinnerSemester.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedSemester = "";
                }else{
                    selectedSemester = sems.get(position);
                    updateSectionSpinner();
                }
            }
        });

        spinnerSection.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedSection = "";
                }else{
                    selectedSection = sections.get(position);
                }
            }
        });
    }

    private void loadSchemeSpinner() throws SQLException {
        schemes = dataFetcher.fetchSchemes();
        HintArrayAdapter adapter = new HintArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, schemes, "Select a Scheme...");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerScheme.setAdapter(adapter);
    }

    private void updateBranchSpinner() throws SQLException {
        branches = dataFetcher.fetchBranches(selectedScheme);
        HintArrayAdapter adapter = new HintArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, branches, "Select a Branch...");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBranch.setAdapter(adapter);
    }

    private void updateYearSpinner() throws SQLException {
        years = dataFetcher.fetchYears(selectedScheme, selectedBranchName);
        HintArrayAdapter adapter = new HintArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, years, "Select a Year...");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(adapter);
    }

    private void updateSemSpinner() throws SQLException {
        sems = dataFetcher.fetchSemesters(selectedScheme, selectedBranchYear);
        HintArrayAdapter adapter = new HintArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, sems, "Select a Semester...");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(adapter);
    }

    private void updateSectionSpinner() {
        sections = dataFetcher.fetchSections();
        HintArrayAdapter adapter = new HintArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, sections, "Select a Section...");
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSection.setAdapter(adapter);
    }

    private void fetchClassAssignments() throws SQLException {
        if(!allSelected()){
            Toast.makeText(requireContext(), "Please select all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("SearchCriteria", "Scheme: " + selectedScheme +
                ", Branch: " + selectedBranchName +
                ", Year: " + selectedYear +
                ", Semester: " + selectedSemester +
                ", Section: " + selectedSection);

        String class_name = selectedSemester+" " + selectedBranchName + " " + selectedSection;
        tableHeading.setText(class_name);
        String reg_code = RegesterCodeCreator.createRegCode(selectedScheme, selectedBranchName, selectedSemester, selectedSection);
        Log.d("seeAssignmentsClass", "reg code = "+reg_code);
        assignmentList = dataFetcher.getClassAssignments(reg_code);

        // Get the table layout


        // Remove all existing rows except the header row
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        // Iterate over the assignment list and add rows dynamically
        for (ClassAssignment assignment : assignmentList) {
            // Create a new row
            TableRow row = new TableRow(requireContext());

            // Create TextViews for each assignment field
            TextView subCodeTextView = new TextView(requireContext());
            subCodeTextView.setText(assignment.getScode());
            subCodeTextView.setBackgroundResource(R.drawable.border_cell);

            TextView empNameTextView = new TextView(requireContext());
            empNameTextView.setText(assignment.getEmployeeName());
            empNameTextView.setBackgroundResource(R.drawable.border_cell);

            TextView empIdTextView = new TextView(requireContext());
            empIdTextView.setText(assignment.getEmpId());
            empIdTextView.setBackgroundResource(R.drawable.border_cell);

            // Add TextViews to the row
            row.addView(subCodeTextView);
            row.addView(empNameTextView);
            row.addView(empIdTextView);

            // Add the row to the table layout
            tableLayout.addView(row);
        }
    }
    private boolean allSelected(){
        return !selectedScheme.isEmpty() && !selectedBranchName.isEmpty() && !selectedYear.isEmpty() && !selectedSemester.isEmpty() && !selectedSection.isEmpty();
    }
    private void logError(String msg){
        Log.d("AssignClass Fragment", "logError: "+msg);
    }

}
