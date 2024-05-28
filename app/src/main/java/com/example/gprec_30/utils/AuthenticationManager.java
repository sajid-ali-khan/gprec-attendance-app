package com.example.gprec_30.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationManager {

    private String role = "";

    public boolean authenticateUser(String emp_id, String password, Context context) {
        boolean isAuthenticated = false;
        String query = "select empid, pwd, role from Employee where empid = ?";

        try (Connection con = DatabaseHelper.SQLConnection()) {
            if (con != null) {
                try (PreparedStatement pst = con.prepareStatement(query)) {
                    pst.setString(1, emp_id);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            String og_pwd = rs.getString("pwd");
                            if (og_pwd.equals(password)) {
                                role = rs.getString("role");
                                return true;
                            } else {
                                showToast(context, "Incorrect password!");
                                return false;
                            }
                        } else {
                            showToast(context, "User doesn't exist.");
                            return false;
                        }
                    } catch (Exception e) {
                        showToast(context, e.getMessage());
                    }
                }
            } else {
                showToast(context, "Failed to connect to the database.");
            }
        } catch (SQLException e) {
            Log.e("AuthenticationManager", "Error while authenticating user", e);
            showToast(context, "An error occurred. Please try again later.");
        }

        return isAuthenticated;
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }


    public String getRole() {
        return role;
    }
}
