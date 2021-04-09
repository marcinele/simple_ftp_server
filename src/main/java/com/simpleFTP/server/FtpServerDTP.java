package com.simpleFTP.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpServerDTP extends Thread{
    private int port;
    private String address;
    private Socket socket;
    private BufferedReader in;
    private PrintStream out;
    private int type;


    public FtpServerDTP(Socket socket, int type){
        this.socket = socket;
        ConfigureStreams();
        this.type = type;
    }

    public FtpServerDTP(String host, int port, int type) throws IOException {
        this.port = port;
        this.socket = new Socket(host, port);
        this.type = type;
        ConfigureStreams();
    }

    private void ConfigureStreams(){
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        while(true){
            SendData("DUPA");
        }
    }

    public void SendData(String data){
        out.print(data + EOL());
    }

    private String EOL(){
        return switch (type) {
            case 0 -> "\r\n";
            case 1 -> "\n";
            default -> "\n";
        };
    }

}

