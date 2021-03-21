package com.simpleFTP.db;
import java.sql.*;

public class DatabaseDriver {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/";

    static final String USER = "root";
    static final String PASS = "root123";

    Connection conn;
    Statement stmt;

    public DatabaseDriver()
    {
        this.conn = null;
        this.stmt = null;
    }

    public void connectToDatabase()
    {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            String select = "SELECT * FROM test_db.users;";

            ResultSet res = stmt.executeQuery(select);
            while(res.next())
            {
                System.out.println("username: " + res.getString("username"));
                System.out.println("password: " + res.getString("password"));
            }
        }  catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                if(conn != null)
                {
                    conn.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }
}
