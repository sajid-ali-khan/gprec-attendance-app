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
import com.example.gprec_30.utils.RegesterCodeCreator;
import com.example.gprec_30.utils.SpinnerHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class AssignClassFragment extends Fragment {

    private Spinner spinnerScheme, spinnerBranch, spinnerYear, spinnerSemester, spinnerSection, spinnerSubject;
    private AutoCompleteTextView autotv_employee;

    // Variables to hold selected values
    private String selectedScheme, selectedBranch, selectedYear, selectedSemester, selectedSection, selectedBranchYear, selectedSubject, selectedEmployee, empName;
    private String sub_name, sub_code;
    private int assignment_id;
    int empId;

    DataFetcher dataFetcher = new DataFetcher();

    public AssignClassFragment() {
        // Required empty public constructor
    }

    // Variables to hold placeholders of spinners
    String ph_scheme, ph_branch, ph_year, ph_sem, ph_sec, ph_sub, ph_emp;

    //items lists that are to be loaded in spinners

    List<String> schemes, branches, years, sems, sections, subjects, employees;

    int courseId;
    String reg_code;

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
        Button buttonAssignClass = rootView.findViewById(R.id.buttonAssignClass);

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
//                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
//                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
//                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
//                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        spinnerSubject.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = parent.getItemAtPosition(position).toString();

                int startIndex = selectedSubject.indexOf("(");
                int endIndex = selectedSubject.lastIndexOf(")");
                if (!selectedSubject.equals(ph_sub)) {
                    sub_name = selectedSubject.substring(0, selectedSubject.indexOf("("));
                    if (startIndex != -1 && endIndex != -1) {
                        sub_code = selectedSubject.substring(startIndex + 1, endIndex);
                        Log.e("my testing", "Subject code for " + selectedSubject + " is " + sub_code);
                    } else {
                        Log.e("my testing", "Subject " + selectedSubject + " doesn't contain parentheses.");
                    }
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
            Log.d("Selected Params", empId+" "+empName+" "+selectedScheme+" "+selectedBranch+" "+selectedSemester+" "+sub_name);
            courseId = dataFetcher.getCourseId(selectedScheme, selectedBranchYear, selectedSemester, sub_code, sub_name);
            reg_code = RegesterCodeCreator.createRegCode(selectedScheme, selectedBranch, selectedSemester, selectedSection, sub_code);
            Log.d("AssignClass", "courseId = "+courseId + ", reg_code = "+reg_code);
            if(dataExists()){
                {
                    //making an entry into the assignments table
                    try (Connection con = DatabaseHelper.SQLConnection()) {
                        String insertQuery = "INSERT INTO assignments (empid, courseid, reg_code, section) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement pst = con.prepareStatement(insertQuery)) {
                            pst.setString(1, String.valueOf(empId));
                            pst.setInt(2, courseId);
                            pst.setString(3, reg_code);
                            pst.setString(4, selectedSection);

                            int rowsAffected = pst.executeUpdate();
                            if (rowsAffected > 0) {
                                Toast.makeText(requireContext(), "Assignment added successfully.", Toast.LENGTH_SHORT).show();
                                // Clear the selection fields after successful submission
                                createRegister();
                                clearSelectionFields();
                            } else {
                                Toast.makeText(requireContext(), "Error occured while submitting the data!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (SQLException e) {
                        showToast(e.getMessage());
                        Log.d("Assignments", Objects.requireNonNull(e.getMessage()));
                    }
                }
            }else {
                showDeleteConfirmationDialog();
            }
        }

    }

    private void createRegister() throws SQLException {
        // Define the table name
        String reg_name = "class_" + reg_code;

        // SQL command to create the table
        String createTableSQL = "CREATE TABLE " + reg_name + " (date DATE PRIMARY KEY DEFAULT CONVERT(DATE, GETDATE()))";

        // Create the table
        try (Connection con = DatabaseHelper.SQLConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(createTableSQL);
            Toast.makeText(requireContext(), "Reg table created successfully.", Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            Log.d("AssignClass", Objects.requireNonNull(e.getMessage()));
            Toast.makeText(requireContext(), "Deleting the assignment..", Toast.LENGTH_SHORT).show();
            performDeletion();
            return;
        }

        // Fetch student roll numbers
        List<String> rolls = new ArrayList<>();
        String fetchRollsSQL = "SELECT rollno FROM students WHERE branch = ? AND sec = ?";

        try (Connection con = DatabaseHelper.SQLConnection();
             PreparedStatement ps = con.prepareStatement(fetchRollsSQL)) {
            ps.setInt(1, Integer.parseInt(selectedBranchYear));
            ps.setString(2, selectedSection);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                rolls.add(rs.getString("rollno"));
            }
        } catch (SQLException e) {
            Log.d("AssignClass", "Fetching rolls error: " + e.getMessage());
            Toast.makeText(requireContext(), "deleting the assignment..", Toast.LENGTH_SHORT).show();
            performDeletion();
            return;
        }

        // Add columns for each roll number
        try (Connection con = DatabaseHelper.SQLConnection();
             Statement st = con.createStatement()) {
            for (String roll : rolls) {
                // Sanitize roll number to avoid SQL injection and ensure valid column names
                String sanitizedRoll = roll.replaceAll("[^a-zA-Z0-9]", "_");
                String addColumnSQL = "ALTER TABLE " + reg_name + " ADD roll_" + sanitizedRoll + " BIT";
                st.executeUpdate(addColumnSQL);
            }

            Toast.makeText(requireContext(), "Columns added successfully.", Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            Log.d("AssignClass", "Error during adding students to reg table: \n" + e.getMessage());
            Toast.makeText(requireContext(), "deleting assignment....", Toast.LENGTH_SHORT).show();
            performDeletion();
        }
    }


    private void performDeletion() throws SQLException {
        Connection con=DatabaseHelper.SQLConnection();

        if(selectedEmployee.isEmpty()){
            Toast.makeText(requireContext(), "Please select the employee.", Toast.LENGTH_SHORT).show();
        }

        if(dataExists()){
            Toast.makeText(requireContext(), "The Record doesn't exist.", Toast.LENGTH_SHORT).show();
            return;
        }

        String insertQuery = "delete from assignments where assignment_id = ?";

        try (PreparedStatement pst = con.prepareStatement(insertQuery)) {
            pst.setInt(1, assignment_id);



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
        con.close();
    }

    private boolean allSelected(){
        return !selectedScheme.equals(ph_scheme) && !selectedBranch.equals(ph_branch) && !selectedYear.equals(ph_year) && !selectedSemester.equals(ph_sem) && !selectedSection.equals(ph_sec) && !selectedSubject.equals(ph_sub) && !selectedEmployee.isEmpty();
    }

    private void showConfirmationDialog(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
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
    private boolean dataExists() throws SQLException {
        String query = "select assignment_id from assignments where empid = ? and courseid = ? and reg_code = ? and section = ?";

        try (Connection con = DatabaseHelper.SQLConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, empId);
            pst.setInt(2, courseId);
            pst.setString(3, reg_code);
            pst.setString(4, selectedSection);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    assignment_id = rs.getInt("assignment_id");
                    return false;
                }
            }
        } catch (SQLException e) {
            Log.e("HomeActivity", "Error checking data existence: " + e.getMessage(), e);
            throw e;
        }
        return true;
    }

    private void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void clearSelectionFields(){
        spinnerScheme.setSelection(0);
        autotv_employee.setText("");
    }
}

