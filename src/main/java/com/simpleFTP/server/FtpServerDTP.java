// DTP
// The data transfer process establishes and manages the data connection.
// The DTP can be passive or active.

// server-DTP
// The data transfer process, in its normal "active" state, establishes the data connection with the "listening" data port.
// It sets up parameters for transfer and storage, and transfers data on command from its PI.  The DTP can be placed in a
// "passive" state to listen for rather than initiate a connection on the data port.

package com.simpleFTP.server;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;


import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpServerDTP extends Thread{
    private int port;
    private String address;
    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader in;
    private PrintStream out;
    private int type;


    public FtpServerDTP(ServerSocket serverSocket, int type){
        this.serverSocket = serverSocket;
        this.type = type;
        this.start();
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
        try {
            socket = serverSocket.accept();
            ConfigureStreams();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Cannot handle data transfer connection.");
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

    public InetAddress getInetAddress(){
        return serverSocket.getInetAddress();
    }

    public int getPort(){
        return serverSocket.getLocalPort();
    }

    public ServerSocket getServerSocket(){
        return serverSocket;
    }

    public void Close(){
        try{
            serverSocket.close();
            socket.close();
            serverSocket = null;
            socket = null;
        } catch (Exception ignored) {
        }
    }

    public int stor(String path) throws IOException {
        File file = new File(path);
        FileWriter fileWriter;
        if(file.exists()){
            fileWriter = new FileWriter(file.getAbsolutePath());
        } else{
            if(!file.createNewFile()){
                return 451;
            }
            fileWriter = new FileWriter(file.getAbsolutePath());
        }
        if(type == 0){
            String input = "";
            while( (input = in.readLine()) != null){
                fileWriter.write(input+EOL());
            }
            fileWriter.close();
        } else{
            InputStream inputStream = socket.getInputStream();
            ByteSink byteSink = Files.asByteSink(file);
            byteSink.writeFrom(inputStream);
        }

        return 250;
    }

    public void setType(int type) {
        this.type = type;
    }
}

