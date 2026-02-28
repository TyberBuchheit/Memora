package com.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerClient {
    


    private BufferedInputStream is;
    private BufferedOutputStream os;



    public ServerClient(Socket s) throws IOException{
        is = new BufferedInputStream(s.getInputStream());
       os = new BufferedOutputStream(s.getOutputStream());

       
        
        
    }
}
