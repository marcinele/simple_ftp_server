package com.simpleFTP.db;
import java.sql.*;

public class DatabaseConnector {
    private  static DatabaseConnector instance;
    private  static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private  static final String DB_URL = "jdbc:mysql://localhost/";

    private static final String USER = "root";
    private static final String PASS = "root123";

    private Connection conn;
    private Statement stmt;

    public DatabaseConnector()
    {
        this.conn = null;
        this.stmt = null;
        this.connectToDatabase();
    }

    public static DatabaseConnector getInstance() {
        if(instance == null){
            instance = new DatabaseConnector();
            return instance;
        } else {
            return instance;
        }
    }

    public void connectToDatabase()
    {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
        }  catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    public ResultSet query(String sql) throws SQLException {

        return stmt.executeQuery(sql);
    }

    public void execute(String insertInstructions) throws SQLException {
        this.stmt.execute(insertInstructions);
    }
}
