package com.example.gprec_30.utils;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class AttendanceQueryBuilder {

public static String generateAttendanceQuery(String branchCode, String sem, String section, String fromDate, String toDate){
    String query;

    //fetch the rollnumbers
    List<String> rollNumbers = fetchRollNumbers(branchCode, sem, section);


    //fetch the table names
    List<String> tableNames = fetchTableNames(branchCode, sem, section);

    query = generateAttendanceQuery(rollNumbers, tableNames, fromDate, toDate);

    return query;
}

    private static List<String> fetchTableNames(String branchCode, String sem, String section) {
        List<String> tableNames = new ArrayList<>();
        //
        String query = "SELECT reg_code FROM assignments\n" +
                "WHERE courseid IN (SELECT courseid FROM course\n" +
                "WHERE branch LIKE ? AND SEM = ?)\n" +
                "AND section = ?";

        try(Connection con = DatabaseHelper.SQLConnection();
            PreparedStatement pst = con.prepareStatement(query)){

            pst.setString(1, branchCode + "%");
            pst.setString(2, sem);
            pst.setString(3, section);

            ResultSet rs = pst.executeQuery();

            while (rs.next()){
                String tableName = rs.getString("reg_code");

                if (tableName.contains("(P)")){
                    tableName = "lab_"+tableName.replace("(P)", "");
                }else{
                    tableName = "class_"+tableName;
                }

                tableNames.add(tableName);
            }
        }catch(Exception e){
            Log.d("AttendanceQueryBuilder:fetchTableNames", "The error :"+e.getMessage());
        }

        Log.d("tableNames", "fetchTableNames: "+tableNames);

        return tableNames;
    }

    private static List<String> fetchRollNumbers(String branchCode, String sem, String section) {
        List<String> rollNumbers = new ArrayList<>();
        //
        String query = "select rollno from students\n" +
                "where branch like ? and sem = ? and sec = ?";

        try(Connection con = DatabaseHelper.SQLConnection();
            PreparedStatement pst = con.prepareStatement(query)){

            pst.setString(1, branchCode + "%");
            pst.setString(2, sem);
            pst.setString(3, section);

            ResultSet rs = pst.executeQuery();

            while (rs.next()){
                String roll = rs.getString("rollno");

                roll = "roll_"+roll;

                rollNumbers.add(roll);
            }
        }catch(Exception e){
            Log.d("AttendanceQueryBuilder:fetchRollNumbers", "fetchRollNumbers: "+e.getMessage());
        }

        Log.d("roll numbers", "fetchRollNumbers: "+rollNumbers);
        return rollNumbers;
    }

    public static String generateAttendanceQuery(List<String> studentRollNumbers, List<String> tableNames, String fromDate, String toDate) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT \n")
                .append("    student_id,\n")
                .append("    SUM(CAST(present AS INT)) AS days_present,\n")
                .append("    COUNT(date) AS total_days,\n")
                .append("    CAST(SUM(CAST(present AS INT)) * 100.0 / COUNT(date) AS DECIMAL(5, 2)) AS attendance_percentage\n")
                .append("FROM (\n");

        // Iterate over each table name to create the UNION structure
        for (int i = 0; i < tableNames.size(); i++) {
            String tableName = tableNames.get(i);

            // For each table, iterate over student roll numbers for the UNPIVOT structure
            for (String rollNumber : studentRollNumbers) {
                query.append("    SELECT date, ")
                        .append(rollNumber).append(" AS present, '")
                        .append(rollNumber.replace("roll_", "")).append("' AS student_id\n")
                        .append("    FROM ").append(tableName);

                // Add UNION ALL if not the last entry in the table/rollNumber combination
                if (!(i == tableNames.size() - 1 && rollNumber.equals(studentRollNumbers.get(studentRollNumbers.size() - 1)))) {
                    query.append("\n    UNION ALL\n");
                }
            }
        }

        query.append("\n) AS attendance\n")
                .append("WHERE date BETWEEN '").append(fromDate).append("' AND '").append(toDate).append("'\n")
                .append("GROUP BY student_id;");

        return query.toString();
    }

}
