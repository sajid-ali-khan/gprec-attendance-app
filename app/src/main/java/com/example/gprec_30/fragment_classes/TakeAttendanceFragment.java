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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TakeAttendanceFragment extends Fragment implements ClassSelectionDialogFragment.ClassSelectionListener {

    TextView textViewClassName;
    LinearLayout checkboxContainer;
    Button buttonSubmit;
    Button buttonAllPresent;
    Button buttonAllAbsent;

    String emp_id;

    DataFetcher dataFetcher = new DataFetcher();

    List<String> students = new ArrayList<>();

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

        // Log user ID
        Log.d("takeAttendanceFragment", "User ID: " + emp_id);

        // Set arguments for ClassSelectionDialogFragment
        Bundle bundle = new Bundle();
        bundle.putString("emp_id", emp_id);

        showClassSelectionDialog(bundle); // Pass the arguments to the method

        // Set button click listeners
        buttonAllPresent.setOnClickListener(v -> setAllCheckboxes(true));
        buttonAllAbsent.setOnClickListener(v -> setAllCheckboxes(false));

        return rootView;
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

        Log.d("takeAttendanceFragment", "Selected class : "+className);
        // Fetch number of students from the database based on the selected class
        students = dataFetcher.fetchStudents(className);
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
