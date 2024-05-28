package com.example.gprec_30.utils;

import java.util.*;

public class BranchYearExtractor {
    private static final Map<Character, String> branchMap = new HashMap<>();

    static {
        branchMap.put('1', "CSE");
        branchMap.put('2', "CIV");
        branchMap.put('3', "CST");
        branchMap.put('4', "ECE");
        branchMap.put('5', "MEC");
        branchMap.put('6', "CSB");
        branchMap.put('7', "EEE");
        branchMap.put('8', "CSD");
        branchMap.put('9', "CSM");
    }

    public static ArrayList<String> extractBranchList(List<String> branches) {
        Set<String> distinctBranches = new HashSet<>();
        for (String branch : branches) {
            if (isValidBranch(branch)) {
                distinctBranches.add(branchMap.get(branch.charAt(0)));
            }
        }
        return new ArrayList<>(distinctBranches);
    }

    public static ArrayList<String> extractYearList(List<String> branches) {
        Set<String> distinctYears = new HashSet<>();
        for (String branch : branches) {
            if (isValidBranch(branch)) {
                distinctYears.add(branch.substring(1));
            }
        }
        return new ArrayList<>(distinctYears);
    }

    public static String generateBranchCode(String branchName, String year) {
        String code = null;
        for (Map.Entry<Character, String> entry : branchMap.entrySet()) {
            if (entry.getValue().equals(branchName)) {
                code = entry.getKey() + String.valueOf(year);
                break;
            }
        }
        return code;
    }

    public static String getBranchOnlyCode(String branchName) {//to get the 1 - digited branch code
        String code = null;
        for (Map.Entry<Character, String> entry : branchMap.entrySet()) {
            if (entry.getValue().equals(branchName)) {
                code = String.valueOf(entry.getKey());
                break;
            }
        }
        return code;
    }

    public static String getBranchName(int branch) {
        char[] parts = String.valueOf(branch).toCharArray();
        String branch_name = branchMap.get(parts[0]); // Convert char to Character
        if (branch_name == null) {
            return "Unknown";
        }
        return branch_name;
    }


    private static boolean isValidBranch(String branch) {
        return branch != null && branch.length() == 2 && Character.isDigit(branch.charAt(0)) && Character.isDigit(branch.charAt(1));
    }
}
