package com.example.gprec_30;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gprec_30.utils.AttendanceReportTable;

import java.util.List;
import androidx.appcompat.widget.Toolbar;


public class AttendanceTableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance_table);


        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setTitle("Attendance Report");
        }

        List<AttendanceReportTable> attendanceReportTableList =
                (List<AttendanceReportTable>) getIntent().getSerializableExtra("attendanceReport");

        if (attendanceReportTableList != null) {
            populateTable(attendanceReportTableList);
        } else {
            Log.d("AttendanceTableActivity", "No attendance data received.");
        }


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Close this activity and go back to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateTable(List<AttendanceReportTable> attendanceList) {
        TableLayout tableLayout = findViewById(R.id.table_layout);

        // Add table header row
        TableRow headerRow = new TableRow(this);
        String[] headers = {"Student ID", "Days Present", "Total Days", "Attendance (%)"};
        for (String header : headers) {
            TextView headerCell = new TextView(this);
            headerCell.setText(header);
            headerCell.setPadding(16, 16, 16, 16);
            headerCell.setTypeface(null, Typeface.BOLD);
            headerRow.addView(headerCell);
        }
        tableLayout.addView(headerRow);

        // Add data rows
        for (AttendanceReportTable record : attendanceList) {
            TableRow row = getTableRow(record);
            tableLayout.addView(row);
        }
    }

    @NonNull
    private TableRow getTableRow(AttendanceReportTable record) {
        TableRow row = new TableRow(this);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        String[] rowData = {record.getRollNumber(), record.getDaysPresent(), record.getTotalDays(), record.getAttendancePercentage()};
        for (String cellData : rowData) {
            TextView cell = new TextView(this);
            cell.setText(cellData);
            cell.setPadding(16, 16, 16, 16);
            row.addView(cell);
        }
        return row;
    }

}