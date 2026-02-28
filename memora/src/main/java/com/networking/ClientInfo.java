package com.networking;


public class ClientInfo {
    private String ID;
    private int clientType;
    public String getID() {
        return ID;
    }
    public void setID(String iD) {
        ID = iD;
    }
    public int getClientType() {
        return clientType;
    }
    public void setClientType(int clientType) {
        this.clientType = clientType;
    }
    public ClientInfo(String iD, int clientType) {
        ID = iD;
        this.clientType = clientType;
    }
    public ClientInfo(){
        
    }

    
}
