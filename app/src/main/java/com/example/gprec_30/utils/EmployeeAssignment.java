package com.example.gprec_30.utils;

public class EmployeeAssignment {
    private int branch;
    private int sem;
    private String section;
    private String scode;

    public EmployeeAssignment(int branch, int sem, String section, String scode) {
        this.branch = branch;
        this.sem = sem;
        this.section = section;
        this.scode = scode;
    }



    public EmployeeAssignment(int branch, int sem, String section) {
        this.branch = branch;
        this.sem = sem;
        this.section = section;
    }

    public int getBranch(){
        return branch;
    }

    public String getBranchName() {
        return BranchYearExtractor.getBranchName(branch);
    }

    public int getYear(){
        return BranchYearExtractor.getYear(branch);
    }

    public int getSem() { return sem; }

    public String getSection() { return section; }

    public String getScode() { return scode; }
}

