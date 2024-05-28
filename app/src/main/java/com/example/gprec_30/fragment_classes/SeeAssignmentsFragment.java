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
import com.example.gprec_30.utils.Assignment;
import com.example.gprec_30.utils.BranchYearExtractor;
import com.example.gprec_30.utils.DataFetcher;
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
    List<Assignment> assignmentList;

    String ph_scheme, ph_branch, ph_year, ph_sem, ph_sec;

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

        ph_scheme = getString(R.string.ph_scheme);
        ph_branch = getString(R.string.ph_branch);
        ph_year = getString(R.string.ph_year);
        ph_sem = getString(R.string.ph_sem);
        ph_sec = getString(R.string.ph_section);

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
                selectedScheme = parent.getItemAtPosition(position).toString();
                try {
                    updateBranchSpinner();
                } catch (SQLException e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinnerBranch.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBranchName = parent.getItemAtPosition(position).toString();
                try {
                    updateYearSpinner();
                } catch (SQLException e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinnerYear.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
                selectedBranchYear = BranchYearExtractor.generateBranchCode(selectedBranchName, selectedYear);
                try {
                    updateSemSpinner();
                } catch (SQLException e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinnerSemester.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSemester = parent.getItemAtPosition(position).toString();
                updateSectionSpinner();
            }
        });

        spinnerSection.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSection = parent.getItemAtPosition(position).toString();
            }
        });
    }

    private void loadSchemeSpinner() throws SQLException {
        schemes = dataFetcher.fetchSchemes();
        SpinnerHelper.populateSpinner(spinnerScheme, schemes);
    }

    private void updateBranchSpinner() throws SQLException {
        branches = dataFetcher.fetchBranches(selectedScheme);
        SpinnerHelper.populateSpinner(spinnerBranch, branches);
    }

    private void updateYearSpinner() throws SQLException {
        years = dataFetcher.fetchYears(selectedScheme, selectedBranchName);
        SpinnerHelper.populateSpinner(spinnerYear, years);
    }

    private void updateSemSpinner() throws SQLException {
        sems = dataFetcher.fetchSemesters(selectedScheme, selectedBranchYear);
        SpinnerHelper.populateSpinner(spinnerSemester, sems);
    }

    private void updateSectionSpinner() {
        sections = dataFetcher.fetchSections();
        SpinnerHelper.populateSpinner(spinnerSection, sections);
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

        assignmentList = dataFetcher.fetchAssignments(selectedScheme, selectedBranchName, Integer.parseInt(selectedYear), Integer.parseInt(selectedSemester), selectedSection);

        // Get the table layout


        // Remove all existing rows except the header row
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        // Iterate over the assignment list and add rows dynamically
        for (Assignment assignment : assignmentList) {
            // Create a new row
            TableRow row = new TableRow(requireContext());

            // Create TextViews for each assignment field
            TextView subCodeTextView = new TextView(requireContext());
            subCodeTextView.setText(assignment.getSubCode());
            subCodeTextView.setBackgroundResource(R.drawable.border_cell);

            TextView empNameTextView = new TextView(requireContext());
            empNameTextView.setText(assignment.getEmpName());
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
        return !selectedScheme.equals(ph_scheme) && !selectedBranchName.equals(ph_branch) && !selectedYear.equals(ph_year) && !selectedSemester.equals(ph_sem) && !selectedSection.equals(ph_sec);
    }
}
