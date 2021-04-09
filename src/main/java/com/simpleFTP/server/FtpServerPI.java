package com.simpleFTP.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;

public class FtpServerPI extends Thread{
    private Socket socket;
    private User user;
    private boolean isRunning;
    private BufferedReader in;
    private PrintStream out;
    private String cwd;
    private String cwd_prefix;
    private Path cwd_path;
    private int type;                   // 0 - ASCII, 1 - Image / Binary
    private FtpServerDTP ftpServerDTP;
    HashMap<Integer, String> replyCodes;

    public FtpServerPI(Socket socket) throws IOException {
        this.socket = socket;
        user = new User();
        user.setLoggedIn(false);
        isRunning = true;
        type = 0;
        replyCodes = new HashMap<>();
        replyCodes.put(200,"200 Command okay.");
        replyCodes.put(220, "220 Service ready for new user.");
        replyCodes.put(221, "221 Service closing control connection.");
        replyCodes.put(230, "230 User logged in, proceed.");
        replyCodes.put(250, "250 Requested file action okay, completed.");
        replyCodes.put(331, "331 User name okay, need password.");
        replyCodes.put(421, "421 Service not available, closing control connection.");
        replyCodes.put(500, "500 Syntax error, command unrecognized.");
        replyCodes.put(501, "501 Syntax error in parameters or arguments.");
        replyCodes.put(503, "503 Bad sequence of commands.");
        replyCodes.put(530, "530 Not logged in.");
        replyCodes.put(550, "550 Requested action not taken.");


        cwd_prefix = System.getProperty("user.dir");
        cwd = cwd_prefix+"/ftp";
        System.out.println(cwd);
        cwd_path = Paths.get(cwd);
        if(!Files.exists(cwd_path)){
            Files.createDirectories(cwd_path);
        }
        configureStreams();
    }

    @Override
    public void run()
    {
        System.out.println("**** Received a connection. ****");
        Response(220);
        System.out.println(cwd);
        try {
            while(isRunning){
                String input = in.readLine();
                if (input == null){
                    isRunning = false;
                    break;
                }
                System.out.println(">>: " + input);
                String cmd = input.split(" ")[0];
                switch (cmd) {
                    case "USER" -> login(input);
                    case "PASSWORD" -> Response(503);
                    case "CWD" -> cwd(input);
                    case "TYPE" -> type(input);
                    case "PORT" -> port(input);
                    case "QUIT" -> quit();
                    default -> Response(500);
                }
            }
            in.close();
            out.close();
            socket.close();
            System.out.println("**** Connection closed. ****");
        } catch (IOException  e) {
            e.printStackTrace();
            System.out.println("**** Unable to handle connection. ****");
        }

    }

    private void configureStreams(){
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void login(String cmd){
        if(!cmd.startsWith("USER")){
            Response(500);
        } else if(cmd.split(" ").length != 2){
            Response(501);
        } else{
            user.setUsername(cmd.split(" ")[1]);
            Response(331);
        }

        //Password
        try {
            cmd = ReadRequest();
            if(!(cmd.startsWith("PASS"))){
                Response(500);
            } else if(!(cmd.split(" ").length == 2)){
                Response(501);
            } else{
                user.setPassword(cmd.split(" ")[1]);
                AuthorizationHandler authorizationHandler = new AuthorizationHandler();
                if(authorizationHandler.checkCredentials(user)){
                    user.setLoggedIn(true);
                    Response(230);
                } else{
                    Response(530);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            out.println();
        }
    }

    public void cwd(String input){              // Change working directory
        String[] args = input.split(" ");
        if(args.length != 2){
            Response(501);
        } else if(args[1].startsWith(cwd_prefix) && Files.exists(Paths.get(args[1]))){
            cwd = args[1];
            Response(250);
        } else if(args[1].startsWith("./") && Files.exists(Paths.get(cwd + args[1].replaceFirst(".","")))) {
            String path = args[1].replaceFirst(".", "");
            cwd = cwd + path;
            Response(250);
        } else {
            Response(550);
        }
    }

    private void type(String input){
        String[] args = input.split(" ");
        if(args.length != 2){
            Response(501);
        } else if(args[1].equals("A")){
            type = 0;
            Response(200);
        } else if(args[1].equals("I")){
            type = 1;
            Response(200);
        } else {
            Response(501);
        }
    }

    private String EOL(){
        return switch (type) {
            case 0 -> "\r\n";
            case 1 -> "\n";
            default -> "\n";
        };
    }

    private void port(String input){
        String[] args = input.split(" ");
        if(args.length != 7){
            Response(501);
        } else {
            int h1 = Integer.parseInt(args[1],2);
            int h2 = Integer.parseInt(args[2],2);
            int h3 = Integer.parseInt(args[3],2);
            int h4 = Integer.parseInt(args[4],2);
            int port = Integer.parseUnsignedInt(args[5]+args[6], 2);

            String host = Integer.toString(h1)+'.'+Integer.toString(h2)+'.'+Integer.toString(h3)+'.'+Integer.toString(h4);
            System.out.println(port);
            try {
                ftpServerDTP = new FtpServerDTP(host, port, type);
                Response(200);
                ftpServerDTP.start();
            } catch (IOException e) {
                e.printStackTrace();
                Response(421);
                CloseControlConnection();
            }
        }
    }

    private void quit(){
        isRunning = false;
        Response(221);
    }

    private void Response(int code){
        out.print(replyCodes.get(code) + EOL());
        System.out.print("<<: " + replyCodes.get(code) + EOL());
    }

    private void CloseControlConnection(){
        isRunning = false;
    }

    private String ReadRequest() throws IOException {
        String req = in.readLine();
        System.out.println(">>: " + req);

        return  req;
    }
}
