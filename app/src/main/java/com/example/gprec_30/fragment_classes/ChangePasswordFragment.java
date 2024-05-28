package com.example.gprec_30.fragment_classes;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.gprec_30.R;
import com.example.gprec_30.utils.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChangePasswordFragment extends Fragment {

    EditText et_old_pwd, et_new_pwd, et_confirm_pwd;

    Button btn_changePassword;

    String emp_id, old_pwd, new_pwd, confirm_pwd;

    CheckBox cb_show_pwd;

    Connection con = DatabaseHelper.SQLConnection();

    public ChangePasswordFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_change_password, container, false);

        if (getArguments() != null) {
            emp_id = getArguments().getString("emp_id");
        }

        Log.d("ChangePasswordFragment", "User ID: " + emp_id);

        if (con == null) {
            Log.e("ChangePasswordFragment", "Database connection is null");
            showToast("Database connection error");
        }

        //hooks
        et_old_pwd = rootView.findViewById(R.id.et_oldPassword);
        et_new_pwd = rootView.findViewById(R.id.et_newPassword);
        et_confirm_pwd = rootView.findViewById(R.id.et_confirmPassword);
        btn_changePassword = rootView.findViewById(R.id.btn_changePassword);
        cb_show_pwd = rootView.findViewById(R.id.checkBoxShowPassword);


        cb_show_pwd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Determine the transformation method based on the checkbox state
            TransformationMethod transformationMethod = isChecked ?
                    HideReturnsTransformationMethod.getInstance() :
                    PasswordTransformationMethod.getInstance();

            // Apply the transformation method to the EditText fields
            et_old_pwd.setTransformationMethod(transformationMethod);
            et_new_pwd.setTransformationMethod(transformationMethod);
            et_confirm_pwd.setTransformationMethod(transformationMethod);

            // Set cursor position to the end of text after transformation
            et_old_pwd.setSelection(et_old_pwd.getText().length());
            et_new_pwd.setSelection(et_new_pwd.getText().length());
            et_confirm_pwd.setSelection(et_confirm_pwd.getText().length());
        });

        btn_changePassword.setOnClickListener(v -> {
            try {
                changePassword();
            } catch (SQLException e) {
                showToast(e.getMessage());
            }
        });
        return rootView;
    }

    public void changePassword() throws SQLException {

        //fetching values
        old_pwd = et_old_pwd.getText().toString().trim();
        new_pwd = et_new_pwd.getText().toString().trim();
        confirm_pwd = et_confirm_pwd.getText().toString().trim();

        if(allFilled()){
            if(AuthenticateInputs()){
                String query = "update Employee set pwd = ? Where empid = ?";
                try(PreparedStatement pst = con.prepareStatement(query)){
                    pst.setString(1, new_pwd);
                    pst.setString(2, emp_id);
                    int rows_affected = pst.executeUpdate();

                    if(rows_affected == 1){
                        Toast.makeText(requireContext(), "Password changed successfully.", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    }else{
                        Toast.makeText(requireContext(), "Couldn't change the password!", Toast.LENGTH_SHORT).show();
                    }
                }
            }else{
                Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean allFilled(){
        return !old_pwd.isEmpty() && !new_pwd.isEmpty() && !confirm_pwd.isEmpty();
    }

    public boolean AuthenticateInputs() throws SQLException {

        String query = "select PWD from Employee where EMPID = ?";

        try(PreparedStatement pst = con.prepareStatement(query)){
            pst.setString(1, emp_id);

            try(ResultSet rs = pst.executeQuery()){
                if(rs.next()){
                    String og_pwd = rs.getString("PWD");
                    if(og_pwd.equals(old_pwd)){
                        if(new_pwd.equals(confirm_pwd)){
                            return true;
                        }else{
                            Toast.makeText(requireContext(), "The confirm password should be same as new password.", Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }else{
                        Toast.makeText(requireContext(), "Incorrect Old Password!", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }else{
                    Toast.makeText(requireContext(), "The User Doesn't Exist", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
    }

    public void clearInputs(){
        et_old_pwd.setText("");
        et_new_pwd.setText("");
        et_confirm_pwd.setText("");
    }

    public void showToast(String e){
        Toast.makeText(requireContext(), e, Toast.LENGTH_SHORT).show();
    }
}
