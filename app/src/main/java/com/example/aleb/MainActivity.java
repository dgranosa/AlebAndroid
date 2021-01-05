package com.example.aleb;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements TCPListener {

    private boolean isConnected = false;
    private TCPCommunicator tcpClient;
    private Handler UIHandler;
    private TextView l_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UIHandler = new Handler();
        l_status = (TextView)findViewById(R.id.l_status);

        if (!TCPCommunicator.isConnected) {
            connectToServer();
        } else {
            l_status.setText("Connected");
            if (Constants.USERNAME == null)
                showLogin();
            else
                showRooms();
        }
    }

    private void connectToServer() {
        tcpClient = TCPCommunicator.getInstance();
        tcpClient.addListener(this);
        tcpClient.init(Constants.SERVER_IP, Constants.SERVER_PORT);
    }

    private void showLogin() {
        Intent i = new Intent(MainActivity.this, Login.class);
        startActivity(i);
    }

    private void showRooms() {
        Intent i = new Intent(MainActivity.this, Rooms.class);
        startActivity(i);
    }

    @Override
    public void onTCPMessageReceived(String message) {
        String[] msg = message.split(";");

        if (msg[0].equals("Version")) {
            UIHandler.post(new Runnable() {
                @Override
                public void run() {
                    l_status.setText("Connected");
                }
            });
            if (Integer.parseInt(msg[1]) == Constants.VERSION) {
                showLogin();
            } else {
                UIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        l_status.setText("Update application\nCurrent version: " + Constants.VERSION);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dgranosa/AlebAndroid/releases/latest/download/Aleb.apk"));
                        startActivity(browserIntent);
                    }
                });
            }
        }
    }

    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {
        isConnected = isConnectedNow;
    }
}