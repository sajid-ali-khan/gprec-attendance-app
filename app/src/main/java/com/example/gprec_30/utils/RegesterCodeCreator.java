package com.example.gprec_30.utils;

import java.time.*;

public class RegesterCodeCreator {

    public static String createRegCode(String selectedScheme, String selectedBranch, String selectedSemester, String selectedSection) {
        String reg_code = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int c_year = Integer.parseInt(String.valueOf(Year.now()));
            if (Integer.parseInt(selectedSemester) % 2 == 0){
                c_year -= 1;
            }
            int n_year = ((c_year%100)+1)%100;
            String academic_year = String.valueOf(c_year)+"-"+String.valueOf(n_year);
            reg_code += academic_year + "_"+selectedScheme+"_"+selectedBranch+"_"+selectedSemester+"_"+selectedSection;
        }else{
            throw new RuntimeException("Time not compatable with your mobile.");
        }
        return reg_code;
    }

    public static String createRegCode(String selectedScheme, String selectedBranch, String selectedSemester, String selectedSection, String scode){
        return createRegCode(selectedScheme, selectedBranch, selectedSemester, selectedSection).concat("_"+scode);
    }
}
