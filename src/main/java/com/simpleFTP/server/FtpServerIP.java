package com.simpleFTP.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class FtpServerIP extends Thread{
    private Socket socket;
    private User user;
    private boolean isRunning;
    private BufferedReader in;
    private PrintStream out;

    public FtpServerIP(Socket socket) {
        this.socket = socket;
        this.user = new User();
        this.user.setLoggedIn(false);
        this.isRunning = true;
        configureStreams();
    }

    @Override
    public void run()
    {
        System.out.println("Received a connection.");
        out.println("220 Service ready for new user.");
        try {
            while(isRunning){
                String input = in.readLine();
                String cmd = input.split(" ")[0];
                switch (cmd){
                    case "USER":
                        login(input);
                        break;
                    case "PASSWORD":
                        out.println("503 Bad sequence of commands.");
                        break;
                }
            }

        } catch (IOException  e) {
            e.printStackTrace();
            System.out.println("Unable to handle connection");
        }

        System.out.println("hehe");

    }

    private void configureStreams(){
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void login(String cmd){
        if(!cmd.startsWith("USER")){
            out.println("500 Syntax error, command unrecognized.");
        } else if(cmd.split(" ").length != 2){
            out.println("501 Syntax error in parameters or arguments.");
        } else{
            user.setUsername(cmd.split(" ")[1]);
            out.println("331 User name okay, need password.");
        }

        //Password
        try {
            cmd = in.readLine();
            if(!(cmd.startsWith("PASS"))){
                out.println("500 Syntax error, command unrecognized.");
            } else if(!(cmd.split(" ").length == 2)){
                out.println("501 Syntax error in parameters or arguments.");
            } else{
                user.setPassword(cmd.split(" ")[1]);
                AuthorizationHandler authorizationHandler = new AuthorizationHandler();
                if(authorizationHandler.checkCredentials(user)){
                    user.setLoggedIn(true);
                    out.println("230 User logged in, proceed.");
                } else{
                    out.println("530 Not logged in.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            out.println();
        }

    }
}
