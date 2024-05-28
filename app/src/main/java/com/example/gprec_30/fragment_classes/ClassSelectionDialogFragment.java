package com.example.gprec_30.fragment_classes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.gprec_30.R;
import com.example.gprec_30.utils.Assignment;
import com.example.gprec_30.utils.DataFetcher;
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

    List<Assignment> assignments;
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
        try {
            assignments = dataFetcher.fetchAssignments(emp_id);
        } catch (SQLException e) {
            Toast.makeText(requireContext(), "Couldn't fetch the assignments of "+emp_id, Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> assignmentCodes = assignments.stream().map(assignment ->
                        assignment.getSem() + " " + assignment.getBranch() + " " + assignment.getSection() + " " + assignment.getSubCode())
                .collect(Collectors.toList());

        SpinnerHelper.populateSpinner(spinnerClassSelection, assignmentCodes);
    }

    public void setListener(ClassSelectionListener listener) {
        mListener = listener;
    }
}
