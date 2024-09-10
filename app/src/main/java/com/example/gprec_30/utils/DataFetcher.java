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

    String query = "";

    //these are still need to be displayed in the see assignments fragment, we haven't modified the code there to not use this placeholders.
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


    //for the see assignments fragment
    public List<ClassAssignment> getClassAssignments(String regCodePattern) {
        List<ClassAssignment> assignments = new ArrayList<>();
        regCodePattern += "%";
        String query = "SELECT course.scode, assignments.empid, employee.name " +
                "FROM course " +
                "INNER JOIN assignments ON course.courseid = assignments.courseid " +
                "INNER JOIN employee ON assignments.empid = employee.empid " +
                "WHERE assignments.reg_code LIKE ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, regCodePattern);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String scode = rs.getString("scode");
                String empId = rs.getString("empid");
                String employeeName = rs.getString("name");

                ClassAssignment assignment = new ClassAssignment(scode, empId, employeeName);
                assignments.add(assignment);
            }
        } catch (SQLException e) {
            Log.d("DataFetcher", "Some error occurred while fetching the employee assignments!");
        }
        return assignments;
    }

    public List<String> getEmployeeAssignmentsSimple(String empid){
        List<String> assignments = new ArrayList<>();

        String q = "select reg_code from assignments where empid = ?";

        try(PreparedStatement pst = con.prepareStatement(q)){
            pst.setString(1, empid);

            ResultSet rs = pst.executeQuery();

            while (rs.next()){
                assignments.add(rs.getString("reg_code"));
            }
        }catch(SQLException e){
            Log.d("Simple employee assignments fetcher", "Error in simple: "+e.getMessage());
        }

        return assignments;
    }
    //for the take attendance fragment, in the class selection dialogue
    public List<EmployeeAssignment> getEmployeeAssignments(String empId) {
        List<EmployeeAssignment> assignments = new ArrayList<>();
        String query = "SELECT course.branch, course.sem, assignments.section, course.scode " +
                "FROM course " +
                "INNER JOIN assignments ON assignments.courseid = course.courseid " +
                "WHERE assignments.empid = ?";

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, empId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int branch = rs.getInt("branch");
                int sem = rs.getInt("sem");
                String section = rs.getString("section");
                String scode = rs.getString("scode");

                EmployeeAssignment assignment = new EmployeeAssignment(branch, sem, section, scode);
                assignments.add(assignment);
            }
        } catch (SQLException e) {
            Log.d("DataFetcher", "Some error occurred while fetching the class assignments!");
        }
        return assignments;
    }



    public int getCourseId(String selectedScheme, String selectedBranchYear, String selectedSemester, String subCode, String selectedSubject) {
        int courseId;
        String query = "select courseid from course WHERE scheme = ? and branch = ? and sem = ? AND scode = ? AND subname = ?";
        try{
            PreparedStatement st = con.prepareStatement(query);
            st.setString(1, selectedScheme);
            st.setInt(2, Integer.parseInt(selectedBranchYear));
            st.setInt(3, Integer.parseInt(selectedSemester));
            st.setString(4, subCode);
            st.setString(5, selectedSubject);

            ResultSet rs = st.executeQuery();
            if (rs.next()){
                return rs.getInt("courseid");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public List<String> fetchStudents(EmployeeAssignment selectedClass) {
        List<String> students = new ArrayList<>();

        String q = "select rollno, name from students where branch = ? and sec = ? and sem = ?";

        try(PreparedStatement pst = con.prepareStatement(q)){
            pst.setInt(1, selectedClass.getBranch());
            pst.setString(2, selectedClass.getSection());
            pst.setInt(3, selectedClass.getSem());

            ResultSet rs = pst.executeQuery();

            while(rs.next()){
                students.add(rs.getString(1)+" "+rs.getString(2));
            }
        }catch(SQLException e){
            Log.d("Data fetcher", "Error fetching the students : \n"+e.getMessage());
        }

        return students;
    }
}


