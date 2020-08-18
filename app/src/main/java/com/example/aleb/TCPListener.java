package com.example.aleb;

public interface TCPListener {
    public void onTCPMessageReceived(String message);
    public void onTCPConnectionStatusChanged(boolean isConnectedNow);
}
