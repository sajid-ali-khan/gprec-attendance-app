package com.example.gprec_30.fragment_classes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.gprec_30.R;
import com.example.gprec_30.utils.DataFetcher;
import com.example.gprec_30.utils.DatabaseHelper;
import com.example.gprec_30.utils.RegesterCodeCreator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TakeAttendanceFragment extends Fragment {

    private TextView textViewClassName;
    private LinearLayout checkboxContainer;
    private Button buttonSubmit;
    private Button buttonAllPresent;
    private Button buttonAllAbsent;
    private String empId;
    private final DataFetcher dataFetcher = new DataFetcher();
    private String selectedClass;
    private int idx;
    private LinearLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_take_attendance, container, false);

        initView(rootView);
        retrieveEmpId();
        showClassSelectionDialog();

        buttonAllPresent.setOnClickListener(v -> setAllCheckboxes(true));
        buttonAllAbsent.setOnClickListener(v -> setAllCheckboxes(false));
        buttonSubmit.setOnClickListener(v -> showSubmitConfirmationDialog());

        return rootView;
    }



    private void initView(View rootView) {
        textViewClassName = rootView.findViewById(R.id.textViewClassName);
        checkboxContainer = rootView.findViewById(R.id.checkboxContainer);
        buttonSubmit = rootView.findViewById(R.id.buttonSubmit);
        buttonAllPresent = rootView.findViewById(R.id.buttonAllPresent);
        buttonAllAbsent = rootView.findViewById(R.id.buttonAllAbsent);
        layout = rootView.findViewById(R.id.frag_takeAttendance);
    }



    private void retrieveEmpId() {
        if (getArguments() != null) {
            empId = getArguments().getString("emp_id");
            Log.d("TakeAttendanceFragment", "Emp ID from arguments: " + empId);
        }
    }




    private void showClassSelectionDialog() {
        List<String> classes = dataFetcher.getEmployeeAssignmentsSimple(empId);
        classes.add(0, "none");
        String[] assignedClasses = classes.toArray(new String[0]);
        idx = 0;
        selectedClass = assignedClasses[idx];

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select a Class")
                .setSingleChoiceItems(assignedClasses, idx, (dialogInterface, i) -> {
                    idx = i;
                    selectedClass = assignedClasses[idx];
                })
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    if (!selectedClass.equals("none")) {
                        try {
                            onClassSelected(selectedClass);
                        } catch (SQLException e) {
                            showSnackBar(e.getMessage());
                        }
                    } else {
                        showSnackBar("'none' selected.");
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> showSnackBar("Cancel Clicked."))
                .show();
    }

    private void showSubmitConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Submit")
                .setMessage("Do you want to submit?")
                .setPositiveButton("Submit", (dialogInterface, i) -> {
                    try {
                        submitAttendance();
                    } catch (SQLException e) {
                        Log.d("TakeAttendanceFragment", "Error with the dialog box.", e);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }




    private void showSnackBar(String message) {
        Snackbar.make(layout, message, Snackbar.LENGTH_LONG).show();
    }




    private void submitAttendance() throws SQLException {
        checkAndInsertRowForToday();

        Map<String, Integer> attendanceMap = new HashMap<>();
        for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
            View view = checkboxContainer.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                String studentId = checkBox.getText().toString();
                int bitValue = checkBox.isChecked() ? 1 : 0;
                attendanceMap.put("roll_" + studentId, bitValue);
            }
        }

        updateAttendanceInDatabase(attendanceMap);
    }




    private void checkAndInsertRowForToday() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM class_" + selectedClass + " WHERE [date] = CONVERT(date, GETDATE())";
        String insertQuery = "INSERT INTO class_" + selectedClass + " ([date]) VALUES (CONVERT(date, GETDATE()))";

        try (Connection conn = DatabaseHelper.SQLConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                insertStmt.executeUpdate();
                Log.d("TakeAttendanceFragment", "Inserted new row for today's date.");
            } else {
                Log.d("TakeAttendanceFragment", "Row already exists for today's date.");
            }

        } catch (SQLException e) {
            Log.e("TakeAttendanceFragment", "Error checking or inserting row for today's date", e);
            throw e;
        }
    }




    private void updateAttendanceInDatabase(Map<String, Integer> attendanceMap) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE class_" + selectedClass + " SET ");
        List<Integer> values = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : attendanceMap.entrySet()) {
            String roll = entry.getKey().substring(0, 15);
            sql.append(roll).append(" = ?, ");
            values.add(entry.getValue());
        }

        sql.setLength(sql.length() - 2);//to remove the last comma and space
        sql.append(" WHERE [date] = CONVERT(date, GETDATE())");

        try (Connection conn = DatabaseHelper.SQLConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                pstmt.setInt(i + 1, values.get(i));
            }

            pstmt.executeUpdate();
            Log.d("TakeAttendanceFragment", "Attendance updated successfully.");
            showAcknowledgementDialog();

        } catch (SQLException e) {
            Log.e("TakeAttendanceFragment", "Error updating attendance", e);
            throw e;
        }
    }





    private void showAcknowledgementDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage("Attendance updated successfully.")
                .setPositiveButton("OK", (dialogInterface, i) -> goToHome())
                .show();
    }



    private void goToHome() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new HomeFragment())
                .commit();
    }




    private void onClassSelected(String className) throws SQLException {
        textViewClassName.setText(className);
        selectedClass = className;
        Log.d("TakeAttendanceFragment", "Selected class: " + className);
        List<String> students = dataFetcher.fetchStudents(RegesterCodeCreator.decodeRegCode(className));
        generateCheckboxes(students);
    }




    private void generateCheckboxes(List<String> students) {
        checkboxContainer.removeAllViews();
        for (String student : students) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(student);
            checkboxContainer.addView(checkBox);
        }
    }




    private void setAllCheckboxes(boolean checked) {
        for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
            View view = checkboxContainer.getChildAt(i);
            if (view instanceof CheckBox) {
                ((CheckBox) view).setChecked(checked);
            }
        }
    }
}
