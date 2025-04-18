package com.ordermanagementsystem.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/orderdb";
    private static final String USER = "root";
    private static final String PASSWORD = "kiruba12";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
