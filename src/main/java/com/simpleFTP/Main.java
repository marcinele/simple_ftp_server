package main.java.com.simpleFTP;

import com.simpleFTP.client.ClientExample;
import com.simpleFTP.db.DatabaseDriver;
import com.simpleFTP.server.ServerExample;

public class Main {

    public static void main(String[] args) {
        DatabaseDriver dbDriver = new DatabaseDriver();

        dbDriver.connectToDatabase();
    }
}
