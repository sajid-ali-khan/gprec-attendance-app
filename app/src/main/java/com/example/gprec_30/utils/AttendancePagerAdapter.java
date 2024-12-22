package com.example.gprec_30.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.gprec_30.fragment_classes.ClassAttendanceFragment;
import com.example.gprec_30.fragment_classes.StudentAttendanceFragment;

public class AttendancePagerAdapter extends FragmentStateAdapter {

    public AttendancePagerAdapter(@NonNull Fragment fragment) {
        super(fragment); // Pass the parent fragment
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the fragment based on tab position
        if (position == 0) {
            return new ClassAttendanceFragment(); // First tab
        } else {
            return new StudentAttendanceFragment(); // Second tab
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of tabs
    }
}

