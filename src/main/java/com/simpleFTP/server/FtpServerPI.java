// PI
// The protocol interpreter. The user and server sides of the
// protocol have distinct roles implemented in a user-PI and a server-PI.

// server-PI
// The server protocol interpreter "listens" on Port L for a connection from a user-PI and establishes a control
// communication connection.  It receives standard FTP commands from the user-PI, sends replies, and governs the server-DTP.

package com.simpleFTP.server;

import com.simpleFTP.log.LoggerSingleton;
import com.simpleFTP.security.AuthorizationHandler;
import com.simpleFTP.security.PathValidator;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

public class FtpServerPI extends Thread {
    private Socket socket;
    private User user;
    private boolean isRunning;
    private BufferedReader in;
    private PrintStream out;
    private String cwd;
    private String cwd_prefix;
    private Path cwd_path;
    private int type;                   // 0 - ASCII, 1 - Image / Binary
    private String stru;                // F - File, R - Record, P - Page
    private String mode;                // S - Stream, B - Block, C - Compressed
    private FtpServerDTP ftpServerDTP;
    private File renamedOldFile = null;
    private Logger logger;
    HashMap<Integer, String> replyCodes;

    public FtpServerPI(Socket socket) throws IOException {
        this.socket = socket;
        user = new User();
        user.setLoggedIn(false);
        isRunning = true;
        type = 0;
        replyCodes = new HashMap<>();
        replyCodes.put(125, "125 Data connection already open; transfer starting.");
        replyCodes.put(200, "200 Command okay.");
        replyCodes.put(220, "220 Service ready for new user.");
        replyCodes.put(221, "221 Service closing control connection.");
        replyCodes.put(227, "227 Entering Passive Mode.");
        replyCodes.put(230, "230 User logged in, proceed.");
        replyCodes.put(250, "250 Requested file action okay, completed.");
        replyCodes.put(257, "257 " + cwd);
        replyCodes.put(331, "331 User name okay, need password.");
        replyCodes.put(421, "421 Service not available, closing control connection.");
        replyCodes.put(451, "451 Requested action aborted: local error in processing.");
        replyCodes.put(500, "500 Syntax error, command unrecognized.");
        replyCodes.put(501, "501 Syntax error in parameters or arguments.");
        replyCodes.put(503, "503 Bad sequence of commands.");
        replyCodes.put(530, "530 Not logged in.");
        replyCodes.put(550, "550 Requested action not taken.");


        cwd_prefix = System.getProperty("user.dir");
        cwd = File.separator + "ftp";
        System.out.println(cwd_prefix + cwd);
        cwd_path = Paths.get(cwd_prefix + cwd);
        if (!Files.exists(cwd_path)) {
            Files.createDirectories(cwd_path);
        }
        configureStreams();
        logger = LoggerSingleton.getLogger();
    }

