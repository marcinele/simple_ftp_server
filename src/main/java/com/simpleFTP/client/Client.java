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

    public Client(String server, int port)
    {
        this.server = server;
        this.port = port;
    }

    public void connect()
    {
        try {
            socket = new Socket(server, port);
            PrintStream out  = new PrintStream(socket.getOutputStream());
            boolean flag = true;
            PasswordEncoder passwordEncoder = new PasswordEncoder();
            Scanner scanner = new Scanner(System.in);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while(flag)
            {
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();
                byte[] hash = passwordEncoder.encrypt(password);
                out.println(username);
                out.println(password);
                String ans = in.readLine();
                if(ans.equals("true")) {
                    System.out.println("Succesfully logged in.");
                    flag = false;
                } else {
                    System.out.println("Login failed, try again.");
                }
            }
            System.out.println("You're now logged in.");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to server: " + this.server + ":" + this.port);
        }

    }
}
