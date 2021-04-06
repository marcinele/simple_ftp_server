package com.simpleFTP.client;

import com.simpleFTP.security.PasswordEncoder;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Scanner;

public class Client {
    private String server;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private Scanner scanner;
    private PrintStream out;

    public Client(String server, int port)
    {
        this.server = server;
        this.port = port;
    }

    public void configureStreams() throws IOException {
        scanner = new Scanner(System.in);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out  = new PrintStream(socket.getOutputStream());
    }
    public void connect()
    {
        try {
            socket = new Socket(server, port);
            configureStreams();
            String response = in.readLine();

        } catch (IOException  e) {
            e.printStackTrace();
            System.out.println("Unable to connect to server: " + this.server + ":" + this.port);
        }
    }

    public void login() throws IOException {
        boolean flag = true;
        while(flag)
        {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            //byte[] hash = passwordEncoder.encrypt(password);
            String user_cmd = "USER " + username;
            String pass_cmd = "PASS " + password;
            out.println(user_cmd);
            String response = in.readLine();
            if(response.startsWith("331")){
                out.println(pass_cmd);
                response = in.readLine();
                if(response.startsWith("230")){
                    flag = false;
                    System.out.println(response);
                } else{
                    System.out.println(response);
                }
            } else{
                System.out.println(response);
            }
        }
    }


}
