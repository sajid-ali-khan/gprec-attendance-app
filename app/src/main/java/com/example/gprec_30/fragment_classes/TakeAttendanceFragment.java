package com.example.gprec_30.fragment_classes;

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeAttendanceFragment extends Fragment implements ClassSelectionDialogFragment.ClassSelectionListener {

    TextView textViewClassName;
    LinearLayout checkboxContainer;
    Button buttonSubmit;
    Button buttonAllPresent;
    Button buttonAllAbsent;

    String emp_id;

    DataFetcher dataFetcher = new DataFetcher();

    List<String> students = new ArrayList<>();

    String selectedClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_take_attendance, container, false);

        textViewClassName = rootView.findViewById(R.id.textViewClassName);
        checkboxContainer = rootView.findViewById(R.id.checkboxContainer);
        buttonSubmit = rootView.findViewById(R.id.buttonSubmit);
        buttonAllPresent = rootView.findViewById(R.id.buttonAllPresent);
        buttonAllAbsent = rootView.findViewById(R.id.buttonAllAbsent);

        // Retrieve user ID from arguments
        if (getArguments() != null) {
            emp_id = getArguments().getString("emp_id");
            Log.d("takeAttendanceFragment", "Emp ID from arguments: " + emp_id); // Add this line
        }

        Log.d("takeAttendanceFragment", "User ID: " + emp_id);

        // Set arguments for ClassSelectionDialogFragment
        Bundle bundle = new Bundle();
        bundle.putString("emp_id", emp_id);

        showClassSelectionDialog(bundle); // Pass the arguments to the method

        buttonAllPresent.setOnClickListener(v -> setAllCheckboxes(true));
        buttonAllAbsent.setOnClickListener(v -> setAllCheckboxes(false));

        buttonSubmit.setOnClickListener(v -> {
            try {
                submitAttendance();
            } catch (SQLException e) {
                Log.d("take attendance fragment", "onCreateView: Problem during the submission.\n problem : "+e.getMessage());
            }
        });


        return rootView;
    }

    private void submitAttendance() throws SQLException {
        checkAndInsertRowForToday();

        Map<String, Integer> attendanceMap = new HashMap<>();

        for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
            View view = checkboxContainer.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) view;
                String studentId = checkBox.getText().toString();
                boolean isChecked = checkBox.isChecked();
                int bitValue = isChecked ? 1 : 0;

                attendanceMap.put("roll_" + studentId, bitValue);
            }
        }

        // Call method to update the attendance for all students at once
        Log.d("TakeAttendance", "229x1a2851: "+attendanceMap.get("roll_229x1a2851"));
        updateAttendanceInDatabase(attendanceMap);
    }

    private void checkAndInsertRowForToday() throws SQLException {
        String checkQuery = "SELECT COUNT(*) FROM class_"+selectedClass+" WHERE [date] = CONVERT(date, GETDATE())";
        String insertQuery = "INSERT INTO class_"+selectedClass+" ([date]) VALUES (CONVERT(date, GETDATE()))";

        try (Connection conn = DatabaseHelper.SQLConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            // Check if the row exists for today's date
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                // No row exists for today, insert a new row
                insertStmt.executeUpdate();
                Log.d("takeAttendanceFragment", "Inserted new row for today's date.");
            } else {
                Log.d("takeAttendanceFragment", "Row already exists for today's date.");
            }

        } catch (SQLException e) {
            Log.e("takeAttendanceFragment", "Error checking or inserting row for today's date", e);
            throw e;
        }
    }





    private void updateAttendanceInDatabase(Map<String, Integer> attendanceMap) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE class_"+selectedClass+" SET ");
        List<Integer> values = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : attendanceMap.entrySet()) {
            String roll = entry.getKey().substring(0, 15);
            sql.append(roll).append(" = ?, ");
            values.add(entry.getValue());
        }

        // Remove the last comma and space
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE [date] = CONVERT(date, GETDATE())");

        try (Connection conn = DatabaseHelper.SQLConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // Set the attendance values in the prepared statement
            for (int i = 0; i < values.size(); i++) {
                pstmt.setInt(i + 1, values.get(i));
            }

            // Execute the update statement
            Log.d("takeattendance", "updateAttendanceInDatabase: "+sql);
            Log.d("takeattendance", "updateAttendanceInDatabase: "+values);
            int rowsAffected = pstmt.executeUpdate();
            Log.d("takeAttendanceFragment", "Attendance updated for " + rowsAffected + " students.");

        } catch (SQLException e) {
            Log.e("takeAttendanceFragment", "Error updating attendance", e);
            throw e;
        }
    }




    private void showClassSelectionDialog(Bundle bundle) {
        // Pass the arguments to ClassSelectionDialogFragment
        ClassSelectionDialogFragment dialogFragment = new ClassSelectionDialogFragment();
        dialogFragment.setArguments(bundle);
        dialogFragment.setListener(this);
        dialogFragment.show(getChildFragmentManager(), "ClassSelectionDialogFragment");
    }

    @Override
    public void onClassSelected(String className) throws SQLException {
        textViewClassName.setText(className);
        selectedClass = className;

        Log.d("takeAttendanceFragment", "Selected class : "+className);
        // Fetch number of students from the database based on the selected class
        students = dataFetcher.fetchStudents(RegesterCodeCreator.decodeRegCode(className));
        generateCheckboxes(students);
    }

    private void generateCheckboxes(List<String> students) {
        checkboxContainer.removeAllViews();

        // Loop through the list of students
        for (String student : students) {
            // Create a new CheckBox
            CheckBox checkBox = new CheckBox(getContext());
            // Set the text of the CheckBox to the student's name or ID
            checkBox.setText(student);
            // Add the CheckBox to the checkboxContainer
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
