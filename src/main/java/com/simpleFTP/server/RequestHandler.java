package com.simpleFTP.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class RequestHandler extends Thread{
    private Socket socket;
    private boolean loggedIn;

    public RequestHandler(Socket socket) {
        this.socket = socket;
        this.loggedIn = false;
    }

    @Override
    public void run()
    {
        System.out.println("Received a connection.");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintStream out = new PrintStream(socket.getOutputStream());
            AuthorizationHandler authorizationHandler = new AuthorizationHandler();
            while(!loggedIn){
                String username = in.readLine();
                String password = in.readLine();
                byte[] hash = password.getBytes(StandardCharsets.UTF_8);
                System.out.println(hash);
                this.loggedIn =  authorizationHandler.checkCredentials(username, hash);
                out.println(this.loggedIn);
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            System.out.println("Unable to handle connection");
        }

        System.out.println("hehe");

    }
}
