package com.simpleFTP.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class RequestHandler extends Thread{
    private Socket socket;

    public RequestHandler(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        System.out.println("Received a connection.");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(socket.isConnected())
            {
                String line = in.readLine();
                System.out.println("Server: " + line);
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to handle connection");
        }

    }
}
