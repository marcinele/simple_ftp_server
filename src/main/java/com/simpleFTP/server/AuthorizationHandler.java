package com.simpleFTP.server;

import com.simpleFTP.db.DatabaseConnector;

import java.lang.module.ResolutionException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class AuthorizationHandler{

    private DatabaseConnector dbConnector;

    public AuthorizationHandler(){
        dbConnector = DatabaseConnector.getInstance();
    }

    public boolean checkCredentials(String username, byte[] password) throws SQLException {

        String use_db = "USE test_db;";
        String sql = "SELECT password FROM users WHERE username=\"" + username + "\";";
        dbConnector.execute(use_db);
        ResultSet result = dbConnector.query(sql);
        byte[] pass_hash;
        try{
            result.next();
            pass_hash = result.getBytes("password");
            System.out.println(pass_hash);
        } catch(Exception e){
            return false;
        }
        return Arrays.equals(pass_hash, password);
    }

}
