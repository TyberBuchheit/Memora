package com.networking.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networking.ClientInfo;
import com.networking.Packet;

public class ServerClient {

    private BufferedInputStream is;
    private BufferedOutputStream os;

    public ClientInfo clientInfo;

    private static ArrayList<HashMap<String, String>> context = new ArrayList<>();

    private static ObjectMapper mapper = new ObjectMapper();

    public ServerClient(Socket s) throws IOException {
        is = new BufferedInputStream(s.getInputStream());
        os = new BufferedOutputStream(s.getOutputStream());

        byte[] buffer = new byte[8192 * 4];
        is.read(buffer);

        String jsonString = new String(buffer, "UTF-8").trim();
        System.out.println("Received JSON: " + jsonString);
        clientInfo = mapper.readValue(jsonString, ClientInfo.class);

        // String p = "memora\\Users\\user\\Conversations\\" + clientInfo.getID();
        // Path path = Paths.get(p);
        // if (java.nio.file.Files.exists(path)) {
        //     loadContext(p);
        // }
        receive();

    }

    public void pathExists(String p) throws IOException {
        Path path = Paths.get(p);
        if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createDirectory(path);

        }
    }

    // public void loadContext(String p) throws IOException {
    //     Scanner scanner = new Scanner(new FileReader(p + "\\context.json"));
    //     StringBuilder sb = new StringBuilder();
    //     while (scanner.hasNextLine()) {
    //         sb.append(scanner.nextLine());
    //     }
    //     String jsonString = sb.toString();
    //     if (jsonString.length() > 0)
    //         context = mapper.readValue(jsonString, new TypeReference<ArrayList<HashMap<String, String>>>() {
    //         });
    //     scanner.close();

    //     Packet p1 = new Packet("context", clientInfo.getID(), new HashMap<>() {
    //         {
    //             put("context", jsonString);
    //         }
    //     });
    //     sendToClient(mapper.writeValueAsString(p1));

    // }

    // public void sendToClient(String message) {
    //     try {
    //         for (ServerClient sc : ServerMain.clients) {

    //             if (sc.clientInfo.getClientType() == 1) {
    //                 sc.sendMessage(message);
    //             } else {

    //             }

    //         }

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    public void appendToFile(String message, String UUID, boolean context) throws IOException {

        pathExists("memora\\Users\\user\\Conversations\\" + UUID);
        if (context) {

            try (FileWriter fw = new FileWriter("memora\\Users\\user\\Conversations\\" + UUID + "\\context.json")) {
                fw.write(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try (FileWriter fw = new FileWriter("memora\\Users\\user\\Conversations\\" + UUID + "\\meta.json")) {
                fw.write(message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String message) {
        try {

            for (ServerClient sc : ServerMain.clients) {

                if (sc != this) {
                    sc.sendMessage(message);
                } else {

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
                    byte[] buffer = new byte[8192 * 4];
                    int bytesRead = is.read(buffer);
                    if (bytesRead == -1) {
                        // End of stream, client disconnected
                        break;
                    }
                    String message = new String(buffer, 0, bytesRead);
                    System.out.println("Received: " + message + "\n\n\n\n\n");
                    Packet pack = mapper.readValue(message, Packet.class);
                    boolean isUser = pack.getType().equals("prompt");
                    HashMap<String, String> data = new HashMap<>() {
                        {
                            put("role", isUser ? "user" : "assistant");
                            put("context", isUser ? pack.getData().get("prompt") : pack.getData().get("response"));
                        }
                    };
                    context.add(data);
                    appendToFile(mapper.writeValueAsString(context), pack.getConv_id(), true);
                    send(message);

                } catch (Exception e) {

                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }

}