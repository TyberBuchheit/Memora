package com.networking.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networking.ClientInfo;

public class ServerClient {

    private BufferedInputStream is;
    private BufferedOutputStream os;

    public ClientInfo clientInfo;

    private static ObjectMapper mapper = new ObjectMapper();

    public ServerClient(Socket s) throws IOException {
        is = new BufferedInputStream(s.getInputStream());
        os = new BufferedOutputStream(s.getOutputStream());

        byte[] buffer = new byte[1024];
        int bytesRead = is.read(buffer);

        String jsonString = new String(buffer, "UTF-8").trim();
        System.out.println("Received JSON: " + jsonString);
        clientInfo = mapper.readValue(jsonString, ClientInfo.class);

        receive();

    }

    public void send(String message) {
        try {

            for (ServerClient sc : ServerMain.clients) {

                if (sc != this) {
                    sc.sendMessage(message);
                }

            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            os.write(message.getBytes());
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void receive() {
        new Thread(() -> {

            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    int bytesRead = is.read(buffer);
                    if (bytesRead == -1) {
                        // End of stream, client disconnected
                        break;
                    }
                    String message = new String(buffer, 0, bytesRead);
                    System.out.println("Received: " + message);

                    send(message);
                    

                } catch (IOException e) {

                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

}