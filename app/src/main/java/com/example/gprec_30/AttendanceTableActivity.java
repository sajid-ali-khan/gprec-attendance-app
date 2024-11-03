package com.example.gprec_30;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gprec_30.utils.AttendanceReportTable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AttendanceTableActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112; // Choose an appropriate number
    private List<AttendanceReportTable> attendanceReportTableList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_attendance_table);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tv_className = findViewById(R.id.tv_className);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back arrow
            getSupportActionBar().setTitle("Attendance Report");
        }

        attendanceReportTableList =
                (List<AttendanceReportTable>) getIntent().getSerializableExtra("attendanceReport");

        String className = getIntent().getStringExtra("className");
        tv_className.setText(className);

        if (attendanceReportTableList != null) {
            populateTable(attendanceReportTableList);
        } else {
            Log.d("AttendanceTableActivity", "No attendance data received.");
        }

        ImageButton downloadButton = findViewById(R.id.action_download);
        downloadButton.setOnClickListener(v -> checkPermissionAndDownload());
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
        String[] headers = {"Roll Number", "Classes Present", "Total Classes", "Attendance (%)"};
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

    private void checkPermissionAndDownload() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10 and above, use scoped storage
            exportCSV(attendanceReportTableList);
        } else {
            // For Android 9 and below, check WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            } else {
                exportCSV(attendanceReportTableList);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Granted.
                exportCSV(attendanceReportTableList);
            } else {
                //Denied.
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void exportCSV(List<AttendanceReportTable> data) {
        // CSV generation logic goes here
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Roll Number,Classes Present,Total Classes,Attendance (%)\n"); // Header

        for (AttendanceReportTable record : data) {
            csvBuilder.append(record.getRollNumber()).append(",")
                    .append(record.getDaysPresent()).append(",")
                    .append(record.getTotalDays()).append(",")
                    .append(record.getAttendancePercentage()).append("\n");
        }

        String fileName = "attendance_report.csv";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);



        try (FileWriter writer = new FileWriter(file)) {
            writer.write(csvBuilder.toString());
            Toast.makeText(this, "CSV file exported successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("AttendanceActivity:exportCSV", "exportCSV: "+e.getMessage() );
            Toast.makeText(this, "Error exporting CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
