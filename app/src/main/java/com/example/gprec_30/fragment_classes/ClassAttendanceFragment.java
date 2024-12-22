package com.example.gprec_30.fragment_classes;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.gprec_30.AttendanceTableActivity;
import com.example.gprec_30.R;
import com.example.gprec_30.utils.AttendanceQueryBuilder;
import com.example.gprec_30.utils.BranchYearExtractor;
import com.example.gprec_30.utils.DatabaseHelper;
import com.example.gprec_30.utils.HintArrayAdapter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.example.gprec_30.utils.AttendanceReportTable;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ClassAttendanceFragment extends Fragment {

    private Spinner sp_branch, sp_sem, sp_section, sp_symbol;
    private EditText et_from, et_to, et_percentage;

    private String selectedBranch = "", selectedSem = "", selectedSection = "";
    private String fromDate = "", toDate = "";

    private final String ph_branch = "Select the Branch";
    private final String ph_sem = "Select the Semester";
    private final String ph_section = "Select the Section";


    private final String[] symbols = {"<", ">", "<=", ">=", "="};

    String selectedSymbol = "";
    String percentage = "";

    SwitchMaterial toggle_filter;

    public ClassAttendanceFragment() {
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
                Log.e("ClassAttendanceFragment Fragment DB Supporter", "getBranches: " + e.getMessage());
            }
            return branches;
        }

        public static List<String> getSemesters(String branchOnlyCode) {
            String query = "SELECT DISTINCT sem FROM course\n" +
                    "  WHERE branch LIKE ?";

            List<String> sems = new ArrayList<>();

            try (Connection conn = DatabaseHelper.SQLConnection();
                 PreparedStatement pst = conn.prepareStatement(query)
            ) {
                String like = branchOnlyCode + "%";
                pst.setString(1, like);

                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    sems.add(rs.getString("sem"));
                }
            } catch (Exception e) {
                Log.e("ClassAttendanceFragment Fragment DB Supporter", "getSemesters: " + e.getMessage());
            }
            Collections.sort(sems);
            return sems;
        }

        public static List<String> getSections(String branchOnlyCode, String selectedSem) {
            List<String> sections = new ArrayList<>();
            String query = "select distinct sec from students\n" +
                    "  where branch like ? and sem = ?";

            try (Connection conn = DatabaseHelper.SQLConnection();
                 PreparedStatement pst = conn.prepareStatement(query)
            ) {
                String like = branchOnlyCode + "%";
                pst.setString(1, like);
                pst.setString(2, selectedSem);

                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    sections.add(rs.getString("sec"));
                }
            } catch (Exception e) {
                Log.e("ClassAttendanceFragment Fragment DB Supporter", "getSections: " + e.getMessage());
            }
            return sections;

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_class_attendance, container, false);

        // Initialize UI components
        sp_branch = rootView.findViewById(R.id.sp_branch);
        sp_sem = rootView.findViewById(R.id.sp_sem);
        sp_section = rootView.findViewById(R.id.sp_section);
        et_from = rootView.findViewById(R.id.date_from);
        et_to = rootView.findViewById(R.id.date_to);
        Button btn_generateReport = rootView.findViewById(R.id.btn_generateReport);

        sp_symbol = rootView.findViewById(R.id.sp_symbol);
        et_percentage = rootView.findViewById(R.id.et_percentage);
        toggle_filter = rootView.findViewById(R.id.toggle_filter);

        setupDatePickers();

        setupFilterActivity();

        // Toggle listener
        toggle_filter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sp_symbol.setEnabled(isChecked);
            et_percentage.setEnabled(isChecked);
        });

        sp_symbol.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the selected symbol in the global variable
                selectedSymbol = symbols[position];
                Log.d("SelectedSymbol", "Selected: " + selectedSymbol);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle no selection if needed
                selectedSymbol = ""; // Clear the global variable
            }
        });

        btn_generateReport.setOnClickListener(v -> {

            fromDate = et_from.getText().toString();
            toDate = et_to.getText().toString();
            percentage = et_percentage.getText().toString();

            //checking if all the input fields are filled
            if (allFieldsNotFilled()){
                Toast.makeText(requireContext(), "Please fill all the input fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (validateDateRange(fromDate, toDate)) {
                try {
                    String formattedFromDate = formatDateString(fromDate);
                    String formattedToDate = formatDateString(toDate);

                    String branchCode = BranchYearExtractor.getBranchOnlyCode(selectedBranch);

                    Executors.newSingleThreadExecutor().execute(() -> {
                        String query = AttendanceQueryBuilder.generateAttendanceQuery(branchCode, selectedSem, selectedSection, formattedFromDate, formattedToDate);

//                        writeQueryToFile(query);

                        // Execute the query and get the report data
                        List<AttendanceReportTable> attendanceReportTable = makeReport(query);

                        // Pass data to the new activity
                        String className = getClassName();

                        Intent intent = new Intent(getActivity(), AttendanceTableActivity.class);
                        intent.putExtra("attendanceReport", new ArrayList<>(attendanceReportTable));  // Key should match when retrieving
                        intent.putExtra("className", className);
                        requireActivity().runOnUiThread(() -> startActivity(intent)); // Switch to the UI thread to start activity
                    });
                } catch (ParseException e) {
                    Toast.makeText(getContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
                    Log.d("ClassAttendanceFragment button", "onCreateView: " + e.getMessage());
                }
            } else {
                Toast.makeText(getContext(), "Invalid date range!", Toast.LENGTH_SHORT).show();
            }
        });



        loadDummySpinners();
        loadSpinnersAsync();

        return rootView;
    }

    private boolean allFieldsNotFilled() {
        Log.d("ClassAttendanceFragment", "allFieldsNotFilled: "+ String.format("%s, %s, %s, %s, %s", selectedBranch, selectedSem, selectedSection, fromDate, toDate));
        return selectedBranch.isEmpty() || selectedSem.isEmpty() || selectedSection.isEmpty() || fromDate.isEmpty() || toDate.isEmpty() || (toggle_filter.isChecked() && (selectedSymbol.isEmpty() || percentage.isEmpty()));
    }

    private void setupFilterActivity() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, symbols);
        adapter.setDropDownViewResource(R.layout.spinner_item);

        sp_symbol.setAdapter(adapter);
        sp_symbol.setEnabled(false);
    }

    @NonNull
    private String getClassName() {
        String end = "";
        if (Objects.equals(selectedSem, "1")){
            end = "st";
        }else if (Objects.equals(selectedSem, "2")){
            end = "nd";
        }else if (Objects.equals(selectedSem, "3")){
            end = "rd";
        }else{
            end = "th";
        }
        end += " sem";
        return selectedSem+end + " "+selectedBranch+" - "+selectedSection;
    }

    private List<AttendanceReportTable> makeReport(String query) {
        ArrayList<AttendanceReportTable> reportTable = new ArrayList<>();
        try(Connection con = DatabaseHelper.SQLConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query)){
            while (rs.next()){
                String rollNumber = rs.getString("student_id");
                String days_present = rs.getString("days_present");
                String total_days = rs.getString("total_days");
                Float percentage = rs.getFloat("attendance_percentage");
                reportTable.add(new AttendanceReportTable(rollNumber, days_present, total_days, percentage));
            }
        }catch(Exception e){
            Log.d("ClassAttendanceFragment:makeReport", "makeReport: "+e);
        }

        if (toggle_filter.isChecked()){
            Float percentageFloat = Float.parseFloat(percentage);
            return reportTable.stream()
                    .filter(report -> {
                        switch (selectedSymbol) {
                            case "<":
                                return report.getAttendancePercentage() < percentageFloat;
                            case ">":
                                return report.getAttendancePercentage() > percentageFloat;
                            case "<=":
                                return report.getAttendancePercentage() <= percentageFloat;
                            case ">=":
                                return report.getAttendancePercentage() >= percentageFloat;
                            case "=":
                                return report.getAttendancePercentage().equals(percentageFloat);
                            default:
                                return true; // No filter if `selectedSymbol` is invalid
                        }
                    })
                    .collect(Collectors.toList());
        }

        return reportTable;
    }

    private String formatDateString(String date) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parsedDate = inputFormat.parse(date);
        return outputFormat.format(parsedDate);
    }

    private boolean validateDateRange(String fromDate, String toDate) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = format.parse(fromDate);
            Date endDate = format.parse(toDate);
            return startDate != null && endDate != null && startDate.before(endDate);
        } catch (ParseException e) {
            Log.e("Date Validation", "Error parsing dates: " + e.getMessage());
            return false;
        }
    }

    private void loadDummySpinners() {
        dummify(sp_branch, ph_branch);
        dummify(sp_sem, ph_sem);
        dummify(sp_section, ph_section);
    }

    public void setupDatePickers() {
        et_from.setOnClickListener(v -> showDatePicker(et_from, "FROM"));
        et_to.setOnClickListener(v -> showDatePicker(et_to, "TO"));
    }

    public void showDatePicker(EditText editText, String title) {
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
            List<String> branches = BranchYearExtractor.extractBranchList(DbSupporter.getBranches());

            if (branches.isEmpty()) {
                Log.e("ClassAttendanceFragment", "No branches found");
                return;
            }

            requireActivity().runOnUiThread(() -> manageSpinners(branches));
        });
    }

    private void manageSpinners(List<String> branches) {
        sp_branch.setAdapter(giveAdapter(ph_branch, branches));

        sp_branch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dummify(sp_sem, ph_sem);
                if (position == 0) {
                    selectedBranch = "";
                    return;
                }
                selectedBranch = sp_branch.getSelectedItem().toString();
                Log.d("ClassAttendanceFragment Fragment", "onItemSelected: selected branch " + selectedBranch);
                String branchCode = BranchYearExtractor.getBranchOnlyCode(selectedBranch);
                Log.d("branch listener", "onItemSelected: branch code is " + branchCode);
                List<String> sems = DbSupporter.getSemesters(branchCode);
                if (!sems.isEmpty()) {
                    sp_sem.setAdapter(giveAdapter("Select the Semester", sems));
                } else {
                    Log.e("ClassAttendanceFragment : branch listener", "onItemSelected: the sems list is empty.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sp_sem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dummify(sp_section, ph_section);
                if (position == 0) {
                    selectedSem = "";
                    return;
                }
                selectedSem = sp_sem.getSelectedItem().toString();
                List<String> sections = DbSupporter.getSections(BranchYearExtractor.getBranchOnlyCode(selectedBranch), selectedSem);


                if (!sections.isEmpty()) {
                    sp_section.setAdapter(giveAdapter(ph_section, sections));
                } else {
                    Log.d("ClassAttendanceFragment : sem listener", "onItemSelected: the sections list is empty.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        sp_section.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedSection = "";
                } else {
                    selectedSection = sp_section.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private HintArrayAdapter giveAdapter(String hint, List<String> list) {
        HintArrayAdapter adapter = new HintArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, list, hint);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void dummify(Spinner sp, String ph) {
        sp.setAdapter(giveAdapter(ph, new ArrayList<>()));
    }


    private void writeQueryToFile(String query) {
        // Use the context to get the internal storage directory
        File dir = requireContext().getFilesDir();
        File file = new File(dir, "attendance_query.sql");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(query);
            Log.d("File Write", "Query written to file: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("File Write", "Error writing to file", e);
        }
    }

}
