package com.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ServerMain {
    


    private static ServerSocket serverSocket;
    public static final int PORT = 8087;

    public static ArrayList<ServerClient> clients = new ArrayList<>();




    public ServerMain(){

        try {
            serverSocket = new ServerSocket(PORT);

            while(true){
                handleClient(new ServerClient(serverSocket.accept()));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    public void handleClient(ServerClient client)
{

}    


}
