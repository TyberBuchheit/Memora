package com.networking.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networking.ClientInfo;

public class Client {



    private BufferedInputStream is;
    private BufferedOutputStream  os;



    private ClientInfo myInfo;
    private static ObjectMapper mapper = new ObjectMapper();
    private Socket socket;
    private static final String IPADDRESS= "localhost";
    private static final int PORT = 8087;

    public Client() throws UnknownHostException, IOException{
        socket = new Socket(IPADDRESS, PORT);

        is = new BufferedInputStream(socket.getInputStream());
        os = new BufferedOutputStream(socket.getOutputStream());
        myInfo = new ClientInfo("Client1", 1);

        send(mapper.writeValueAsString(myInfo));

        receive();
            Scanner scanner = new Scanner(System.in);

        while(true){
            String message = scanner.nextLine();
            if(message.equalsIgnoreCase("exit")) {
                break;
            }
            send(message);
        }
        scanner.close();
    }
    public void send(String message) {
        

        try {
            os.write(message.getBytes());
            os.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void receive() {
        

        new Thread(()->{

            while(true) {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead = is.read(buffer);
                    if (bytesRead == -1) {
                        // End of stream, server disconnected
                        break;
                    }
                    String message = new String(buffer, 0, bytesRead);
                    System.out.println("Received: " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        try {
            new Client();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
