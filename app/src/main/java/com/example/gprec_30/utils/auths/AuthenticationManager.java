package com.example.gprec_30.utils.auths;

import android.util.Log;

import com.example.gprec_30.utils.DatabaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationManager {

    public static AuthResult authenticateUser(String emp_id, String password) {
        String query = "select empid, pwd, role from Employee where empid = ?";

        try (Connection con = DatabaseHelper.SQLConnection()) {
            if (con != null) {
                try (PreparedStatement pst = con.prepareStatement(query)) {
                    pst.setString(1, emp_id);
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            String og_pwd = rs.getString("pwd");
                            if (og_pwd.equals(password)) {
                                String role = rs.getString("role");
                                return new AuthResult(AuthStatus.SUCCESS, role);
                            } else {
                                return new AuthResult(AuthStatus.INCORRECT_PASSWORD, null);
                            }
                        } else {
                            return new AuthResult(AuthStatus.USER_NOT_FOUND, null);
                        }
                    } catch (Exception e) {
                        Log.d("AuthenticationManager", "authenticateUser: "+e.getMessage());
                        return new AuthResult(AuthStatus.ERROR, null);
                    }
                }
            } else {
                return new AuthResult(AuthStatus.FAILED_CONNECTION, null);
            }
        } catch (SQLException e) {
            Log.e("AuthenticationManager", "Error while authenticating user", e);
            return new AuthResult(AuthStatus.ERROR, null);
        }
    }
}


