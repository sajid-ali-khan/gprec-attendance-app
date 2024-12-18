package com.example.gprec_30.fragment_classes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.gprec_30.R;
import com.example.gprec_30.utils.BranchYearExtractor;
import com.example.gprec_30.utils.DataFetcher;
import com.example.gprec_30.utils.DatabaseHelper;
import com.example.gprec_30.utils.HintArrayAdapter;
import com.example.gprec_30.utils.RegesterCodeCreator;
import com.example.gprec_30.utils.SpinnerHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AssignClassFragment extends Fragment {

    private Spinner spinnerScheme, spinnerBranch, spinnerYear, spinnerSemester, spinnerSection, spinnerSubject;
    private AutoCompleteTextView autotv_employee;

    // Variables to hold selected values
    private String selectedScheme, selectedBranch, selectedYear, selectedSemester, selectedSection, selectedBranchYear, selectedSubject, selectedEmployee, empName;
    private String sub_name, sub_code;
    private int assignment_id;
    int empId;

    LinearLayout layout;

    DataFetcher dataFetcher = new DataFetcher();

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    public AssignClassFragment() {
        // Required empty public constructor
    }

    List<String> schemes, branches, years, sems, sections, subjects, employees;

    private final String phScheme = "Select the Scheme";
    private final String phBranch = "Select the Branch";
    private final String phYear = "Select the Year";
    private final String phSemester = "Select the Semester";
    private final String phSection = "Select the Section";
    private final String phSubject = "Select the Subject";
    int courseId;
    String reg_code;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_assign_class, container, false);

        layout = rootView.findViewById(R.id.main);
        // Initialize views
        spinnerScheme = rootView.findViewById(R.id.spinnerScheme);
        spinnerBranch = rootView.findViewById(R.id.spinnerBranch);
        spinnerYear = rootView.findViewById(R.id.spinnerYear);
        spinnerSemester = rootView.findViewById(R.id.spinnerSemester);
        spinnerSection = rootView.findViewById(R.id.spinnerSection);
        spinnerSubject = rootView.findViewById(R.id.spinnerSubject);
        autotv_employee = rootView.findViewById(R.id.editTextTeacher);
        Button buttonAssignClass = rootView.findViewById(R.id.buttonAssignClass);

        spinnerListeners();

        buttonAssignClass.setOnClickListener(v -> showConfirmationDialog());

        loadDummySpinners();
        try {
            loadSchemeSpinner();
            loadEmployees();
        } catch (SQLException e) {
            logError(e.getMessage());
        }

        return rootView;
    }

    private void loadDummySpinners() {
        dummify(spinnerScheme, phScheme);
        dummify(spinnerBranch, phBranch);
        dummify(spinnerYear, phYear);
        dummify(spinnerSemester, phSemester);
        dummify(spinnerSection, phSection);
        dummify(spinnerSubject, phSubject);
    }

    private void dummify(Spinner sp, String ph) {
        sp.setAdapter(giveAdapter(ph, new ArrayList<>()));
    }

    private HintArrayAdapter giveAdapter(String hint, List<String> list) {
        HintArrayAdapter adapter = new HintArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list, hint);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void spinnerListeners(){
        spinnerScheme.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    //ignore
                    selectedScheme = "";
                }else{
                    selectedScheme = schemes.get(position);
                    showSnackBar(selectedScheme);
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
                    selectedBranch = "";
                }else{
                    selectedBranch = branches.get(position);
                    showSnackBar(selectedBranch);
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
                    selectedBranchYear = BranchYearExtractor.generateBranchCode(selectedBranch, selectedYear);
                    showSnackBar(selectedYear);

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
                    showSnackBar(selectedSemester);
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
                    showSnackBar(selectedSection);
                    try {
                        updateSubjectSpinner();
                    } catch (SQLException e) {
                        logError(e.getMessage());
                    }
                }
            }
        });

        spinnerSubject.setOnItemSelectedListener(new SpinnerHelper.SpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedSubject = "";
                    sub_name = "";
                    sub_code = "";
                }else{
                    selectedSubject = subjects.get(position);
                    int start = selectedSubject.indexOf("(");
                    int end = selectedSubject.lastIndexOf(")");
                    sub_name = selectedSubject.substring(0, start);
                    sub_code = selectedSubject.substring(start+1, end);
                    showSnackBar(selectedSubject);
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
        spinnerScheme.setAdapter(giveAdapter(phScheme, schemes));
    }

    private void loadEmployees() throws SQLException {
        employees = dataFetcher.fetchEmployees();
        SpinnerHelper.populateEmployees(autotv_employee, employees);
    }
    private void updateBranchSpinner() throws SQLException {
        branches = dataFetcher.fetchBranches(selectedScheme);
        spinnerBranch.setAdapter(giveAdapter(phBranch, branches));
    }
    private void updateYearSpinner() throws SQLException {
        years = dataFetcher.fetchYears(selectedScheme, selectedBranch);
        spinnerYear.setAdapter(giveAdapter(phYear, years));
    }

    private void updateSemSpinner() throws SQLException {
        sems = dataFetcher.fetchSemesters(selectedScheme, selectedBranchYear);
        spinnerSemester.setAdapter(giveAdapter(phSemester, sems));
    }

    private void updateSectionSpinner() {
        sections = dataFetcher.fetchSections();
        spinnerSection.setAdapter(giveAdapter(phSection, sections));
    }
    private void updateSubjectSpinner() throws SQLException {
        subjects = dataFetcher.fetchSubjects(selectedScheme, selectedBranchYear, selectedSemester);
        spinnerSubject.setAdapter(giveAdapter(phSubject, subjects));
    }


    // Method to handle class assignment
    private void assignClass() {
        if (allSelected()) {
            Log.d("Selected Params", empId + " " + empName + " " + selectedScheme + " " + selectedBranch + " " + selectedSemester + " " + sub_name);

            executorService.execute(() -> {
                try {
                    courseId = dataFetcher.getCourseId(selectedScheme, selectedBranchYear, selectedSemester, sub_code, sub_name);
                    reg_code = RegesterCodeCreator.createRegCode(selectedScheme, selectedBranch, selectedSemester, selectedSection, sub_code);
                    Log.d("AssignClass", "courseId = " + courseId + ", reg_code = " + reg_code);

                    if (!assignmentExists()) {
                        try (Connection con = DatabaseHelper.SQLConnection()) {
                            String insertQuery = "INSERT INTO assignments (empid, courseid, reg_code, section) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement pst = con.prepareStatement(insertQuery)) {
                                pst.setString(1, String.valueOf(empId));
                                pst.setInt(2, courseId);
                                pst.setString(3, reg_code);
                                pst.setString(4, selectedSection);

                                int rowsAffected = pst.executeUpdate();
                                if (rowsAffected > 0) {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "Assignment added successfully.", Toast.LENGTH_SHORT).show();
                                        try {
                                            createRegister();
                                        } catch (SQLException e) {
                                            logError(e.getMessage());
                                        }
                                        clearSelectionFields();
                                    });
                                } else {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "Error occurred while submitting the data!", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        } catch (SQLException e) {
                            requireActivity().runOnUiThread(() -> {
                                showSnackBar(e.getMessage());
                            });
                            Log.d("Assignments", Objects.requireNonNull(e.getMessage()));
                        }
                    } else {
                        requireActivity().runOnUiThread(this::showDeleteConfirmationDialog);
                    }
                } catch (SQLException e) {
                    Log.d("AssignClass", Objects.requireNonNull(e.getMessage()));
                }
            });
        }
    }

    private void createRegister() throws SQLException {
        String reg_name;

        if (reg_code.contains("(P)")){
            reg_name = "lab_"+reg_code.replace("(P)", "");
        }else{
            reg_name = "class_"+reg_code;
        }

        // SQL command to create the table
        String createTableSQL = "CREATE TABLE " + reg_name + " (date DATE PRIMARY KEY DEFAULT CONVERT(DATE, GETDATE()))";

        // Create the table
        try (Connection con = DatabaseHelper.SQLConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate(createTableSQL);
            Toast.makeText(requireContext(), "Reg table created successfully.", Toast.LENGTH_SHORT).show();
        } catch (SQLException e) {
            Log.d("AssignClass/createRegister", Objects.requireNonNull(e.getMessage()));
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

        if(!assignmentExists()){
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

                String reg_name = reg_code.contains("(P)")? "lab_"+reg_code.replace("(P)", "") : "class_"+reg_code;

                deleteRegisterTable(reg_name);
            } else {
                Toast.makeText(requireContext(), "The Record doesn't exist.", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            Log.e("HomeActivity", "SQLException in submitSelectionsToDatabase", e);
            Toast.makeText(requireContext(), "Database error occurred.", Toast.LENGTH_SHORT).show();
        }
        con.close();
    }

    private void deleteRegisterTable(String regCode) {
        // Validate the table name (you can improve this as needed)
        if (!regCode.matches("[a-zA-Z0-9_]+")) {
            showSnackBar("Invalid table name.");
            return;
        }
        String del_sql = String.format("DROP TABLE IF EXISTS %s", regCode);

        try (Connection con = DatabaseHelper.SQLConnection();
             Statement stmt = con.createStatement()) {

            Log.d("AssignClass/regCode", "deleteRegisterTable: reg code is " + regCode);

            stmt.executeUpdate(del_sql);

            showSnackBar("The register related table deleted successfully.");

        } catch (SQLException e) {
            showSnackBar(e.getMessage());
            Log.d("AssignClass/deleteRegisterTable", "deleteRegisterTable: " + e.getMessage());
        }
    }


    private boolean allSelected(){
        return !selectedScheme.isEmpty() && !selectedBranch.isEmpty() && !selectedYear.isEmpty() && !selectedSemester.isEmpty() && !selectedSection.isEmpty() && !selectedSubject.isEmpty() && !selectedEmployee.isEmpty();
    }

    private void showConfirmationDialog(){
        String selected = String.format("Scheme : %s\nBranch : %s\nYear : %s\nSemester : %s\nSection : %s\nSubject : %s\n", selectedScheme, selectedBranch, selectedYear, selectedSemester, selectedSection, selectedSubject);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to submit the data ?\n"+selected);
        builder.setPositiveButton("Yes", ((dialog, which) -> {
            assignClass();
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
    private boolean assignmentExists() throws SQLException {
        String query = "select assignment_id from assignments where  reg_code = ?";

        try (Connection con = DatabaseHelper.SQLConnection(); PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, reg_code);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    assignment_id = rs.getInt("assignment_id");
                    return true;
                }
            }
        } catch (SQLException e) {
            Log.e("HomeActivity", "Error checking data existence: " + e.getMessage(), e);
            throw e;
        }
        return false;
    }

    private void showSnackBar(String message){
        Snackbar.make(layout, message, Snackbar.LENGTH_LONG).show();
    }

    private void logError(String msg){
        Log.d("AssignClass Fragment", "logError: "+msg);
    }

    private void clearSelectionFields(){
        spinnerSubject.setSelection(0);
        autotv_employee.setText("");
    }
}

