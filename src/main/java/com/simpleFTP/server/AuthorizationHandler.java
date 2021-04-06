package com.simpleFTP.server;

import com.simpleFTP.db.DatabaseConnector;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.module.ResolutionException;
import java.util.Arrays;
import java.util.Scanner;

public class AuthorizationHandler{



    public AuthorizationHandler(){

    }

    public boolean checkCredentials(User user) throws FileNotFoundException {
        File file = new File("./users.txt");
        Scanner reader = new Scanner(file);
        while(reader.hasNextLine()){
            String line  = reader.nextLine();
            if(line.split(" ")[0].equals(user.getUsername())){
                if(line.split(" ")[1].equals(user.getPassword())){
                    return true;
                }
            }
        }
        return false;
    }

}
