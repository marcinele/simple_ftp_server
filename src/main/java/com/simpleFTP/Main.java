package com.simpleFTP;

import com.simpleFTP.client.ClientPI;
import com.simpleFTP.server.ConnectionRequestHandler;

public class Main {

    public static void main(String[] args) {

        if(args[0].equals("server"))
        {
            if(args.length != 3){
                System.out.println("Wrong arguments. Proper args \" server <serv_ip_addr> <port>\"");
            }
            try{
                ConnectionRequestHandler connectionRequestHandler = new ConnectionRequestHandler(args[1], Integer.parseInt(args[2]));
                connectionRequestHandler.startServer();
            } catch (Exception e){
                System.out.println("Wrong arguments or internal error");
            }
        }
        else if(args[0].equals("client")){
            /*ClientPI clientPI = new ClientPI("localhost", 10101);
            clientPI.connect();
            try{
                clientPI.login();
                clientPI.cwd("./lol");
                //clientPI.port("127.0.0.1",12345);
                clientPI.quit();
            } catch (Exception e){
                e.printStackTrace();
            }
            */
            System.out.println("Client is not supported yet.");
        }
    }
}