    @Override
    public void run() {
        System.out.println("**** Received a connection. ****");
        Response(220);
        System.out.println(cwd);
        try {
            while (isRunning) {
                String input = ReadRequest();
                if (input == null) {
                    isRunning = false;
                    break;
                }
                String cmd = input.split(" ")[0];
                if(user.isLoggedIn()){
                    switch (cmd) {
                        case "USER" -> login(input);
                        case "PASSWORD" -> Response(503);
                        case "CWD" -> cwd(input);
                        case "TYPE" -> type(input);
                        case "PORT" -> port(input);
                        case "QUIT" -> quit();
                        case "CDUP" -> cdup();
                        case "LIST" -> list(input);
                        case "STRU" -> stru(input);
                        case "MODE" -> mode(input);
                        case "PWD" -> pwd();
                        case "NOOP" -> noop();
                        case "RETR" -> retr(input);
                        case "PASV" -> pasv();
                        case "STOR" -> stor(input);
                        case "MKD" -> mkd(input);
                        case "DELE", "RMD" -> dele(input);
                        case "RNFR" -> rnfr(input);
                        case "RNTO" -> rnto(input);
                        default -> Response(500);
                    }
                }else if(cmd.equals("USER")) login(input);
                else Response(530);
            }
            in.close();
            out.close();
            socket.close();
            System.out.println("**** Connection closed. ****");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("**** Unable to handle connection. ****");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void configureStreams() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintStream(this.socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void login(String cmd) {
        if (!cmd.startsWith("USER")) {
            Response(500);
        } else if (cmd.split(" ").length != 2) {
            Response(501);
        } else {
            user.setUsername(cmd.split(" ")[1]);
            Response(331);
        }

        //Password
        try {
            cmd = ReadRequest();
            if (!(cmd.startsWith("PASS"))) {
                Response(500);
            } else if (!(cmd.split(" ").length == 2)) {
                Response(501);
            } else {
                user.setPassword(cmd.split(" ")[1]);
                AuthorizationHandler authorizationHandler = new AuthorizationHandler();
                if (authorizationHandler.checkCredentials(user, cwd_prefix)) {
                    user.setLoggedIn(true);
                    Response(230);
                } else {
                    Response(530);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            out.println();
        }
    }

    public void cwd(String input) {              // Change working directory
        String[] args = input.split(" ");
        if (args.length != 2) {
            Response(501);
        } else if (PathValidator.CwdValidator(args[1], cwd) != null) {
            String candidate = PathValidator.CwdValidator(args[1], cwd);
            if (new File(cwd_prefix + candidate).exists()) {
                cwd = PathValidator.CwdValidator(args[1], cwd);
                Response(250);
            } else {
                Response(550);
            }
        } else {
            Response(550);
        }
    }

    private void type(String input) {
        String[] args = input.split(" ");
        if (args.length != 2) {
            Response(501);
        } else if (args[1].equals("A")) {
            type = 0;
            if (ftpServerDTP != null) ftpServerDTP.setType(0);
            Response(200);
        } else if (args[1].equals("I")) {
            type = 1;
            if (ftpServerDTP != null) ftpServerDTP.setType(1);
            Response(200);
        } else {
            Response(501);
        }
    }

    private String EOL() {
        return switch (type) {
            case 0 -> "\r\n";
            case 1 -> "\n";
            default -> "\n";
        };
    }

    private void port(String input) {
        String[] args = input.split(" ");
        args = args[1].split(",");
        if (args.length != 6) {
            Response(501);
        } else {
            if (ftpServerDTP != null) {
                ftpServerDTP.Close();
                ftpServerDTP = null;
            }
            int p1 = Integer.parseInt(args[4]);
            int p2 = Integer.parseInt(args[5]);
            p1 = p1 << 8;
            int port = p1 + p2;
            String host = args[0] + '.' + args[1] + '.' + args[2] + '.' + args[3];
            System.out.println(port);
            try {
                ftpServerDTP = new FtpServerDTP(host, port, type);
                Response(200);
            } catch (IOException e) {
                e.printStackTrace();
                Response(421);
                CloseControlConnection();
            }
        }
    }

    private void quit() {
        isRunning = false;
        Response(221);
    }

    private void cdup() {
        // change to parent directory
        if (cwd == null || cwd.length() == 0)
            Response(550);
        else if (PathValidator.CdupValidator(cwd)) {
            cwd = cwd.substring(0, cwd.lastIndexOf(File.separator));
            Response(250);
        } else {
            Response(550);
        }
    }

    private void stru(String input) {
        // specyfing file structure
        // F - File (default), R - Record, P - Page
        // args -> <SP> <structure-code> <CRLF>
        String[] pos = {"R", "P", "F"};
        String[] args = input.split(" ");
        if (args.length != 2)
            Response(501);
        if (Arrays.asList(pos).contains(args[1])) {
            switch (args[1]) {
                case "R" -> stru = "R";
                case "P" -> stru = "P";
                default -> stru = "F";
            }
            Response(200);
        } else if (!Arrays.asList(pos).contains(args[1])) {
            Response(501);
        } else
            Response(500);
    }

    private void mode(String input) {
        // specyfing the data transfer modes
        // S - Stream (default), B - Block, C - Compressed
        // args -> <SP> <mode-code> <CRLF>
        String[] pos = {"S", "B", "C"};
        String[] args = input.split(" ");
        if (args.length != 2)
            Response(501);
        if (Arrays.asList(pos).contains(args[1])) {
            switch (args[1]) {
                case "B" -> stru = "B";
                case "C" -> stru = "C";
                default -> stru = "S";
            }
            Response(200);
        } else if (!Arrays.asList(pos).contains(args[1])) {
            Response(501);
        } else
            Response(500);
    }

    private void pwd() {
        // return current working directory
        if (cwd == null || cwd.length() == 0)
            Response(551);
        else {
            replyCodes.replace(257, "257 " + cwd);
            Response(257);
        }
    }

    private void noop() {
        // sending OK
        Response(200);
    }

    private void Response(int code) {
        out.print(replyCodes.get(code) + EOL());
        System.out.print("[Server -> " + socket.getRemoteSocketAddress().toString().replace("/", "") + "]: " + replyCodes.get(code) + EOL());
        logger.info("[Server -> " + socket.getRemoteSocketAddress().toString().replace("/", "") + "]: " + replyCodes.get(code) + EOL());
    }

    private void CloseControlConnection() {
        isRunning = false;
    }

    private String ReadRequest() throws IOException {
        String req = in.readLine();
        System.out.println("[" + socket.getRemoteSocketAddress().toString().replace("/", "") + " -> Server]: " + req);
        logger.info("[" + socket.getRemoteSocketAddress().toString().replace("/", "") + " -> Server]: " + req);
        return req;
    }

    private void pasv() throws IOException {
        try {
            int dtp_port;
            int right_octet = 0;
            int left_octet;
            String dtp_address;
            if (ftpServerDTP != null) {
                ftpServerDTP.Close();
                ftpServerDTP = null;
            }
            dtp_address = socket.getInetAddress().toString().replace("/", "");
            dtp_port = 0; // Should allocate for us
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(dtp_address, dtp_port));
            dtp_port = serverSocket.getLocalPort();
            dtp_address = dtp_address.replace(".", ",");
            ftpServerDTP = new FtpServerDTP(serverSocket, type);

            right_octet = dtp_port >> 8;
            right_octet = right_octet << 8;
            left_octet = dtp_port - right_octet;
            right_octet = right_octet >> 8;
            replyCodes.replace(227, "227 Entering passive mode (" + dtp_address + "," + Integer.toString(right_octet) + "," + Integer.toString(left_octet) + ")");
            Response(227);
            ftpServerDTP.join();
        } catch (Exception e) {
            e.printStackTrace();
            Response(421);
            CloseControlConnection();
        }
    }

    private void stor(String input) throws IOException {
        String path = input.split(" ")[1];
        if ((path = PathValidator.GeneralValidator(path, cwd_prefix, cwd)) != null) {
            if (ftpServerDTP != null) {
                Response(125);
                Response(ftpServerDTP.stor(path));
            } else {
                Response(421);
            }
        }
    }

    private void list(String input) throws Exception {
        // send a list from the server to the passive DTP
        // args -> [<SP> <pathname>] <CRLF>
        if (ftpServerDTP != null) {
            String[] input_splitted = input.split(" ");
            if(input_splitted.length == 2){
                String path = PathValidator.GeneralValidator(input_splitted[1], cwd_prefix, cwd);
                if(path != null) {
                    Response(125);
                    Response(ftpServerDTP.list_ver2(path));
                } else {
                    Response(501);
                }
            } else if(input_splitted.length == 1){
                Response(125);
                Response(ftpServerDTP.list_ver2(cwd_prefix + cwd + File.separator));
            } else {
                Response(501);
            }
        } else {
            Response(421);
        }
    }

    private void retr(String input) throws IOException, InterruptedException {
        String path = input.split(" ")[1];
        if ((path = PathValidator.GeneralValidator(path, cwd_prefix, cwd)) != null) {
            if (ftpServerDTP != null) {
                Response(125);
                Response(ftpServerDTP.retr(path));
            } else {
                Response(421);
            }
        }
    }

    private void mkd(String input) {
        String path = null;
        String[] splitted = input.split(" ");
        if(splitted.length != 2){
            Response(501);
        } else {
            if ((path = PathValidator.GeneralValidator(splitted[1], cwd_prefix, cwd)) != null) {
                File newDir = new File(path);
                try {
                    newDir.mkdir();
                    Response(250); // sprawdzic czy na pewno ten kod
                } catch (Exception e) {
                    Response(451);
                }
            } else Response(501);
        }
    }

    private void dele(String input) {
        String[] input_splitted = input.split(" ");
        if (input_splitted.length != 2) {
            Response(501);
        } else {
            String path = PathValidator.DeleValidator(input_splitted[1], cwd_prefix, cwd);
            if (path != null) {
                File toDelete = new File(path);
                if (toDelete.delete()) {
                    Response(250);
                } else {
                    Response(550);
                }
            } else {
                Response(550);
            }
        }
    }
    private void rnfr(String input) throws IOException {
        String[] input_splitted = input.split(" ");
        if (input_splitted.length != 2)
            Response(501);
        else {
            String fileName = input_splitted[1];
            String path = PathValidator.GeneralValidator(fileName, cwd_prefix, cwd);
            renamedOldFile = new File(path);
            Response(250);
        }
    }

    private void rnto(String input) throws IOException {
        String[] input_splitted = input.split(" ");
        if (input_splitted.length != 2)
            Response(501);
        else {
            System.out.println(input_splitted[1]);
            String path = PathValidator.GeneralValidator(input_splitted[1], cwd_prefix, cwd);
            System.out.println(path);
            assert path != null;
            File renamedNewFile = new File(path);
            if (renamedOldFile == null) {
                Response(501);
            } else {
                boolean success = renamedOldFile.renameTo(renamedNewFile);
                if (success) {
                    renamedOldFile = null;
                    Response(250);
                } else
                    Response(550);
            }
        }
    }
}
