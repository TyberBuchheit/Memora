package com.networking;

import java.util.HashMap;

public class Packet {
    private String type;
    public String getConv_id() {
        return conv_id;
    }
    public void setConv_id(String conv_id) {
        this.conv_id = conv_id;
    }
    private String conv_id;
    private HashMap<String, String> data;

    public String getType() {
        return type;
    }
    public Packet(String type, String conv_id, HashMap<String, String> data) {
        this.type = type;
        this.conv_id = conv_id;
        this.data = data;
    }
    public void setType(String type) {
        this.type = type;
    }
    public HashMap<String, String> getData() {
        return data;
    }
    public void setData(HashMap<String, String> data) {
        this.data = data;
    }


    public Packet() {
        
    }
}
