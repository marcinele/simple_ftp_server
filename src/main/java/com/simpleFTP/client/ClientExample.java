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

    public ClientExample(String server, int port)
    {
        this.server = server;
        this.port = port;
    }

    public void Connect()
    {
        try {
            socket = new Socket(server, port);
            PrintStream out  = new PrintStream(socket.getOutputStream());
            while(socket.isConnected())
            {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Client: ");
                String line = scanner.nextLine();
                out.println(line);
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to server: " + this.server + ":" + this.port);
        }

    }
}
