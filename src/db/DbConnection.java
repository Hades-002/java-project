package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public abstract class DbConnection {
    private final String URL = "jdbc:mysql://localhost:3306/projectjava";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private final String DRIVER = "com.mysql.cj.jdbc.Driver"; // modern driver

    public Connection con;
    public PreparedStatement prep;

    public void connect() {
        try {
            // call JDBC driver
            Class.forName(DRIVER);
            con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println(e);
        }
    }

    // Optional: get connection object
    public Connection getConnection() {
        return con;
    }
}
