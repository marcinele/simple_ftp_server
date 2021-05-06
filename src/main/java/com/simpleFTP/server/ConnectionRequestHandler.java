package com.simpleFTP.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionRequestHandler extends Thread{
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;

    public ConnectionRequestHandler(int port)
    {
        this.port = port;
    }

    public void startServer()
    {
        try
        {
            this.serverSocket = new ServerSocket(this.port);
            this.start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Unable to start server.");
        }
    }

    @Override
    public void run()
    {
        this.running = true;
        while(running)
        {
            System.out.println("Listening for a connection on addres: " + serverSocket.getInetAddress().getHostAddress());
            try {
                Socket socket = this.serverSocket.accept();
                FtpServerPI ftpServerPI = new FtpServerPI(socket);
                ftpServerPI.start();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Unable to accept a connection.");
            }

        }

    }

    public void turnOff()
    {
        this.running = false;
    }


}