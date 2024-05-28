package com.example.gprec_30.utils;
public class Assignment{

    String emp_id;

    String emp_name;
    String branch;
    int sem;
    String section;
    String sub_code;
    String scheme;

    public Assignment(int sem, String branch, String section, String sub_code) {
        this.sem = sem;
        this.branch = branch;
        this.section = section;
        this.sub_code = sub_code;
    }

    public Assignment(String sub_code, String emp_name, String emp_id){
        this.sub_code = sub_code;
        this.emp_name = emp_name;
        this.emp_id = emp_id;
    }

    public String getEmpId() {
        return emp_id;
    }

    public String getEmpName() {
        return emp_name;
    }

    public String getScheme() {
        return scheme;
    }

    public int getSem() {
        return sem;
    }

    public String getBranch() {
        return branch;
    }

    public String getSection() {
        return section;
    }

    public String getSubCode() {
        return sub_code;
    }
}
