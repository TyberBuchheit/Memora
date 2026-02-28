package com.networking.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontend.Panel;
import com.networking.ClientInfo;
import com.networking.Packet;

public class Client {

    private BufferedInputStream is;
    private BufferedOutputStream os;

    private ClientInfo myInfo;
    private static ObjectMapper mapper = new ObjectMapper();
    private Socket socket;
    private static final String IPADDRESS = "localhost";
    private static final int PORT = 8087;

    public Client() throws UnknownHostException, IOException {
        socket = new Socket(IPADDRESS, PORT);

        is = new BufferedInputStream(socket.getInputStream());
        os = new BufferedOutputStream(socket.getOutputStream());
        myInfo = new ClientInfo("Client1", 1);

        send(mapper.writeValueAsString(myInfo));

        receive();

    }

    public void sendPrompt(String prompt) throws JsonProcessingException {
        Packet p = new Packet("prompt", "1234", new HashMap<>() {
            {
                put("conv_id", "1234");
                put("prompt", prompt);

            }
        } );
        send(mapper.writeValueAsString(p));
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

        new Thread(() -> {

            while (true) {
                try {
                    byte[] buffer = new byte[8192*4];
                    int bytesRead = is.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    String message = new String(buffer, 0, bytesRead);
                    System.out.println("Received: " + message);

                    Packet pack = mapper.readValue(message, Packet.class);
                    switch (pack.getType()) {
                        case "response":
                            String response = (String) pack.getData().get("response");
                            Panel.drawResponse(response);
                            break;
                        default:
                            System.out.println("Unknown packet type: " + pack.getType());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}
