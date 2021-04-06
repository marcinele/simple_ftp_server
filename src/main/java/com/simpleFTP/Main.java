package com.simpleFTP;

import com.simpleFTP.client.ClientExample;
import com.simpleFTP.db.DatabaseDriver;
import com.simpleFTP.server.ServerExample;

public class Main {

    public static void main(String[] args) {
        ServerExample serverExample = new ServerExample(10101);
        ClientExample clientExample = new ClientExample("localhost", 10101);

        serverExample.startServer();
        clientExample.Connect();
    }
}
