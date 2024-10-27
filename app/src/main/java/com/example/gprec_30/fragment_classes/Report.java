package com.example.gprec_30.fragment_classes;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.example.gprec_30.R;
import com.example.gprec_30.utils.BranchYearExtractor;
import com.example.gprec_30.utils.DatabaseHelper;
import com.example.gprec_30.utils.HintArrayAdapter;
import com.example.gprec_30.utils.MyBranchSorter;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class Report extends Fragment {

    private Spinner sp_branch, sp_year, sp_section;
    private EditText et_from, et_to;

    private Button btn_generateReport;
    private String selectedBranch = "", selectedYear = "", selectedSection = "";

    private final String ph_branch = "Select a Branch";
    private final String ph_year = "Select a Year";
    private final String ph_section = "Select a Section";

    public Report() {
        // Required empty public constructor
    }
    static class DbSupporter {
        static List<String> getBranches() {
            String query = "SELECT DISTINCT branch FROM STUDENTS";
            List<String> branches = new ArrayList<>();

            try (Connection conn = DatabaseHelper.SQLConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(query)) {

                while (rs.next()) {
                    branches.add(rs.getString("branch"));
                }
            } catch (Exception e) {
                Log.e("Report Fragment DB Supporter", "getBranches: " + e.getMessage());
            }
            return branches;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        // Initialize UI components
        sp_branch = rootView.findViewById(R.id.sp_branch);
        sp_year = rootView.findViewById(R.id.sp_sem);
        sp_section = rootView.findViewById(R.id.sp_section);
        et_from = rootView.findViewById(R.id.date_from);
        et_to = rootView.findViewById(R.id.date_to);
        btn_generateReport = rootView.findViewById(R.id.btn_generateReport);

        setupDatePickers();

        loadDummySpinners();
        loadSpinnersAsync();

        return rootView;
    }

    private void loadDummySpinners() {
        dummify(sp_branch, ph_branch);
        dummify(sp_year, ph_year);
        dummify(sp_section, ph_section);
    }

    private void setupDatePickers() {
        et_from.setOnClickListener(v -> showDatePicker(et_from, "FROM"));
        et_to.setOnClickListener(v -> showDatePicker(et_to, "TO"));
    }

    private void showDatePicker(EditText editText, String title) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .build();

        datePicker.show(getChildFragmentManager(), "DATE_PICKER");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Date date = new Date(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editText.setText(dateFormat.format(date));
        });
    }

    private void loadSpinnersAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<String> branches = DbSupporter.getBranches();
            MyBranchSorter sorter = new MyBranchSorter(branches);

            if (branches.isEmpty()) {
                Log.e("Report", "No branches found");
                return;
            }

            requireActivity().runOnUiThread(() -> setupBranchSpinner(sorter));
        });
    }

    private void setupBranchSpinner(MyBranchSorter sorter) {
        sp_branch.setAdapter(giveAdapter("Select a Branch", sorter.getBranchNames()));

        sp_branch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedBranch = "";
                    dummify(sp_year, ph_year);
                } else {
                    selectedBranch = sp_branch.getSelectedItem().toString();
                    List<String> years = sorter.getYears(selectedBranch);
                    Log.d("Report Fragment", "onItemSelected: selected branch "+ selectedBranch );

                    if (years != null && !years.isEmpty()) {
                        sp_year.setAdapter(giveAdapter("Select a Year", years));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        sp_year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedYear = "";
                    dummify(sp_section, ph_section);
                }else{
                    selectedYear = sp_year.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private HintArrayAdapter giveAdapter(String hint, List<String> list) {
        HintArrayAdapter adapter = new HintArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list, hint);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    // Utility method to parse dates from EditText
    private long parseDate(EditText editText) {
        String dateString = editText.getText().toString();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            Date date = format.parse(dateString);
            return (date != null) ? date.getTime() : 0;
        } catch (Exception e) {
            Log.e("Date Parse", "Error parsing date: " + e.getMessage());
            return 0;
        }
    }

    private void dummify(Spinner sp, String ph){
        sp.setAdapter(giveAdapter(ph, new ArrayList<>()));
    }
}
