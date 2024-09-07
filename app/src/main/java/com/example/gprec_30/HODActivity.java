package com.example.gprec_30;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gprec_30.fragment_classes.ChangePasswordFragment;
import com.example.gprec_30.fragment_classes.HomeFragment;
import com.example.gprec_30.fragment_classes.SeeAssignmentsFragment;
import com.example.gprec_30.fragment_classes.TakeAttendanceFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

public class HODActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    String emp_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hod);

        emp_id = getIntent().getStringExtra("emp_id");

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        FrameLayout container = findViewById(R.id.container);

        TakeAttendanceFragment takeAttendanceFragment = new TakeAttendanceFragment();
        Bundle bundle = new Bundle();
        bundle.putString("emp_id", emp_id);
        takeAttendanceFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(container.getId(), new HomeFragment())
                .commit();


        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_home){
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
            }
            else if(id == R.id.nav_see_assignments){
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new SeeAssignmentsFragment()).commit();
            }else if(id == R.id.nav_take_attendance){
                TakeAttendanceFragment fragment = new TakeAttendanceFragment();
                fragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(container.getId(), fragment)
                        .commit();
            }else if(id == R.id.nav_change_password){
                ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                changePasswordFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, changePasswordFragment)
                        .commit();
            }else if(id == R.id.nav_logout){
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Logout")
                        .setMessage("Do you want to logout?")
                        .setPositiveButton("Logout", (dialog, which) -> logoutUser())
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            drawerLayout.closeDrawers();
            return true;
        });
    }

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
}