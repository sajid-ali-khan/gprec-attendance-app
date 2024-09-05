package com.example.gprec_30.fragment_classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.gprec_30.R;
import com.example.gprec_30.utils.BranchYearExtractor;
import com.example.gprec_30.utils.DataFetcher;
import com.example.gprec_30.utils.EmployeeAssignment;
import com.example.gprec_30.utils.SpinnerHelper;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ClassSelectionDialogFragment extends DialogFragment {

    Spinner spinnerClassSelection;
    Button buttonSelectClass;

    String emp_id;

    public interface ClassSelectionListener {
        void onClassSelected(String className) throws SQLException;
    }

    ClassSelectionListener mListener;

    List<EmployeeAssignment> assignments;
    DataFetcher dataFetcher = new DataFetcher();

    String selectedClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.class_selection_dialog, container, false);

        if(getArguments() != null){
            emp_id = getArguments().getString("emp_id");
        }

        spinnerClassSelection = rootView.findViewById(R.id.spinnerClassSelection);
        buttonSelectClass = rootView.findViewById(R.id.buttonSelectClass);

        loadClasses();


        buttonSelectClass.setOnClickListener(v -> {
            // Notify the listener with the selected class
            try {
                selectedClass = spinnerClassSelection.getSelectedItem().toString();
                mListener.onClassSelected(selectedClass);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            // Dismiss the dialog
            dismiss();
        });

        return rootView;
    }

    public void loadClasses(){
//        assignments = dataFetcher.getEmployeeAssignments(emp_id);
        List<String> simples = dataFetcher.getEmployeeAssignmentsSimple(emp_id);

//        List<String> assignmentCodes = assignments.stream().map(assignment ->
//                        assignment.getYear()+ " year "+assignment.getBranchName() +" "+ assignment.getSection()+" "+assignment.getScode())
//                .collect(Collectors.toList());


        SpinnerHelper.populateSpinner(spinnerClassSelection, simples);
    }

    public void setListener(ClassSelectionListener listener) {
        mListener = listener;
    }
}
