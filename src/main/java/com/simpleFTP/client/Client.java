package com.simpleFTP.client;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientExample {
    private String server;
    private int port;
    private Socket socket;

    public Client(String server, int port)
    {
        this.server = server;
        this.port = port;
    }

    public void Connect()
    {
        try {
            socket = new Socket(server, port);
            PrintStream out  = new PrintStream(socket.getOutputStream());
            boolean flag = true;
            while(flag)
            {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Username: ");
                String username = scanner.nextLine();
                System.out.println("Password: ");
                String password = scanner.nextLine();
                out.println(username);
                out.println(password);
                flag = false;
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to server: " + this.server + ":" + this.port);
        }

    }
}
