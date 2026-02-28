package com.networking.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontend.Bubble;
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
    private static String chatID = "1234";

    public static String cur_conv_id = "1234";


    public Client() throws UnknownHostException, IOException {
        socket = new Socket(IPADDRESS, PORT);

        is = new BufferedInputStream(socket.getInputStream());
        os = new BufferedOutputStream(socket.getOutputStream());
        myInfo = new ClientInfo(chatID, 1);

        send(mapper.writeValueAsString(myInfo));

        receive();

    }

    public void sendPrompt(String prompt, String cur_id) throws JsonProcessingException {
        Packet p = new Packet("prompt", cur_id, new HashMap<>() {
            {
                put("conv_id", cur_id);
                put("prompt", prompt);

            }
        });
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

    // public void loadContext(String context) throws JsonProcessingException {
    //     ArrayList<HashMap<String, String>> contextList = mapper.readValue(context, new com.fasterxml.jackson.core.type.TypeReference<ArrayList<HashMap<String, String>>>() {});
    
    //     for(HashMap<String, String> entry : contextList) {
    //         String role = entry.get("role");
    //         String content = entry.get("context");

    //         if(role.equals("user")) {
    //                 Bubble bub = new Bubble(content);
    //                     Panel.bubbles.add(bub);
    //                     Panel.pan.add(bub);
    //         } else if(role.equals("assistant")) {
    //             Panel.drawResponse(content);
    //         }
    //         System.out.println("loading context entry with content");
    //     }

    // }

    public void receive() {

        new Thread(() -> {

            while (true) {
                try {
                    byte[] buffer = new byte[8192 * 4];
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
                        // case "context":
                        //     String context = (String) pack.getData().get("context");
                        //     loadContext(context);
                        //     break;
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
