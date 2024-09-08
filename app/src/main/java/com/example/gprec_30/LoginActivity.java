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

import androidx.appcompat.app.AppCompatActivity;

import com.example.gprec_30.utils.auths.AuthResult;
import com.example.gprec_30.utils.auths.AuthenticationManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText et_userId, et_password;

    ExecutorService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        service = Executors.newSingleThreadExecutor();

        et_userId = findViewById(R.id.et_emp_id);
        et_password = findViewById(R.id.et_pwd);
        CheckBox checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword);
        Button btn_submit = findViewById(R.id.buttonSubmit);

        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); //show password
            } else {
                et_password.setTransformationMethod(PasswordTransformationMethod.getInstance()); //hide password
            }
            et_password.setSelection(et_password.getText().length());
        });

        btn_submit.setOnClickListener(v -> {
            service.execute(() -> {
                String empid = et_userId.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                AuthResult authResult = AuthenticationManager.authenticateUser(empid, password);

                runOnUiThread(() -> {
                    switch(authResult.getStatus()){
                        case SUCCESS:
                            showToast("Login Successful");
                            navigateToRoleActivity(authResult.getRole(), empid);
                            break;
                        case INCORRECT_PASSWORD:
                            showToast("Incorrect Password");
                            break;
                        case USER_NOT_FOUND:
                            showToast("User not found");
                            break;
                        case FAILED_CONNECTION:
                            showToast("Failed connecting to database.");
                            break;
                        case ERROR:
                            showToast("Error!!");
                    }
                });
            });
        });


    }


    private void navigateToRoleActivity(String role, String emp_id) {
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
        new MaterialAlertDialogBuilder(this)
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
