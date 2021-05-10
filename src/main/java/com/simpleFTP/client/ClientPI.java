// PI
// The protocol interpreter. The user and server sides of the
// protocol have distinct roles implemented in a user-PI and a server-PI.

// user-PI
// The user protocol interpreter initiates the control connection from its port U to the server-FTP process,
// initiates FTP commands, and governs the user-DTP if that process is part of the file transfers.

package com.simpleFTP.client;

// Used for debugging
// Left for future development


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ClientPI {
    private String server;
    private int port;
    private Socket socket;
    private BufferedReader in;
    private Scanner scanner;
    private PrintStream out;
    private int type;

    public ClientPI(String server, int port) {
        this.server = server;
        this.port = port;
        type = 0;
    }

    public void configureStreams() throws IOException {
        scanner = new Scanner(System.in);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintStream(socket.getOutputStream());
    }

    public void connect() {
        try {
            socket = new Socket(server, port);
            configureStreams();
            String response = ReadResponse();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to connect to server: " + this.server + ":" + this.port);
        }
    }

    public boolean login() throws IOException {
        boolean flag = true;
        while (flag) {
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            //byte[] hash = passwordEncoder.encrypt(password);
            String user_cmd = "USER " + username;
            String pass_cmd = "PASS " + password;
            SendRequest(user_cmd);
            String response = ReadResponse();
            if (response.startsWith("331")) {
                SendRequest(pass_cmd);
                response = ReadResponse();
                if (response.startsWith("230")) {
                    flag = false;
                    //System.out.println(response);
                    return true;
                } else {
                    //System.out.println(response);
                    return false;
                }
            } else {
                //System.out.println(response);
                return false;
            }
        }
        return false;
    }

    public boolean port(String host, int port) throws IOException {
        String[] ip_parts = host.split("\\.");
        for (int i = 0; i < ip_parts.length; i++) {
            ip_parts[i] = Integer.toBinaryString(Integer.parseInt(ip_parts[i]));
        }
        StringBuilder port_s = new StringBuilder(Integer.toBinaryString(port));
        System.out.println(port_s);
        while (port_s.length() < 16) port_s.insert(0, "0");
        String req = "PORT " + ip_parts[0] + " " + ip_parts[1] + " " + ip_parts[2] + " " + ip_parts[3] + " " + port_s.substring(0, 8) + " " + port_s.substring(8, 16);
        ServerSocket serverSocket = new ServerSocket(port);
        Thread clientdtp = new ClientDTP(serverSocket);
        SendRequest(req);
        String response = ReadResponse();
        if (response.startsWith("200 ")) {
            return true;
        } else {
            //System.out.println(response);
            return false;
        }
    }

    public boolean cwd(String path) throws IOException {
        SendRequest("CWD " + path);
        String response = ReadResponse();
        if (response.startsWith("200")) {
            return true;
        } else {
            //System.out.println(response);
            return false;
        }
    }

    public boolean quit() throws IOException {
        SendRequest("QUIT");
        String res = ReadResponse();
        if (res.startsWith("221")) {
            return true;
        }

        return false;
    }


    private String EOL() {
        return switch (type) {
            case 0 -> "\r\n";
            case 1 -> "\n";
            default -> "\n";
        };
    }

    private void SendRequest(String req) {
        out.print(req + EOL());
        System.out.print("<<: " + req + EOL());
    }

    private String ReadResponse() throws IOException {
        String res = in.readLine();
        System.out.println(">>: " + res);

        return res;
    }
}
