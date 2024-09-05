package com.example.gprec_30.utils;

import java.time.*;
import java.util.List;

public class RegesterCodeCreator {

    public static String createRegCode(String selectedScheme, String selectedBranch, String selectedSemester, String selectedSection) {
        String reg_code = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int c_year = Integer.parseInt(String.valueOf(Year.now()));
            if (Integer.parseInt(selectedSemester) % 2 == 0){
                c_year -= 1;
            }
            int n_year = ((c_year%100)+1)%100;
            String academic_year = c_year +"_"+ n_year;
            reg_code += academic_year + "_"+selectedScheme+"_"+selectedBranch+"_"+selectedSemester+"_"+selectedSection;
        }else{
            throw new RuntimeException("Time not compatable with your mobile.");
        }
        return reg_code;
    }

    public static String createRegCode(String selectedScheme, String selectedBranch, String selectedSemester, String selectedSection, String scode){
        return createRegCode(selectedScheme, selectedBranch, selectedSemester, selectedSection).concat("_"+scode);
    }


    public static EmployeeAssignment decodeRegCode(String reg_code){
        EmployeeAssignment decoded;
        int cnt = 0;
        int i;
        for (i = 0; i < reg_code.length(); i++){
            if (reg_code.charAt(i) == '_'){
                cnt += 1;
            }

            if (cnt == 3){
                i+=1;
                break;
            }
        }
        String branchname = reg_code.substring(i, i+3);
        i+= 4;
        int sem = Integer.parseInt(reg_code.substring(i, i+1));
        i+=2;
        String section = reg_code.substring(i, i+1);


        int year;
        if (sem%2 == 0){
            year = sem/2;
        }else{
            year = sem/2 + 1;
        }

        int branch = Integer.parseInt(BranchYearExtractor.generateBranchCode(branchname, String.valueOf(year)));


        decoded = new EmployeeAssignment(branch, sem, section);

        return decoded;
    }
}
