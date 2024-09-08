package com.example.gprec_30.utils.auths;

public class AuthResult{
    private final AuthStatus status;
    private final String role;

    public AuthResult(AuthStatus status, String role) {
        this.status = status;
        this.role = role;
    }

    public AuthStatus getStatus() {
        return status;
    }

    public String getRole() {
        return role;
    }
}
