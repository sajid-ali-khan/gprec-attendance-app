package com.example.gprec_30.utils;

import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataFetcher {
    List<String> schemes = new ArrayList<>();
    List<String> branch_codes = new ArrayList<>();
    List<String> branches = new ArrayList<>();
    List<String> years = new ArrayList<>();
    List<String> semesters = new ArrayList<>();
    List<String> sections = new ArrayList<>();
    List<String> subjects = new ArrayList<>();

    List<String> employees = new ArrayList<>();
    List<String> students = new ArrayList<>();
    List<Assignment> assignments = new ArrayList<>();

    List<Assignment> classAssignments = new ArrayList<>();

    String query = "";

    String phScheme = "--- select scheme ---";
    String phBranch = "--- select branch ---";
    String phYear = "--- select year ---";
    String phSem = "--- select semester ---";
    String phSec = "--- select section ---";
    String phSub = "--- select subject ---";
    Connection con = DatabaseHelper.SQLConnection();
    public List<String> fetchSchemes() throws SQLException {

        query = "SELECT DISTINCT SCHEME FROM course";
        try(PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery()){
             schemes.clear();
             schemes.add(phScheme);
             while(rs.next()){
                 schemes.add(rs.getString("SCHEME"));
             }
        }
        return schemes;
    }

    public List<String> fetchBranches(String scheme) throws SQLException {

        query = "SELECT DISTINCT branch FROM course WHERE scheme = ? order by branch";

        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, scheme);
            try(ResultSet rs = pst.executeQuery()){
                branch_codes.clear();

                while(rs.next()){
                    branch_codes.add(rs.getString("branch"));
                }
            }
        }
        branches.clear();
        branches.add(phBranch);
        branches.addAll(BranchYearExtractor.extractBranchList(branch_codes));

        return branches;
    }

    public List<String> fetchYears(String scheme, String branch) throws SQLException {

        query = "SELECT DISTINCT branch FROM course WHERE scheme = ? AND branch like ?";

        try(PreparedStatement pst = con.prepareStatement(query)){
            String branch_code = BranchYearExtractor.getBranchOnlyCode(branch); // one digit branch code like CST = 3
            pst.setString(1, scheme);
            pst.setString(2, branch_code+"_");

            try(ResultSet rs = pst.executeQuery()){

                branch_codes.clear();
                while(rs.next()){
                    branch_codes.add(rs.getString("branch"));
                }
            }
        }

        years.clear();
        years.add(phYear);
        years.addAll(BranchYearExtractor.extractYearList(branch_codes));

        return years;
    }

    public List<String> fetchSemesters(String scheme, String branchcode) throws SQLException {

        query = "SELECT DISTINCT sem FROM course WHERE scheme = ? AND branch = ?";

        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, scheme);
            pst.setString(2, branchcode);
            try(ResultSet rs = pst.executeQuery()){
                semesters.clear();
                semesters.add(phSem);
                while(rs.next()){
                    semesters.add(rs.getString("sem"));
                }
            }
        }
        return semesters;
    }

    public List<String> fetchSections(){
        sections.clear();
        List<String> temp_sec = Arrays.asList("A","B","C","D");
        sections.add(phSec);
        sections.addAll(temp_sec);
        return sections;
    }

    public List<String> fetchSubjects(String scheme, String branch, String sem) throws SQLException {
        query = "SELECT DISTINCT subname, scode FROM course WHERE scheme = ? AND branch = ? AND sem = ?";

        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, scheme);
            pst.setString(2, branch); //here branch = branchcode like 12,33...
            pst.setString(3, sem);

            try(ResultSet rs = pst.executeQuery()){

                subjects.clear();
                subjects.add(phSub);

                while(rs.next()){
                    String subname_code = rs.getString("subname") + "(" + rs.getString("scode") + ")";
                    subjects.add(subname_code);// subname_code = subname + scode
                }
            }
        }


        return subjects;
    }

    public List<String> fetchEmployees() throws SQLException {
        query = "SELECT DISTINCT empid, name FROM Employee ORDER BY empid";

        try(PreparedStatement pst = con.prepareStatement(query);
            ResultSet rs = pst.executeQuery()){

            employees.clear();

            while(rs.next()){
                String empId = rs.getString("empid");
                String empName = rs.getString("name");
                String employee = empId + " - " + empName;
                employees.add(employee);
            }
        }
        return employees;
    }

    public List<Assignment> fetchAssignments(String emp_id) throws SQLException {
        query = "SELECT branch, section, sem, scode FROM AssignmentsTable WHERE employee_id = ?";

        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, emp_id);
            try(ResultSet rs = pst.executeQuery()){

                assignments.clear();
                while(rs.next()){
                    int sem = rs.getInt("sem");
                    String branch_name = BranchYearExtractor.getBranchName(rs.getInt("branch"));
                    String section = rs.getString("section");
                    String sub_code = rs.getString("scode");

                    Assignment assignment = new Assignment(sem, branch_name, section, sub_code);
                    assignments.add(assignment);
                }
            }
        }

        return assignments;
    }
    public List<Assignment> fetchAssignments(String scheme, String branch_name, int year, int sem, String section) throws SQLException {

        String branch_code = BranchYearExtractor.generateBranchCode(branch_name, String.valueOf(year));

        query = "select scode, employee_name, employee_id from AssignmentsTable where branch = ? and sem = ? and section = ? and scheme = ?";
        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setInt(1, Integer.parseInt(branch_code));
            pst.setInt(2, sem);
            pst.setString(3, section);
            pst.setString(4, scheme);

            try(ResultSet rs = pst.executeQuery()){
                classAssignments.clear();

                while(rs.next()){
                    String sub_code = rs.getString("scode");
                    String emp_id = rs.getString("employee_id");
                    String emp_name = rs.getString("employee_name");

                    classAssignments.add(new Assignment(sub_code, emp_name, emp_id));
                }
            }
        }

        return classAssignments;
    }

    public List<String> fetchStudents(String class_name) throws SQLException {

        String[] parts = class_name.split(" ");
        int sem = Integer.parseInt(parts[0]);
        String branch_name = parts[1];
        String section = parts[2];
        int year = (sem % 2 == 0 ? sem/2 : sem/2+1);

        String branch = BranchYearExtractor.generateBranchCode(branch_name, String.valueOf(year));

        Log.d("dataFetcher",
                "sem : "+sem+", sec : "+section + ", branch : "+ branch
                );

        query = "select distinct rollno, name from StudentTable where sem = ? and sec = ? and BRANCH = ?";

        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setInt(1, sem);
            pst.setString(2, section);
            pst.setString(3, branch);
            Log.d("Datafetcher","Statement created ....");

            try(ResultSet rs = pst.executeQuery()){
                students.clear();

                Log.d("datafetcher","Statement executed....");

                while(rs.next()){
                    String roll = rs.getString("ROLLNO");
                    String name = rs.getString("NAME");

                    students.add(roll+" "+name);
                }
            }
        }
        return students;
    }
}


