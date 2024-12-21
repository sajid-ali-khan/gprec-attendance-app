package com.example.gprec_30;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gprec_30.utils.AttendanceReportTable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

    String className = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        className = getIntent().getStringExtra("className");
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
            headerCell.setGravity(Gravity.CENTER);
            headerCell.setBackgroundResource(R.drawable.border_cell);
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
            cell.setGravity(Gravity.CENTER);
            cell.setBackgroundResource(R.drawable.border_cell);
            cell.setPadding(16, 16, 16, 16);
            row.addView(cell);
        }
        return row;
    }

    private void checkPermissionAndDownload() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // For Android 10 and above, use scoped storage
            exportCSVWithCustomName(attendanceReportTableList);
        } else {
            // For Android 9 and below, check WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
            } else {
                exportCSVWithCustomName(attendanceReportTableList);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Granted.
                exportCSVWithCustomName(attendanceReportTableList);
            } else {
                //Denied.
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void exportCSVWithCustomName(List<AttendanceReportTable> data) {
        showFileNameInputDialog(data);
    }

    private void showFileNameInputDialog(List<AttendanceReportTable> data) {
        String defaultFileName = "attendance_report.csv";
        Log.d("showFileNameInputDialog", "showFileNameInputDialog: class name = "+defaultFileName);

        final EditText input = new EditText(this);
        input.setText(defaultFileName);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Save As")
                .setMessage("Enter the name for your attendance report file:")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String fileName = input.getText().toString().trim();
                    if (fileName.isEmpty()) {
                        Toast.makeText(this, "Filename cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!fileName.endsWith(".csv")) {
                        fileName += ".csv"; // Ensure the file name has .csv extension
                    }
                    exportCSV(data, fileName);
                })
                .setNegativeButton("Cancel", (dialog, which) -> Toast.makeText(this, "Download cancelled.", Toast.LENGTH_SHORT).show())
                .show();
    }

    private void exportCSV(List<AttendanceReportTable> data, String fileName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);

        if (file.exists()) {
            showReplaceDialog(file, data);
        } else {
            writeCsvToFile(file, data);
        }
    }

    private void showReplaceDialog(File file, List<AttendanceReportTable> data) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("File Exists")
                .setMessage("The file already exists. Do you want to replace it?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User chose to replace the file
                    if (file.delete()){
                        writeCsvToFile(file, data);
                    }else{
                        Toast.makeText(this, "Failed to delete the existing file.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User chose not to replace the file
                    Toast.makeText(this, "Export cancelled.", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void writeCsvToFile(File file, List<AttendanceReportTable> data) {
        // Delete the file if it exists to avoid the "EXIST" error
        if (file.exists() && !file.delete()) {
            Toast.makeText(this, "Failed to delete the existing file.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Roll Number,Classes Present,Total Classes,Attendance (%)\n");

        for (AttendanceReportTable record : data) {
            csvBuilder.append(record.getRollNumber()).append(",")
                    .append(record.getDaysPresent()).append(",")
                    .append(record.getTotalDays()).append(",")
                    .append(record.getAttendancePercentage()).append("\n");
        }

        try (FileWriter writer = new FileWriter(file, false)) { // FileWriter with false (overwrite mode)
            writer.write(csvBuilder.toString());
            Toast.makeText(this, "CSV file saved to downloads!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("AttendanceActivity:writeCsvToFile", "Error writing CSV file", e);
            Toast.makeText(this, "Error exporting CSV!", Toast.LENGTH_SHORT).show();
        }
    }


}
