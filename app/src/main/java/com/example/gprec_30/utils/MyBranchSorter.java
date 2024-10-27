package com.example.gprec_30.utils;

import android.util.Log;

import java.util.*;

public class MyBranchSorter {
    private final Map<String, Set<String>> branchYearMap = new HashMap<>();

    public MyBranchSorter(List<String> branches) {
        for (String branch : branches) {
            String bname = branch.substring(0, 1);
            String year = branch.substring(1);

            branchYearMap.putIfAbsent(bname, new HashSet<>());

            branchYearMap.get(bname).add(year);
        }
    }

    public List<String> getBranchNames() {
        List<String> branches = BranchYearExtractor.extractBranchList(new ArrayList<>(branchYearMap.keySet()));
        Log.d("MyBranchYearSorter", "getBranchNames: \n"+branches);
        if (branches.isEmpty()){
            Log.d("myBranchYearSorter", "getBranchNames: no branch extracter from branch year extractor");
        }
        return branches;
    }

    public List<String> getYears(String branch) {
        String branchNum = BranchYearExtractor.getBranchOnlyCode(branch);
        Log.d("My Branch Year Sorter", "getYears: the branch num is" + branchNum);

        Set<String> years = branchYearMap.getOrDefault(branchNum, new HashSet<>());
        return new ArrayList<>(years);
    }
}
