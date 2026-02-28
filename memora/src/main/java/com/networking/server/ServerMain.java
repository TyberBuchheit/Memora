package com.networking.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networking.ClientInfo;

public class ServerMain {

    private static ServerSocket serverSocket;
    public static final int PORT = 8087;

    public static ArrayList<ServerClient> clients = new ArrayList<>();

    public ServerMain() {

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            ClientInfo clientInfo = new ClientInfo("Server", 0);
            System.out.println(new ObjectMapper().writeValueAsString(clientInfo));

            while (true) {
                
                handleClient(new ServerClient(serverSocket.accept()));
                Thread.sleep(10);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void handleClient(ServerClient client) {
        clients.add(client);
        System.out.println("Welcome to the server! Your ID is: " + client.clientInfo.getID()+" and you are of type: " + client.clientInfo.getClientType());        
    }
    public static void main(String[] args) {
        new ServerMain();
    }

}
