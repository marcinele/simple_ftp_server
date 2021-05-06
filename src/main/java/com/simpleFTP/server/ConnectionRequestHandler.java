package com.simpleFTP.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionRequestHandler extends Thread{
    private ServerSocket serverSocket;
    private String ip_addr;
    private int port;
    private boolean running = false;

    public ConnectionRequestHandler(String ip_addr, int port) {
        this.port = port;
        this.ip_addr = ip_addr;
    }

    public void startServer()
    {
        try
        {
            this.serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip_addr, port));
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
            System.out.println("Listening for a connection on addres: " + serverSocket.getInetAddress().getHostAddress() +":"  + serverSocket.getLocalPort());
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
