package com.example.gprec_30.utils;

import java.io.Serializable;

public class AttendanceReportTable implements Serializable {
    String rollNumber;
    String daysPresent;
    String totalDays;
    Float attendancePercentage;

    public AttendanceReportTable(String rollNumber, String daysPresent, String totalDays, Float attendancePercentage) {
        this.rollNumber = rollNumber;
        this.daysPresent = daysPresent;
        this.totalDays = totalDays;
        this.attendancePercentage = attendancePercentage;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getDaysPresent() {
        return daysPresent;
    }

    public String getTotalDays() {
        return totalDays;
    }

    public Float getAttendancePercentage() {
        return attendancePercentage;
    }
}
