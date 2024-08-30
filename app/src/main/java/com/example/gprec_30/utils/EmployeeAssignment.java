package com.example.gprec_30.utils;

public class EmployeeAssignment {
    private int branch;  // Branch of the course
    private int sem;  // Semester
    private String section;  // Section assigned
    private String scode;  // Subject code

    // Constructor, getters, and setters
    public EmployeeAssignment(int branch, int sem, String section, String scode) {
        this.branch = branch;
        this.sem = sem;
        this.section = section;
        this.scode = scode;
    }

    // Getters and Setters
    public String getBranch() {
        return BranchYearExtractor.getBranchName(branch);
    }

    public int getYear(){
        return BranchYearExtractor.getYear(branch);
    }

    public int getSem() { return sem; }

    public String getSection() { return section; }

    public String getScode() { return scode; }
}

