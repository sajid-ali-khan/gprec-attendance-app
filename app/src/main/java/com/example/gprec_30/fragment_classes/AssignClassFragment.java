package com.example.gprec_30.fragment_classes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.gprec_30.R;
import com.example.gprec_30.utils.BranchYearExtractor;
import com.example.gprec_30.utils.DataFetcher;
import com.example.gprec_30.utils.DatabaseHelper;
import com.example.gprec_30.utils.SpinnerHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


public class AssignClassFragment extends Fragment {

    private Spinner spinnerScheme, spinnerBranch, spinnerYear, spinnerSemester, spinnerSection, spinnerSubject;
    private AutoCompleteTextView autotv_employee;
    private Button buttonAssignClass;

    // Variables to hold selected values
    private String selectedScheme, selectedBranch, selectedYear, selectedSemester, selectedSection, selectedBranchYear, selectedSubject, sub_code, selectedEmployee, empName;
    int empId;

    DataFetcher dataFetcher = new DataFetcher();

    public AssignClassFragment() {
        // Required empty public constructor
    }

    // Variables to hold placeholders of spinners
    String ph_scheme, ph_branch, ph_year, ph_sem, ph_sec, ph_sub, ph_emp;

    //items lists that are to be loaded in spinners

    List<String> schemes, branches, years, sems, sections, subjects, employees;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_assign_class, container, false);

        // Initialize views
        spinnerScheme = rootView.findViewById(R.id.spinnerScheme);
        spinnerBranch = rootView.findViewById(R.id.spinnerBranch);
        spinnerYear = rootView.findViewById(R.id.spinnerYear);
        spinnerSemester = rootView.findViewById(R.id.spinnerSemester);
        spinnerSection = rootView.findViewById(R.id.spinnerSection);
        spinnerSubject = rootView.findViewById(R.id.spinnerSubject);
        autotv_employee = rootView.findViewById(R.id.editTextTeacher);
        buttonAssignClass = rootView.findViewById(R.id.buttonAssignClass);

        // Initialize placeholders of spinners
        ph_scheme = getString(R.string.ph_scheme);
        ph_branch = getString(R.string.ph_branch);
        ph_year = getString(R.string.ph_year);
        ph_sem = getString(R.string.ph_sem);
        ph_sec = getString(R.string.ph_section);
        ph_sub = getString(R.string.ph_sub);
        ph_emp = getString(R.string.ph_empid);

        spinnerListeners();

        buttonAssignClass.setOnClickListener(v -> showConfirmationDialog());


        try {
            loadSchemeSpinner();
            loadEmployees();
        } catch (SQLException e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void spinnerListeners(){
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
                selectedBranch = parent.getItemAtPosition(position).toString();
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
                selectedBranchYear = BranchYearExtractor.generateBranchCode(selectedBranch, selectedYear);
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
                try {
                    updateSubjectSpinner();
                } catch (SQLException e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinnerSubject.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = parent.getItemAtPosition(position).toString();
                int startIndex = selectedSubject.indexOf("(");
                int endIndex = selectedSubject.lastIndexOf(")");
                if (startIndex != -1 && endIndex != -1) {
                    sub_code = selectedSubject.substring(startIndex + 1, endIndex);
                    Log.e("my testing","Subject code for "+ selectedSubject + " is "+sub_code);
                } else {
                    Log.e("my testing", "Subject " + selectedSubject + " doesn't contain parentheses.");
                }

            }
        });

        autotv_employee.setOnItemClickListener((parent, view, position, id) -> {
            selectedEmployee = (String) parent.getItemAtPosition(position);

            String[] parts = selectedEmployee.split(" - ");
            String empIdStr = parts[0]; // The employee ID is the first part
            empName = parts[1]; // The employee name is the second part

            try {
                empId = Integer.parseInt(empIdStr);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadSchemeSpinner() throws SQLException {
        schemes = dataFetcher.fetchSchemes();
        SpinnerHelper.populateSpinner(spinnerScheme, schemes);
    }

    private void loadEmployees() throws SQLException {
        employees = dataFetcher.fetchEmployees();
        SpinnerHelper.populateEmployees(autotv_employee, employees);
    }
    private void updateBranchSpinner() throws SQLException {
        branches = dataFetcher.fetchBranches(selectedScheme);
        SpinnerHelper.populateSpinner(spinnerBranch, branches);
    }
    private void updateYearSpinner() throws SQLException {
        years = dataFetcher.fetchYears(selectedScheme, selectedBranch);
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
    private void updateSubjectSpinner() throws SQLException {
        subjects = dataFetcher.fetchSubjects(selectedScheme, selectedBranchYear, selectedSemester);
        SpinnerHelper.populateSpinner(spinnerSubject, subjects);
    }


    // Method to handle class assignment
    private void assignClass() throws SQLException {
        if(allSelected()){
            if(!isDataExists()){
                {
                    try (Connection con = DatabaseHelper.SQLConnection()) {
                        String insertQuery = "INSERT INTO AssignmentsTable (employee_id, employee_name, branch, sem, section, scheme, scode) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement pst = con.prepareStatement(insertQuery)) {
                            pst.setInt(1, empId);
                            pst.setString(2, empName);
                            pst.setString(3, selectedBranchYear);
                            pst.setString(4, selectedSemester);
                            pst.setString(5, selectedSection);
                            pst.setString(6, selectedScheme);
                            pst.setString(7, sub_code);

                            int rowsAffected = pst.executeUpdate();
                            if (rowsAffected > 0) {
                                Toast.makeText(requireContext(), "Data submitted successfully.", Toast.LENGTH_SHORT).show();
                                // Clear the selection fields after successful submission
                                clearSelectionFields();
                            } else {
                                Toast.makeText(requireContext(), "Error occured while submitting the data!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (SQLException e) {
                        showToast(e.getMessage());
                    }
                }
            }else {
                showDeleteConfirmationDialog();
            }
        }

        // Perform your logic for assigning the class here
        // This might involve database operations or other actions
        // You can pass these values to a method that handles the assignment process
    }
    private void performDeletion() throws SQLException {
        Connection con=DatabaseHelper.SQLConnection();

        if(selectedEmployee.isEmpty()){
            Toast.makeText(requireContext(), "Please select the employee.", Toast.LENGTH_SHORT).show();
        }

        if(!isDataExists()){
            Toast.makeText(requireContext(), "The Record doesn't exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        String insertQuery = "delete from AssignmentsTable where employee_id = ? and branch = ? and sem = ? and section = ? and scode = ? and  scheme = ?";

        try (PreparedStatement pst = con.prepareStatement(insertQuery)) {
            pst.setInt(1, empId);
            pst.setInt(2, Integer.parseInt(selectedBranchYear));
            pst.setInt(3, Integer.parseInt(selectedSemester));
            pst.setString(4, selectedSection);
            pst.setString(5, sub_code);
            pst.setString(6, selectedScheme);



            int rowsAffected = pst.executeUpdate();
            if (rowsAffected > 0) {
                Toast.makeText(requireContext(), "Assignment deleted successfully.", Toast.LENGTH_SHORT).show();
                // Clear the selection fields after successful submission
                clearSelectionFields();
            } else {
                Toast.makeText(requireContext(), "The Record doesn't exist.", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            Log.e("HomeActivity", "SQLException in submitSelectionsToDatabase", e);
            Toast.makeText(requireContext(), "Database error occurred.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean allSelected(){
        if(selectedScheme.equals(ph_scheme) || selectedBranch.equals(ph_branch) || selectedYear.equals(ph_year) || selectedSemester.equals(ph_sem) || selectedSection.equals(ph_sec)|| selectedSubject.equals(ph_sub) || selectedEmployee.isEmpty()){
            return false;
        }
        return true;
    }

    private void showConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to submit the data ?");
        builder.setPositiveButton("Yes", ((dialog, which) -> {
            try {
                assignClass();
            } catch (SQLException e) {
                showToast(e.getMessage());
            }
        }));
        builder.setNegativeButton("No", null);
        builder.show();
    }
    private void showDeleteConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Data Exists");
        builder.setMessage("Data already exists. Do you want to delete it?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            try {
                performDeletion();
            } catch (SQLException e) {
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
    private boolean isDataExists() throws SQLException {
        Connection con= DatabaseHelper.SQLConnection();

        String query = "select employee_id, employee_name, branch, sem, section, scode, scheme from AssignmentsTable where employee_id = ? and branch = ? and sem = ? and section = ? and scode = ? and  scheme = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, empId);
            pst.setInt(2, Integer.parseInt(selectedBranchYear));
            pst.setInt(3, Integer.parseInt(selectedSemester));
            pst.setString(4, selectedSection);
            pst.setString(5, sub_code);
            pst.setString(6, selectedScheme);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            Log.e("HomeActivity", "Error checking data existence: " + e.getMessage(), e);
            throw e;
        }
        return false;
    }

    private void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void clearSelectionFields(){
        spinnerScheme.setSelection(0);
        autotv_employee.setText("");
    }
}

