package com.example.gprec_30.utils;

public class ClassAssignment {
    private String scode;  // Subject code
    private String empId;  // Employee ID
    private String employeeName;  // Employee Name

    // Constructor, getters, and setters
    public ClassAssignment(String scode, String empId, String employeeName) {
        this.scode = scode;
        this.empId = empId;
        this.employeeName = employeeName;
    }

    // Getters and Setters
    public String getScode() { return scode; }
    public void setScode(String scode) { this.scode = scode; }

    public String getEmpId() { return empId; }
    public void setEmpId(String empId) { this.empId = empId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}

