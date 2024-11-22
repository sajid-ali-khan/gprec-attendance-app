package com.example.gprec_30;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gprec_30.fragment_classes.ChangePasswordFragment;
import com.example.gprec_30.fragment_classes.HomeFragment;
import com.example.gprec_30.fragment_classes.TakeAttendanceFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

public class TeacherActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    String emp_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        emp_id = getIntent().getStringExtra("emp_id");

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set the Toolbar as the ActionBar
        setSupportActionBar(toolbar);

        // Set up the hamburger icon for the navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        FrameLayout container = findViewById(R.id.container);

        // Create the fragment instance and set the arguments
        TakeAttendanceFragment takeAttendanceFragment = new TakeAttendanceFragment();
        Bundle bundle = new Bundle();
        bundle.putString("emp_id", emp_id);
        takeAttendanceFragment.setArguments(bundle);

        // Replace the container with the TakeAttendanceFragment
        getSupportFragmentManager().beginTransaction()
                .replace(container.getId(), new HomeFragment())
                .commit();

        // Set a listener on the navigation drawer to handle item clicks
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            // Handle navigation item clicks here
            int id = menuItem.getItemId();
            if (id == R.id.nav_home){
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
            }else if (id == R.id.nav_take_attendance) {
                // Create the fragment instance and set the arguments
                TakeAttendanceFragment fragment = new TakeAttendanceFragment();
                fragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(container.getId(), fragment)
                        .commit();
            } else if (id == R.id.nav_change_password) {
                ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                changePasswordFragment.setArguments(bundle);


                // Replace the current fragment with the ChangePasswordFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, changePasswordFragment)
                        .commit();
            } else if (id == R.id.nav_logout) {
                // Display logout confirmation dialog
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setPositiveButton("Logout", (dialog, which) -> logoutUser())
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            // Close the drawer after handling item click
            drawerLayout.closeDrawers();
            return true;
        });
    }

    // Handle logout action
    private void logoutUser() {
        // Perform logout action here, e.g., clear session, reset preferences
        // Then navigate back to the login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Close the current activity
    }

    // Override onBackPressed to show logout confirmation dialog
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> logoutUser())
                .setNegativeButton("Cancel", null)
                .show();
    }


    public void handleException(Exception e){
        Toast.makeText(TeacherActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
