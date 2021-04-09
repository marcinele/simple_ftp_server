package com.simpleFTP;

import com.simpleFTP.client.ClientPI;
import com.simpleFTP.server.Server;

public class Main {

    public static void main(String[] args) {

        if(args[0].equals("server"))
        {
            Server server = new Server(10101);
            server.startServer();
        }
        else if(args[0].equals("client")){
            ClientPI clientPI = new ClientPI("localhost", 10101);
            clientPI.connect();
            try{
                clientPI.login();
                clientPI.cwd("./lol");
                //clientPI.port("127.0.0.1",12345);
                clientPI.quit();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
