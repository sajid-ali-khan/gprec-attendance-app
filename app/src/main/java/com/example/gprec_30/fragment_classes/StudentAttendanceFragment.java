package com.example.gprec_30.fragment_classes;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.gprec_30.R;

import com.example.gprec_30.utils.AttendanceQueryBuilder;
import com.example.gprec_30.utils.AttendanceReportTable;
import com.example.gprec_30.utils.DatabaseHelper;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Executors;

public class StudentAttendanceFragment extends Fragment {

    EditText et_from, et_to, et_rollNumber;

    TextView tv_status;
    Button btn_fetchAttendance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_student_attendance, container, false);

        et_from = rootView.findViewById(R.id.date_from);
        et_to = rootView.findViewById(R.id.date_to);
        btn_fetchAttendance = rootView.findViewById(R.id.btn_fetchAttendance);
        tv_status = rootView.findViewById(R.id.tv_studentName);
        et_rollNumber = rootView.findViewById(R.id.et_rollNumber);

        setupDatePickers();

        btn_fetchAttendance.setOnClickListener(v -> {
            String rollNumber = et_rollNumber.getText().toString().trim().toUpperCase();
            String fromDate = et_from.getText().toString().trim();
            String toDate = et_to.getText().toString().trim();
            if (rollNumber.isEmpty() || fromDate.isEmpty() || toDate.isEmpty()){
                showToast("Please fill all the input fields.");
                return;
            }

            if(!validateDateRange(fromDate, toDate)){
                showToast("Invalid date range.");
                return;
            }

            String formattedFromDate = "";
            String formattedToDate = "";

            try{
                 formattedFromDate = formatDateString(fromDate);
                 formattedToDate = formatDateString(toDate);
            }catch(Exception e){
                showToast("Error parsing the dates.");
                return;
            }

            String finalFormattedFromDate = formattedFromDate;
            String finalFormattedToDate = formattedToDate;
            Executors.newSingleThreadExecutor().execute(() -> {
                String[] classValues = branchSectionSem(rollNumber);

                if(classValues[0].isEmpty()){
                    requireActivity().runOnUiThread(() -> showToast("Student not found."));
                    return;
                }
                String branchCode = classValues[0];
                String sem = classValues[1];
                String sec = classValues[2];
                String name = classValues[3];

                List<String> tableNames = AttendanceQueryBuilder.fetchTableNames(branchCode, sem, sec);
                if (tableNames.isEmpty()){
                    requireActivity().runOnUiThread(() -> {
                        tv_status.setText("No class found");
                        tv_status.setGravity(Gravity.CENTER);
                    });
                    return;
                }

                String query = AttendanceQueryBuilder.generateAttendanceQuery(Collections.singletonList("roll_" + rollNumber), tableNames, finalFormattedFromDate, finalFormattedToDate);

                logError("\n"+query);
                Optional<AttendanceReportTable> stuAtt = executeQuery(query); // {roll_number, days_present, total_days, %}

                requireActivity().runOnUiThread(() -> {
                    if (stuAtt.isPresent()){
                        AttendanceReportTable res = stuAtt.get();
                        StringBuilder status = new StringBuilder();

                        status.append("Name: ").append(name)
                                .append("\n")
                                .append("Days Present: ").append(res.getDaysPresent()).append("\n")
                                .append("Total Days: ").append(res.getTotalDays()).append("\n")
                                .append("Attendance Percentage: ").append(String.format("%.2f", res.getAttendancePercentage())).append("\n");

                        tv_status.setText(status);
                        tv_status.setGravity(Gravity.START);
                    }
                });


            });

        });

        return rootView;
    }


    private Optional<AttendanceReportTable> executeQuery(String query) {
        Optional<AttendanceReportTable> res = Optional.empty();
        try(
                Connection connection = DatabaseHelper.SQLConnection();
                Statement st = connection.createStatement();
                ){
            ResultSet rs = st.executeQuery(query);
            if(rs.next()){
                String rollNumber = rs.getString("student_id");
                String days_present = rs.getString("days_present");
                String total_days = rs.getString("total_days");
                Float percentage = rs.getFloat("attendance_percentage");
                res = Optional.of(new AttendanceReportTable(rollNumber, days_present, total_days, percentage));
            }
        }catch(Exception e){
            logError(e.getMessage());
        }
        return res;
    }


    private String[] branchSectionSem(String rollNumber){
        String query = "select name, branch, sec, sem from students where ROLLNO = ?";
        String[] res =  new String[]{"", "", "", ""};
        try(
                Connection connection = DatabaseHelper.SQLConnection();
                PreparedStatement pst = connection.prepareStatement(query);
                ){
            pst.setString(1, rollNumber);
            ResultSet rs = pst.executeQuery();

            if (rs.next()){
                res[0] = rs.getString("branch");
                res[1] = rs.getString("sem");
                res[2] = rs.getString("sec");
                res[3] = rs.getString("name");
            }
        }catch(Exception e){
            logError(e.getMessage());
        }
        return res;
    }

    private void logError(String message){
        Log.d("StudentAttFragment", "logError: "+ message);
    }

    private void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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

}
