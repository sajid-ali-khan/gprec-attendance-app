package com.example.gprec_30;


//finally made the change
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gprec_30.utils.AuthenticationManager;

public class LoginActivity extends AppCompatActivity {

    private String emp_id = "";
    private EditText et_userId, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_userId = findViewById(R.id.et_emp_id);
        et_password = findViewById(R.id.et_pwd);
        CheckBox checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        Button btn_submit = findViewById(R.id.buttonSubmit);
        Button buttonForgotPassword = findViewById(R.id.buttonForgotPassword);

        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); //show password
            } else {
                et_password.setTransformationMethod(PasswordTransformationMethod.getInstance()); //hide password
            }
            et_password.setSelection(et_password.getText().length());
        });

        btn_submit.setOnClickListener(v -> {
            emp_id = et_userId.getText().toString().trim().toLowerCase();
            String password = et_password.getText().toString().trim();

            AuthenticationManager authenticator = new AuthenticationManager(); // Instantiate here

            if(authenticator.authenticateUser(emp_id, password, this)){
                showToast("Login Successful");
                String role = authenticator.getRole();
                navigateToRoleActivity(role);
            }
        });


        buttonForgotPassword.setOnClickListener(v -> Toast.makeText(LoginActivity.this, "Forgot Password clicked", Toast.LENGTH_SHORT).show());
    }


    private void navigateToRoleActivity(String role) {
        Intent intent;
        switch (role.toLowerCase()) {
            case "teacher":
                intent = new Intent(LoginActivity.this, TeacherActivity.class);
                break;
            case "head":
                intent = new Intent(LoginActivity.this, HODActivity.class);
                break;
            case "admin":
                intent = new Intent(LoginActivity.this, AdminActivity.class);
                break;
            default:
                showToast("Though login was successful, some error occured while taking you to next activity.");
                return;
        }
        intent.putExtra("emp_id", emp_id);
        startActivity(intent);
        finish();
    }

    // In LoginActivity
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you really want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    finishAffinity(); // Close all activities in the task, effectively exiting the app
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    void showToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
