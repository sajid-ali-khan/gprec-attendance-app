package com.example.gprec_30.utils;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Objects;

public class DatabaseHelper {
    static Connection con;
    @SuppressLint("NewApi")
    public static Connection SQLConnection()
    {
        String ip="192.168.1.114",port="49170",dbname="college",un="sajid",pass="S@j1d2024!";
        StrictMode.ThreadPolicy tp=new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(tp);
        String ConURL;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConURL="jdbc:jtds:sqlserver://"+ip+ ":"+port+";"+"databasename="+dbname+";user="+un+";password="+pass+";";
            con= DriverManager.getConnection(ConURL);
        }
        catch (Exception e)
        {
            Log.e("Error1", Objects.requireNonNull(e.getMessage()));
        }
        return con;
    }
}
