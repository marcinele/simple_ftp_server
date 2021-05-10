// DTP
// The data transfer process establishes and manages the data connection.
// The DTP can be passive or active.

// user-DTP
// The data transfer process "listens" on the data port for a connection from a server-FTP process.
// If two servers are transferring data between them, the user-DTP is inactive.

package com.simpleFTP.client;
// Used for debugging
// Left for future development

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ClientDTP extends Thread{
    private Socket socket;
    private int port;
    private String host;
    private BufferedReader in;
    private PrintStream out;
    private ServerSocket serverSocket;

    public ClientDTP(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.start();
    }
    public ClientDTP(Socket socket){
        this.socket = socket;
        ConfigureStreams();
    }

    public ClientDTP(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
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
        try {
            this.socket = serverSocket.accept();
            ConfigureStreams();
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean flag = true;
        while(flag){
            try {
                String s = in.readLine();
                if(s == null){
                  flag = false;
                    System.out.println("KUNIEC");
                  break;
                }
                System.out.println(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
