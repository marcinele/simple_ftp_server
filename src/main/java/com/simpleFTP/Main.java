package com.simpleFTP;

import com.simpleFTP.client.Client;
import com.simpleFTP.server.Server;

public class Main {

    public static void main(String[] args) {

        if(args[0].equals("server"))
        {
            Server server = new Server(10101);
            server.startServer();
        }
        else if(args[0].equals("client")){
            Client client = new Client("localhost", 10101);
            client.connect();
        }
    }
}
